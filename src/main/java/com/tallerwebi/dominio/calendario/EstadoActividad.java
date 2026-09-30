package com.tallerwebi.dominio.calendario;

/**
 * Estado de seguimiento de una actividad (parcial o trabajo práctico) del calendario, derivado de
 * la {@code Tarea} vinculada en /tareas. El orden de las constantes es también el de prioridad
 * cuando más de una condición aplica: completada siempre gana, después la urgencia por fecha, y
 * recién después el progreso parcial.
 */
public enum EstadoActividad {
  COMPLETADA("Completada"),
  VENCIDA("Vencida"),
  PROXIMA_A_VENCER("Próxima a vencer"),
  PARCIALMENTE_COMPLETADA("Parcialmente completada"),
  EN_TIEMPO("En tiempo");

  private final String etiqueta;

  EstadoActividad(String etiqueta) {
    this.etiqueta = etiqueta;
  }

  public String getEtiqueta() {
    return etiqueta;
  }
}
