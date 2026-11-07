package com.tallerwebi.dominio.gemini;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.gemini.errores.ErroresGemini;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestOperations;

public class ServicioGeminiTest {

  private ServicioGemini servicioGemini;
  private RestOperations restTemplateMock;
  private static final String PREGUNTA = "¿Hola?";
  private static final String RESPUESTA_ESPERADA = "Respuesta Gemini";
  private static final String JSON_RESPONSE = jsonConTexto(RESPUESTA_ESPERADA);

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
    // La respuesta de preguntar() se devuelve convertida de Markdown a HTML.
    entoncesLaRespuestaEs(respuestaObtenida, "<p>" + RESPUESTA_ESPERADA + "</p>\n");
  }

  @Test
  public void deberiaConvertirLosAsteriscsDeMarkdownEnNegritaYCursiva() {
    dadoQueLaApiResponde(jsonConTexto("Esto es **negrita** y *cursiva*"));

    String respuesta = cuandoPregunto(PREGUNTA);

    assertThat(respuesta, containsString("<strong>negrita</strong>"));
    assertThat(respuesta, containsString("<em>cursiva</em>"));
    assertThat(respuesta, not(containsString("*")));
  }

  @Test
  public void deberiaConvertirLasListasConAsteriscosEnItemsHtml() {
    dadoQueLaApiResponde(jsonConTexto("*   **Tema 1:** uno\\n*   **Tema 2:** dos"));

    String respuesta = cuandoPregunto(PREGUNTA);

    assertThat(respuesta, containsString("<ul>"));
    assertThat(respuesta, containsString("<li><strong>Tema 1:</strong> uno</li>"));
    assertThat(respuesta, containsString("<li><strong>Tema 2:</strong> dos</li>"));
  }

  @Test
  public void deberiaEscaparElHtmlCrudoQueVengaEnLaRespuesta() {
    dadoQueLaApiResponde(jsonConTexto("hola <img src=x onerror=alert(1)> mundo"));

    String respuesta = cuandoPregunto(PREGUNTA);

    assertThat(respuesta, not(containsString("<img")));
    assertThat(respuesta, containsString("&lt;img src=x onerror=alert(1)&gt;"));
  }

  @Test
  public void deberiaEscaparUnScriptEnBloque() {
    dadoQueLaApiResponde(jsonConTexto("<script>alert(1)</script>"));

    String respuesta = cuandoPregunto(PREGUNTA);

    assertThat(respuesta, not(containsString("<script>")));
    assertThat(respuesta, containsString("&lt;script&gt;"));
  }

  @Test
  public void deberiaDevolverVacioSiGeminiRespondeSinTexto() {
    dadoQueLaApiResponde(jsonConTexto(""));

    entoncesLaRespuestaEs(cuandoPregunto(PREGUNTA), "");
  }

  @Test
  public void deberiaDevolverErrorDePeticionSiFallaElEnvio() {
    when(restTemplateMock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenThrow(new RuntimeException("sin conexión"));

    String respuesta = cuandoPregunto(PREGUNTA);

    assertThat(respuesta, startsWith(ErroresGemini.ERROR_AL_ARMAR_LA_PETICION));
    assertThat(respuesta, containsString("Error al armar la petición: "));
  }

  @Test
  public void deberiaDevolverErrorDePeticionAunqueLaExcepcionNoTengaMensaje() {
    when(restTemplateMock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenThrow(new RuntimeException());

    String respuesta = cuandoPregunto(PREGUNTA);

    assertThat(respuesta, equalTo(ErroresGemini.ERROR_AL_ARMAR_LA_PETICION + "null"));
  }

  @Test
  public void deberiaDevolverErrorDeRespuestaSiElJsonNoTieneCandidatos() {
    dadoQueLaApiResponde("{}");

    String respuesta = cuandoPregunto(PREGUNTA);

    assertThat(respuesta, startsWith("<p>" + ErroresGemini.ERROR_PROCESANDO_RESPUESTA));
    assertThat(ErroresGemini.esFallida(respuesta), equalTo(true));
  }

  @Test
  public void deberiaDevolverErrorDeRespuestaSiElJsonEsInvalido() {
    dadoQueLaApiResponde("esto no es json");
    assertThat(
      cuandoPregunto(PREGUNTA),
      startsWith("<p>" + ErroresGemini.ERROR_PROCESANDO_RESPUESTA)
    );
  }

  @Test
  public void deberiaEnviarLaPreguntaAGeminiConLaInstruccionDeResumen() {
    dadoQueLaApiResponde(JSON_RESPONSE);
    cuandoPregunto(PREGUNTA);

    String cuerpo = cuerpoEnviado();
    assertThat(cuerpo, containsString("resumen breve"));
    assertThat(cuerpo, containsString(PREGUNTA));
  }

  @Test
  public void generarEjerciciosDeberiaDevolverElJsonSinConvertirloAHtml() {
    String json = "{\\\"preguntas\\\":[]}";
    dadoQueLaApiResponde(jsonConTexto(json));

    String respuesta = servicioGemini.generarEjercicios("Programación", "Herencia", 5);

    entoncesLaRespuestaEs(respuesta, "{\"preguntas\":[]}");
    assertThat(cuerpoEnviado(), containsString("application/json"));
    assertThat(cuerpoEnviado(), containsString("Herencia"));
  }

  @Test
  public void generarEjerciciosDeberiaDevolverErrorSiFallaElEnvio() {
    when(restTemplateMock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenThrow(new RuntimeException("caído"));

    String respuesta = servicioGemini.generarEjercicios("Programación", "Herencia", 5);

    assertThat(respuesta, startsWith("Error procesando respuesta de Gemini: "));
  }

  private static String jsonConTexto(String texto) {
    return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + texto + "\"}]}}]}";
  }

  private void dadoQueLaApiResponde(String json) {
    when(this.restTemplateMock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenReturn(json);
  }

  private String cuandoPregunto(String pregunta) {
    return servicioGemini.preguntar(pregunta);
  }

  private String cuerpoEnviado() {
    ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplateMock).postForObject(anyString(), captor.capture(), eq(String.class));
    return String.valueOf(captor.getValue().getBody());
  }

  private void entoncesLaRespuestaEs(String respuestaActual, String respuestaEsperada) {
    assertThat(respuestaActual, equalTo(respuestaEsperada));
  }
}
