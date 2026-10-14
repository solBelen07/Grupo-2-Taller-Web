package com.tallerwebi.presentacion.gemini;

public class GeminiDto {

  private String pregunta;
  private String respuesta;
  private String reglaAdicional;

  public String getPregunta() {
    return pregunta;
  }

  public void setPregunta(String pregunta) {
    this.pregunta = pregunta;
  }

  public String getRespuesta() {
    return respuesta;
  }

  public void setRespuesta(String respuesta) {
    this.respuesta = respuesta;
  }

  public String getReglaAdicional() {
    return reglaAdicional;
  }

  public void setReglaAdicional(String reglaAdicional) {
    this.reglaAdicional = reglaAdicional;
  }
}
