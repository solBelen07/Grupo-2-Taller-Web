package com.tallerwebi.dominio;

import jakarta.persistence.*;

@Embeddable
public class DisponibilidadHoraria {

  private String dia;
  private int horas;

  // Constructor para inicializar fácilmente el día y las horas
  public DisponibilidadHoraria(String dia, int horas) {
    this.dia = dia;
    this.horas = horas;
  }

  // Constructor vacío (por si lo necesita Spring o algún framework)
  public DisponibilidadHoraria() {}

  // Getters y Setters
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
