package com.tallerwebi.dominio.gemini;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Service
public class ServicioGeminiImpl implements ServicioGemini {

  private final RestOperations restTemplate;
  private final String apiKey;
  private final String modelo = "gemini-2.5-flash";
  private final String url =
    "https://generativelanguage.googleapis.com/v1beta/models/" + modelo + ":generateContent";

  public ServicioGeminiImpl() {
    this.restTemplate = new RestTemplate();
    this.apiKey = System.getenv("GEMINI_API_KEY");
  }

  @Override
  public String preguntar(String pregunta) {
    String body = "{\"contents\":[{\"parts\":[{\"text\":\"" + pregunta + "\"}]}]}";
    @SuppressWarnings("PMD.LooseCoupling")
    HttpHeaders headers = new HttpHeaders();
    headers.set("x-goog-api-key", apiKey);
    headers.set("Content-Type", "application/json");

    HttpEntity<String> request = new HttpEntity<>(body, headers);

    return restTemplate.postForObject(url, request, String.class);
  }

  @Override
  public String preguntarConRegla(String pregunta, String regla) {
    String prompt = regla + "\n\nPregunta: " + pregunta;
    return preguntar(prompt);
  }

  @Override
  public void limpiar() {
    // Se implementará cuando conectemos el historial de conversación.
  }
}
