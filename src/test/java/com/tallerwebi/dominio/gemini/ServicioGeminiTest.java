package com.tallerwebi.dominio.gemini;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.excepcionGemini.CuotaDeGeminiAgotada;
import com.tallerwebi.dominio.excepcion.excepcionGemini.ErrorDeGemini;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestOperations;

public class ServicioGeminiTest {

  private ServicioGemini servicioGemini;
  private RestOperations restTemplateMock;
  private static final String PREGUNTA = "¿Hola?";
  private static final String RESPUESTA_ESPERADA = "Respuesta Gemini";
  private static final String JSON_RESPONSE = jsonConTexto(RESPUESTA_ESPERADA);
  private static final String MODELO_PRINCIPAL = "modelo-principal";
  private static final String MODELO_ALTERNATIVO = "modelo-alternativo";
  private static final String API_KEY_VALIDA = "api-key-falsa-de-prueba";
  private static final String API_KEY_INVALIDA = "api-key-invalida-de-prueba";
  private static final String API_KEY_SIN_PERMISOS = "api-key-sin-permisos-de-prueba";

  @BeforeEach
  public void inicializar() {
    restTemplateMock = mock(RestOperations.class);
    servicioGemini = crearServicioConApiKey(API_KEY_VALIDA);
  }

  private ServicioGemini crearServicioConApiKey(String apiKey) {
    return new ServicioGeminiImpl(
      restTemplateMock,
      apiKey,
      List.of(MODELO_PRINCIPAL, MODELO_ALTERNATIVO)
    );
  }

