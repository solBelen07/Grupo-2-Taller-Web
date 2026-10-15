package com.tallerwebi.dominio.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service
public class ServicioGeminiImpl implements ServicioGemini {

  private static final String MODELO = "gemini-3.5-flash-lite";
  private static final String URL =
    "https://generativelanguage.googleapis.com/v1beta/models/" + MODELO + ":generateContent";

  private final RestOperations restTemplate;
  private final String apiKey;
  private final ObjectMapper objectMapper;

  public ServicioGeminiImpl() {
    this.restTemplate = new RestTemplate();
    this.apiKey = System.getenv("GEMINI_API_KEY");
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public String preguntar(String pregunta) {
    return ejecutarConsulta(pregunta);
  }

  @Override
  public String preguntarConRegla(String pregunta, String regla) {
    String prompt =
      "Instrucciones adicionales:\n" + regla + "\n\nConsulta del estudiante:\n" + pregunta;
    return ejecutarConsulta(prompt);
  }

  @Override
  public void limpiar() {
    // No hay historial persistente: cada consulta se envía de forma independiente.
  }

  private String ejecutarConsulta(String mensaje) {
    if (apiKey == null || apiKey.isBlank()) {
      return "Error de configuración: no se encontró la variable de entorno GEMINI_API_KEY.";
    }

    try {
      MultiValueMap<String, String> headers = new HttpHeaders();
      headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
      headers.set("x-goog-api-key", apiKey);

      Map<String, Object> body = new HashMap<>();
      body.put("contents", List.of(Map.of("parts", List.of(Map.of("text", mensaje)))));

      String requestBody = objectMapper.writeValueAsString(body);
      HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

      String response = restTemplate.postForObject(URL, request, String.class);
      return extraerRespuesta(response);
    } catch (RestClientResponseException e) {
      return (
        "Error al consultar Gemini (HTTP " +
        e.getStatusCode() +
        "). " +
        "Verificá el modelo, la clave API y las cuotas disponibles."
      );
    } catch (JsonProcessingException e) {
      return "Error al preparar o procesar el JSON de Gemini.";
    } catch (Exception e) {
      return "No se pudo conectar con Gemini. Verificá la conexión y la configuración.";
    }
  }

  private String extraerRespuesta(String json) {
    try {
      JsonNode root = objectMapper.readTree(json);
      JsonNode texto = root
        .path("candidates")
        .path(0)
        .path("content")
        .path("parts")
        .path(0)
        .path("text");

      if (texto.isMissingNode() || texto.asText().isBlank()) {
        return "Gemini no devolvió texto. Puede que la respuesta haya sido bloqueada o no contenga candidatos.";
      }

      return texto.asText();
    } catch (JsonProcessingException e) {
      return "No se pudo interpretar la respuesta JSON de Gemini.";
    }
  }
}
