package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.tarea.ServicioTarea;
import com.tallerwebi.dominio.tarea.Tarea;
import com.tallerwebi.presentacion.tarea.ControladorTarea;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorTareaTest {

  private ControladorTarea controladorTarea;
  private ServicioTarea servicioTareaMock;

  @BeforeEach
  public void init() {
    servicioTareaMock = mock(ServicioTarea.class);
    controladorTarea = new ControladorTarea(servicioTareaMock);
  }

  @Test
  public void verTareasDeberiaMostrarLasTareasYLosIndicadores() {
    // preparacion
    Tarea tarea = new Tarea();
    tarea.setTitulo("Tarea 1");

    Map<String, Long> aporteIndividual = new HashMap<>();
    aporteIndividual.put("Camila", 2L);

    when(servicioTareaMock.obtenerTodas()).thenReturn(Arrays.asList(tarea));

    when(servicioTareaMock.calcularProgreso()).thenReturn(50.0);

    when(servicioTareaMock.contarTareasCompletadas()).thenReturn(2L);

    when(servicioTareaMock.contarTareasPendientes()).thenReturn(2L);

    when(servicioTareaMock.contarTareasTotales()).thenReturn(4L);

    when(servicioTareaMock.calcularAporteIndividual()).thenReturn(aporteIndividual);

    // ejecucion
    ModelAndView modelAndView = controladorTarea.verTareas();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("paginas/materia/tareas"));

    assertThat(modelAndView.getModel().get("tareas"), equalTo(Arrays.asList(tarea)));

    assertThat(modelAndView.getModel().get("progreso"), equalTo(50.0));

    assertThat(modelAndView.getModel().get("completadas"), equalTo(2L));

    assertThat(modelAndView.getModel().get("pendientes"), equalTo(2L));

    assertThat(modelAndView.getModel().get("total"), equalTo(4L));

    assertThat(modelAndView.getModel().get("aporteIndividual"), equalTo(aporteIndividual));

    verify(servicioTareaMock, times(1)).obtenerTodas();
    verify(servicioTareaMock, times(1)).calcularProgreso();
    verify(servicioTareaMock, times(1)).contarTareasCompletadas();
    verify(servicioTareaMock, times(1)).contarTareasPendientes();
    verify(servicioTareaMock, times(1)).contarTareasTotales();
    verify(servicioTareaMock, times(1)).calcularAporteIndividual();
  }

  @Test
  public void crearTareaDeberiaCrearYGuardarLaTarea() {
    // ejecucion
    ModelAndView resultado = controladorTarea.crearTarea(
      "Estudiar parcial",
      "Programación Web",
      "PENDIENTE",
      "Camila",
      10,
      5,
      "PARCIAL",
      java.time.LocalDate.of(2026, 10, 10)
    );

    // validacion
    assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/tareas"));

    verify(servicioTareaMock, times(1)).crearTarea(any(Tarea.class));
  }
}
