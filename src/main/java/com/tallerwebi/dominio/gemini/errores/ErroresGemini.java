package com.tallerwebi.dominio.gemini.errores;

public class ErroresGemini {

  public static final String ERROR_AL_ARMAR_LA_PETICION = "Error al armar la petición: ";
  public static final String ERROR_PROCESANDO_RESPUESTA = "Error procesando respuesta de Gemini: ";

  private static final String PARRAFO_ABIERTO = "<p>";

  public static boolean esFallida(String respuesta) {
    return (
      respuesta == null ||
      respuesta.isBlank() ||
      respuesta.startsWith(ERROR_AL_ARMAR_LA_PETICION) ||
      respuesta.startsWith(PARRAFO_ABIERTO + ERROR_PROCESANDO_RESPUESTA)
    );
  }
}
