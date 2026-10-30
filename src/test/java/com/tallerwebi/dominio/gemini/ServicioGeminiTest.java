package com.tallerwebi.dominio.gemini;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestOperations;

public class ServicioGeminiTest {

  private ServicioGemini servicioGemini;
  private RestOperations restTemplateMock;
  private static final String PREGUNTA = "¿Hola?";
  private static final String RESPUESTA_ESPERADA = "Respuesta Gemini";
  private static final String JSON_RESPONSE =
    "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + RESPUESTA_ESPERADA + "\"}]}}]}";

  @BeforeEach
  public void inicializar() {
    restTemplateMock = mock(RestOperations.class);
    String apiKey = System.getenv("GEMINI_API_KEY");
    servicioGemini = new ServicioGeminiImpl(restTemplateMock, apiKey);
  }

  @Test
  public void deberiaObtenerUnaRespuestaAPartirDeUnaPreguntaYRegla() {
    dadoQueLaApiResponde(JSON_RESPONSE);
    String respuestaObtenida = cuandoPregunto(PREGUNTA);
    entoncesLaRespuestaEs(respuestaObtenida, RESPUESTA_ESPERADA);
  }

  private void dadoQueLaApiResponde(String json) {
    when(this.restTemplateMock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenReturn(json);
  }

  private String cuandoPregunto(String pregunta) {
    return servicioGemini.preguntar(pregunta);
  }

  private void entoncesLaRespuestaEs(String respuestaActual, String respuestaEsperada) {
    assertThat(respuestaActual, equalTo(respuestaEsperada));
  }
}
