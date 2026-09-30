package com.tallerwebi.dominio.calendario;

import com.tallerwebi.dominio.tarea.Tarea;
import java.time.LocalDate;

/**
 * Deriva el {@link EstadoActividad} de una {@code Tarea} para el calendario (CAL-02). A
 * diferencia de {@code ServicioTareaImpl.calcularIndicador} (que trata Parciales y Trabajos
 * Prácticos con reglas separadas, uno por fecha y otro por horas), acá se usa una única regla
 * para los dos tipos: la urgencia por fecha pesa más que el progreso, y el progreso solo importa
 * cuando todavía hay tiempo de sobra. Por eso el calendario puede mostrar un estado distinto al
 * que ve /tareas para la misma Tarea — es información más rica, no un error.
 *
 * <p>Orden de prioridad: Completada &gt; Vencida &gt; Próxima a vencer &gt; Parcialmente
 * completada &gt; En tiempo.
 */
public final class CalculadorEstadoActividad {

  static final String ESTADO_COMPLETADA = "COMPLETADA";
  static final int DIAS_PROXIMO_VENCIMIENTO = 3;
  static final double PORCENTAJE_COMPLETO = 100.0;
  static final double UMBRAL_PARCIALMENTE_COMPLETADA = 50.0;

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
    double porcentaje = calcularPorcentajeHoras(tarea);
    if (porcentaje >= UMBRAL_PARCIALMENTE_COMPLETADA && porcentaje < PORCENTAJE_COMPLETO) {
      return EstadoActividad.PARCIALMENTE_COMPLETADA;
    }
    return EstadoActividad.EN_TIEMPO;
  }

  /** Mismo cálculo que {@code ServicioTareaImpl.calcularPorcentajeHoras}. */
  private static double calcularPorcentajeHoras(Tarea tarea) {
    Integer planificadas = tarea.getHorasPlanificadas();
    if (planificadas == null || planificadas <= 0) {
      return 0;
    }
    Integer realizadas = tarea.getHorasRealizadas();
    if (realizadas == null || realizadas < 0) {
      return 0;
    }
    return (realizadas * 100.0) / planificadas;
  }
}
