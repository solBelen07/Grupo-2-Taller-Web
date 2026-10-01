package com.tallerwebi.dominio.tecnicasestudio;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("ServicioPomodoro")
@Transactional
public class ServicioPomodoroImpl implements ServicioPomodoro {

  static final int PUNTOS_POR_SESION_COMPLETADA = 3;
  static final int PUNTOS_PENALIZACION_CANCELACION = 2;
  private static final int SEGUNDOS_POR_MINUTO = 60;

  private final RepositorioPomodoro repositorioPomodoro;
  private final RepositorioUsuario repositorioUsuario;
  private final Clock reloj;

  @Autowired
  public ServicioPomodoroImpl(
    RepositorioPomodoro repositorioPomodoro,
    RepositorioUsuario repositorioUsuario
  ) {
    this(repositorioPomodoro, repositorioUsuario, Clock.systemDefaultZone());
  }

  public ServicioPomodoroImpl(
    RepositorioPomodoro repositorioPomodoro,
    RepositorioUsuario repositorioUsuario,
    Clock reloj
  ) {
    this.repositorioPomodoro = repositorioPomodoro;
    this.repositorioUsuario = repositorioUsuario;
    this.reloj = reloj;
  }

  @Override
  public Pomodoro registrarSesionEstudio(
    String origen,
    String destino,
    int duracion,
    Materia materia,
    Usuario usuario,
    String objetivo
  ) {
    if (duracion <= 0) throw new IllegalArgumentException("La duracion debe ser mayor a cero");

    if (this.buscarSesionActiva(usuario).isPresent()) {
      throw new RuntimeException("Ya tenes una sesion en curso");
    }

    String nombreMateria = materia != null ? materia.getNombre() : null;
    String objetivoSesion = objetivo != null ? objetivo : "";

    Pomodoro sesion = new Pomodoro();
    sesion.setUsuario(usuario);
    sesion.setPlan(new PlanDeVuelo(nombreMateria, origen, destino, objetivoSesion));
    sesion.setDuracionMinutos(duracion);
    sesion.setEstado(Estado.EN_CURSO);
    sesion.setFechaInicio(this.ahora());

    this.repositorioPomodoro.guardar(sesion);
    return sesion;
  }

  @Override
  public long obtenerSegundosRestantes(long pomodoroId) {
    Optional<Pomodoro> encontrada = this.repositorioPomodoro.buscarPorId(pomodoroId);
    if (encontrada.isEmpty()) return 0;

    Pomodoro sesion = encontrada.get();
    if (this.estaFinalizada(sesion)) return 0;

    if (this.haExpirado(sesion)) {
      this.completarAlVencer(sesion);
      return 0;
    }

    return this.calcularSegundosRestantes(sesion);
  }

  @Override
  public void modificarEstadoCompletada(long pomodoroId) {
    Pomodoro sesion = this.obtenerSesion(pomodoroId);
    if (this.estaFinalizada(sesion)) return;
    this.completar(sesion, this.ahora());
  }

  @Override
  public void modificarEstadoCancelada(long pomodoroId) {
    Pomodoro sesion = this.obtenerSesion(pomodoroId);
    if (this.estaFinalizada(sesion)) return;

    sesion.setEstado(Estado.CANCELADO);
    sesion.setFechaFin(this.ahora());
    this.repositorioPomodoro.actualizar(sesion);

    Usuario usuario = sesion.getUsuario();
    usuario.setPuntos(Math.max(0, usuario.getPuntos() - PUNTOS_PENALIZACION_CANCELACION));
    this.repositorioUsuario.modificar(usuario);
  }

  @Override
  public void pausarSesion(long pomodoroId) {
    Pomodoro sesion = this.obtenerSesion(pomodoroId);
    if (sesion.getEstado() != Estado.EN_CURSO) return;

    if (this.haExpirado(sesion)) {
      this.completarAlVencer(sesion);
      return;
    }

    sesion.setEstado(Estado.PAUSADO);
    sesion.setFechaPausa(this.ahora());
    this.repositorioPomodoro.actualizar(sesion);
  }

