package com.tallerwebi.dominio;

import com.tallerwebi.dominio.calendario.CalendarioMensual;
import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import java.time.LocalDate;

/** Arma el calendario unificado (todas las materias) de un alumno. */
public interface ServicioCalendario {
  /** Vista mensual del mes que contiene a {@code referencia}. */
  CalendarioMensual obtenerMes(Long usuarioId, LocalDate referencia);

  /** Vista semanal (lunes a domingo) de la semana que contiene a {@code referencia}. */
  CalendarioSemanal obtenerSemana(Long usuarioId, LocalDate referencia);
}
