package com.tallerwebi.presentacion.grupo;

public class DatosGrupo {

  private String nombre;
  private Integer cantidadDeIntegrantes;

  public DatosGrupo(String nombre, Integer cantidadDeIntegrantes) {
    this.nombre = nombre;
    this.cantidadDeIntegrantes = cantidadDeIntegrantes;
  }

  public DatosGrupo() {}

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Integer getCantidadDeIntegrantes() {
    return cantidadDeIntegrantes;
  }

  public void setCantidadDeIntegrantes(Integer cantidadDeIntegrantes) {
    this.cantidadDeIntegrantes = cantidadDeIntegrantes;
  }
}
