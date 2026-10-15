package com.tallerwebi.dominio.tarea;

//para hacer los test unitarios:
import static org.hamcrest.MatcherAssert.assertThat; //para comprobar que el resultado es el esperado
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*; // crea un repo simulado

import com.tallerwebi.dominio.grupo.Grupo;
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

  @Test
  public void crearTareaDeberiaGuardarLaTareaEnElRepositorio() {
    Tarea tarea = new Tarea();
    tarea.setTitulo("Estudiar para el parcial");

    this.servicioTarea.crearTarea(tarea);

    verify(this.repositorioTareaMock, times(1)).guardar(tarea);
  }

  @Test
  public void calcularPorcentajeHorasDeberiaCalcularElPorcentajeCorrectamente() {
    // preparacion
    Tarea tarea = new Tarea();
    tarea.setHorasPlanificadas(10);
    tarea.setHorasRealizadas(5);

    // ejecucion
    double resultado = this.servicioTarea.calcularPorcentajeHoras(tarea);

    // validacion
    assertThat(resultado, equalTo(50.0));
  }

  @Test
  public void calcularIndicadorDeberiaIndicarPreparacionAdecuadaParaUnParcial() {
    // preparacion
    Tarea tarea = new Tarea();
    tarea.setTipo("PARCIAL");
    tarea.setHorasPlanificadas(10);
    tarea.setHorasRealizadas(8);

    // ejecucion
    String resultado = this.servicioTarea.calcularIndicador(tarea);

    // validacion
    assertThat(resultado, equalTo("PREPARACIÓN ADECUADA"));
  }

  // GRP-02: tareas agrupadas por grupo y progreso individual

  private Grupo grupo(String nombre) {
    Grupo grupo = new Grupo();
    grupo.setNombre(nombre);
    return grupo;
  }

  private Tarea tarea(String estado, Grupo grupo) {
    Tarea tarea = new Tarea();
    tarea.setEstado(estado);
    tarea.setGrupo(grupo);
    return tarea;
  }

  @Test
  public void agruparPorGrupoDeberiaSepararLasTareasSegunSuGrupo() {
    // preparacion
    Grupo grupo1 = grupo("grupo1");
    Grupo grupo2 = grupo("grupo2");
    Tarea tarea1 = tarea("PENDIENTE", grupo1);
    Tarea tarea2 = tarea("COMPLETADA", grupo1);
    Tarea tarea3 = tarea("PENDIENTE", grupo2);

    when(this.repositorioTareaMock.buscarTodas()).thenReturn(Arrays.asList(tarea1, tarea2, tarea3));

    // ejecucion
    Map<String, List<Tarea>> resultado = this.servicioTarea.agruparPorGrupo();

    // validacion
    assertThat(resultado.size(), equalTo(2));
    assertThat(resultado.get("grupo1"), equalTo(Arrays.asList(tarea1, tarea2)));
    assertThat(resultado.get("grupo2"), equalTo(Arrays.asList(tarea3)));
  }

  @Test
  public void agruparPorGrupoDeberiaPonerLasTareasSinGrupoEnSinGrupo() {
    // preparacion
    Tarea sinGrupo = tarea("PENDIENTE", null);
    Tarea grupoSinNombre = tarea("PENDIENTE", new Grupo());

    when(this.repositorioTareaMock.buscarTodas())
      .thenReturn(Arrays.asList(sinGrupo, grupoSinNombre));

    // ejecucion
    Map<String, List<Tarea>> resultado = this.servicioTarea.agruparPorGrupo();

    // validacion
    assertThat(resultado.size(), equalTo(1));
    assertThat(resultado.get("Sin grupo"), equalTo(Arrays.asList(sinGrupo, grupoSinNombre)));
  }

  @Test
  public void agruparPorGrupoDeberiaDevolverUnMapaVacioSiNoHayTareas() {
    when(this.repositorioTareaMock.buscarTodas()).thenReturn(Arrays.asList());

    Map<String, List<Tarea>> resultado = this.servicioTarea.agruparPorGrupo();

    assertThat(resultado.isEmpty(), equalTo(true));
  }

  @Test
  public void calcularProgresoPorGrupoDeberiaCalcularElProgresoDeCadaGrupoPorSeparado() {
    // preparacion
    Grupo grupo1 = grupo("grupo1");
    Grupo grupo2 = grupo("grupo2");
    Grupo grupo3 = grupo("grupo3");

    // grupo1: 1 de 2 completadas = 50%
    // grupo2: 2 de 2 completadas = 100%
    // grupo3: 0 de 1 completadas = 0%
    when(this.repositorioTareaMock.buscarTodas())
      .thenReturn(
        Arrays.asList(
          tarea("COMPLETADA", grupo1),
          tarea("PENDIENTE", grupo1),
          tarea("COMPLETADA", grupo2),
          tarea("COMPLETADA", grupo2),
          tarea("PENDIENTE", grupo3)
        )
      );

    // ejecucion
    Map<String, Double> progreso = this.servicioTarea.calcularProgresoPorGrupo();

    // validacion
    assertThat(progreso.get("grupo1"), equalTo(50.0));
    assertThat(progreso.get("grupo2"), equalTo(100.0));
    assertThat(progreso.get("grupo3"), equalTo(0.0));
  }

  @Test
  public void calcularProgresoPorGrupoNoDeberiaMezclarElProgresoEntreGrupos() {
    // preparacion: el grupo1 no completó nada, aunque el grupo2 sí completó todo
    Grupo grupo1 = grupo("grupo1");
    Grupo grupo2 = grupo("grupo2");

    when(this.repositorioTareaMock.buscarTodas())
      .thenReturn(
        Arrays.asList(
          tarea("PENDIENTE", grupo1),
          tarea("COMPLETADA", grupo2),
          tarea("COMPLETADA", grupo2)
        )
      );

    // ejecucion
    Map<String, Double> progreso = this.servicioTarea.calcularProgresoPorGrupo();

    // validacion: el progreso global sería 66%, pero cada grupo tiene el suyo
    assertThat(progreso.get("grupo1"), equalTo(0.0));
    assertThat(progreso.get("grupo2"), equalTo(100.0));
  }

  @Test
  public void calcularProgresoPorGrupoDeberiaIncluirElGrupoSinGrupo() {
    // preparacion
    when(this.repositorioTareaMock.buscarTodas())
      .thenReturn(Arrays.asList(tarea("COMPLETADA", null), tarea("PENDIENTE", null)));

    // ejecucion
    Map<String, Double> progreso = this.servicioTarea.calcularProgresoPorGrupo();

    // validacion
    assertThat(progreso.get("Sin grupo"), equalTo(50.0));
  }
}
