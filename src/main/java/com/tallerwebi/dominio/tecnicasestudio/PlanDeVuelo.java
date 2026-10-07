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

  public String getMateria() {
    return materia;
  }

  public void setMateria(String materia) {
    this.materia = materia;
  }

  public String getOrigen() {
    return origen;
  }

  public void setOrigen(String origen) {
    this.origen = origen;
  }

  public String getDestino() {
    return destino;
  }

  public void setDestino(String destino) {
    this.destino = destino;
  }

  public String getObjetivo() {
    return objetivo;
  }

  public void setObjetivo(String objetivo) {
    this.objetivo = objetivo;
  }
}
