package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.calendario.CalendarioMensual;
import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.calendario.DiaMensual;
import com.tallerwebi.dominio.calendario.DiaSemanal;
import com.tallerwebi.dominio.calendario.ResumenMateria;
import com.tallerwebi.dominio.materia.Materia;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCalendarioImplTest {

  private static final Long USUARIO = 1L;
  private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
  // Lunes 28/09/2026 a las 10:00 en Buenos Aires.
  private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-09-28T13:00:00Z"), ZONA);

  private RepositorioEvento repositorioEventoMock;
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
    this.analisis = materia("Análisis II", "#2F6FDE");
    this.fisica = materia("Física II", "#D98A00");
    when(
      this.repositorioEventoMock.buscarEnRango(
          eq(USUARIO),
          any(LocalDateTime.class),
          any(LocalDateTime.class)
        )
    )
      .thenReturn(List.of());
    this.servicioCalendario = new ServicioCalendarioImpl(this.repositorioEventoMock, RELOJ);
  }

  private void conEventos(Evento... eventos) {
    when(
      this.repositorioEventoMock.buscarEnRango(
          eq(USUARIO),
          any(LocalDateTime.class),
          any(LocalDateTime.class)
        )
    )
      .thenReturn(List.of(eventos));
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
    verify(this.repositorioEventoMock, times(1))
      .buscarEnRango(
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
      dia(mes, lunes).eventos().stream().map(e -> e.titulo()).toList(),
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
    CalendarioMensual diciembre =
      this.servicioCalendario.obtenerMes(USUARIO, LocalDate.of(2026, 12, 5));

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
    CalendarioSemanal semana =
      this.servicioCalendario.obtenerSemana(USUARIO, LocalDate.of(2026, 9, 15));

    // validacion
    assertThat(semana.vacio(), is(true));
    assertThat(semana.totalEventos(), is(0));
  }

  @Test
  public void deberiaArmarLaSemanaDeLunesADomingoDesdeCualquierDiaDeLaSemana() {
    // ejecucion
    CalendarioSemanal semana =
      this.servicioCalendario.obtenerSemana(USUARIO, LocalDate.of(2026, 9, 30));

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
    CalendarioSemanal semana =
      this.servicioCalendario.obtenerSemana(USUARIO, LocalDate.of(2026, 9, 30));

    // validacion
    assertThat(semana.titulo(), is("28 sep – 4 oct 2026"));
    assertThat(semana.anterior(), is(LocalDate.of(2026, 9, 21)));
    assertThat(semana.siguiente(), is(LocalDate.of(2026, 10, 5)));
    verify(this.repositorioEventoMock, times(1))
      .buscarEnRango(
        USUARIO,
        LocalDateTime.of(2026, 9, 28, 0, 0),
        LocalDateTime.of(2026, 10, 5, 0, 0)
      );
  }

  @Test
  public void deberiaUsarLaGrillaDeSieteAVeintidosHorasPorDefecto() {
    // ejecucion
    CalendarioSemanal semana =
      this.servicioCalendario.obtenerSemana(USUARIO, LocalDate.of(2026, 9, 28));

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
}
