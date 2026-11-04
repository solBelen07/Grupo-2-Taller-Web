package com.tallerwebi.presentacion.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.gemini.ServicioGemini;
import com.tallerwebi.dominio.materia.ServicioMateria;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ControladorEjercicio {

  private final ServicioGemini servicioGemini;
  private final ServicioMateria servicioMateria;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ControladorEjercicio(ServicioGemini servicioGemini, ServicioMateria servicioMateria) {
    this.servicioGemini = servicioGemini;
    this.servicioMateria = servicioMateria;
  }

  @PostMapping("/api/ejercicios/generar")
  @ResponseBody
  public ResponseEntity<?> generarEjercicios(
    @RequestBody SolicitudEjercicio solicitud,
    HttpServletRequest request
  ) {
    Usuario usuario = obtenerUsuario(request);

    if (usuario == null) {
      return ResponseEntity.status(401).body("Debés iniciar sesión.");
    }

    if (!esSolicitudValida(solicitud)) {
      return ResponseEntity.badRequest().body("Los datos del ejercicio no son válidos.");
    }

    var materia = servicioMateria.buscarMateriaPorIdYUsuario(
      solicitud.getMateriaId(),
      usuario.getId()
    );

    if (materia == null) {
      return ResponseEntity.badRequest().body("La materia seleccionada no es válida.");
    }

    try {
      String respuesta = servicioGemini.generarEjercicios(
        materia.getNombre(),
        solicitud.getTema(),
        solicitud.getCantidad()
      );

      if (respuesta.startsWith("Error")) {
        return ResponseEntity.internalServerError().body(respuesta);
      }

      RespuestaEjercicios ejercicios = objectMapper.readValue(respuesta, RespuestaEjercicios.class);

      List<PreguntaEjercicio> preguntas = ejercicios.getPreguntas();

      if (!sonPreguntasValidas(preguntas, solicitud.getCantidad())) {
        return ResponseEntity
          .internalServerError()
          .body("Gemini generó preguntas con formato inválido.");
      }

      request.getSession().setAttribute("EJERCICIOS_IA", preguntas);

      return ResponseEntity.ok(convertirPreguntas(preguntas));
    } catch (Exception e) {
      return ResponseEntity
        .internalServerError()
        .body("No se pudieron procesar los ejercicios generados.");
    }
  }

  @PostMapping("/api/ejercicios/corregir")
  @ResponseBody
  @SuppressWarnings("unchecked")
  public ResponseEntity<?> corregirEjercicios(
    @RequestBody SolicitudCorreccionEjercicio solicitud,
    HttpServletRequest request
  ) {
    Usuario usuario = obtenerUsuario(request);

    if (usuario == null) {
      return ResponseEntity.status(401).body("Debés iniciar sesión.");
    }

    List<PreguntaEjercicio> preguntas = (List<PreguntaEjercicio>) request
      .getSession()
      .getAttribute("EJERCICIOS_IA");

    if (preguntas == null || preguntas.isEmpty()) {
      return ResponseEntity.badRequest().body("No hay ejercicios para corregir.");
    }

    if (!sonRespuestasValidas(solicitud, preguntas)) {
      return ResponseEntity.badRequest().body("Las respuestas enviadas no son válidas.");
    }

    Map<String, Object> resultado = calcularResultado(solicitud.getRespuestas(), preguntas);

    request.getSession().removeAttribute("EJERCICIOS_IA");

    return ResponseEntity.ok(resultado);
  }

  private Usuario obtenerUsuario(HttpServletRequest request) {
    return (Usuario) request.getSession().getAttribute("USUARIO");
  }

  private boolean esSolicitudValida(SolicitudEjercicio solicitud) {
    return (
      solicitud != null &&
      solicitud.getMateriaId() != null &&
      solicitud.getTema() != null &&
      !solicitud.getTema().isBlank() &&
      solicitud.getCantidad() != null &&
      solicitud.getCantidad() >= 1 &&
      solicitud.getCantidad() <= 10
    );
  }

  private boolean sonPreguntasValidas(List<PreguntaEjercicio> preguntas, int cantidad) {
    if (preguntas == null || preguntas.size() != cantidad) {
      return false;
    }

    for (PreguntaEjercicio pregunta : preguntas) {
      if (!esPreguntaValida(pregunta)) {
        return false;
      }
    }

    return true;
  }

  private boolean esPreguntaValida(PreguntaEjercicio pregunta) {
    if (pregunta == null) {
      return false;
    }

    return (
      pregunta.getEnunciado() != null &&
      pregunta.getOpciones() != null &&
      pregunta.getOpciones().size() == 4 &&
      esIndiceRespuestaValido(pregunta.getRespuestaCorrecta())
    );
  }

  private boolean esIndiceRespuestaValido(Integer respuesta) {
    return respuesta != null && respuesta >= 0 && respuesta <= 3;
  }

  private List<PreguntaParaResolver> convertirPreguntas(List<PreguntaEjercicio> preguntas) {
    List<PreguntaParaResolver> preguntasVisibles = new ArrayList<>();

    for (PreguntaEjercicio pregunta : preguntas) {
      preguntasVisibles.add(
        new PreguntaParaResolver(pregunta.getEnunciado(), pregunta.getOpciones())
      );
    }

    return preguntasVisibles;
  }

  private boolean sonRespuestasValidas(
    SolicitudCorreccionEjercicio solicitud,
    List<PreguntaEjercicio> preguntas
  ) {
    if (
      solicitud == null ||
      solicitud.getRespuestas() == null ||
      solicitud.getRespuestas().size() != preguntas.size()
    ) {
      return false;
    }

    for (int i = 0; i < preguntas.size(); i++) {
      Integer respuesta = solicitud.getRespuestas().get(i);

      if (
        respuesta == null || respuesta < 0 || respuesta >= preguntas.get(i).getOpciones().size()
      ) {
        return false;
      }
    }

    return true;
  }

  private Map<String, Object> calcularResultado(
    List<Integer> respuestas,
    List<PreguntaEjercicio> preguntas
  ) {
    int correctas = 0;
    List<Map<String, Object>> correcciones = new ArrayList<>();

    for (int i = 0; i < preguntas.size(); i++) {
      PreguntaEjercicio pregunta = preguntas.get(i);
      Integer respuestaElegida = respuestas.get(i);

      boolean esCorrecta = respuestaElegida.equals(pregunta.getRespuestaCorrecta());

      if (esCorrecta) {
        correctas++;
      }

      correcciones.add(crearDetalleCorreccion(pregunta, respuestaElegida, esCorrecta));
    }

    int total = preguntas.size();

    Map<String, Object> resultado = new HashMap<>();
    resultado.put("correctas", correctas);
    resultado.put("incorrectas", total - correctas);
    resultado.put("total", total);
    resultado.put("porcentaje", (correctas * 100) / total);
    resultado.put("correcciones", correcciones);

    return resultado;
  }

  private Map<String, Object> crearDetalleCorreccion(
    PreguntaEjercicio pregunta,
    Integer respuestaElegida,
    boolean esCorrecta
  ) {
    Map<String, Object> detalle = new HashMap<>();

    detalle.put("enunciado", pregunta.getEnunciado());
    detalle.put("respuestaElegida", pregunta.getOpciones().get(respuestaElegida));
    detalle.put("respuestaCorrecta", pregunta.getOpciones().get(pregunta.getRespuestaCorrecta()));
    detalle.put("esCorrecta", esCorrecta);
    detalle.put("explicacion", pregunta.getExplicacion());

    return detalle;
  }
}
