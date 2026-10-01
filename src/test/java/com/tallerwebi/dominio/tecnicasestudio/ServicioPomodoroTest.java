package com.tallerwebi.dominio.tecnicasestudio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioPomodoroTest {

  private static final long ID_SESION = 1L;
  private static final long ID_USUARIO = 10L;
  private static final int DURACION_MINUTOS = 25;
  private static final LocalDateTime INICIO = LocalDateTime.of(2026, 9, 30, 10, 0, 0);

  private RepositorioPomodoro repositorioPomodoroMock;
  private RepositorioUsuario repositorioUsuarioMock;
  private RelojManual reloj;
  private ServicioPomodoro servicioPomodoro;

  @BeforeEach
  public void init() {
    this.repositorioPomodoroMock = mock(RepositorioPomodoro.class);
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.reloj = new RelojManual(INICIO);
    this.servicioPomodoro =
      new ServicioPomodoroImpl(
        this.repositorioPomodoroMock,
        this.repositorioUsuarioMock,
        this.reloj
      );
  }

  @Test
  public void registrarSesionEstudioDeberiaGuardarUnaSesionEnCursoConLaHoraDeInicio() {
    Usuario usuario = this.crearUsuario(0);
    Materia materia = new Materia();
    materia.setNombre("Taller Web I");

    Pomodoro registrada =
      this.servicioPomodoro.registrarSesionEstudio(
          "ARG",
          "ESP",
          DURACION_MINUTOS,
          materia,
          usuario,
          "Estudiar para Taller Web"
        );

    ArgumentCaptor<Pomodoro> captor = ArgumentCaptor.forClass(Pomodoro.class);
    verify(this.repositorioPomodoroMock, times(1)).guardar(captor.capture());

    Pomodoro guardada = captor.getValue();
    assertThat(guardada, is(registrada));
  }

  @Test
  public void registrarSesionEstudioDeberiaLanzarExcepcionSiYaExisteUnaSesionEnCurso() {
    Usuario usuario = this.crearUsuario(0);
    Pomodoro sesionEnCurso = this.crearSesion(usuario, Estado.EN_CURSO);
    when(this.repositorioPomodoroMock.buscarPorUsuarioYEstado(ID_USUARIO, Estado.EN_CURSO))
      .thenReturn(Optional.of(sesionEnCurso));

    RuntimeException excepcion = assertThrows(
      RuntimeException.class,
      () -> this.registrarSesionDePrueba(usuario)
    );

    assertThat(excepcion.getMessage(), equalTo("Ya tenes una sesion en curso"));
    verify(this.repositorioPomodoroMock, never()).guardar(any(Pomodoro.class));
  }

  @Test
  public void registrarSesionEstudioDeberiaLanzarExcepcionSiYaExisteUnaSesionPausada() {
    Usuario usuario = this.crearUsuario(0);
    Pomodoro sesionPausada = this.crearSesion(usuario, Estado.PAUSADO);
    when(this.repositorioPomodoroMock.buscarPorUsuarioYEstado(ID_USUARIO, Estado.PAUSADO))
      .thenReturn(Optional.of(sesionPausada));

    assertThrows(RuntimeException.class, () -> this.registrarSesionDePrueba(usuario));

    verify(this.repositorioPomodoroMock, never()).guardar(any(Pomodoro.class));
  }

  @Test
  public void registrarSesionEstudioDeberiaCerrarLaSesionVencidaYPermitirRegistrarUnaNueva() {
    Usuario usuario = this.crearUsuario(0);
    Pomodoro vencida = this.crearSesion(usuario, Estado.EN_CURSO);
    when(this.repositorioPomodoroMock.buscarPorUsuarioYEstado(ID_USUARIO, Estado.EN_CURSO))
      .thenReturn(Optional.of(vencida));

    this.reloj.avanzar(Duration.ofMinutes(DURACION_MINUTOS + 5));
    this.registrarSesionDePrueba(usuario);

    assertThat(vencida.getEstado(), equalTo(Estado.COMPLETADO));
    assertThat(usuario.getPuntos(), equalTo(3));
    verify(this.repositorioPomodoroMock, times(1)).guardar(any(Pomodoro.class));
  }

  @Test
  public void registrarSesionEstudioDeberiaLanzarExcepcionSiLaDuracionNoEsValida() {
    Usuario usuario = this.crearUsuario(0);

    assertThrows(
      IllegalArgumentException.class,
      () ->
        this.servicioPomodoro.registrarSesionEstudio("ARG", "ESP", 0, new Materia(), usuario, "")
    );

    verify(this.repositorioPomodoroMock, never()).guardar(any(Pomodoro.class));
  }

  @Test
  public void obtenerSegundosRestantesDeberiaDevolverLaDuracionCompletaAlIniciarLaSesion() {
    this.dadoUnaSesion(this.crearSesion(this.crearUsuario(0), Estado.EN_CURSO));

    assertThat(this.servicioPomodoro.obtenerSegundosRestantes(ID_SESION), equalTo(1500L));
  }

  @Test
  public void obtenerSegundosRestantesDeberiaDescontarElTiempoTranscurridoSegunElServidor() {
    this.dadoUnaSesion(this.crearSesion(this.crearUsuario(0), Estado.EN_CURSO));

    this.reloj.avanzar(Duration.ofMinutes(10));

    assertThat(this.servicioPomodoro.obtenerSegundosRestantes(ID_SESION), equalTo(900L));
  }

  @Test
  public void obtenerSegundosRestantesDeberiaCompletarLaSesionYSumarPuntosCuandoSeAgotaElTiempo() {
    Usuario usuario = this.crearUsuario(5);
    Pomodoro sesion = this.dadoUnaSesion(this.crearSesion(usuario, Estado.EN_CURSO));

    this.reloj.avanzar(Duration.ofMinutes(DURACION_MINUTOS + 3));

    assertThat(this.servicioPomodoro.obtenerSegundosRestantes(ID_SESION), equalTo(0L));
    assertThat(sesion.getEstado(), equalTo(Estado.COMPLETADO));
    assertThat(sesion.getFechaFin(), equalTo(INICIO.plusMinutes(DURACION_MINUTOS)));
    assertThat(usuario.getPuntos(), equalTo(8));
    verify(this.repositorioPomodoroMock, times(1)).actualizar(sesion);
    verify(this.repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void obtenerSegundosRestantesNoDeberiaOtorgarPuntosDosVecesAlConsultarDeNuevo() {
    Usuario usuario = this.crearUsuario(0);
    this.dadoUnaSesion(this.crearSesion(usuario, Estado.EN_CURSO));
    this.reloj.avanzar(Duration.ofMinutes(DURACION_MINUTOS + 1));

    this.servicioPomodoro.obtenerSegundosRestantes(ID_SESION);
    this.servicioPomodoro.obtenerSegundosRestantes(ID_SESION);

    assertThat(usuario.getPuntos(), equalTo(3));
    verify(this.repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void obtenerSegundosRestantesDeberiaDevolverCeroSiLaSesionNoExiste() {
    when(this.repositorioPomodoroMock.buscarPorId(ID_SESION)).thenReturn(Optional.empty());

    assertThat(this.servicioPomodoro.obtenerSegundosRestantes(ID_SESION), equalTo(0L));
  }

  @Test
  public void obtenerSegundosRestantesDeberiaDevolverCeroSiLaSesionFueCancelada() {
    Usuario usuario = this.crearUsuario(4);
    this.dadoUnaSesion(this.crearSesion(usuario, Estado.CANCELADO));

    assertThat(this.servicioPomodoro.obtenerSegundosRestantes(ID_SESION), equalTo(0L));
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void pausarSesionDeberiaGuardarElMomentoDeLaPausaYCongelarElTiempoRestante() {
    Pomodoro sesion = this.dadoUnaSesion(this.crearSesion(this.crearUsuario(0), Estado.EN_CURSO));
    this.reloj.avanzar(Duration.ofMinutes(5));

    this.servicioPomodoro.pausarSesion(ID_SESION);

    assertThat(sesion.getEstado(), equalTo(Estado.PAUSADO));
    assertThat(sesion.getFechaPausa(), equalTo(INICIO.plusMinutes(5)));
    verify(this.repositorioPomodoroMock, times(1)).actualizar(sesion);

    this.reloj.avanzar(Duration.ofMinutes(20));
    assertThat(this.servicioPomodoro.obtenerSegundosRestantes(ID_SESION), equalTo(1200L));
  }

  @Test
  public void pausarSesionNoDeberiaCambiarNadaSiLaSesionYaEstaPausada() {
    Pomodoro sesion = this.dadoUnaSesion(this.crearSesion(this.crearUsuario(0), Estado.PAUSADO));
    LocalDateTime pausaOriginal = INICIO.plusMinutes(2);
    sesion.setFechaPausa(pausaOriginal);
    this.reloj.avanzar(Duration.ofMinutes(10));

    this.servicioPomodoro.pausarSesion(ID_SESION);

    assertThat(sesion.getFechaPausa(), equalTo(pausaOriginal));
    verify(this.repositorioPomodoroMock, never()).actualizar(any(Pomodoro.class));
  }

  @Test
  public void pausarSesionDeberiaCompletarLaSesionSiElTiempoYaSeAgoto() {
    Usuario usuario = this.crearUsuario(0);
    Pomodoro sesion = this.dadoUnaSesion(this.crearSesion(usuario, Estado.EN_CURSO));
    this.reloj.avanzar(Duration.ofMinutes(DURACION_MINUTOS + 1));

    this.servicioPomodoro.pausarSesion(ID_SESION);

    assertThat(sesion.getEstado(), equalTo(Estado.COMPLETADO));
    assertThat(usuario.getPuntos(), equalTo(3));
  }

  @Test
  public void reanudarSesionDeberiaReajustarLaReferenciaDeInicioYRetomarElTiempo() {
    Pomodoro sesion = this.dadoUnaSesion(this.crearSesion(this.crearUsuario(0), Estado.EN_CURSO));
    this.reloj.avanzar(Duration.ofMinutes(5));
    this.servicioPomodoro.pausarSesion(ID_SESION);
    this.reloj.avanzar(Duration.ofMinutes(20));

    this.servicioPomodoro.reanudarSesion(ID_SESION);

    assertThat(sesion.getEstado(), equalTo(Estado.EN_CURSO));
    assertThat(sesion.getFechaPausa(), is(nullValue()));
    assertThat(sesion.getFechaInicio(), equalTo(INICIO.plusMinutes(20)));
    assertThat(this.servicioPomodoro.obtenerSegundosRestantes(ID_SESION), equalTo(1200L));

    this.reloj.avanzar(Duration.ofMinutes(2));
    assertThat(this.servicioPomodoro.obtenerSegundosRestantes(ID_SESION), equalTo(1080L));
  }

  @Test
  public void reanudarSesionNoDeberiaCambiarNadaSiLaSesionNoEstaPausada() {
    Pomodoro sesion = this.dadoUnaSesion(this.crearSesion(this.crearUsuario(0), Estado.EN_CURSO));
    this.reloj.avanzar(Duration.ofMinutes(3));

    this.servicioPomodoro.reanudarSesion(ID_SESION);

    assertThat(sesion.getFechaInicio(), equalTo(INICIO));
    verify(this.repositorioPomodoroMock, never()).actualizar(any(Pomodoro.class));
  }

  @Test
  public void pausarYReanudarDeberianLanzarExcepcionSiLaSesionNoExiste() {
    when(this.repositorioPomodoroMock.buscarPorId(ID_SESION)).thenReturn(Optional.empty());

    RuntimeException alPausar = assertThrows(
      RuntimeException.class,
      () -> this.servicioPomodoro.pausarSesion(ID_SESION)
    );
    RuntimeException alReanudar = assertThrows(
      RuntimeException.class,
      () -> this.servicioPomodoro.reanudarSesion(ID_SESION)
    );

    assertThat(alPausar.getMessage(), equalTo("La sesion no exite"));
    assertThat(alReanudar.getMessage(), equalTo("La sesion no exite"));
  }

  @Test
  public void modificarEstadoCompletadaDeberiaMarcarLaSesionYOtorgarPuntosAlUsuario() {
    Usuario usuario = this.crearUsuario(0);
    Pomodoro sesion = this.dadoUnaSesion(this.crearSesion(usuario, Estado.EN_CURSO));
    this.reloj.avanzar(Duration.ofMinutes(25));

    this.servicioPomodoro.modificarEstadoCompletada(ID_SESION);

    assertThat(sesion.getEstado(), equalTo(Estado.COMPLETADO));
    assertThat(sesion.getFechaFin(), equalTo(INICIO.plusMinutes(25)));
    assertThat(usuario.getPuntos(), equalTo(3));
    verify(this.repositorioPomodoroMock, times(1)).actualizar(sesion);
    verify(this.repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void modificarEstadoCompletadaNoDeberiaOtorgarPuntosSiLaSesionYaFinalizo() {
    Usuario usuario = this.crearUsuario(3);
    this.dadoUnaSesion(this.crearSesion(usuario, Estado.COMPLETADO));

    this.servicioPomodoro.modificarEstadoCompletada(ID_SESION);

    assertThat(usuario.getPuntos(), equalTo(3));
    verify(this.repositorioPomodoroMock, never()).actualizar(any(Pomodoro.class));
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void modificarEstadoCompletadaDeberiaLanzarExcepcionSiLaSesionNoExiste() {
    when(this.repositorioPomodoroMock.buscarPorId(ID_SESION)).thenReturn(Optional.empty());

    RuntimeException excepcion = assertThrows(
      RuntimeException.class,
      () -> this.servicioPomodoro.modificarEstadoCompletada(ID_SESION)
    );

    assertThat(excepcion.getMessage(), equalTo("La sesion no exite"));
    verify(this.repositorioPomodoroMock, never()).actualizar(any(Pomodoro.class));
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void modificarEstadoCanceladaDeberiaMarcarLaSesionYDescontarPuntos() {
    Usuario usuario = this.crearUsuario(10);
    Pomodoro sesion = this.dadoUnaSesion(this.crearSesion(usuario, Estado.EN_CURSO));
    this.reloj.avanzar(Duration.ofMinutes(4));

    this.servicioPomodoro.modificarEstadoCancelada(ID_SESION);

    assertThat(sesion.getEstado(), equalTo(Estado.CANCELADO));
    assertThat(sesion.getFechaFin(), equalTo(INICIO.plusMinutes(4)));
    assertThat(usuario.getPuntos(), equalTo(8));
    verify(this.repositorioPomodoroMock, times(1)).actualizar(sesion);
    verify(this.repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void modificarEstadoCanceladaNoDeberiaDejarElPuntajeEnNegativo() {
    Usuario usuario = this.crearUsuario(1);
    this.dadoUnaSesion(this.crearSesion(usuario, Estado.EN_CURSO));

    this.servicioPomodoro.modificarEstadoCancelada(ID_SESION);

    assertThat(usuario.getPuntos(), equalTo(0));
    verify(this.repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void modificarEstadoCanceladaDeberiaPoderCancelarUnaSesionPausada() {
    Usuario usuario = this.crearUsuario(5);
    Pomodoro sesion = this.dadoUnaSesion(this.crearSesion(usuario, Estado.PAUSADO));

    this.servicioPomodoro.modificarEstadoCancelada(ID_SESION);

    assertThat(sesion.getEstado(), equalTo(Estado.CANCELADO));
    assertThat(usuario.getPuntos(), equalTo(3));
  }

  @Test
  public void modificarEstadoCanceladaNoDeberiaDescontarPuntosDosVeces() {
    Usuario usuario = this.crearUsuario(10);
    this.dadoUnaSesion(this.crearSesion(usuario, Estado.EN_CURSO));

    this.servicioPomodoro.modificarEstadoCancelada(ID_SESION);
    this.servicioPomodoro.modificarEstadoCancelada(ID_SESION);

    assertThat(usuario.getPuntos(), equalTo(8));
    verify(this.repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void modificarEstadoCanceladaDeberiaLanzarExcepcionSiLaSesionNoExiste() {
    when(this.repositorioPomodoroMock.buscarPorId(ID_SESION)).thenReturn(Optional.empty());

    RuntimeException excepcion = assertThrows(
      RuntimeException.class,
      () -> this.servicioPomodoro.modificarEstadoCancelada(ID_SESION)
    );

    assertThat(excepcion.getMessage(), equalTo("La sesion no exite"));
    verify(this.repositorioPomodoroMock, never()).actualizar(any(Pomodoro.class));
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void buscarPorIdDeberiaDevolverLaSesionONullSiNoExiste() {
    Pomodoro sesion = this.dadoUnaSesion(this.crearSesion(this.crearUsuario(0), Estado.EN_CURSO));

    assertThat(this.servicioPomodoro.buscarPorId(ID_SESION), equalTo(sesion));

    when(this.repositorioPomodoroMock.buscarPorId(99L)).thenReturn(Optional.empty());
    assertThat(this.servicioPomodoro.buscarPorId(99L), is(nullValue()));
  }

  @Test
  public void buscarSesionActivaDeberiaDevolverLaSesionPausadaDelUsuario() {
    Usuario usuario = this.crearUsuario(0);
    Pomodoro pausada = this.crearSesion(usuario, Estado.PAUSADO);
    when(this.repositorioPomodoroMock.buscarPorUsuarioYEstado(ID_USUARIO, Estado.PAUSADO))
      .thenReturn(Optional.of(pausada));

    Optional<Pomodoro> activa = this.servicioPomodoro.buscarSesionActiva(usuario);

    assertThat(activa.isPresent(), is(true));
    assertThat(activa.get(), equalTo(pausada));
  }

  @Test
  public void buscarSesionActivaDeberiaDevolverVacioSiElUsuarioNoTieneSesiones() {
    Optional<Pomodoro> activa = this.servicioPomodoro.buscarSesionActiva(this.crearUsuario(0));
    assertThat(activa.isPresent(), is(false));
  }

  private void registrarSesionDePrueba(Usuario usuario) {
    this.servicioPomodoro.registrarSesionEstudio(
        "ARG",
        "ESP",
        DURACION_MINUTOS,
        new Materia(),
        usuario,
        "Estudiar para Taller Web"
      );
  }

  private Usuario crearUsuario(int puntos) {
    Usuario usuario = new Usuario();
    usuario.setId(ID_USUARIO);
    usuario.setEmail("nuevo@test.com");
    usuario.setPuntos(puntos);
    return usuario;
  }

  private Pomodoro crearSesion(Usuario usuario, Estado estado) {
    Pomodoro sesion = new Pomodoro();
    sesion.setId(ID_SESION);
    sesion.setUsuario(usuario);
    sesion.setEstado(estado);
    sesion.setDuracionMinutos(DURACION_MINUTOS);
    sesion.setFechaInicio(INICIO);
    sesion.setPlan(new PlanDeVuelo("Taller Web I", "ARG", "ESP", "Repasar"));
    return sesion;
  }

  private Pomodoro dadoUnaSesion(Pomodoro sesion) {
    when(this.repositorioPomodoroMock.buscarPorId(ID_SESION)).thenReturn(Optional.of(sesion));
    return sesion;
  }

  private static class RelojManual extends Clock {

    private Instant instante;

    RelojManual(LocalDateTime inicio) {
      this.instante = inicio.toInstant(ZoneOffset.UTC);
    }

    void avanzar(Duration duracion) {
      this.instante = this.instante.plus(duracion);
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zona) {
      return this;
    }

    @Override
    public Instant instant() {
      return this.instante;
    }
  }
}
