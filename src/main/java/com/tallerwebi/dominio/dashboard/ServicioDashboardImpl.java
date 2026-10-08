package com.tallerwebi.dominio.dashboard;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.ServicioCalendario;
import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import com.tallerwebi.dominio.sesionEstudio.RepositorioSesionEstudio;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import com.tallerwebi.dominio.tarea.ServicioTarea;
import com.tallerwebi.dominio.tecnicasestudio.RepositorioPomodoro;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service("ServicioDashboard")
@Transactional
public class ServicioDashboardImpl implements ServicioDashboard {

  private static final int MINUTOS_POR_HORA = 60;

  private final RepositorioPomodoro repositorioPomodoro;
  private final RepositorioMateria repositorioMateria;
  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioSesionEstudio repositorioSesionEstudio;

  private final ServicioCalendario servicioCalendario;
  private final ServicioTarea servicioTarea;

  public ServicioDashboardImpl(
    RepositorioPomodoro repositorioPomodoro,
    RepositorioMateria repositorioMateria,
    RepositorioUsuario repositorioUsuario,
    RepositorioSesionEstudio repositorioSesionEstudio,
    ServicioCalendario servicioCalendario,
    ServicioTarea servicioTarea
  ) {
    this.repositorioPomodoro = repositorioPomodoro;
    this.repositorioMateria = repositorioMateria;
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioSesionEstudio = repositorioSesionEstudio;
    this.servicioCalendario = servicioCalendario;
    this.servicioTarea = servicioTarea;
  }

  @Override
  public Long obtenerHorasEstudio(Long idUsuario) {
    return this.repositorioPomodoro.calcularMinutosCompletados(idUsuario) / MINUTOS_POR_HORA;
  }

  @Override
  public Double porcentajeCumplimiento(Long idUsuario) {
    String nombreUsuario = this.repositorioUsuario.obtenerNombre(idUsuario);

    Map<String, Long> aportes = this.servicioTarea.calcularAporteIndividual();

    Long total = aportes.values().stream().mapToLong(Long::longValue).sum();

    if (total == null || total == 0) return 0.0;

    Long cumplimiento = aportes.getOrDefault(nombreUsuario, 0L);
    return ((double) cumplimiento / total) * 100;
  }

  @Override
  public Long obtenerCantidadMaterias(Long idUsuario) {
    return this.repositorioMateria.obtenerCantidadMateriasPorUsuario(idUsuario);
  }

  @Override
  public CalendarioSemanal obtenerCalendarioSemanal(Long idUsuario, LocalDate referencia) {
    return this.servicioCalendario.obtenerSemana(idUsuario, referencia);
  }

  @Override
  public SesionEstudio obtenerProximaSesion(Long idUsuario) {
    return this.repositorioSesionEstudio.buscarProximaSesionEstudio(idUsuario);
  }
}
