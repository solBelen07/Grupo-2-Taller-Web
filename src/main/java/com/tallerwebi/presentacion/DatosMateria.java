package com.tallerwebi.presentacion;

public class DatosMateria {

  private String nombre;
  private String carrera;

  public DatosMateria(String nombre, String carrera) {
    this.nombre = nombre;
    this.carrera = carrera;
  }

  public String getNombre() {
    return nombre;
  }

  public String getCarrera() {
    return carrera;
  }
}
