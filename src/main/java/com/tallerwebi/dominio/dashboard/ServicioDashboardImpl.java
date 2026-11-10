package com.tallerwebi.dominio.dashboard;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.ServicioCalendario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.calendario.EstadoActividad;
import com.tallerwebi.dominio.calendario.EventoConEstado;
import com.tallerwebi.dominio.calendario.FuenteDeEventos;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import com.tallerwebi.dominio.racha.ServicioRacha;
import com.tallerwebi.dominio.sesionEstudio.RepositorioSesionEstudio;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import com.tallerwebi.dominio.tarea.ServicioTarea;
import com.tallerwebi.dominio.tecnicasestudio.RepositorioPomodoro;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service("ServicioDashboard")
@Transactional
public class ServicioDashboardImpl implements ServicioDashboard {

  private static final int MINUTOS_POR_HORA = 60;
  static final int DIAS_A_FUTURO_VENCIMIENTOS = 30;
  static final int MAXIMO_VENCIMIENTOS = 3;

  private final RepositorioPomodoro repositorioPomodoro;
  private final RepositorioMateria repositorioMateria;
  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioSesionEstudio repositorioSesionEstudio;

  private final ServicioTarea servicioTarea;
  private final ServicioRacha servicioRacha;
  private final ServicioCalendario servicioCalendario;
  private final List<FuenteDeEventos> fuentesDeEventos;

  public ServicioDashboardImpl(
    RepositorioPomodoro repositorioPomodoro,
    RepositorioMateria repositorioMateria,
    RepositorioUsuario repositorioUsuario,
    RepositorioSesionEstudio repositorioSesionEstudio,
    ServicioTarea servicioTarea,
    ServicioRacha servicioRacha,
    ServicioCalendario servicioCalendario,
    List<FuenteDeEventos> fuentesDeEventos
  ) {
    this.repositorioPomodoro = repositorioPomodoro;
    this.repositorioMateria = repositorioMateria;
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioSesionEstudio = repositorioSesionEstudio;
    this.servicioTarea = servicioTarea;
    this.servicioRacha = servicioRacha;
    this.servicioCalendario = servicioCalendario;
    this.fuentesDeEventos = fuentesDeEventos;
  }

  @Override
  public Long obtenerHorasEstudio(Long idUsuario) {
    long horasPomodoro =
      this.repositorioPomodoro.calcularMinutosCompletados(idUsuario) / MINUTOS_POR_HORA;
    long horasSesiones = this.repositorioSesionEstudio.calcularHorasCompletadas(idUsuario);

    return horasPomodoro + horasSesiones;
  }

  @Override
  public Double porcentajeCumplimiento(Long idUsuario) {
    // Las tareas guardan el EMAIL del responsable, así que hay que buscar el aporte por email.
    Usuario usuario = this.repositorioUsuario.buscarPorId(idUsuario);
    if (usuario == null) return 0.0;
    String emailUsuario = usuario.getEmail();

    Map<String, Long> aportes = this.servicioTarea.calcularAporteIndividual();

    long total = aportes.values().stream().mapToLong(Long::longValue).sum();

    if (total == 0) return 0.0;

    Long cumplimiento = aportes.getOrDefault(emailUsuario, 0L);
    return ((double) cumplimiento / total) * 100;
  }

  @Override
  public CalendarioSemanal obtenerCalendarioSemanal(Long idUsuario, LocalDate referencia) {
    return this.servicioCalendario.obtenerSemana(idUsuario, referencia);
  }

  @Override
  public Long obtenerCantidadMaterias(Long idUsuario) {
    return this.repositorioMateria.obtenerCantidadMateriasPorUsuario(idUsuario);
  }

  @Override
  public SesionEstudio obtenerProximaSesion(Long idUsuario) {
    return this.repositorioSesionEstudio.buscarProximaSesionEstudio(idUsuario);
  }

  @Override
  public Integer obtenerRacha(Long idUsuario) {
    Usuario usuario = this.repositorioUsuario.buscarPorId(idUsuario);
    return this.servicioRacha.obtenerRacha(usuario);
  }

  @Override
  public Integer obtenerRecordRacha(Long idUsuario) {
    Usuario usuario = this.repositorioUsuario.buscarPorId(idUsuario);
    return this.servicioRacha.obtenerRecordRacha(usuario);
  }

  /**
   * Los próximos parciales y entregas de TP del usuario, de ahora en adelante y ordenados por
   * fecha. Reutiliza las fuentes de eventos del calendario, así la conversión de parciales y TPs
   * en eventos está en un solo lugar.
   */
  @Override
  public List<EventoConEstado> obtenerProximosVencimientos(Long idUsuario, LocalDateTime ahora) {
    LocalDate hoy = ahora.toLocalDate();
    LocalDateTime hasta = hoy.plusDays(DIAS_A_FUTURO_VENCIMIENTOS + 1L).atStartOfDay();
    Set<Integer> materiasDelUsuario = idsDeMateriasDelUsuario(idUsuario);

    return this.fuentesDeEventos.stream()
      .flatMap(fuente -> fuente.eventosEntre(idUsuario, ahora, hasta, hoy).stream())
      .filter(vencimiento -> !vencimiento.evento().getInicio().isBefore(ahora))
      .filter(vencimiento -> vencimiento.estado() != EstadoActividad.COMPLETADA)
      .filter(vencimiento -> esDeSusMaterias(vencimiento, materiasDelUsuario))
      .sorted(Comparator.comparing(vencimiento -> vencimiento.evento().getInicio()))
      .limit(MAXIMO_VENCIMIENTOS)
      .collect(Collectors.toList());
  }

  private Set<Integer> idsDeMateriasDelUsuario(Long idUsuario) {
    return this.repositorioMateria.obtenerPorUsuario(idUsuario)
      .stream()
      .map(Materia::getId)
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
  }

  private static boolean esDeSusMaterias(EventoConEstado vencimiento, Set<Integer> materias) {
    Materia materia = vencimiento.evento().getMateria();
    return materia != null && materias.contains(materia.getId());
  }
}
