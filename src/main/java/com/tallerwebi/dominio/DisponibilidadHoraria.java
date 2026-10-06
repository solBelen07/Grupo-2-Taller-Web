package com.tallerwebi.dominio;

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

  public String getDia() {
    return dia;
  }

  public void setDia(String dia) {
    this.dia = dia;
  }

  public int getHoras() {
    return horas;
  }

  public void setHoras(int horas) {
    this.horas = horas;
  }
}
