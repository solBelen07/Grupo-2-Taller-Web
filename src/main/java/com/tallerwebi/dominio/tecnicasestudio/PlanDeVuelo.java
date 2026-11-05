package com.tallerwebi.dominio.tecnicasestudio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PlanDeVuelo {

  @Column(name = "materia", nullable = false)
  private String materia;

  @Column(name = "origen", nullable = false)
  private String origen;

  @Column(name = "destino", nullable = false)
  private String destino;

  @Column(name = "objetivo", nullable = false)
  private String objetivo;

  public PlanDeVuelo() {}

  public PlanDeVuelo(String materia, String origen, String destino, String objetivo) {
    this.materia = materia;
    this.origen = origen;
    this.destino = destino;
    this.objetivo = objetivo;
  }

  @SuppressWarnings("all")
  public String getMateria() {
    return materia;
  }

  @SuppressWarnings("all")
  public void setMateria(String materia) {
    this.materia = materia;
  }

  @SuppressWarnings("all")
  public String getOrigen() {
    return origen;
  }

  @SuppressWarnings("all")
  public void setOrigen(String origen) {
    this.origen = origen;
  }

  @SuppressWarnings("all")
  public String getDestino() {
    return destino;
  }

  @SuppressWarnings("all")
  public void setDestino(String destino) {
    this.destino = destino;
  }

  @SuppressWarnings("all")
  public String getObjetivo() {
    return objetivo;
  }

  @SuppressWarnings("all")
  public void setObjetivo(String objetivo) {
    this.objetivo = objetivo;
  }
}
