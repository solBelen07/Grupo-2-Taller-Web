package com.tallerwebi.presentacion.gemini;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.gemini.ServicioGemini;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class ControladorEjercicioTest {

  private ServicioGemini servicioGemini;
  private ServicioMateria servicioMateria;
  private ControladorEjercicio controlador;
  private MockHttpServletRequest request;
  private Usuario usuario;

  @BeforeEach
  void inicializar() {
    servicioGemini = mock(ServicioGemini.class);
    servicioMateria = mock(ServicioMateria.class);
    controlador = new ControladorEjercicio(servicioGemini, servicioMateria);

    request = new MockHttpServletRequest();
    usuario = mock(Usuario.class);

    when(usuario.getId()).thenReturn(1L);
    request.getSession().setAttribute("USUARIO", usuario);
  }

  @Test
  void deberiaRechazarGeneracionSiNoHayUsuarioAutenticado() {
    request.getSession().removeAttribute("USUARIO");

    ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitudValida(), request);

    assertEquals(401, respuesta.getStatusCode().value());
  }

  @Test
  void deberiaRechazarSolicitudInvalida() {
    SolicitudEjercicio solicitud = new SolicitudEjercicio();
    solicitud.setMateriaId(1);
    solicitud.setTema(" ");
    solicitud.setCantidad(5);

    ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitud, request);

    assertEquals(400, respuesta.getStatusCode().value());
    verifyNoInteractions(servicioGemini);
  }

  @Test
  void deberiaRechazarMateriaQueNoPerteneceAlUsuario() {
    when(servicioMateria.buscarMateriaPorIdYUsuario(1, 1L)).thenReturn(null);

    ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitudValida(), request);

    assertEquals(400, respuesta.getStatusCode().value());
    verifyNoInteractions(servicioGemini);
  }

  @Test
  void deberiaGenerarEjerciciosCorrectamente() {
    Materia materia = new Materia();
    materia.setNombre("Programación");

    when(servicioMateria.buscarMateriaPorIdYUsuario(1, 1L)).thenReturn(materia);

    when(servicioGemini.generarEjercicios("Programación", "Java", 1))
      .thenReturn(jsonEjerciciosValido());

    ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitudValida(), request);

    assertEquals(200, respuesta.getStatusCode().value());
    assertNotNull(request.getSession().getAttribute("EJERCICIOS_IA"));
  }

  @Test
  void deberiaDevolverErrorSiGeminiFalla() {
    Materia materia = new Materia();
    materia.setNombre("Programación");

    when(servicioMateria.buscarMateriaPorIdYUsuario(1, 1L)).thenReturn(materia);

    when(servicioGemini.generarEjercicios("Programación", "Java", 1))
      .thenReturn("Error al generar ejercicios");

    ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitudValida(), request);

    assertEquals(500, respuesta.getStatusCode().value());
  }

  @Test
  void deberiaRechazarCorreccionSinEjerciciosEnSesion() {
    ResponseEntity<?> respuesta = controlador.corregirEjercicios(solicitudCorreccion(), request);

    assertEquals(400, respuesta.getStatusCode().value());
  }

  @Test
  void deberiaRechazarCorreccionSiNoHayUsuarioAutenticado() {
    request.getSession().removeAttribute("USUARIO");

    ResponseEntity<?> respuesta = controlador.corregirEjercicios(solicitudCorreccion(), request);

    assertEquals(401, respuesta.getStatusCode().value());
  }

  @Test
  void deberiaRechazarCantidadDeRespuestasIncorrecta() {
    request.getSession().setAttribute("EJERCICIOS_IA", Collections.singletonList(pregunta()));

    SolicitudCorreccionEjercicio solicitud = new SolicitudCorreccionEjercicio();
    solicitud.setRespuestas(Arrays.asList(0, 1));

    ResponseEntity<?> respuesta = controlador.corregirEjercicios(solicitud, request);

    assertEquals(400, respuesta.getStatusCode().value());
  }

  @Test
  void deberiaCorregirRespuestasCorrectamente() {
    request.getSession().setAttribute("EJERCICIOS_IA", Collections.singletonList(pregunta()));

    SolicitudCorreccionEjercicio solicitud = new SolicitudCorreccionEjercicio();
    solicitud.setRespuestas(Collections.singletonList(1));

    ResponseEntity<?> respuesta = controlador.corregirEjercicios(solicitud, request);

    assertEquals(200, respuesta.getStatusCode().value());
    assertNull(request.getSession().getAttribute("EJERCICIOS_IA"));
  }

  private SolicitudEjercicio solicitudValida() {
    SolicitudEjercicio solicitud = new SolicitudEjercicio();
    solicitud.setMateriaId(1);
    solicitud.setTema("Java");
    solicitud.setCantidad(1);
    return solicitud;
  }

  private SolicitudCorreccionEjercicio solicitudCorreccion() {
    SolicitudCorreccionEjercicio solicitud = new SolicitudCorreccionEjercicio();
    solicitud.setRespuestas(Collections.singletonList(1));
    return solicitud;
  }

  private PreguntaEjercicio pregunta() {
    PreguntaEjercicio pregunta = new PreguntaEjercicio();
    pregunta.setEnunciado("¿Qué es Java?");
    pregunta.setOpciones(
      Arrays.asList("Un lenguaje", "Un navegador", "Un sistema operativo", "Una base de datos")
    );
    pregunta.setRespuestaCorrecta(1);
    pregunta.setExplicacion("Java es un lenguaje de programación.");
    return pregunta;
  }

  private String jsonEjerciciosValido() {
    return (
      "{\"preguntas\":[{" +
      "\"enunciado\":\"¿Qué es Java?\"," +
      "\"opciones\":[\"Un lenguaje\",\"Un navegador\",\"Un sistema operativo\",\"Una base de datos\"]," +
      "\"respuestaCorrecta\":1," +
      "\"explicacion\":\"Java es un lenguaje de programación.\"" +
      "}]}"
    );
  }
}
