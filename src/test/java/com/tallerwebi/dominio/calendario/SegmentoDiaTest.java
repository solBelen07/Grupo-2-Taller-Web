package com.tallerwebi.dominio.calendario;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.tallerwebi.dominio.Evento;
import com.tallerwebi.dominio.TipoEvento;
import com.tallerwebi.dominio.materia.Materia;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class SegmentoDiaTest {

  private static final Long USUARIO = 1L;
  private static final LocalDate LUNES = LocalDate.of(2026, 9, 28);

  private static Materia materia() {
    Materia materia = new Materia();
    materia.setNombre("Análisis II");
    materia.setColor("#2F6FDE");
    return materia;
  }

  private static Evento evento(LocalDateTime inicio, LocalDateTime fin) {
    return new Evento(USUARIO, materia(), TipoEvento.CLASE, "Teoría", inicio, fin);
  }

  @Test
  public void deberiaRecortarUnEventoQueEstaDentroDelDia() {
    // preparacion
    Evento evento = evento(LUNES.atTime(18, 30), LUNES.atTime(20, 0));

    // ejecucion
    SegmentoDia segmento = SegmentoDia.recortar(evento, LUNES).orElseThrow();

    // validacion
    assertThat(segmento.minutoInicio(), is(18 * 60 + 30));
    assertThat(segmento.minutoFin(), is(20 * 60));
    assertThat(segmento.duracion(), is(90));
    assertThat(segmento.continuaDesdeAntes(), is(false));
    assertThat(segmento.continuaDespues(), is(false));
    assertThat(segmento.materia(), is("Análisis II"));
    assertThat(segmento.color(), is("#2F6FDE"));
  }

  @Test
  public void deberiaDevolverVacioSiElEventoEsDeOtroDia() {
    // preparacion
    Evento evento = evento(LUNES.atTime(18, 0), LUNES.atTime(20, 0));

    // ejecucion y validacion
    assertThat(SegmentoDia.recortar(evento, LUNES.plusDays(1)), is(Optional.empty()));
    assertThat(SegmentoDia.recortar(evento, LUNES.minusDays(1)), is(Optional.empty()));
  }

  @Test
  public void deberiaPartirUnEventoQueCruzaLaMedianoche() {
    // preparacion
    Evento evento = evento(LUNES.atTime(22, 0), LUNES.plusDays(1).atTime(1, 0));

    // ejecucion
    SegmentoDia primero = SegmentoDia.recortar(evento, LUNES).orElseThrow();
    SegmentoDia segundo = SegmentoDia.recortar(evento, LUNES.plusDays(1)).orElseThrow();

    // validacion
    assertThat(primero.minutoInicio(), is(22 * 60));
    assertThat(primero.minutoFin(), is(24 * 60));
    assertThat(primero.continuaDespues(), is(true));
    assertThat(primero.continuaDesdeAntes(), is(false));
    assertThat(segundo.minutoInicio(), is(0));
    assertThat(segundo.minutoFin(), is(60));
    assertThat(segundo.continuaDesdeAntes(), is(true));
    assertThat(segundo.continuaDespues(), is(false));
  }

  @Test
  public void noDeberiaGenerarSegmentoSiElEventoTerminaJustoALaMedianoche() {
    // preparacion
    Evento evento = evento(LUNES.atTime(22, 0), LUNES.plusDays(1).atStartOfDay());

    // ejecucion y validacion
    assertThat(SegmentoDia.recortar(evento, LUNES).isPresent(), is(true));
    assertThat(SegmentoDia.recortar(evento, LUNES.plusDays(1)).isPresent(), is(false));
  }

  @Test
  public void deberiaCubrirLosDiasDeMedioDeUnEventoMultidia() {
    // preparacion
    Evento evento = evento(LUNES.atTime(10, 0), LUNES.plusDays(2).atTime(12, 0));

    // ejecucion
    SegmentoDia enMedio = SegmentoDia.recortar(evento, LUNES.plusDays(1)).orElseThrow();

    // validacion
    assertThat(enMedio.minutoInicio(), is(0));
    assertThat(enMedio.minutoFin(), is(24 * 60));
    assertThat(enMedio.continuaDesdeAntes(), is(true));
    assertThat(enMedio.continuaDespues(), is(true));
  }

  @Test
  public void deberiaFormatearElHorarioYLaDescripcion() {
    // preparacion
    Evento evento = evento(LUNES.atTime(9, 5), LUNES.atTime(11, 0));

    // ejecucion
    SegmentoDia segmento = SegmentoDia.recortar(evento, LUNES).orElseThrow();

    // validacion
    assertThat(segmento.horaInicio(), is("09:05"));
    assertThat(segmento.horaFin(), is("11:00"));
    assertThat(segmento.horario(), is("09:05 – 11:00"));
    assertThat(segmento.descripcion(), is("Clase · Análisis II · Teoría · 09:05 – 11:00"));
  }

  @Test
  public void deberiaMostrarLaMedianocheFinalComoCeroHoras() {
    // preparacion
    Evento evento = evento(LUNES.atTime(23, 0), LUNES.plusDays(1).atStartOfDay());

    // ejecucion
    SegmentoDia segmento = SegmentoDia.recortar(evento, LUNES).orElseThrow();

    // validacion
    assertThat(segmento.horaFin(), is("00:00"));
  }
}
