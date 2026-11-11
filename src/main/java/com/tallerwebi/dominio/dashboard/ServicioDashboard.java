package com.tallerwebi.dominio.dashboard;

import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.calendario.EventoConEstado;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ServicioDashboard {
  Long obtenerHorasEstudio(Long idUsuario);
  Double porcentajeCumplimiento(Long idUsuario);
  Long obtenerCantidadMaterias(Long idUsuario);
  CalendarioSemanal obtenerCalendarioSemanal(Long idUsuario, LocalDate referencia);
  SesionEstudio obtenerProximaSesion(Long idUsuario);
  Integer obtenerRacha(Long idUsuario);
  Integer obtenerRecordRacha(Long idUsuario);

  List<EventoConEstado> obtenerProximosVencimientos(Long idUsuario, LocalDateTime ahora);
}
