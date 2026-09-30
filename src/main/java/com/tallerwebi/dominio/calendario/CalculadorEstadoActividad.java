package com.tallerwebi.dominio.calendario;

import com.tallerwebi.dominio.tarea.Tarea;
import java.time.LocalDate;

/**
 * Deriva el {@link EstadoActividad} de una {@code Tarea} sin modificar esa clase ni el indicador
 * que ya calcula {@code ServicioTareaImpl} para /tareas: esto es una lectura de sus mismos datos
 * (estado, horas, fecha de vencimiento) adaptada al vocabulario de 5 estados que pide CAL-02.
 */
public final class CalculadorEstadoActividad {

  static final String ESTADO_COMPLETADA = "COMPLETADA";
  static final int DIAS_PROXIMO_VENCIMIENTO = 3;

  private CalculadorEstadoActividad() {}

  public static EstadoActividad calcular(Tarea tarea, LocalDate hoy) {
    if (ESTADO_COMPLETADA.equals(tarea.getEstado())) {
      return EstadoActividad.COMPLETADA;
    }
    LocalDate vencimiento = tarea.getFechaVencimiento();
    if (vencimiento != null && vencimiento.isBefore(hoy)) {
      return EstadoActividad.VENCIDA;
    }
    if (vencimiento != null && !vencimiento.isAfter(hoy.plusDays(DIAS_PROXIMO_VENCIMIENTO))) {
      return EstadoActividad.PROXIMA_A_VENCER;
    }
    if (tieneProgresoParcial(tarea)) {
      return EstadoActividad.PARCIALMENTE_COMPLETADA;
    }
    return EstadoActividad.EN_TIEMPO;
  }

  private static boolean tieneProgresoParcial(Tarea tarea) {
    Integer realizadas = tarea.getHorasRealizadas();
    Integer planificadas = tarea.getHorasPlanificadas();
    return (
      realizadas != null && realizadas > 0 && planificadas != null && realizadas < planificadas
    );
  }
}
