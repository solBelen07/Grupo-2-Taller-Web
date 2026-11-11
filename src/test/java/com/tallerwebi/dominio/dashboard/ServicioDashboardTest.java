package com.tallerwebi.dominio.dashboard;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Evento;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.ServicioCalendario;
import com.tallerwebi.dominio.TipoEvento;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.calendario.EstadoActividad;
import com.tallerwebi.dominio.calendario.EventoConEstado;
import com.tallerwebi.dominio.calendario.FuenteDeEventos;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import com.tallerwebi.dominio.racha.ServicioRacha;
import com.tallerwebi.dominio.sesionEstudio.RepositorioSesionEstudio;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import com.tallerwebi.dominio.tarea.ServicioTarea;
import com.tallerwebi.dominio.tecnicasestudio.RepositorioPomodoro;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioDashboardTest {

  private static final Long ID_USUARIO = 1L;
  private static final String NOMBRE_USUARIO = "test@unlam.edu.ar";
  private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 10, 0);
  private RepositorioPomodoro repositorioPomodoroMock;
  private RepositorioMateria repositorioMateriaMock;
  private RepositorioUsuario repositorioUsuarioMock;
  private RepositorioSesionEstudio repositorioSesionEstudioMock;

  private ServicioTarea servicioTareaMock;
  private ServicioRacha servicioRachaMock;
  private ServicioCalendario servicioCalendarioMock;
  private FuenteDeEventos fuenteDeParcialesMock;
  private FuenteDeEventos fuenteDeTrabajosMock;

  private ServicioDashboard servicioDashboard;

  @BeforeEach
  public void init() {
    this.repositorioPomodoroMock = mock(RepositorioPomodoro.class);
    this.repositorioMateriaMock = mock(RepositorioMateria.class);
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.repositorioSesionEstudioMock = mock(RepositorioSesionEstudio.class);

    this.servicioTareaMock = mock(ServicioTarea.class);
    this.servicioRachaMock = mock(ServicioRacha.class);
    this.servicioCalendarioMock = mock(ServicioCalendario.class);
    this.fuenteDeParcialesMock = mock(FuenteDeEventos.class);
    this.fuenteDeTrabajosMock = mock(FuenteDeEventos.class);

    this.servicioDashboard =
      new ServicioDashboardImpl(
        this.repositorioPomodoroMock,
        this.repositorioMateriaMock,
        this.repositorioUsuarioMock,
        this.repositorioSesionEstudioMock,
        this.servicioTareaMock,
        this.servicioRachaMock,
        this.servicioCalendarioMock,
        List.of(this.fuenteDeParcialesMock, this.fuenteDeTrabajosMock)
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
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(usuarioConEmail());

    Map<String, Long> aportes = new HashMap<>();
    aportes.put(NOMBRE_USUARIO, 3L);
    aportes.put("Mateo", 1L); // Total tareas = 4
    when(this.servicioTareaMock.calcularAporteIndividual()).thenReturn(aportes);

    Double porcentaje = this.servicioDashboard.porcentajeCumplimiento(ID_USUARIO);

    assertThat(porcentaje, equalTo(75.0));
    verify(this.repositorioUsuarioMock, times(1)).buscarPorId(ID_USUARIO);
    verify(this.servicioTareaMock, times(1)).calcularAporteIndividual();
  }

  @Test
  public void porcentajeCumplimientoDeberiaDevolverCeroSiNoHayTareasCompletadasEnGeneral() {
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(usuarioConEmail());
    when(this.servicioTareaMock.calcularAporteIndividual()).thenReturn(Collections.emptyMap());

    Double porcentaje = this.servicioDashboard.porcentajeCumplimiento(ID_USUARIO);

    assertThat(porcentaje, equalTo(0.0));
  }

  @Test
  public void porcentajeCumplimientoDeberiaDevolverCeroSiElUsuarioNoTieneTareasCompletadas() {
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(usuarioConEmail());

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

  @Test
  public void obtenerRachaDeberiaDevolverLaRachaDelUsuario() {
    Usuario usuarioMock = mock(Usuario.class);
    Integer rachaEsperada = 5;

    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(usuarioMock);
    when(this.servicioRachaMock.obtenerRacha(usuarioMock)).thenReturn(rachaEsperada);

    Integer rachaObtenida = this.servicioDashboard.obtenerRacha(ID_USUARIO);

    assertThat(rachaObtenida, equalTo(rachaEsperada));
    verify(this.repositorioUsuarioMock, times(1)).buscarPorId(ID_USUARIO);
    verify(this.servicioRachaMock, times(1)).obtenerRacha(usuarioMock);
  }

  @Test
  public void obtenerRecordRachaDeberiaDevolverElRecordDelUsuario() {
    Usuario usuarioMock = mock(Usuario.class);
    Integer recordEsperado = 10;

    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(usuarioMock);
    when(this.servicioRachaMock.obtenerRecordRacha(usuarioMock)).thenReturn(recordEsperado);

    Integer recordObtenido = this.servicioDashboard.obtenerRecordRacha(ID_USUARIO);

    assertThat(recordObtenido, equalTo(recordEsperado));
    verify(this.repositorioUsuarioMock, times(1)).buscarPorId(ID_USUARIO);
    verify(this.servicioRachaMock, times(1)).obtenerRecordRacha(usuarioMock);
  }

  private static Usuario usuarioConEmail() {
    Usuario usuario = new Usuario();
    usuario.setEmail(NOMBRE_USUARIO);
    return usuario;
  }

  @Test
  public void proximosVencimientosJuntaParcialesYTrabajosOrdenadosPorFecha() {
    Materia taller = materia(1, "Taller Web I");
    dadoQueElUsuarioCursa(taller);
    dadoQueLaFuenteDevuelve(
      this.fuenteDeParcialesMock,
      vencimiento(taller, TipoEvento.PARCIAL, "Parcial", AHORA.plusDays(5))
    );
    dadoQueLaFuenteDevuelve(
      this.fuenteDeTrabajosMock,
      vencimiento(taller, TipoEvento.TRABAJO_PRACTICO, "TP", AHORA.plusDays(2))
    );

    List<EventoConEstado> vencimientos =
      this.servicioDashboard.obtenerProximosVencimientos(ID_USUARIO, AHORA);

    assertThat(titulos(vencimientos), contains("TP", "Parcial"));
  }

  @Test
  public void proximosVencimientosMuestraComoMaximoTres() {
    Materia taller = materia(1, "Taller Web I");
    dadoQueElUsuarioCursa(taller);
    dadoQueLaFuenteDevuelve(
      this.fuenteDeParcialesMock,
      vencimiento(taller, TipoEvento.PARCIAL, "P4", AHORA.plusDays(4)),
      vencimiento(taller, TipoEvento.PARCIAL, "P1", AHORA.plusDays(1)),
      vencimiento(taller, TipoEvento.PARCIAL, "P3", AHORA.plusDays(3)),
      vencimiento(taller, TipoEvento.PARCIAL, "P2", AHORA.plusDays(2))
    );
    dadoQueLaFuenteDevuelve(this.fuenteDeTrabajosMock);

    List<EventoConEstado> vencimientos =
      this.servicioDashboard.obtenerProximosVencimientos(ID_USUARIO, AHORA);

    assertThat(titulos(vencimientos), contains("P1", "P2", "P3"));
  }

  @Test
  public void proximosVencimientosNoIncluyeLosQueYaPasaronNiLosCompletados() {
    Materia taller = materia(1, "Taller Web I");
    dadoQueElUsuarioCursa(taller);
    dadoQueLaFuenteDevuelve(
      this.fuenteDeParcialesMock,
      vencimiento(taller, TipoEvento.PARCIAL, "Pasado", AHORA.minusHours(1))
    );
    dadoQueLaFuenteDevuelve(
      this.fuenteDeTrabajosMock,
      new EventoConEstado(
        evento(taller, TipoEvento.TRABAJO_PRACTICO, "Entregado", AHORA.plusDays(3)),
        EstadoActividad.COMPLETADA
      ),
      vencimiento(taller, TipoEvento.TRABAJO_PRACTICO, "Pendiente", AHORA.plusDays(4))
    );

    List<EventoConEstado> vencimientos =
      this.servicioDashboard.obtenerProximosVencimientos(ID_USUARIO, AHORA);

    assertThat(titulos(vencimientos), contains("Pendiente"));
  }

  @Test
  public void proximosVencimientosSoloIncluyeLasMateriasDelUsuario() {
    Materia taller = materia(1, "Taller Web I");
    Materia ajena = materia(9, "Materia de otro");
    dadoQueElUsuarioCursa(taller);
    dadoQueLaFuenteDevuelve(
      this.fuenteDeParcialesMock,
      vencimiento(ajena, TipoEvento.PARCIAL, "De otro", AHORA.plusDays(1)),
      vencimiento(taller, TipoEvento.PARCIAL, "Mio", AHORA.plusDays(2))
    );
    dadoQueLaFuenteDevuelve(this.fuenteDeTrabajosMock);

    List<EventoConEstado> vencimientos =
      this.servicioDashboard.obtenerProximosVencimientos(ID_USUARIO, AHORA);

    assertThat(titulos(vencimientos), contains("Mio"));
  }

  @Test
  public void proximosVencimientosDevuelveVacioSiNoHayNada() {
    dadoQueElUsuarioCursa();
    dadoQueLaFuenteDevuelve(this.fuenteDeParcialesMock);
    dadoQueLaFuenteDevuelve(this.fuenteDeTrabajosMock);

    List<EventoConEstado> vencimientos =
      this.servicioDashboard.obtenerProximosVencimientos(ID_USUARIO, AHORA);

    assertThat(vencimientos, is(empty()));
  }

  @Test
  public void porcentajeCumplimientoDeberiaDevolverCeroSiElUsuarioNoExiste() {
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(null);

    Double porcentaje = this.servicioDashboard.porcentajeCumplimiento(ID_USUARIO);

    assertThat(porcentaje, equalTo(0.0));
  }

  @Test
  public void obtenerCalendarioSemanalDeberiaPedirleLaSemanaAlServicioDeCalendario() {
    LocalDate referencia = LocalDate.of(2026, 11, 10);
    CalendarioSemanal semana = mock(CalendarioSemanal.class);
    when(this.servicioCalendarioMock.obtenerSemana(ID_USUARIO, referencia)).thenReturn(semana);

    CalendarioSemanal resultado = this.servicioDashboard.obtenerCalendarioSemanal(ID_USUARIO, referencia);

    assertThat(resultado, is(sameInstance(semana)));
  }

  @Test
  public void proximosVencimientosNoIncluyeEventosSinMateria() {
    dadoQueElUsuarioCursa(materia(1, "Taller Web I"));
    Evento sinMateria = mock(Evento.class);
    when(sinMateria.getInicio()).thenReturn(AHORA.plusDays(1));
    when(sinMateria.getMateria()).thenReturn(null);
    dadoQueLaFuenteDevuelve(
      this.fuenteDeParcialesMock,
      new EventoConEstado(sinMateria, EstadoActividad.EN_TIEMPO)
    );
    dadoQueLaFuenteDevuelve(this.fuenteDeTrabajosMock);

    List<EventoConEstado> vencimientos =
      this.servicioDashboard.obtenerProximosVencimientos(ID_USUARIO, AHORA);

    assertThat(vencimientos, is(empty()));
  }

  private void dadoQueElUsuarioCursa(Materia... materias) {
    when(this.repositorioMateriaMock.obtenerPorUsuario(ID_USUARIO)).thenReturn(List.of(materias));
  }

  private void dadoQueLaFuenteDevuelve(FuenteDeEventos fuente, EventoConEstado... vencimientos) {
    when(fuente.eventosEntre(eq(ID_USUARIO), any(), any(), any())).thenReturn(List.of(vencimientos));
  }

  private static Materia materia(Integer id, String nombre) {
    Materia materia = new Materia();
    materia.setId(id);
    materia.setNombre(nombre);
    return materia;
  }

  private static Evento evento(Materia materia, TipoEvento tipo, String titulo, LocalDateTime inicio) {
    return new Evento(ID_USUARIO, materia, tipo, titulo, inicio, inicio.plusHours(2));
  }

  private static EventoConEstado vencimiento(
    Materia materia,
    TipoEvento tipo,
    String titulo,
    LocalDateTime inicio
  ) {
    return new EventoConEstado(evento(materia, tipo, titulo, inicio), EstadoActividad.EN_TIEMPO);
  }

  private static List<String> titulos(List<EventoConEstado> vencimientos) {
    return vencimientos
      .stream()
      .map(vencimiento -> vencimiento.evento().getTitulo())
      .collect(Collectors.toList());
  }
}
