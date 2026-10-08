package com.tallerwebi.dominio.calendario;

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
