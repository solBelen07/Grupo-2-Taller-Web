package com.tallerwebi.dominio.calendario;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Evento;
import com.tallerwebi.dominio.TipoEvento;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.parcial.Parcial;
import com.tallerwebi.dominio.parcial.RepositorioParcial;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FuenteDeParcialesTest {

  private static final Long USUARIO = 1L;
  private static final LocalDate HOY = LocalDate.of(2026, 9, 28);
  private static final LocalDateTime DESDE = LocalDate.of(2026, 9, 28).atStartOfDay();
  private static final LocalDateTime HASTA = LocalDate.of(2026, 10, 5).atStartOfDay();

  private RepositorioParcial repositorioParcialMock;
  private FuenteDeParciales fuente;
  private Materia analisis;

  @BeforeEach
  public void init() {
    this.repositorioParcialMock = mock(RepositorioParcial.class);
    this.fuente = new FuenteDeParciales(this.repositorioParcialMock);
    this.analisis = new Materia();
    this.analisis.setNombre("Análisis II");
    this.analisis.setColor("#2F6FDE");
  }

  private Parcial parcial(LocalDate fecha, LocalTime horario, String nombre) {
    Parcial parcial = new Parcial();
    parcial.setMateria(this.analisis);
    parcial.setFecha(fecha);
    parcial.setHorario(horario);
    parcial.setSesiones(List.of());
    parcial.setNombre(nombre);
    return parcial;
  }

  private List<EventoConEstado> traer(Parcial... parciales) {
    when(this.repositorioParcialMock.obtenerTodos()).thenReturn(List.of(parciales));
    return this.fuente.eventosEntre(USUARIO, DESDE, HASTA, HOY);
  }

  @Test
  public void unParcialSeConvierteEnUnEventoDeDosHoras() {
    // ejecucion
    List<EventoConEstado> eventos = traer(parcial(HOY, LocalTime.of(14, 0), null));

    // validacion
    assertThat(eventos, hasSize(1));
    Evento evento = eventos.get(0).evento();
    assertThat(evento.getTipo(), is(TipoEvento.PARCIAL));
    assertThat(evento.getMateria(), is(this.analisis));
    assertThat(evento.getUsuarioId(), is(USUARIO));
    assertThat(evento.getInicio(), is(HOY.atTime(14, 0)));
    assertThat(evento.getFin(), is(HOY.atTime(16, 0)));
  }

  @Test
  public void sinNombreUsaElTituloGenerico() {
    assertThat(
      traer(parcial(HOY, LocalTime.of(14, 0), null)).get(0).evento().getTitulo(),
      is("Parcial de Análisis II")
    );
  }

  @Test
  public void conNombreEnBlancoUsaElTituloGenerico() {
    assertThat(
      traer(parcial(HOY, LocalTime.of(14, 0), "   ")).get(0).evento().getTitulo(),
      is("Parcial de Análisis II")
    );
  }

  @Test
  public void conNombreCargadoUsaEseNombre() {
    assertThat(
      traer(parcial(HOY, LocalTime.of(14, 0), "Primer Parcial")).get(0).evento().getTitulo(),
      is("Primer Parcial")
    );
  }

  @Test
  public void unParcialAnteriorAlPeriodoNoSeIncluye() {
    assertThat(traer(parcial(HOY.minusMonths(2), LocalTime.of(14, 0), null)), empty());
  }

  @Test
  public void unParcialPosteriorAlPeriodoNoSeIncluye() {
    assertThat(traer(parcial(HOY.plusMonths(2), LocalTime.of(14, 0), null)), empty());
  }

  @Test
  public void unParcialQueEmpiezaJustoCuandoTerminaElPeriodoNoSeIncluye() {
    assertThat(traer(parcial(HASTA.toLocalDate(), LocalTime.MIDNIGHT, null)), empty());
  }

  @Test
  public void unParcialNocturnoQueCruzaElInicioDelPeriodoSeIncluye() {
    // empieza 23:00 del día anterior y termina 01:00 del primer día
    assertThat(traer(parcial(HOY.minusDays(1), LocalTime.of(23, 0), null)), hasSize(1));
  }

  @Test
  public void unParcialSinFechaOSinHorarioSeIgnora() {
    assertThat(traer(parcial(null, LocalTime.of(14, 0), null), parcial(HOY, null, null)), empty());
  }

  @Test
  public void elEstadoSeCalculaConLasSesionesDelParcial() {
    // sin sesiones completadas y con 6 días por delante -> En tiempo
    assertThat(
      traer(parcial(HOY.plusDays(6), LocalTime.of(14, 0), null)).get(0).estado(),
      is(EstadoActividad.EN_TIEMPO)
    );
  }

  @Test
  public void unParcialQueEsHoyEstaProximoAVencer() {
    assertThat(
      traer(parcial(HOY, LocalTime.of(14, 0), null)).get(0).estado(),
      is(EstadoActividad.PROXIMA_A_VENCER)
    );
  }

  private static final LocalDate HOY_ESTADOS = LocalDate.of(2026, 9, 30);

  private static SesionEstudio sesion(boolean completada) {
    SesionEstudio sesion = new SesionEstudio();
    sesion.setCompletada(completada);
    return sesion;
  }

  private static Parcial parcialConSesiones(LocalDate fecha, Boolean... sesionesCompletadas) {
    Parcial parcial = new Parcial();
    parcial.setFecha(fecha);
    List<SesionEstudio> sesiones = new ArrayList<>();
    for (Boolean completada : sesionesCompletadas) {
      sesiones.add(sesion(completada));
    }
    parcial.setSesiones(sesiones);
    return parcial;
  }

  @Test
  public void conTodasLasSesionesCompletadasDaCompletada() {
    // preparacion
    Parcial parcial = parcialConSesiones(HOY_ESTADOS.plusDays(30), true, true);

    // ejecucion y validacion
    assertThat(FuenteDeParciales.estadoDe(parcial, HOY_ESTADOS), is(EstadoActividad.COMPLETADA));
  }

  @Test
  public void laCompletadaGanaAunqueLaFechaYaHayaVencido() {
    // preparacion
    Parcial parcial = parcialConSesiones(HOY_ESTADOS.minusDays(5), true, true);

    // ejecucion y validacion
    assertThat(FuenteDeParciales.estadoDe(parcial, HOY_ESTADOS), is(EstadoActividad.COMPLETADA));
  }

  @Test
  public void fechaVencidaYNoCompletadaDaVencida() {
    // preparacion
    Parcial parcial = parcialConSesiones(HOY_ESTADOS.minusDays(1), false, false);

    // ejecucion y validacion
    assertThat(FuenteDeParciales.estadoDe(parcial, HOY_ESTADOS), is(EstadoActividad.VENCIDA));
  }

  @Test
  public void laVencidaGanaAunqueTengaProgresoParcial() {
    // preparacion: 50% de sesiones, pero ya venció.
    Parcial parcial = parcialConSesiones(HOY_ESTADOS.minusDays(1), true, false);

    // ejecucion y validacion
    assertThat(FuenteDeParciales.estadoDe(parcial, HOY_ESTADOS), is(EstadoActividad.VENCIDA));
  }

  @Test
  public void venceEnTresDiasEstaProximaAVencer() {
    // preparacion
    Parcial parcial = parcialConSesiones(HOY_ESTADOS.plusDays(3), false);

    // ejecucion y validacion
    assertThat(
      FuenteDeParciales.estadoDe(parcial, HOY_ESTADOS),
      is(EstadoActividad.PROXIMA_A_VENCER)
    );
  }

  @Test
  public void venceEnCuatroDiasNoEstaProximaAVencerTodavia() {
    // preparacion
    Parcial parcial = parcialConSesiones(HOY_ESTADOS.plusDays(4), false);

    // ejecucion y validacion
    assertThat(FuenteDeParciales.estadoDe(parcial, HOY_ESTADOS), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void laUrgenciaPorFechaGanaAunqueTengaBuenProgreso() {
    // preparacion: 50% de sesiones, pero vence en 2 días.
    Parcial parcial = parcialConSesiones(HOY_ESTADOS.plusDays(2), true, false);

    // ejecucion y validacion
    assertThat(
      FuenteDeParciales.estadoDe(parcial, HOY_ESTADOS),
      is(EstadoActividad.PROXIMA_A_VENCER)
    );
  }

  @Test
  public void conCincuentaPorCientoDeSesionesYTiempoDeSobraDaParcialmenteCompletada() {
    // preparacion
    Parcial parcial = parcialConSesiones(HOY_ESTADOS.plusDays(30), true, false);

    // ejecucion y validacion
    assertThat(
      FuenteDeParciales.estadoDe(parcial, HOY_ESTADOS),
      is(EstadoActividad.PARCIALMENTE_COMPLETADA)
    );
  }

  @Test
  public void conMenosDeCincuentaPorCientoDeSesionesDaEnTiempo() {
    // preparacion: 1 de 3 sesiones, 33% (truncado por Parcial.getPorcentajeCompletado()).
    Parcial parcial = parcialConSesiones(HOY_ESTADOS.plusDays(30), true, false, false);

    // ejecucion y validacion
    assertThat(FuenteDeParciales.estadoDe(parcial, HOY_ESTADOS), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void sinNingunaSesionCargadaDaEnTiempo() {
    // preparacion
    Parcial parcial = parcialConSesiones(HOY_ESTADOS.plusDays(30));

    // ejecucion y validacion
    assertThat(FuenteDeParciales.estadoDe(parcial, HOY_ESTADOS), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void sinFechaCargadaYSinProgresoDaEnTiempo() {
    // preparacion
    Parcial parcial = parcialConSesiones(null, false);

    // ejecucion y validacion
    assertThat(FuenteDeParciales.estadoDe(parcial, HOY_ESTADOS), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void siElPorcentajeCompletadoDeParcialVieneNuloNoExplota() {
    // preparacion: caso defensivo, por si getPorcentajeCompletado() alguna vez devolviera null.
    Parcial parcial = mock(Parcial.class);
    when(parcial.getPorcentajeCompletado()).thenReturn(null);
    when(parcial.getFecha()).thenReturn(HOY_ESTADOS.plusDays(30));

    // ejecucion y validacion
    assertThat(FuenteDeParciales.estadoDe(parcial, HOY_ESTADOS), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void sinFechaCargadaPeroConProgresoDaParcialmenteCompletada() {
    // preparacion
    Parcial parcial = parcialConSesiones(null, true, false);

    // ejecucion y validacion
    assertThat(
      FuenteDeParciales.estadoDe(parcial, HOY_ESTADOS),
      is(EstadoActividad.PARCIALMENTE_COMPLETADA)
    );
  }
}
