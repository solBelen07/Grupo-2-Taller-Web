package com.tallerwebi.presentacion.logro;

public class DatosLogro {

  private String nombre;
  private String descripcion;
  private boolean obtenido;
  private String icono;

  public DatosLogro(String nombre, String descripcion, boolean obtenido, String icono) {
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.obtenido = obtenido;
    this.icono = icono;
  }

  public String getNombre() {
    return nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public boolean isObtenido() {
    return obtenido;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public void setObtenido(boolean obtenido) {
    this.obtenido = obtenido;
  }

  public String getIcono() {
    return icono;
  }

  public void setIcono(String icono) {
    this.icono = icono;
  }
}