  @Override
  public void reanudarSesion(long pomodoroId) {
    Pomodoro sesion = this.obtenerSesion(pomodoroId);
    if (sesion.getEstado() != Estado.PAUSADO) return;

    if (sesion.getFechaPausa() != null) {
      Duration tiempoEnPausa = Duration.between(sesion.getFechaPausa(), this.ahora());
      sesion.setFechaInicio(sesion.getFechaInicio().plus(tiempoEnPausa));
    }

    sesion.setFechaPausa(null);
    sesion.setEstado(Estado.EN_CURSO);
    this.repositorioPomodoro.actualizar(sesion);
  }

  @Override
  public Pomodoro buscarPorId(long id) {
    return this.repositorioPomodoro.buscarPorId(id).orElse(null);
  }

  @Override
  public Optional<Pomodoro> buscarSesionActiva(Usuario usuario) {
    Optional<Pomodoro> enCurso =
      this.repositorioPomodoro.buscarPorUsuarioYEstado(usuario.getId(), Estado.EN_CURSO);

    if (enCurso.isPresent()) {
      if (!this.haExpirado(enCurso.get())) return enCurso;
      this.completarAlVencer(enCurso.get());
    }

    return this.repositorioPomodoro.buscarPorUsuarioYEstado(usuario.getId(), Estado.PAUSADO);
  }

  private Pomodoro obtenerSesion(long pomodoroId) {
    return this.repositorioPomodoro.buscarPorId(pomodoroId)
      .orElseThrow(() -> new RuntimeException("La sesion no exite"));
  }

  private LocalDateTime ahora() {
    return LocalDateTime.now(this.reloj);
  }

  private boolean estaFinalizada(Pomodoro sesion) {
    return sesion.getEstado() == Estado.COMPLETADO || sesion.getEstado() == Estado.CANCELADO;
  }

  private boolean haExpirado(Pomodoro sesion) {
    return (
      sesion.getEstado() == Estado.EN_CURSO &&
      sesion.getFechaInicio() != null &&
      this.calcularSegundosRestantes(sesion) <= 0
    );
  }

  /**
   * Segundos que le quedan a la sesion. Si esta pausada el tiempo se congela en el momento de la
   * pausa; si esta en curso se mide contra la hora actual del servidor. Como al reanudar se
   * corre fechaInicio hacia adelante, el tiempo pausado nunca se descuenta.
   */
  private long calcularSegundosRestantes(Pomodoro sesion) {
    if (sesion.getFechaInicio() == null) return 0;

    long segundosTotales = (long) sesion.getDuracionMinutos() * SEGUNDOS_POR_MINUTO;
    boolean congelada = sesion.getEstado() == Estado.PAUSADO && sesion.getFechaPausa() != null;
    LocalDateTime referencia = congelada ? sesion.getFechaPausa() : this.ahora();

    long transcurridos = Math.max(
      0,
      Duration.between(sesion.getFechaInicio(), referencia).getSeconds()
    );
    return Math.max(0, segundosTotales - transcurridos);
  }

  private void completarAlVencer(Pomodoro sesion) {
    LocalDateTime finTeorico = sesion
      .getFechaInicio()
      .plusSeconds((long) sesion.getDuracionMinutos() * SEGUNDOS_POR_MINUTO);
    this.completar(sesion, finTeorico);
  }

  private void completar(Pomodoro sesion, LocalDateTime fechaFin) {
    sesion.setEstado(Estado.COMPLETADO);
    sesion.setFechaFin(fechaFin);
    this.repositorioPomodoro.actualizar(sesion);

    Usuario usuario = sesion.getUsuario();
    usuario.setPuntos(usuario.getPuntos() + PUNTOS_POR_SESION_COMPLETADA);
    this.repositorioUsuario.modificar(usuario);
  }
}
