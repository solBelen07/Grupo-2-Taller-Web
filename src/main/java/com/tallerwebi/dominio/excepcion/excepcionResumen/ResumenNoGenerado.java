package com.tallerwebi.dominio.excepcion.excepcionResumen;

public class ResumenNoGenerado extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ResumenNoGenerado(String message) {
    super(message);
  }
}
