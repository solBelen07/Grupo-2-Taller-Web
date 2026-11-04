package com.tallerwebi.dominio.excepcion.excepcionGrupo;

public class NoEsIntegranteDelGrupo extends Exception {

  private static final long serialVersionUID = 1L;

  public NoEsIntegranteDelGrupo() {
    super("Solo los integrantes del grupo pueden hacer esto");
  }
}
