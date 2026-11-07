package com.tallerwebi.dominio.gemini;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tallerwebi.dominio.gemini.errores.ErroresGemini;
import org.junit.jupiter.api.Test;

public class ErroresGeminiTest {

  @Test
  void unaRespuestaNulaEsFallida() {
    assertTrue(ErroresGemini.esFallida(null));
  }

  @Test
  void unaRespuestaVaciaOEnBlancoEsFallida() {
    assertTrue(ErroresGemini.esFallida(""));
    assertTrue(ErroresGemini.esFallida("   \n "));
  }

  @Test
  void elErrorDePeticionEsFallido() {
    assertTrue(ErroresGemini.esFallida(ErroresGemini.ERROR_AL_ARMAR_LA_PETICION + "timeout"));
  }

  @Test
  void elErrorDeRespuestaEnvueltoEnParrafoEsFallido() {
    assertTrue(
      ErroresGemini.esFallida("<p>" + ErroresGemini.ERROR_PROCESANDO_RESPUESTA + "null</p>\n")
    );
  }

  @Test
  void unResumenNormalNoEsFallido() {
    assertFalse(ErroresGemini.esFallida("<p>Un resumen válido</p>\n"));
  }

  @Test
  void unResumenQueHablaDeErroresNoSeConfundeConUnError() {
    assertFalse(ErroresGemini.esFallida("<p>Error cuadrático medio: mide la diferencia</p>\n"));
  }
}
