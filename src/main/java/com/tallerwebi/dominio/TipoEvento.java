package com.tallerwebi.dominio;

/** Tipos de evento académico que se muestran en el calendario. */
public enum TipoEvento {
  CLASE("Clase"),
  PARCIAL("Parcial"),
  TRABAJO_PRACTICO("Trabajo práctico"),
  ESTUDIO("Estudio"),
  ACTIVIDAD_GRUPAL("Actividad grupal");

  private final String etiqueta;

  TipoEvento(String etiqueta) {
    this.etiqueta = etiqueta;
  }

  public String getEtiqueta() {
    return etiqueta;
  }
}
