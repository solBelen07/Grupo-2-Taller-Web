package com.tallerwebi.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Materia {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String nombre;
  private String carrera;

  public Materia(String nombre, String carrera) {
    this.nombre = nombre;
    this.carrera = carrera;
  }

  public Materia() {}

  public String getNombre() {
    return nombre;
  }

  public String getCarrera() {
    return carrera;
  }
}
