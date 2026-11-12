package com.tallerwebi.presentacion.gemini;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.gemini.ServicioGemini;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

  @Test
  void deberiaCalcularPorcentajeConRespuestasCorrectasEIncorrectas() {
    PreguntaEjercicio primera = pregunta();

    PreguntaEjercicio segunda = pregunta();
    segunda.setEnunciado("¿Qué es Python?");
    segunda.setRespuestaCorrecta(2);

    request.getSession().setAttribute("EJERCICIOS_IA", Arrays.asList(primera, segunda));

    SolicitudCorreccionEjercicio solicitud = new SolicitudCorreccionEjercicio();
    solicitud.setRespuestas(Arrays.asList(1, 0));

    ResponseEntity<?> respuesta = controlador.corregirEjercicios(solicitud, request);

    assertEquals(200, respuesta.getStatusCode().value());

    java.util.Map<?, ?> resultado = (java.util.Map<?, ?>) respuesta.getBody();

    assertEquals(1, resultado.get("correctas"));
    assertEquals(1, resultado.get("incorrectas"));
    assertEquals(2, resultado.get("total"));
    assertEquals(50, resultado.get("porcentaje"));

    List<?> correcciones = (List<?>) resultado.get("correcciones");
    assertEquals(2, correcciones.size());
  }

  @Test
  void deberiaRechazarSolicitudNula() {
    ResponseEntity<?> respuesta = controlador.generarEjercicios(null, request);

    assertEquals(400, respuesta.getStatusCode().value());
    verifyNoInteractions(servicioMateria, servicioGemini);
  }

  @Test
  void deberiaRechazarSolicitudSinMateria() {
    SolicitudEjercicio solicitud = new SolicitudEjercicio();
    solicitud.setTema("Java");
    solicitud.setCantidad(1);

    ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitud, request);

    assertEquals(400, respuesta.getStatusCode().value());
  }

  @Test
  void deberiaRechazarSolicitudSinTema() {
    SolicitudEjercicio solicitud = new SolicitudEjercicio();
    solicitud.setMateriaId(1);
    solicitud.setCantidad(1);

    ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitud, request);

    assertEquals(400, respuesta.getStatusCode().value());
  }

  @Test
  void deberiaRechazarSolicitudSinCantidad() {
    SolicitudEjercicio solicitud = new SolicitudEjercicio();
    solicitud.setMateriaId(1);
    solicitud.setTema("Java");

    ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitud, request);

    assertEquals(400, respuesta.getStatusCode().value());
  }

  @Test
  void deberiaRechazarCantidadFueraDelLimite() {
    SolicitudEjercicio solicitud = solicitudValida();
    solicitud.setCantidad(11);

    ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitud, request);

    assertEquals(400, respuesta.getStatusCode().value());

    solicitud.setCantidad(0);

    respuesta = controlador.generarEjercicios(solicitud, request);

    assertEquals(400, respuesta.getStatusCode().value());
    verifyNoInteractions(servicioMateria, servicioGemini);
  }

  @Test
  void deberiaDevolverErrorSiGeminiDevuelveJsonInvalido() {
    prepararMateria();

    when(servicioGemini.generarEjercicios("Programación", "Java", 1))
      .thenReturn("esto no es un JSON válido");

    ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitudValida(), request);

    assertEquals(500, respuesta.getStatusCode().value());
    assertEquals("No se pudieron procesar los ejercicios generados.", respuesta.getBody());
  }

  @Test
  void deberiaRechazarPreguntasGeneradasConFormatoInvalido() {
    prepararMateria();

    String jsonInvalido =
      "{\"preguntas\":[{" +
      "\"enunciado\":\"¿Qué es Java?\"," +
      "\"opciones\":[\"A\",\"B\",\"C\"]," +
      "\"respuestaCorrecta\":1," +
      "\"explicacion\":\"Explicación\"" +
      "}]}";

    when(servicioGemini.generarEjercicios("Programación", "Java", 1)).thenReturn(jsonInvalido);

    ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitudValida(), request);

    assertEquals(500, respuesta.getStatusCode().value());
    assertEquals("Gemini generó preguntas con formato inválido.", respuesta.getBody());
    assertNull(request.getSession().getAttribute("EJERCICIOS_IA"));
  }

  @Test
  void deberiaRechazarRespuestaSinListaDePreguntas() {
    prepararMateria();

    when(servicioGemini.generarEjercicios("Programación", "Java", 1))
      .thenReturn("{\"preguntas\":null}");

    ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitudValida(), request);

    assertEquals(500, respuesta.getStatusCode().value());
  }

  @Test
  void deberiaRechazarPreguntaNulaOConDatosIncompletos() {
    prepararMateria();

    String[] preguntasInvalidas = {
      "null",
      "{\"opciones\":[\"A\",\"B\",\"C\",\"D\"],\"respuestaCorrecta\":1}",
      "{\"enunciado\":\"Pregunta\",\"opciones\":null,\"respuestaCorrecta\":1}",
      "{\"enunciado\":\"Pregunta\",\"opciones\":[\"A\",\"B\",\"C\",\"D\"],\"respuestaCorrecta\":null}",
    };

    for (String preguntaJson : preguntasInvalidas) {
      when(servicioGemini.generarEjercicios("Programación", "Java", 1))
        .thenReturn("{\"preguntas\":[" + preguntaJson + "]}");

      ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitudValida(), request);

      assertEquals(500, respuesta.getStatusCode().value());
      assertEquals("Gemini generó preguntas con formato inválido.", respuesta.getBody());
    }
  }

  @Test
  void deberiaRechazarIndiceDeRespuestaCorrectaFueraDeRango() {
    prepararMateria();

    String[] indicesInvalidos = { "-1", "4" };

    for (String indice : indicesInvalidos) {
      String json =
        "{\"preguntas\":[{" +
        "\"enunciado\":\"Pregunta\"," +
        "\"opciones\":[\"A\",\"B\",\"C\",\"D\"]," +
        "\"respuestaCorrecta\":" +
        indice +
        "}]}";

      when(servicioGemini.generarEjercicios("Programación", "Java", 1)).thenReturn(json);

      ResponseEntity<?> respuesta = controlador.generarEjercicios(solicitudValida(), request);

      assertEquals(500, respuesta.getStatusCode().value());
    }
  }

  @Test
  void deberiaRechazarSolicitudConRespuestasNulas() {
    request.getSession().setAttribute("EJERCICIOS_IA", Collections.singletonList(pregunta()));

    ResponseEntity<?> respuesta = controlador.corregirEjercicios(null, request);

    assertEquals(400, respuesta.getStatusCode().value());

    SolicitudCorreccionEjercicio solicitud = new SolicitudCorreccionEjercicio();
    solicitud.setRespuestas(null);

    respuesta = controlador.corregirEjercicios(solicitud, request);

    assertEquals(400, respuesta.getStatusCode().value());
  }

  @Test
  void deberiaRechazarRespuestasNulasNegativasOFueraDeRango() {
    request.getSession().setAttribute("EJERCICIOS_IA", Collections.singletonList(pregunta()));

    List<Integer> respuestasInvalidas = Arrays.asList((Integer) null);

    for (Integer respuestaInvalida : respuestasInvalidas) {
      SolicitudCorreccionEjercicio solicitud = new SolicitudCorreccionEjercicio();
      solicitud.setRespuestas(Collections.singletonList(respuestaInvalida));

      ResponseEntity<?> respuesta = controlador.corregirEjercicios(solicitud, request);

      assertEquals(400, respuesta.getStatusCode().value());
    }

    for (Integer respuestaInvalida : Arrays.asList(-1, 4)) {
      SolicitudCorreccionEjercicio solicitud = new SolicitudCorreccionEjercicio();
      solicitud.setRespuestas(Collections.singletonList(respuestaInvalida));

      ResponseEntity<?> respuesta = controlador.corregirEjercicios(solicitud, request);

      assertEquals(400, respuesta.getStatusCode().value());
    }
  }

  @Test
  void deberiaCrearPreguntaParaResolverConConstructorVacio() {
    PreguntaParaResolver pregunta = new PreguntaParaResolver();

    assertNull(pregunta.getEnunciado());
    assertNull(pregunta.getOpciones());
  }

  @Test
  void deberiaCrearPreguntaParaResolverConConstructorCompleto() {
    List<String> opciones = Arrays.asList("A", "B", "C", "D");

    PreguntaParaResolver pregunta = new PreguntaParaResolver("¿Pregunta de prueba?", opciones);

    assertEquals("¿Pregunta de prueba?", pregunta.getEnunciado());
    assertEquals(opciones, pregunta.getOpciones());
  }

  @Test
  void deberiaModificarElEnunciadoDePreguntaParaResolver() {
    PreguntaParaResolver pregunta = new PreguntaParaResolver();

    pregunta.setEnunciado("Nuevo enunciado");

    assertEquals("Nuevo enunciado", pregunta.getEnunciado());
  }

  @Test
  void deberiaModificarLasOpcionesDePreguntaParaResolver() {
    PreguntaParaResolver pregunta = new PreguntaParaResolver();
    List<String> opciones = Arrays.asList("Primera", "Segunda", "Tercera", "Cuarta");

    pregunta.setOpciones(opciones);

    assertEquals(opciones, pregunta.getOpciones());
  }

  private void prepararMateria() {
    Materia materia = new Materia();
    materia.setNombre("Programación");

    when(servicioMateria.buscarMateriaPorIdYUsuario(1, 1L)).thenReturn(materia);
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
