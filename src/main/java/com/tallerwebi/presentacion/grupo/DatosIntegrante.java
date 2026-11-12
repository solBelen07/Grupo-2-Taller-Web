package com.tallerwebi.presentacion.grupo;

import lombok.Getter;

// Datos de un integrante que se muestran en el detalle del grupo (sin exponer la contraseña).
@Getter
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
}
