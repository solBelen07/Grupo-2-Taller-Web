package com.tallerwebi.dominio.trabajosPracticos;

import jakarta.persistence.*;

@Embeddable
public class DisponibilidadHoraria {

  private String dia;
  private int horas;

  public DisponibilidadHoraria(String dia, int horas) {
    this.dia = dia;
    this.horas = horas;
  }

  public DisponibilidadHoraria() {}

  @SuppressWarnings("all")
  public String getDia() {
    return dia;
  }

  @SuppressWarnings("all")
  public void setDia(String dia) {
    this.dia = dia;
  }

  @SuppressWarnings("all")
  public int getHoras() {
    return horas;
  }

  @SuppressWarnings("all")
  public void setHoras(int horas) {
    this.horas = horas;
  }
}
