package com.tallerwebi.presentacion.grupo;

// Datos de un integrante que se muestran en el detalle del grupo (sin exponer la contraseña).
public class DatosIntegrante {

  private String nombre;
  private String apellido;
  private String email;
  private boolean administrador;

  public DatosIntegrante(String nombre, String apellido, String email) {
    this(nombre, apellido, email, false);
  }

  public DatosIntegrante(String nombre, String apellido, String email, boolean administrador) {
    this.nombre = nombre;
    this.apellido = apellido;
    this.email = email;
    this.administrador = administrador;
  }

  public boolean isAdministrador() {
    return administrador;
  }

  public String getNombre() {
    return nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public String getEmail() {
    return email;
  }
}
