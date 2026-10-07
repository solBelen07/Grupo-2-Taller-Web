package com.tallerwebi.dominio.calendario;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.tallerwebi.dominio.tarea.Tarea;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class CalculadorEstadoActividadTest {

  private static final LocalDate HOY = LocalDate.of(2026, 9, 30);

  private static Tarea tarea(
    String estado,
    Integer horasRealizadas,
    Integer horasPlanificadas,
    LocalDate fechaVencimiento
  ) {
    Tarea tarea = new Tarea();
    tarea.setEstado(estado);
    tarea.setHorasRealizadas(horasRealizadas);
    tarea.setHorasPlanificadas(horasPlanificadas);
    tarea.setFechaVencimiento(fechaVencimiento);
    return tarea;
  }

  // completada

  @Test
  public void marcadaComoCompletadaDaEstadoCompletada() {
    // preparacion
    Tarea tarea = tarea("COMPLETADA", 3, 10, HOY.plusDays(30));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.COMPLETADA));
  }

  @Test
  public void laCompletadaGanaAunqueLaFechaYaHayaVencido() {
    // preparacion
    Tarea tarea = tarea("COMPLETADA", 10, 10, HOY.minusDays(5));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.COMPLETADA));
  }

  @Test
  public void cienPorCientoDeHorasSinMarcarCompletadaNoEsCompletada() {
    // preparacion: hizo el 100% de las horas pero no lo marcó como completada.
    Tarea tarea = tarea("PENDIENTE", 10, 10, HOY.plusDays(30));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  // Vencida

  @Test
  public void fechaVencidaYNoCompletadaDaVencida() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", 0, 10, HOY.minusDays(1));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.VENCIDA));
  }

  @Test
  public void laVencidaGanaAunqueTengaProgresoParcial() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", 6, 10, HOY.minusDays(1));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.VENCIDA));
  }

  // Próxima a vencer

  @Test
  public void venceEnTresDiasEstaProximaAVencer() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", 0, 10, HOY.plusDays(3));

    // ejecucion y validacion
    assertThat(
      CalculadorEstadoActividad.calcular(tarea, HOY),
      is(EstadoActividad.PROXIMA_A_VENCER)
    );
  }

  @Test
  public void veceHoyEstaProximaAVencer() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", 0, 10, HOY);

    // ejecucion y validacion
    assertThat(
      CalculadorEstadoActividad.calcular(tarea, HOY),
      is(EstadoActividad.PROXIMA_A_VENCER)
    );
  }

  @Test
  public void laUrgenciaPorFechaGanaAunqueTengaBuenProgreso() {
    // preparacion: 60% de avance, pero vence en 2 días — la urgencia pesa más.
    Tarea tarea = tarea("PENDIENTE", 6, 10, HOY.plusDays(2));

    // ejecucion y validacion
    assertThat(
      CalculadorEstadoActividad.calcular(tarea, HOY),
      is(EstadoActividad.PROXIMA_A_VENCER)
    );
  }

  @Test
  public void laUrgenciaPorFechaGanaAunqueNoTengaNingunProgreso() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", 0, 10, HOY.plusDays(2));

    // ejecucion y validacion
    assertThat(
      CalculadorEstadoActividad.calcular(tarea, HOY),
      is(EstadoActividad.PROXIMA_A_VENCER)
    );
  }

  @Test
  public void venceEnCuatroDiasNoEstaProximaAVencerTodavia() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", 0, 10, HOY.plusDays(4));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  //  Parcialmente completada

  @Test
  public void conProgresoIntermedioYTiempoDeSobraDaParcialmenteCompletada() {
    // preparacion: 60% de avance, vence en 30 días.
    Tarea tarea = tarea("PENDIENTE", 6, 10, HOY.plusDays(30));

    // ejecucion y validacion
    assertThat(
      CalculadorEstadoActividad.calcular(tarea, HOY),
      is(EstadoActividad.PARCIALMENTE_COMPLETADA)
    );
  }

  @Test
  public void conCincuentaPorCientoExactoYaEsParcialmenteCompletada() {
    // preparacion: el umbral es a partir de 50%, inclusive.
    Tarea tarea = tarea("PENDIENTE", 50, 100, HOY.plusDays(30));

    // ejecucion y validacion
    assertThat(
      CalculadorEstadoActividad.calcular(tarea, HOY),
      is(EstadoActividad.PARCIALMENTE_COMPLETADA)
    );
  }


  @Test
  public void conMenosDeCincuentaPorCientoTodaviaEsEnTiempo() {
    // preparacion: 49%, justo por debajo del umbral.
    Tarea tarea = tarea("PENDIENTE", 49, 100, HOY.plusDays(30));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void conNoventaYNuevePorCientoTodaviaEsParcialmenteCompletada() {
    // preparacion: no llega al 100%, sigue siendo "parcial".
    Tarea tarea = tarea("PENDIENTE", 99, 100, HOY.plusDays(30));

    // ejecucion y validacion
    assertThat(
      CalculadorEstadoActividad.calcular(tarea, HOY),
      is(EstadoActividad.PARCIALMENTE_COMPLETADA)
    );
  }

  @Test
  public void sinFechaDeVencimientoPeroConProgresoDaParcialmenteCompletada() {
    // preparacion: sin fecha cargada, el progreso igual cuenta.
    Tarea tarea = tarea("PENDIENTE", 6, 10, null);

    // ejecucion y validacion
    assertThat(
      CalculadorEstadoActividad.calcular(tarea, HOY),
      is(EstadoActividad.PARCIALMENTE_COMPLETADA)
    );
  }

  // En tiempo

  @Test
  public void sinProgresoYConTiempoDeSobraDaEnTiempo() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", 0, 10, HOY.plusDays(30));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void sinFechaNiHorasCargadasDaEnTiempo() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", null, null, null);

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void sinHorasPlanificadasCargadasDaEnTiempo() {
    // preparacion: no se puede calcular porcentaje sin saber cuánto planificó.
    Tarea tarea = tarea("PENDIENTE", 5, null, HOY.plusDays(30));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void conHorasPlanificadasEnCeroDaEnTiempo() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", 5, 0, HOY.plusDays(30));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void sinHorasRealizadasCargadasDaEnTiempo() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", null, 10, HOY.plusDays(30));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void conHorasRealizadasNegativasDaEnTiempo() {
    // preparacion: dato corrupto/negativo, no debería contar como progreso.
    Tarea tarea = tarea("PENDIENTE", -1, 10, HOY.plusDays(30));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  // El tipo ya no importa

  @Test
  public void elResultadoEsElMismoSeaCualSeaElTipo() {
    // preparacion: mismos datos, tres tipos distintos.
    Tarea comoTp = tarea("PENDIENTE", 6, 10, HOY.plusDays(30));
    comoTp.setTipo("TP");
    Tarea comoParcial = tarea("PENDIENTE", 6, 10, HOY.plusDays(30));
    comoParcial.setTipo("PARCIAL");
    Tarea sinTipo = tarea("PENDIENTE", 6, 10, HOY.plusDays(30));
    sinTipo.setTipo(null);

    // ejecucion y validacion: los tres dan exactamente lo mismo.
    assertThat(
      CalculadorEstadoActividad.calcular(comoTp, HOY),
      is(EstadoActividad.PARCIALMENTE_COMPLETADA)
    );
    assertThat(
      CalculadorEstadoActividad.calcular(comoParcial, HOY),
      is(EstadoActividad.PARCIALMENTE_COMPLETADA)
    );
    assertThat(
      CalculadorEstadoActividad.calcular(sinTipo, HOY),
      is(EstadoActividad.PARCIALMENTE_COMPLETADA)
    );
  }
}
