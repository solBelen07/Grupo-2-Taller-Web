package com.tallerwebi.presentacion.gemini;

public class SolicitudEjercicio {

  private Integer materiaId;
  private String tema;
  private Integer cantidad;

  public Integer getMateriaId() {
    return materiaId;
  }

  public void setMateriaId(Integer materiaId) {
    this.materiaId = materiaId;
  }

  public String getTema() {
    return tema;
  }

  public void setTema(String tema) {
    this.tema = tema;
  }

  public Integer getCantidad() {
    return cantidad;
  }

  public void setCantidad(Integer cantidad) {
    this.cantidad = cantidad;
  }
}
