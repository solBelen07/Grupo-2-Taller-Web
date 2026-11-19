package com.tallerwebi.dominio.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.excepcion.excepcionGemini.CuotaDeGeminiAgotada;
import com.tallerwebi.dominio.excepcion.excepcionGemini.ErrorDeGemini;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service("servicioGemini")
public class ServicioGeminiImpl implements ServicioGemini {

  private static final Logger LOG = Logger.getLogger(ServicioGeminiImpl.class.getName());

  static final List<String> MODELOS = List.of(
    "gemini-3.5-flash-lite",
    "gemini-3.5-flash",
    "gemini-2.5-flash"
  );

  private static final String URL_BASE =
    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

  private static final DataHolder OPCIONES = crearOpcionesMarkdown();
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final RestOperations restTemplate;
  private final String apiKey;
  private final List<String> modelos;
  private final Parser parser = Parser.builder(OPCIONES).build();
  private final HtmlRenderer renderer = HtmlRenderer.builder(OPCIONES).build();

  @Autowired
  public ServicioGeminiImpl(@Value("${GEMINI_API_KEY}") String apiKey) {
    this(new RestTemplate(), apiKey);
  }

  public ServicioGeminiImpl(RestOperations restTemplate, String apiKey) {
    this(restTemplate, apiKey, MODELOS);
  }

  public ServicioGeminiImpl(RestOperations restTemplate, String apiKey, List<String> modelos) {
    this.restTemplate = restTemplate;
    this.apiKey = apiKey;
    this.modelos = List.copyOf(modelos);
  }

  @Override
  public String preguntar(String pregunta) {
    String prompt =
      "Quiero que hagas un resumen breve del siguiente fragmento de texto, marcando en items los temas principales:" +
      pregunta;
    Map<String, Object> bodyMap = Map.of(
      "contents",
      List.of(Map.of("parts", List.of(Map.of("text", prompt))))
    );
    return convertirMarkdownAHtml(extraerRespuesta(enviar(bodyMap)));
  }

  @Override
  public String preguntarConRegla(String pregunta, String regla) {
    String prompt = regla + "\n\nPregunta: " + pregunta;
    return preguntar(prompt);
  }

  @Override
  public String generarEjercicios(String materia, String tema, int cantidad) {
    String prompt =
      """
      Generá %d preguntas de opción múltiple para practicar un parcial.

      Materia: %s
      Tema: %s

      Requisitos:
      - Cada pregunta debe tener exactamente 4 opciones.
      - Debe existir una sola respuesta correcta.
      - Las opciones deben identificarse mediante índices del 0 al 3.
      - Incluí una explicación breve de la respuesta correcta.
      - El nivel debe ser adecuado para un estudiante universitario.

      Respondé únicamente con JSON válido, sin bloques Markdown,
      siguiendo esta estructura:

      {
        "preguntas": [
          {
            "enunciado": "Texto de la pregunta",
            "opciones": ["Opción A", "Opción B", "Opción C", "Opción D"],
            "respuestaCorrecta": 0,
            "explicacion": "Explicación de la respuesta"
          }
        ]
      }
      """.formatted(cantidad, materia, tema);

    Map<String, Object> bodyMap = Map.of(
      "contents",
      List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
      "generationConfig",
      Map.of("responseMimeType", "application/json")
    );
    return extraerRespuesta(enviar(bodyMap));
  }

  @Override
  public void limpiar() {
    // Historial de conversación (a implementar luego si es necesario)
  }

  private String enviar(Map<String, Object> bodyMap) {
    HttpEntity<String> request = armarPeticion(bodyMap);
    List<Integer> fallas = new ArrayList<>();

    for (String modelo : modelos) {
      Optional<String> respuesta = intentarConModelo(modelo, request, fallas);
      if (respuesta.isPresent()) return respuesta.get();
    }

    if (fallas.contains(HttpStatus.TOO_MANY_REQUESTS.value())) {
      LOG.severe(() -> "Gemini: se acabó la cuota de todos los modelos disponibles " + modelos);
      throw new CuotaDeGeminiAgotada("Se agotó la cuota de todos los modelos de Gemini.");
    }
    throw new ErrorDeGemini("Ningún modelo de Gemini está disponible en este momento.");
  }

  private Optional<String> intentarConModelo(
    String modelo,
    HttpEntity<String> request,
    List<Integer> fallas
  ) {
    try {
      String json = restTemplate.postForObject(URL_BASE.formatted(modelo), request, String.class);
      return Optional.of(Objects.toString(json, ""));
    } catch (HttpStatusCodeException error) {
      int status = error.getStatusCode().value();
      if (!sePuedeProbarOtroModelo(modelo, error)) {
        throw new ErrorDeGemini("Gemini respondió con error HTTP " + status + ".", error);
      }
      fallas.add(status);
      return Optional.empty();
    } catch (RestClientException error) {
      LOG.log(Level.SEVERE, error, () -> "Gemini: no se pudo conectar con el modelo " + modelo);
      throw new ErrorDeGemini("No se pudo conectar con Gemini.", error);
    }
  }

  private static boolean sePuedeProbarOtroModelo(String modelo, HttpStatusCodeException error) {
    int status = error.getStatusCode().value();
    String detalle = mensajeDeError(error.getResponseBodyAsString());

    if (status == HttpStatus.TOO_MANY_REQUESTS.value()) {
      LOG.warning(() -> "Gemini: se acabó la cuota del modelo " + modelo + " (HTTP 429). " + detalle
      );
      return true;
    }
    if (status == HttpStatus.SERVICE_UNAVAILABLE.value()) {
      LOG.warning(() -> "Gemini: el modelo " + modelo + " no está disponible (HTTP 503). " + detalle
      );
      return true;
    }
    LOG.severe(() -> "Gemini: error HTTP " + status + " con el modelo " + modelo + ". " + detalle);
    return false;
  }

  private HttpEntity<String> armarPeticion(Map<String, Object> bodyMap) {
    try {
      MultiValueMap<String, String> headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
      headers.add("x-goog-api-key", this.apiKey);
      return new HttpEntity<>(MAPPER.writeValueAsString(bodyMap), headers);
    } catch (JsonProcessingException e) {
      throw new ErrorDeGemini("Error al armar la petición a Gemini.", e);
    }
  }

  private String extraerRespuesta(String json) {
    JsonNode texto;
    try {
      texto =
        MAPPER
          .readTree(json == null ? "" : json)
          .path("candidates")
          .path(0)
          .path("content")
          .path("parts")
          .path(0)
          .path("text");
    } catch (JsonProcessingException e) {
      throw new ErrorDeGemini("Gemini devolvió una respuesta que no es JSON válido.", e);
    }
    if (texto.isMissingNode() || texto.isNull()) {
      throw new ErrorDeGemini("Gemini devolvió una respuesta sin contenido.");
    }
    return texto.asText();
  }

  private static String mensajeDeError(String cuerpo) {
    try {
      return MAPPER.readTree(cuerpo).path("error").path("message").asText("");
    } catch (Exception e) {
      return "";
    }
  }

  private String convertirMarkdownAHtml(String texto) {
    if (texto == null || texto.isEmpty()) return "";
    return renderer.render(parser.parse(texto));
  }

  private static DataHolder crearOpcionesMarkdown() {
    MutableDataSet opciones = new MutableDataSet();
    opciones.set(HtmlRenderer.ESCAPE_HTML, true);
    return opciones;
  }
}
