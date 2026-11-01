package com.tallerwebi.presentacion.gemini;

import java.util.List;

public class PreguntaEjercicio {

  private String enunciado;
  private List<String> opciones;
  private Integer respuestaCorrecta;
  private String explicacion;

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

  public Integer getRespuestaCorrecta() {
    return respuestaCorrecta;
  }

  public void setRespuestaCorrecta(Integer respuestaCorrecta) {
    this.respuestaCorrecta = respuestaCorrecta;
  }

  public String getExplicacion() {
    return explicacion;
  }

  public void setExplicacion(String explicacion) {
    this.explicacion = explicacion;
  }
}
