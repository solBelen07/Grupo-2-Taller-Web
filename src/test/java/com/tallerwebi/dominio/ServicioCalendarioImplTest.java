package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.calendario.CalendarioMensual;
import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.calendario.DiaMensual;
import com.tallerwebi.dominio.calendario.DiaSemanal;
import com.tallerwebi.dominio.calendario.EstadoActividad;
import com.tallerwebi.dominio.calendario.EventoConEstado;
import com.tallerwebi.dominio.calendario.FuenteDeEventos;
import com.tallerwebi.dominio.calendario.ResumenMateria;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.tarea.RepositorioTarea;
import com.tallerwebi.dominio.tarea.Tarea;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCalendarioImplTest {

  private static final Long USUARIO = 1L;
  private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
  // Lunes 28/09/2026 a las 10:00 en Buenos Aires.
  private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-09-28T13:00:00Z"), ZONA);

  private RepositorioEvento repositorioEventoMock;
  private RepositorioTarea repositorioTareaMock;
  private List<FuenteDeEventos> fuentes;
  private ServicioCalendario servicioCalendario;
  private Materia analisis;
  private Materia fisica;

  private static Materia materia(String nombre, String color) {
    Materia materia = new Materia();
    materia.setNombre(nombre);
    materia.setColor(color);
    return materia;
  }

  @BeforeEach
  public void init() {
    this.repositorioEventoMock = mock(RepositorioEvento.class);
    this.repositorioTareaMock = mock(RepositorioTarea.class);
    this.fuentes = new ArrayList<>();
    this.analisis = materia("Análisis II", "#2F6FDE");
    this.fisica = materia("Física II", "#D98A00");
    when(
            this.repositorioEventoMock.buscarEnRango(
                    eq(USUARIO),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class)
            )
    ).thenReturn(List.of());
    when(this.repositorioTareaMock.buscarTodas()).thenReturn(List.of());
    armarServicio();
  }

  private void armarServicio() {
    this.servicioCalendario = new ServicioCalendarioImpl(
            this.repositorioEventoMock,
            this.repositorioTareaMock,
            this.fuentes,
            RELOJ
    );
  }

  private void conEventos(Evento... eventos) {
    when(
            this.repositorioEventoMock.buscarEnRango(
                    eq(USUARIO),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class)
            )
    ).thenReturn(List.of(eventos));
  }

  private static Evento evento(
          Materia materia,
          String titulo,
          LocalDateTime desde,
          LocalDateTime hasta
  ) {
    return new Evento(USUARIO, materia, TipoEvento.CLASE, titulo, desde, hasta);
  }

  private static DiaMensual dia(CalendarioMensual calendario, LocalDate fecha) {
    return calendario
            .semanas()
            .stream()
            .flatMap(semana -> semana.dias().stream())
            .filter(d -> d.fecha().equals(fecha))
            .findFirst()
            .orElseThrow();
  }

  // ---------------------------------------------------------------- vista mensual

  @Test
  public void deberiaArmarCincoSemanasCompletasParaSeptiembreDe2026() {
    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, LocalDate.of(2026, 9, 15));

    // validacion
    assertThat(mes.semanas(), hasSize(5));
    assertThat(mes.semanas().get(0).lunes(), is(LocalDate.of(2026, 8, 31)));
    assertThat(mes.semanas().get(0).numero(), is(36));
    assertThat(mes.semanas().get(4).numero(), is(40));
    mes.semanas().forEach(semana -> assertThat(semana.dias(), hasSize(7)));
    assertThat(mes.semanas().get(4).dias().get(6).fecha(), is(LocalDate.of(2026, 10, 4)));
  }

  @Test
  public void deberiaArmarSoloCuatroSemanasCuandoElMesEntraExacto() {
    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, LocalDate.of(2027, 2, 10));

    // validacion
    assertThat(mes.semanas(), hasSize(4));
    assertThat(mes.semanas().get(0).dias().get(0).fecha(), is(LocalDate.of(2027, 2, 1)));
    assertThat(mes.semanas().get(0).dias().get(0).delMesActual(), is(true));
  }

  @Test
  public void deberiaMarcarLosDiasQueNoSonDelMesYElDiaDeHoy() {
    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, LocalDate.of(2026, 9, 15));

    // validacion
    assertThat(dia(mes, LocalDate.of(2026, 8, 31)).delMesActual(), is(false));
    assertThat(dia(mes, LocalDate.of(2026, 9, 1)).delMesActual(), is(true));
    assertThat(dia(mes, LocalDate.of(2026, 10, 1)).delMesActual(), is(false));
    assertThat(dia(mes, LocalDate.of(2026, 9, 28)).esHoy(), is(true));
    assertThat(dia(mes, LocalDate.of(2026, 9, 27)).esHoy(), is(false));
    assertThat(mes.hoy(), is(LocalDate.of(2026, 9, 28)));
  }

  @Test
  public void deberiaPedirAlRepositorioTodoElRangoDeLaGrilla() {
    // ejecucion
    this.servicioCalendario.obtenerMes(USUARIO, LocalDate.of(2026, 9, 15));

    // validacion
    verify(this.repositorioEventoMock, times(1)).buscarEnRango(
            USUARIO,
            LocalDateTime.of(2026, 8, 31, 0, 0),
            LocalDateTime.of(2026, 10, 5, 0, 0)
    );
  }

  @Test
  public void deberiaUbicarLosEventosEnSuDiaOrdenadosPorHora() {
    // preparacion
    LocalDate lunes = LocalDate.of(2026, 9, 28);
    conEventos(
            evento(this.fisica, "Física", lunes.atTime(19, 0), lunes.atTime(21, 0)),
            evento(this.analisis, "Análisis", lunes.atTime(8, 0), lunes.atTime(10, 0)),
            evento(
                    this.analisis,
                    "Otro día",
                    lunes.plusDays(1).atTime(8, 0),
                    lunes.plusDays(1).atTime(9, 0)
            )
    );

    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, lunes);

    // validacion
    assertThat(
            dia(mes, lunes)
                    .eventos()
                    .stream()
                    .map(e -> e.titulo())
                    .toList(),
            contains("Análisis", "Física")
    );
    assertThat(dia(mes, lunes.plusDays(1)).eventos(), hasSize(1));
    assertThat(dia(mes, lunes.plusDays(2)).eventos(), is(empty()));
  }

  @Test
  public void deberiaRepetirUnEventoQueCruzaLaMedianocheEnAmbosDias() {
    // preparacion
    LocalDate sabado = LocalDate.of(2026, 10, 3);
    conEventos(
            evento(this.fisica, "Maratón", sabado.atTime(22, 0), sabado.plusDays(1).atTime(1, 0))
    );

    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, sabado);

    // validacion
    assertThat(dia(mes, sabado).eventos(), hasSize(1));
    assertThat(dia(mes, sabado.plusDays(1)).eventos(), hasSize(1));
    assertThat(dia(mes, sabado.plusDays(1)).eventos().get(0).continuaDesdeAntes(), is(true));
    assertThat(mes.totalEventos(), is(1));
  }

  @Test
  public void deberiaCalcularTituloYNavegacionDelMes() {
    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, LocalDate.of(2026, 9, 15));

    // validacion
    assertThat(mes.titulo(), is("septiembre de 2026"));
    assertThat(mes.anterior(), is(LocalDate.of(2026, 8, 1)));
    assertThat(mes.siguiente(), is(LocalDate.of(2026, 10, 1)));
  }

  @Test
  public void deberiaNavegarCorrectamenteEntreAnios() {
    // ejecucion
    CalendarioMensual diciembre = this.servicioCalendario.obtenerMes(
            USUARIO,
            LocalDate.of(2026, 12, 5)
    );

    // validacion
    assertThat(diciembre.siguiente(), is(LocalDate.of(2027, 1, 1)));
    assertThat(diciembre.anterior(), is(LocalDate.of(2026, 11, 1)));
  }

  @Test
  public void deberiaListarSoloLasMateriasConEventosEnElPeriodo() {
    // preparacion
    LocalDate lunes = LocalDate.of(2026, 9, 28);
    conEventos(
            evento(this.analisis, "A1", lunes.atTime(8, 0), lunes.atTime(9, 0)),
            evento(this.analisis, "A2", lunes.atTime(10, 0), lunes.atTime(11, 0))
    );

    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, lunes);

    // validacion: solo Análisis II aparece (Física II no tiene eventos este período)
    assertThat(mes.materias(), contains(new ResumenMateria("Análisis II", "#2F6FDE", 2)));
    assertThat(mes.vacio(), is(false));
  }

  @Test
  public void deberiaIndicarQueEstaVacioSinEventos() {
    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, LocalDate.of(2026, 9, 15));

    // validacion
    assertThat(mes.vacio(), is(true));
    assertThat(mes.totalEventos(), is(0));
    assertThat(mes.materias(), is(empty()));
  }

  // ---------------------------------------------------------------- vista semanal

  @Test
  public void deberiaIndicarQueLaSemanaEstaVaciaSinEventos() {
    // ejecucion
    CalendarioSemanal semana = this.servicioCalendario.obtenerSemana(
            USUARIO,
            LocalDate.of(2026, 9, 15)
    );

    // validacion
    assertThat(semana.vacio(), is(true));
    assertThat(semana.totalEventos(), is(0));
  }

  @Test
  public void deberiaArmarLaSemanaDeLunesADomingoDesdeCualquierDiaDeLaSemana() {
    // ejecucion
    CalendarioSemanal semana = this.servicioCalendario.obtenerSemana(
            USUARIO,
            LocalDate.of(2026, 9, 30)
    );

    // validacion
    assertThat(semana.dias(), hasSize(7));
    assertThat(semana.dias().get(0).fecha(), is(LocalDate.of(2026, 9, 28)));
    assertThat(semana.dias().get(0).nombre(), is("Lunes"));
    assertThat(semana.dias().get(6).fecha(), is(LocalDate.of(2026, 10, 4)));
    assertThat(semana.dias().get(6).abreviatura(), is("Dom"));
    assertThat(semana.dias().get(0).esHoy(), is(true));
    assertThat(semana.dias().get(1).esHoy(), is(false));
  }

  @Test
  public void deberiaCalcularTituloYNavegacionDeLaSemana() {
    // ejecucion
    CalendarioSemanal semana = this.servicioCalendario.obtenerSemana(
            USUARIO,
            LocalDate.of(2026, 9, 30)
    );

    // validacion
    assertThat(semana.titulo(), is("28 sep – 4 oct 2026"));
    assertThat(semana.anterior(), is(LocalDate.of(2026, 9, 21)));
    assertThat(semana.siguiente(), is(LocalDate.of(2026, 10, 5)));
    verify(this.repositorioEventoMock, times(1)).buscarEnRango(
            USUARIO,
            LocalDateTime.of(2026, 9, 28, 0, 0),
            LocalDateTime.of(2026, 10, 5, 0, 0)
    );
  }

  @Test
  public void deberiaUsarLaGrillaDeSieteAVeintidosHorasPorDefecto() {
    // ejecucion
    CalendarioSemanal semana = this.servicioCalendario.obtenerSemana(
            USUARIO,
            LocalDate.of(2026, 9, 28)
    );

    // validacion
    assertThat(semana.horaInicio(), is(7));
    assertThat(semana.horaFin(), is(22));
    assertThat(semana.etiquetasHoras(), hasSize(15));
    assertThat(semana.etiquetasHoras().get(0), is("07:00"));
    assertThat(semana.etiquetasHoras().get(14), is("21:00"));
  }

  @Test
  public void deberiaExtenderLaGrillaSiHayEventosFueraDelHorarioHabitual() {
    // preparacion
    LocalDate lunes = LocalDate.of(2026, 9, 28);
    conEventos(
            evento(this.fisica, "Temprano", lunes.atTime(6, 30), lunes.atTime(8, 0)),
            evento(this.analisis, "Tarde", lunes.atTime(22, 30), lunes.atTime(23, 30))
    );

    // ejecucion
    CalendarioSemanal semana = this.servicioCalendario.obtenerSemana(USUARIO, lunes);

    // validacion
    assertThat(semana.horaInicio(), is(6));
    assertThat(semana.horaFin(), is(24));
    assertThat(semana.etiquetasHoras(), hasSize(18));
    assertThat(semana.etiquetasHoras().get(0), is("06:00"));
    assertThat(semana.etiquetasHoras().get(17), is("23:00"));
  }

  @Test
  public void deberiaPosicionarLosBloquesRelativosAlInicioDeLaGrilla() {
    // preparacion
    LocalDate martes = LocalDate.of(2026, 9, 29);
    conEventos(evento(this.analisis, "Teoría", martes.atTime(18, 0), martes.atTime(20, 0)));

    // ejecucion
    CalendarioSemanal semana = this.servicioCalendario.obtenerSemana(USUARIO, martes);

    // validacion
    DiaSemanal dia = semana.dias().get(1);
    assertThat(dia.bloques(), hasSize(1));
    assertThat(dia.bloques().get(0).top(), is((18 - 7) * 60));
    assertThat(dia.bloques().get(0).alto(), is(120));
    assertThat(dia.bloques().get(0).columnas(), is(1));
    assertThat(semana.totalEventos(), is(1));
    assertThat(semana.vacio(), is(false));
  }

  @Test
  public void deberiaMostrarLadoALadoLosEventosSuperpuestos() {
    // preparacion
    LocalDate jueves = LocalDate.of(2026, 10, 1);
    conEventos(
            evento(this.fisica, "Clase", jueves.atTime(19, 0), jueves.atTime(21, 0)),
            evento(this.analisis, "Reunión", jueves.atTime(20, 0), jueves.atTime(22, 0))
    );

    // ejecucion
    CalendarioSemanal semana = this.servicioCalendario.obtenerSemana(USUARIO, jueves);

    // validacion
    DiaSemanal dia = semana.dias().get(3);
    assertThat(dia.bloques(), hasSize(2));
    dia.bloques().forEach(bloque -> assertThat(bloque.columnas(), is(2)));
  }

  @Test
  public void deberiaPartirEnDosDiasUnEventoNocturnoDeLaSemana() {
    // preparacion
    LocalDate sabado = LocalDate.of(2026, 10, 3);
    conEventos(
            evento(this.fisica, "Maratón", sabado.atTime(22, 0), sabado.plusDays(1).atTime(1, 0))
    );

    // ejecucion
    CalendarioSemanal semana = this.servicioCalendario.obtenerSemana(USUARIO, sabado);

    // validacion
    assertThat(semana.dias().get(5).bloques(), hasSize(1));
    assertThat(semana.dias().get(6).bloques(), hasSize(1));
    assertThat(semana.horaInicio(), is(0));
    assertThat(semana.horaFin(), is(24));
  }

  // ---------------------------------------------------------------- CAL-02: estado de actividades

  private static Tarea tarea(Long id, String estado, LocalDate fechaVencimiento) {
    Tarea tarea = new Tarea();
    tarea.setId(id);
    tarea.setEstado(estado);
    tarea.setFechaVencimiento(fechaVencimiento);
    return tarea;
  }

  @Test
  public void unEventoSinTareaVinculadaNoTieneEstado() {
    // preparacion
    LocalDate lunes = LocalDate.of(2026, 9, 28);
    conEventos(evento(this.analisis, "Teoría", lunes.atTime(18, 0), lunes.atTime(20, 0)));

    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, lunes);

    // validacion
    assertThat(dia(mes, lunes).eventos().get(0).estado(), is(nullValue()));
    verify(this.repositorioTareaMock, never()).buscarTodas();
  }

  @Test
  public void unEventoConTareaVinculadaMuestraElEstadoDeEsaTarea() {
    // preparacion
    LocalDate lunes = LocalDate.of(2026, 9, 28);
    Evento evento = evento(this.analisis, "Entrega TP", lunes.atTime(18, 0), lunes.atTime(20, 0));
    evento.vincularTarea(5L);
    when(this.repositorioTareaMock.buscarTodas()).thenReturn(
            List.of(tarea(5L, "COMPLETADA", lunes.minusDays(1)))
    );
    conEventos(evento);

    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, lunes);

    // validacion
    assertThat(dia(mes, lunes).eventos().get(0).estado(), is(EstadoActividad.COMPLETADA));
  }

  @Test
  public void siLaTareaVinculadaNoExisteMasElEventoQuedaSinEstado() {
    // preparacion
    LocalDate lunes = LocalDate.of(2026, 9, 28);
    Evento evento = evento(this.analisis, "Entrega TP", lunes.atTime(18, 0), lunes.atTime(20, 0));
    evento.vincularTarea(999L);
    when(this.repositorioTareaMock.buscarTodas()).thenReturn(List.of());
    conEventos(evento);

    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, lunes);

    // validacion
    assertThat(dia(mes, lunes).eventos().get(0).estado(), is(nullValue()));
  }

  @Test
  public void enUnPeriodoConVariosEventosSoloTraeEstadoElQueTieneTareaVinculada() {
    // preparacion
    LocalDate lunes = LocalDate.of(2026, 9, 28);
    Evento conTarea = evento(this.analisis, "Entrega TP", lunes.atTime(18, 0), lunes.atTime(20, 0));
    conTarea.vincularTarea(3L);
    Evento sinTarea = evento(this.fisica, "Clase común", lunes.atTime(8, 0), lunes.atTime(10, 0));
    when(this.repositorioTareaMock.buscarTodas()).thenReturn(
            List.of(tarea(3L, "COMPLETADA", lunes.minusDays(1)))
    );
    conEventos(conTarea, sinTarea);

    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, lunes);

    // validacion
    List<com.tallerwebi.dominio.calendario.SegmentoDia> eventosDelDia = dia(mes, lunes).eventos();
    assertThat(eventosDelDia.get(0).estado(), is(nullValue())); // "Clase común" (08:00) va primero por horario
    assertThat(eventosDelDia.get(1).estado(), is(EstadoActividad.COMPLETADA));
  }

  @Test
  public void elEstadoTambienSeVeEnLaVistaSemanal() {
    // preparacion
    LocalDate martes = LocalDate.of(2026, 9, 29);
    Evento evento = evento(this.analisis, "Entrega TP", martes.atTime(18, 0), martes.atTime(20, 0));
    evento.vincularTarea(7L);
    when(this.repositorioTareaMock.buscarTodas()).thenReturn(
            List.of(tarea(7L, "PENDIENTE", martes.plusDays(1)))
    );
    conEventos(evento);

    // ejecucion
    CalendarioSemanal semana = this.servicioCalendario.obtenerSemana(USUARIO, martes);

    // validacion
    assertThat(
            semana.dias().get(1).bloques().get(0).segmento().estado(),
            is(EstadoActividad.PROXIMA_A_VENCER)
    );
  }

  // ---------------------------------------------------------------- fuentes de eventos (CAL-04)

  private static EventoConEstado derivado(
          Materia materia,
          TipoEvento tipo,
          String titulo,
          LocalDateTime desde,
          EstadoActividad estado
  ) {
    return new EventoConEstado(
            new Evento(USUARIO, materia, tipo, titulo, desde, desde.plusHours(2)),
            estado
    );
  }

  private void conFuente(EventoConEstado... eventos) {
    FuenteDeEventos fuente = mock(FuenteDeEventos.class);
    when(
            fuente.eventosEntre(
                    eq(USUARIO),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    any(LocalDate.class)
            )
    ).thenReturn(List.of(eventos));
    this.fuentes.add(fuente);
    armarServicio();
  }

  @Test
  public void sinFuentesElCalendarioSoloMuestraLosEventosReales() {
    // preparacion
    LocalDate lunes = LocalDate.of(2026, 9, 28);
    conEventos(evento(this.fisica, "Clase", lunes.atTime(8, 0), lunes.atTime(10, 0)));

    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, lunes);

    // validacion
    assertThat(mes.totalEventos(), is(1));
  }

  @Test
  public void losEventosDeUnaFuenteApareceEnElCalendarioConSuEstado() {
    // preparacion
    LocalDate lunes = LocalDate.of(2026, 9, 28);
    conFuente(
            derivado(
                    this.analisis,
                    TipoEvento.PARCIAL,
                    "Primer Parcial",
                    lunes.atTime(14, 0),
                    EstadoActividad.PROXIMA_A_VENCER
            )
    );

    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, lunes);

    // validacion
    assertThat(dia(mes, lunes).eventos(), hasSize(1));
    assertThat(dia(mes, lunes).eventos().get(0).titulo(), is("Primer Parcial"));
    assertThat(dia(mes, lunes).eventos().get(0).horaInicio(), is("14:00"));
    assertThat(dia(mes, lunes).eventos().get(0).estado(), is(EstadoActividad.PROXIMA_A_VENCER));
  }

  @Test
  public void losEventosDeUnaFuenteTambienApareceEnLaVistaSemanal() {
    // preparacion
    LocalDate lunes = LocalDate.of(2026, 9, 28);
    conFuente(
            derivado(
                    this.analisis,
                    TipoEvento.TRABAJO_PRACTICO,
                    "Entrega TP",
                    lunes.plusDays(2).atTime(20, 0),
                    EstadoActividad.EN_TIEMPO
            )
    );

    // ejecucion
    CalendarioSemanal semana = this.servicioCalendario.obtenerSemana(USUARIO, lunes);

    // validacion
    assertThat(semana.dias().get(2).bloques(), hasSize(1));
    assertThat(
            semana.dias().get(2).bloques().get(0).segmento().estado(),
            is(EstadoActividad.EN_TIEMPO)
    );
    assertThat(semana.totalEventos(), is(1));
  }

  @Test
  public void variasFuentesYEventosRealesConviven() {
    // preparacion
    LocalDate lunes = LocalDate.of(2026, 9, 28);
    conEventos(evento(this.fisica, "Clase", lunes.atTime(8, 0), lunes.atTime(10, 0)));
    conFuente(derivado(this.analisis, TipoEvento.PARCIAL, "Parcial", lunes.atTime(14, 0), null));
    conFuente(
            derivado(this.analisis, TipoEvento.TRABAJO_PRACTICO, "TP", lunes.atTime(20, 0), null)
    );

    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, lunes);

    // validacion
    assertThat(dia(mes, lunes).eventos(), hasSize(3));
    assertThat(mes.totalEventos(), is(3));
  }

  @Test
  public void laMateriaDeUnEventoDeUnaFuenteSumaEnElResumenDeMaterias() {
    // preparacion
    LocalDate lunes = LocalDate.of(2026, 9, 28);
    conEventos(evento(this.analisis, "Clase", lunes.atTime(8, 0), lunes.atTime(10, 0)));
    conFuente(derivado(this.analisis, TipoEvento.PARCIAL, "Parcial", lunes.atTime(14, 0), null));

    // ejecucion
    CalendarioMensual mes = this.servicioCalendario.obtenerMes(USUARIO, lunes);

    // validacion
    assertThat(mes.materias(), hasSize(1));
    assertThat(mes.materias().get(0).cantidadEventos(), is(2));
  }

  @Test
  public void deberiaPedirleALasFuentesElMismoRangoQueAlRepositorio() {
    // preparacion
    FuenteDeEventos fuente = mock(FuenteDeEventos.class);
    when(fuente.eventosEntre(any(), any(), any(), any())).thenReturn(List.of());
    this.fuentes.add(fuente);
    armarServicio();

    // ejecucion
    this.servicioCalendario.obtenerSemana(USUARIO, LocalDate.of(2026, 9, 30));

    // validacion
    verify(fuente).eventosEntre(
            USUARIO,
            LocalDate.of(2026, 9, 28).atStartOfDay(),
            LocalDate.of(2026, 10, 5).atStartOfDay(),
            LocalDate.of(2026, 9, 28)
    );
  }
}
