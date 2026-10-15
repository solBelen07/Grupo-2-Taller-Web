package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.ServicioGrupo;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.tarea.ServicioTarea;
import com.tallerwebi.dominio.tarea.Tarea;
import com.tallerwebi.presentacion.tarea.ControladorTarea;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.ModelAndView;

public class ControladorTareaTest {

  private ControladorTarea controladorTarea;
  private ServicioTarea servicioTareaMock;
  private ServicioMateria servicioMateriaMock;
  private ServicioGrupo servicioGrupoMock;

  @BeforeEach
  public void init() {
    servicioTareaMock = mock(ServicioTarea.class);
    servicioMateriaMock = mock(ServicioMateria.class);
    servicioGrupoMock = mock(ServicioGrupo.class);
    controladorTarea =
      new ControladorTarea(servicioTareaMock, servicioMateriaMock, servicioGrupoMock);
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
  public void crearTareaDeberiaCrearYGuardarLaTarea() throws GrupoNoEncontrado {
    // preparacion
    when(servicioGrupoMock.buscarPorNombre("grupo1")).thenReturn(new Grupo());

    // ejecucion
    ModelAndView resultado = controladorTarea.crearTarea(
      "Estudiar parcial",
      "Programación Web",
      "PENDIENTE",
      "Camila",
      10,
      5,
      "PARCIAL",
      java.time.LocalDate.of(2026, 10, 10),
      "grupo1"
    );

    // validacion
    assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/tareas"));

    verify(servicioTareaMock, times(1)).crearTarea(any(Tarea.class));
  }

  @Test
  public void alPedirVerNuevaTareaDevuelvaLaVistaCorrecta() {
    List<Materia> materias = new ArrayList<>();
    materias.add(new Materia());

    when(this.servicioMateriaMock.obtenerMaterias()).thenReturn(materias);

    ModelAndView modelAndView = controladorTarea.verNuevaTarea();

    assertThat(modelAndView.getViewName(), equalTo("paginas/materia/nueva-tarea"));
    assertThat(modelAndView.getModel().get("materias"), equalTo(materias));

    verify(servicioMateriaMock, times(1)).obtenerMaterias();
  }

  @Test
  public void crearTareaDeberiaAsociarLaTareaAlGrupoElegido() throws GrupoNoEncontrado {
    // preparacion
    Grupo grupo = new Grupo();
    grupo.setNombre("grupo2");
    when(servicioGrupoMock.buscarPorNombre("grupo2")).thenReturn(grupo);

    // ejecucion
    controladorTarea.crearTarea(
      "Entrega TP",
      "Taller Web",
      "PENDIENTE",
      "Camila",
      10,
      0,
      "TP",
      java.time.LocalDate.of(2026, 10, 10),
      "grupo2"
    );

    // validacion
    ArgumentCaptor<Tarea> captor = ArgumentCaptor.forClass(Tarea.class);
    verify(servicioTareaMock, times(1)).crearTarea(captor.capture());
    assertThat(captor.getValue().getGrupo(), equalTo(grupo));
  }

  @Test
  public void crearTareaConUnGrupoInexistenteNoDeberiaGuardarYDeberiaMostrarUnError()
    throws GrupoNoEncontrado {
    // preparacion
    when(servicioGrupoMock.buscarPorNombre("noExiste")).thenThrow(new GrupoNoEncontrado());

    // ejecucion
    ModelAndView resultado = controladorTarea.crearTarea(
      "Entrega TP",
      "Taller Web",
      "PENDIENTE",
      "Camila",
      10,
      0,
      "TP",
      java.time.LocalDate.of(2026, 10, 10),
      "noExiste"
    );

    // validacion
    assertThat(resultado.getViewName(), equalTo("paginas/materia/nueva-tarea"));
    assertThat(resultado.getModel().containsKey("error"), equalTo(true));
    verify(servicioTareaMock, never()).crearTarea(any(Tarea.class));
  }

  @Test
  public void verNuevaTareaDeberiaCargarLosGruposParaElSelect() {
    // preparacion
    List<Grupo> grupos = new ArrayList<>();
    grupos.add(new Grupo());
    when(servicioGrupoMock.listarGrupos()).thenReturn(grupos);

    // ejecucion
    ModelAndView modelAndView = controladorTarea.verNuevaTarea();

    // validacion
    assertThat(modelAndView.getModel().get("grupos"), equalTo(grupos));
    verify(servicioGrupoMock, times(1)).listarGrupos();
  }

  @Test
  public void verTareasDeberiaEnviarLasTareasAgrupadasYElProgresoDeCadaGrupo() {
    // preparacion
    Tarea tarea = new Tarea();
    Map<String, List<Tarea>> tareasPorGrupo = new HashMap<>();
    tareasPorGrupo.put("grupo1", Arrays.asList(tarea));
    Map<String, Double> progresoPorGrupo = new HashMap<>();
    progresoPorGrupo.put("grupo1", 50.0);

    when(servicioTareaMock.obtenerTodas()).thenReturn(Arrays.asList(tarea));
    when(servicioTareaMock.agruparPorGrupo()).thenReturn(tareasPorGrupo);
    when(servicioTareaMock.calcularProgresoPorGrupo()).thenReturn(progresoPorGrupo);

    // ejecucion
    ModelAndView modelAndView = controladorTarea.verTareas();

    // validacion
    assertThat(modelAndView.getModel().get("tareasPorGrupo"), equalTo(tareasPorGrupo));
    assertThat(modelAndView.getModel().get("progresoPorGrupo"), equalTo(progresoPorGrupo));
  }
}
