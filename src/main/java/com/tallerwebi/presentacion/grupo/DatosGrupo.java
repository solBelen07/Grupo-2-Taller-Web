package com.tallerwebi.presentacion.grupo;

import java.util.ArrayList;
import java.util.List;

public class DatosGrupo {

  private String nombre;
  private Integer cantidadDeIntegrantes;
  private List<DatosIntegrante> integrantes = new ArrayList<>();
  private boolean soyIntegrante;
  private boolean soyAdministrador;

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

  public List<DatosIntegrante> getIntegrantes() {
    return integrantes;
  }

  public void setIntegrantes(List<DatosIntegrante> integrantes) {
    this.integrantes = integrantes;
  }

  public boolean isSoyIntegrante() {
    return soyIntegrante;
  }

  public void setSoyIntegrante(boolean soyIntegrante) {
    this.soyIntegrante = soyIntegrante;
  }

  public boolean isSoyAdministrador() {
    return soyAdministrador;
  }

  public void setSoyAdministrador(boolean soyAdministrador) {
    this.soyAdministrador = soyAdministrador;
  }
}
