package com.tallerwebi.presentacion.gemini;

public class DatosGemini {

  private String pregunta;
  private String respuesta;

  public DatosGemini(String respuesta, String pregunta) {
    this.respuesta = respuesta;
    this.pregunta = pregunta;
  }

  public DatosGemini() {}

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
}
