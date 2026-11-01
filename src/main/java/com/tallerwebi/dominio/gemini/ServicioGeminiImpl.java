package com.tallerwebi.dominio.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service("servicioGemini")
public class ServicioGeminiImpl implements ServicioGemini {

  private final RestOperations restTemplate;
  private final String apiKey;
  private final String modelo = "gemini-3.5-flash-lite";

  private final String url =
    "https://generativelanguage.googleapis.com/v1beta/models/" + modelo + ":generateContent";

  @Autowired
  public ServicioGeminiImpl(@Value("${GEMINI_API_KEY}") String apiKey) {
    this.restTemplate = new RestTemplate();
    this.apiKey = apiKey;
  }

  public ServicioGeminiImpl(RestOperations restTemplate, String apiKey) {
    this.restTemplate = restTemplate;
    this.apiKey = apiKey;
  }

  @Override
  public String preguntar(String pregunta) {
    try {
      Map<String, Object> bodyMap = Map.of(
        "contents",
        List.of(
          Map.of(
            "parts",
            List.of(
              Map.of(
                "text",
                "Quiero que hagas un resumen breve del siguiente fragmento de texto, marcando en items los temas principales:" +
                pregunta
              )
            )
          )
        )
      );

      ObjectMapper mapper = new ObjectMapper();
      String body = mapper.writeValueAsString(bodyMap);
      MultiValueMap<String, String> headers = new HttpHeaders();
      headers.add("Content-Type", "application/json");
      headers.add("x-goog-api-key", this.apiKey);
      HttpEntity<String> request = new HttpEntity<>(body, headers);
      String json = restTemplate.postForObject(url, request, String.class);

      return extraerRespuesta(json);
    } catch (Exception e) {
      return "Error al armar la petición: " + e.getMessage();
    }
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

    try {
      Map<String, Object> bodyMap = Map.of(
        "contents",
        List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
        "generationConfig",
        Map.of("responseMimeType", "application/json")
      );
      ObjectMapper mapper = new ObjectMapper();
      String body = mapper.writeValueAsString(bodyMap);

      MultiValueMap<String, String> headers = new HttpHeaders();
      headers.add("Content-Type", "application/json");
      headers.add("x-goog-api-key", this.apiKey);

      HttpEntity<String> request = new HttpEntity<>(body, headers);

      String json = restTemplate.postForObject(url, request, String.class);

      return extraerRespuesta(json);
    } catch (Exception e) {
      return "Error al generar ejercicios: " + e.getMessage();
    }
  }

  @Override
  public void limpiar() {
    // Historial de conversación (a implementar luego si es necesario)
  }

  private String extraerRespuesta(String json) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(json);
      return root
        .path("candidates")
        .get(0)
        .path("content")
        .path("parts")
        .get(0)
        .path("text")
        .asText();
    } catch (Exception e) {
      return "Error procesando respuesta de Gemini: " + e.getMessage();
    }
  }
}
