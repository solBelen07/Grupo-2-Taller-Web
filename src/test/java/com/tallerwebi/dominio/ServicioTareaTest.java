package com.tallerwebi.dominio;

//para hacer los test unitarios:
import static org.hamcrest.MatcherAssert.assertThat; //para comprobar que el resultado es el esperado
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*; // crea un repo simulado

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioTareaTest {

  private ServicioTarea servicioTarea;
  private RepositorioTarea repositorioTareaMock;

  @BeforeEach
  public void init() {
    this.repositorioTareaMock = mock(RepositorioTarea.class);
    this.servicioTarea = new ServicioTareaImpl(this.repositorioTareaMock);
  }

  @Test
  public void obtenerTodasDeberiaLlamarAlRepositorio() {
    // preparacion
    Tarea tarea = new Tarea();
    tarea.setTitulo("Tarea 1");

    List<Tarea> tareasEsperadas = Arrays.asList(tarea);

    when(this.repositorioTareaMock.buscarTodas()).thenReturn(tareasEsperadas);

    // ejecucion
    List<Tarea> tareasObtenidas = this.servicioTarea.obtenerTodas();

    // validacion
    assertThat(tareasObtenidas, equalTo(tareasEsperadas));
    verify(this.repositorioTareaMock, times(1)).buscarTodas();
  }

  @Test
  public void calcularProgresoDeberiaCalcularPorcentajeDeTareasCompletadas() {
    // preparacion
    Tarea tarea1 = new Tarea();
    tarea1.setEstado("COMPLETADA");

    Tarea tarea2 = new Tarea();
    tarea2.setEstado("PENDIENTE");

    Tarea tarea3 = new Tarea();
    tarea3.setEstado("COMPLETADA");

    when(this.repositorioTareaMock.buscarTodas()).thenReturn(Arrays.asList(tarea1, tarea2, tarea3));

    // ejecucion
    double progreso = this.servicioTarea.calcularProgreso();

    // validacion
    assertThat(progreso, equalTo(66.66666666666667));
  }

  @Test
  public void contarTareasCompletadasDeberiaContarSoloLasCompletadas() {
    // preparacion
    Tarea tarea1 = new Tarea();
    tarea1.setEstado("COMPLETADA");

    Tarea tarea2 = new Tarea();
    tarea2.setEstado("PENDIENTE");

    Tarea tarea3 = new Tarea();
    tarea3.setEstado("COMPLETADA");

    when(this.repositorioTareaMock.buscarTodas()).thenReturn(Arrays.asList(tarea1, tarea2, tarea3));

    // ejecucion
    long resultado = this.servicioTarea.contarTareasCompletadas();

    // validacion
    assertThat(resultado, equalTo(2L));
  }

  @Test
  public void contarTareasPendientesDeberiaContarLasNoCompletadas() {
    // preparacion
    Tarea tarea1 = new Tarea();
    tarea1.setEstado("COMPLETADA");

    Tarea tarea2 = new Tarea();
    tarea2.setEstado("PENDIENTE");

    Tarea tarea3 = new Tarea();
    tarea3.setEstado("PENDIENTE");

    when(this.repositorioTareaMock.buscarTodas()).thenReturn(Arrays.asList(tarea1, tarea2, tarea3));

    // ejecucion
    long resultado = this.servicioTarea.contarTareasPendientes();

    // validacion
    assertThat(resultado, equalTo(2L));
  }

  @Test
  public void contarTareasTotalesDeberiaContarTodasLasTareas() {
    // preparacion
    Tarea tarea1 = new Tarea();
    Tarea tarea2 = new Tarea();
    Tarea tarea3 = new Tarea();

    when(this.repositorioTareaMock.buscarTodas()).thenReturn(Arrays.asList(tarea1, tarea2, tarea3));

    // ejecucion
    long resultado = this.servicioTarea.contarTareasTotales();

    // validacion
    assertThat(resultado, equalTo(3L));
  }

  @Test
  public void calcularAporteIndividualDeberiaContarSoloTareasCompletadasPorResponsable() {
    // preparacion
    Tarea tarea1 = new Tarea();
    tarea1.setEstado("COMPLETADA");
    tarea1.setResponsable("Camila");

    Tarea tarea2 = new Tarea();
    tarea2.setEstado("COMPLETADA");
    tarea2.setResponsable("Camila");

    Tarea tarea3 = new Tarea();
    tarea3.setEstado("COMPLETADA");
    tarea3.setResponsable("Juan");

    Tarea tarea4 = new Tarea();
    tarea4.setEstado("PENDIENTE");
    tarea4.setResponsable("Juan");

    when(this.repositorioTareaMock.buscarTodas())
      .thenReturn(Arrays.asList(tarea1, tarea2, tarea3, tarea4));

    // ejecucion
    Map<String, Long> resultado = this.servicioTarea.calcularAporteIndividual();

    // validacion
    assertThat(resultado.get("Camila"), equalTo(2L));
    assertThat(resultado.get("Juan"), equalTo(1L));
    verify(this.repositorioTareaMock, times(1)).buscarTodas();
  }
}
