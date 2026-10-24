package com.tallerwebi.presentacion.grupo;

// Datos de un integrante que se muestran en el detalle del grupo (sin exponer la contraseña).
public class DatosIntegrante {

  private String nombre;
  private String apellido;
  private String email;

  public DatosIntegrante(String nombre, String apellido, String email) {
    this.nombre = nombre;
    this.apellido = apellido;
    this.email = email;
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
