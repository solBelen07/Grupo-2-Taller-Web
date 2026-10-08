package com.tallerwebi.dominio.dashboard;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.ServicioCalendario;
import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import com.tallerwebi.dominio.sesionEstudio.RepositorioSesionEstudio;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import com.tallerwebi.dominio.tarea.ServicioTarea;
import com.tallerwebi.dominio.tecnicasestudio.RepositorioPomodoro;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioDashboardTest {

  private static final Long ID_USUARIO = 1L;
  private static final String NOMBRE_USUARIO = "Juan";

  private RepositorioPomodoro repositorioPomodoroMock;
  private RepositorioMateria repositorioMateriaMock;
  private RepositorioUsuario repositorioUsuarioMock;
  private RepositorioSesionEstudio repositorioSesionEstudioMock;

  private ServicioCalendario servicioCalendarioMock;
  private ServicioTarea servicioTareaMock;

  private ServicioDashboard servicioDashboard;

  @BeforeEach
  public void init() {
    this.repositorioPomodoroMock = mock(RepositorioPomodoro.class);
    this.repositorioMateriaMock = mock(RepositorioMateria.class);
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.repositorioSesionEstudioMock = mock(RepositorioSesionEstudio.class);

    this.servicioCalendarioMock = mock(ServicioCalendario.class);
    this.servicioTareaMock = mock(ServicioTarea.class);

    this.servicioDashboard =
      new ServicioDashboardImpl(
        this.repositorioPomodoroMock,
        this.repositorioMateriaMock,
        this.repositorioUsuarioMock,
        this.repositorioSesionEstudioMock,
        this.servicioCalendarioMock,
        this.servicioTareaMock
      );
  }

  @Test
  public void obtenerHorasEstudioDeberiaConvertirMinutosAHoras() {
    when(this.repositorioPomodoroMock.calcularMinutosCompletados(ID_USUARIO)).thenReturn(120L);

    Long horasResultantes = this.servicioDashboard.obtenerHorasEstudio(ID_USUARIO);

    assertThat(horasResultantes, equalTo(2L));
    verify(this.repositorioPomodoroMock, times(1)).calcularMinutosCompletados(ID_USUARIO);
  }

  @Test
  public void porcentajeCumplimientoDeberiaCalcularElPorcentajeCorrectamente() {
    when(this.repositorioUsuarioMock.obtenerNombre(ID_USUARIO)).thenReturn(NOMBRE_USUARIO);

    Map<String, Long> aportes = new HashMap<>();
    aportes.put(NOMBRE_USUARIO, 3L);
    aportes.put("Mateo", 1L); // Total tareas = 4
    when(this.servicioTareaMock.calcularAporteIndividual()).thenReturn(aportes);

    Double porcentaje = this.servicioDashboard.porcentajeCumplimiento(ID_USUARIO);

    assertThat(porcentaje, equalTo(75.0));
    verify(this.repositorioUsuarioMock, times(1)).obtenerNombre(ID_USUARIO);
    verify(this.servicioTareaMock, times(1)).calcularAporteIndividual();
  }

  @Test
  public void porcentajeCumplimientoDeberiaDevolverCeroSiNoHayTareasCompletadasEnGeneral() {
    when(this.repositorioUsuarioMock.obtenerNombre(ID_USUARIO)).thenReturn(NOMBRE_USUARIO);
    when(this.servicioTareaMock.calcularAporteIndividual()).thenReturn(Collections.emptyMap());

    Double porcentaje = this.servicioDashboard.porcentajeCumplimiento(ID_USUARIO);

    assertThat(porcentaje, equalTo(0.0));
  }

  @Test
  public void porcentajeCumplimientoDeberiaDevolverCeroSiElUsuarioNoTieneTareasCompletadas() {
    when(this.repositorioUsuarioMock.obtenerNombre(ID_USUARIO)).thenReturn(NOMBRE_USUARIO);

    Map<String, Long> aportes = new HashMap<>();
    aportes.put("Mateo", 5L); // El usuario buscado no está en la lista de aportes
    when(this.servicioTareaMock.calcularAporteIndividual()).thenReturn(aportes);

    Double porcentaje = this.servicioDashboard.porcentajeCumplimiento(ID_USUARIO);

    assertThat(porcentaje, equalTo(0.0));
  }

  @Test
  public void obtenerCantidadMateriasDeberiaDevolverElTotalDelRepositorio() {
    when(this.repositorioMateriaMock.obtenerCantidadMateriasPorUsuario(ID_USUARIO)).thenReturn(5L);

    Long cantidadMaterias = this.servicioDashboard.obtenerCantidadMaterias(ID_USUARIO);

    assertThat(cantidadMaterias, equalTo(5L));
    verify(this.repositorioMateriaMock, times(1)).obtenerCantidadMateriasPorUsuario(ID_USUARIO);
  }

  @Test
  public void obtenerCalendarioSemanalDeberiaDelegarEnElServicioCalendario() {
    LocalDate hoy = LocalDate.now();
    CalendarioSemanal calendarioEsperado = mock(CalendarioSemanal.class);

    when(this.servicioCalendarioMock.obtenerSemana(ID_USUARIO, hoy)).thenReturn(calendarioEsperado);

    CalendarioSemanal resultado = this.servicioDashboard.obtenerCalendarioSemanal(ID_USUARIO, hoy);

    assertThat(resultado, equalTo(calendarioEsperado));
    verify(this.servicioCalendarioMock, times(1)).obtenerSemana(ID_USUARIO, hoy);
  }

  @Test
  public void obtenerProximaSesionDeberiaDevolverLaSesionObtenidaDelRepositorio() {
    SesionEstudio sesionEsperada = new SesionEstudio();
    when(this.repositorioSesionEstudioMock.buscarProximaSesionEstudio(ID_USUARIO))
      .thenReturn(sesionEsperada);

    SesionEstudio resultado = this.servicioDashboard.obtenerProximaSesion(ID_USUARIO);

    assertThat(resultado, equalTo(sesionEsperada));
    verify(this.repositorioSesionEstudioMock, times(1)).buscarProximaSesionEstudio(ID_USUARIO);
  }

  @Test
  public void obtenerProximaSesionDeberiaDevolverNullSiNoHayProximaSesion() {
    when(this.repositorioSesionEstudioMock.buscarProximaSesionEstudio(ID_USUARIO)).thenReturn(null);

    SesionEstudio resultado = this.servicioDashboard.obtenerProximaSesion(ID_USUARIO);

    assertThat(resultado, is(nullValue()));
  }
}
