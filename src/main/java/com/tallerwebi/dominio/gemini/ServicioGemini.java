package com.tallerwebi.dominio.gemini;

public interface ServicioGemini {
  String preguntar(String pregunta);

  String preguntarConRegla(String pregunta, String regla);

  void limpiar();
}
