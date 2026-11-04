package com.tallerwebi.dominio.excepcion.excepcionGrupo;

public class GrupoExistente extends Exception {

  private static final long serialVersionUID = 1L;

  public GrupoExistente() {
    super("Ya existe un grupo con ese nombre");
  }
}