  @Test
  public void deberiaObtenerUnaRespuestaAPartirDeUnaPreguntaYRegla() {
    dadoQueLaApiResponde(JSON_RESPONSE);
    String respuestaObtenida = cuandoPregunto(PREGUNTA);
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
  public void deberiaLanzarErrorDeGeminiSiNoHayConexion() {
    when(restTemplateMock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenThrow(new ResourceAccessException("sin conexión"));

    assertThrows(ErrorDeGemini.class, () -> cuandoPregunto(PREGUNTA));
  }

  @Test
  public void deberiaLanzarErrorDeGeminiSiElJsonNoTieneCandidatos() {
    dadoQueLaApiResponde("{}");

    assertThrows(ErrorDeGemini.class, () -> cuandoPregunto(PREGUNTA));
  }

  @Test
  public void deberiaLanzarErrorDeGeminiSiElJsonEsInvalido() {
    dadoQueLaApiResponde("esto no es json");

    assertThrows(ErrorDeGemini.class, () -> cuandoPregunto(PREGUNTA));
  }

  @Test
  public void deberiaCambiarAlSiguienteModeloSiSeAcaboLaCuota() {
    dadoQueElModeloResponde(MODELO_PRINCIPAL, error(HttpStatus.TOO_MANY_REQUESTS));
    when(
      restTemplateMock.postForObject(
        contains(MODELO_ALTERNATIVO),
        any(HttpEntity.class),
        eq(String.class)
      )
    )
      .thenReturn(JSON_RESPONSE);

    entoncesLaRespuestaEs(cuandoPregunto(PREGUNTA), "<p>" + RESPUESTA_ESPERADA + "</p>\n");
    verify(restTemplateMock)
      .postForObject(contains(MODELO_ALTERNATIVO), any(HttpEntity.class), eq(String.class));
  }

  @Test
  public void deberiaCambiarAlSiguienteModeloSiElModeloNoEstaDisponible() {
    dadoQueElModeloResponde(MODELO_PRINCIPAL, error(HttpStatus.SERVICE_UNAVAILABLE));
    when(
      restTemplateMock.postForObject(
        contains(MODELO_ALTERNATIVO),
        any(HttpEntity.class),
        eq(String.class)
      )
    )
      .thenReturn(JSON_RESPONSE);

    entoncesLaRespuestaEs(cuandoPregunto(PREGUNTA), "<p>" + RESPUESTA_ESPERADA + "</p>\n");
  }

  @Test
  public void deberiaLanzarCuotaAgotadaSiTodosLosModelosSeQuedaronSinCuota() {
    when(restTemplateMock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenThrow(error(HttpStatus.TOO_MANY_REQUESTS));

    assertThrows(CuotaDeGeminiAgotada.class, () -> cuandoPregunto(PREGUNTA));
    verify(restTemplateMock, times(2))
      .postForObject(anyString(), any(HttpEntity.class), eq(String.class));
  }

  @Test
  public void deberiaLanzarErrorDeGeminiYNoCuotaSiNingunModeloEstaDisponible() {
    when(restTemplateMock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenThrow(error(HttpStatus.SERVICE_UNAVAILABLE));

    ErrorDeGemini error = assertThrows(ErrorDeGemini.class, () -> cuandoPregunto(PREGUNTA));
    assertThat(error, not(instanceOf(CuotaDeGeminiAgotada.class)));
  }

  @Test
  public void noDeberiaCambiarDeModeloAnteUnErrorDeAutenticacion() {
    dadoQueElModeloResponde(MODELO_PRINCIPAL, error(HttpStatus.FORBIDDEN));

    assertThrows(ErrorDeGemini.class, () -> cuandoPregunto(PREGUNTA));
    verify(restTemplateMock, never())
      .postForObject(contains(MODELO_ALTERNATIVO), any(HttpEntity.class), eq(String.class));
  }

  @Test
  public void deberiaEnviarLaApiKeyConfiguradaEnElHeaderDeLaPeticion() {
    dadoQueLaApiResponde(JSON_RESPONSE);
    cuandoPregunto(PREGUNTA);

    assertThat(peticionEnviada().getHeaders().getFirst("x-goog-api-key"), equalTo(API_KEY_VALIDA));
  }

  @Test
  public void noDeberiaEnviarLaApiKeyEnLaUrl() {
    dadoQueLaApiResponde(JSON_RESPONSE);
    cuandoPregunto(PREGUNTA);

    ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
    verify(restTemplateMock).postForObject(url.capture(), any(HttpEntity.class), eq(String.class));
    assertThat(url.getValue(), not(containsString(API_KEY_VALIDA)));
  }

  @Test
  public void deberiaLanzarErrorDeGeminiSiLaApiKeyEsInvalida() {
    servicioGemini = crearServicioConApiKey(API_KEY_INVALIDA);
    dadoQueElModeloResponde(
      MODELO_PRINCIPAL,
      error(HttpStatus.BAD_REQUEST, "API key not valid. Please pass a valid API key.")
    );

    ErrorDeGemini error = assertThrows(ErrorDeGemini.class, () -> cuandoPregunto(PREGUNTA));

    assertThat(error, not(instanceOf(CuotaDeGeminiAgotada.class)));
    assertThat(
      peticionEnviada().getHeaders().getFirst("x-goog-api-key"),
      equalTo(API_KEY_INVALIDA)
    );
    verify(restTemplateMock, never())
      .postForObject(contains(MODELO_ALTERNATIVO), any(HttpEntity.class), eq(String.class));
  }

  @Test
  public void deberiaLanzarErrorDeGeminiSiLaApiKeyNoTienePermisos() {
    servicioGemini = crearServicioConApiKey(API_KEY_SIN_PERMISOS);
    dadoQueElModeloResponde(
      MODELO_PRINCIPAL,
      error(HttpStatus.FORBIDDEN, "Method doesn't allow unregistered callers.")
    );

    assertThrows(ErrorDeGemini.class, () -> cuandoPregunto(PREGUNTA));
    assertThat(
      peticionEnviada().getHeaders().getFirst("x-goog-api-key"),
      equalTo(API_KEY_SIN_PERMISOS)
    );
    verify(restTemplateMock, never())
      .postForObject(contains(MODELO_ALTERNATIVO), any(HttpEntity.class), eq(String.class));
  }

  @Test
  public void deberiaUsarLaMismaApiKeyAlCambiarDeModelo() {
    dadoQueElModeloResponde(MODELO_PRINCIPAL, error(HttpStatus.TOO_MANY_REQUESTS));
    when(
      restTemplateMock.postForObject(
        contains(MODELO_ALTERNATIVO),
        any(HttpEntity.class),
        eq(String.class)
      )
    )
      .thenReturn(JSON_RESPONSE);

    cuandoPregunto(PREGUNTA);

    ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplateMock, times(2))
      .postForObject(anyString(), captor.capture(), eq(String.class));
    for (HttpEntity<?> peticion : captor.getAllValues()) {
      assertThat(peticion.getHeaders().getFirst("x-goog-api-key"), equalTo(API_KEY_VALIDA));
    }
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
  public void generarEjerciciosDeberiaLanzarCuotaAgotadaSiTodosLosModelosSeQuedaronSinCuota() {
    when(restTemplateMock.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
      .thenThrow(error(HttpStatus.TOO_MANY_REQUESTS));

    assertThrows(
      CuotaDeGeminiAgotada.class,
      () -> servicioGemini.generarEjercicios("Programación", "Herencia", 5)
    );
  }

  private static RuntimeException error(HttpStatus status) {
    return error(status, "detalle de prueba");
  }

  private static RuntimeException error(HttpStatus status, String mensaje) {
    String cuerpo = "{\"error\":{\"message\":\"" + mensaje + "\"}}";
    byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
    if (status.is5xxServerError()) {
      return HttpServerErrorException.create(
        status,
        status.name(),
        null,
        bytes,
        StandardCharsets.UTF_8
      );
    }
    return HttpClientErrorException.create(
      status,
      status.name(),
      null,
      bytes,
      StandardCharsets.UTF_8
    );
  }

  private void dadoQueElModeloResponde(String modelo, RuntimeException error) {
    when(restTemplateMock.postForObject(contains(modelo), any(HttpEntity.class), eq(String.class)))
      .thenThrow(error);
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

  private HttpEntity<?> peticionEnviada() {
    ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
    verify(restTemplateMock).postForObject(anyString(), captor.capture(), eq(String.class));
    return captor.getValue();
  }

  private String cuerpoEnviado() {
    return String.valueOf(peticionEnviada().getBody());
  }

  private void entoncesLaRespuestaEs(String respuestaActual, String respuestaEsperada) {
    assertThat(respuestaActual, equalTo(respuestaEsperada));
  }
}
