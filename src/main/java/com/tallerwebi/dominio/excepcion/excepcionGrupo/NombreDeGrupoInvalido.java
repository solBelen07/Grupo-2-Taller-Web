package com.tallerwebi.dominio.excepcion.excepcionGrupo;

public class NombreDeGrupoInvalido extends Exception {

  private static final long serialVersionUID = 1L;

  public NombreDeGrupoInvalido(String mensaje) {
    super(mensaje);
  }
}
