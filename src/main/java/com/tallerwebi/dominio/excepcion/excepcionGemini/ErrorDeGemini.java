package com.tallerwebi.dominio.excepcion.excepcionGemini;

public class ErrorDeGemini extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ErrorDeGemini(String message) {
    super(message);
  }

  public ErrorDeGemini(String message, Throwable cause) {
    super(message, cause);
  }
}
