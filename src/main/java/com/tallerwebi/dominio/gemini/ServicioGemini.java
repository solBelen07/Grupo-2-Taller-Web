package com.tallerwebi.dominio.gemini;

public interface ServicioGemini {
  String preguntar(String pregunta);

  String preguntarConRegla(String pregunta, String regla);

  String generarEjercicios(String materia, String tema, int cantidad);

  void limpiar();
}
