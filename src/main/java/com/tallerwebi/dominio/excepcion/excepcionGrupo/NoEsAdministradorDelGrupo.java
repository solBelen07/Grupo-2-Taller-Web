package com.tallerwebi.dominio.excepcion.excepcionGrupo;

public class NoEsAdministradorDelGrupo extends Exception {

  private static final long serialVersionUID = 1L;

  public NoEsAdministradorDelGrupo() {
    super("Solo el administrador del grupo puede hacer esto");
  }
}
