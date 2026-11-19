package com.tallerwebi.presentacion.gemini;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.excepcion.excepcionGemini.CuotaDeGeminiAgotada;
import com.tallerwebi.dominio.excepcion.excepcionGemini.ErrorDeGemini;
import com.tallerwebi.dominio.gemini.ServicioGemini;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

public class ControladorGeminiTest {

  private ServicioGemini servicioGemini;
  private ControladorGemini controlador;

  @BeforeEach
  void inicializar() {
    servicioGemini = mock(ServicioGemini.class);
    controlador = new ControladorGemini();
    ReflectionTestUtils.setField(controlador, "servicioGemini", servicioGemini);
  }

  @Test
  void deberiaDevolverLaRespuestaDeGemini() {
    when(servicioGemini.preguntar("hola")).thenReturn("<p>chau</p>");

    ResponseEntity<?> respuesta = controlador.preguntar(datos("hola"));

    assertEquals(200, respuesta.getStatusCode().value());
    assertEquals("<p>chau</p>", ((DatosGemini) respuesta.getBody()).getRespuesta());
  }

  @Test
  void deberiaDevolver503ConMensajeSiSeAcaboLaCuota() {
    when(servicioGemini.preguntar("hola")).thenThrow(new CuotaDeGeminiAgotada("sin cuota"));

    ResponseEntity<?> respuesta = controlador.preguntar(datos("hola"));

    assertEquals(503, respuesta.getStatusCode().value());
    assertTrue(String.valueOf(respuesta.getBody()).contains("límite de uso"));
  }

  @Test
  void deberiaDevolver500SiGeminiFalla() {
    when(servicioGemini.preguntar("hola")).thenThrow(new ErrorDeGemini("falla"));

    ResponseEntity<?> respuesta = controlador.preguntar(datos("hola"));

    assertEquals(500, respuesta.getStatusCode().value());
  }

  private DatosGemini datos(String pregunta) {
    DatosGemini datos = new DatosGemini();
    datos.setPregunta(pregunta);
    return datos;
  }
}
