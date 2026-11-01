package com.tallerwebi.presentacion.gemini;

import java.util.List;

//SOLO PREGUNTAS Y OPCIONES
public class PreguntaParaResolver {

  private String enunciado;
  private List<String> opciones;

  public PreguntaParaResolver() {}

  public PreguntaParaResolver(String enunciado, List<String> opciones) {
    this.enunciado = enunciado;
    this.opciones = opciones;
  }

  public String getEnunciado() {
    return enunciado;
  }

  public void setEnunciado(String enunciado) {
    this.enunciado = enunciado;
  }

  public List<String> getOpciones() {
    return opciones;
  }

  public void setOpciones(List<String> opciones) {
    this.opciones = opciones;
  }
}
