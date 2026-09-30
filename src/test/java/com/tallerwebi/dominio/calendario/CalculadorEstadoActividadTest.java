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
    return tarea(estado, null, horasRealizadas, horasPlanificadas, fechaVencimiento);
  }

  private static Tarea tarea(
    String estado,
    String tipo,
    Integer horasRealizadas,
    Integer horasPlanificadas,
    LocalDate fechaVencimiento
  ) {
    Tarea tarea = new Tarea();
    tarea.setEstado(estado);
    tarea.setTipo(tipo);
    tarea.setHorasRealizadas(horasRealizadas);
    tarea.setHorasPlanificadas(horasPlanificadas);
    tarea.setFechaVencimiento(fechaVencimiento);
    return tarea;
  }

  @Test
  public void unaTareaCompletadaDaEstadoCompletada() {
    // preparacion
    Tarea tarea = tarea("COMPLETADA", 10, 10, HOY.minusDays(5));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.COMPLETADA));
  }

  @Test
  public void unaTareaConFechaVencidaYNoCompletadaDaEstadoVencida() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", 0, 10, HOY.minusDays(1));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.VENCIDA));
  }

  @Test
  public void laCompletadaGanaAunqueLaFechaYaHayaVencido() {
    // preparacion
    Tarea tarea = tarea("COMPLETADA", 10, 10, HOY.minusDays(1));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.COMPLETADA));
  }

  @Test
  public void unaTareaQueVenceEnTresDiasEstaProximaAVencer() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", 0, 10, HOY.plusDays(3));

    // ejecucion y validacion
    assertThat(
      CalculadorEstadoActividad.calcular(tarea, HOY),
      is(EstadoActividad.PROXIMA_A_VENCER)
    );
  }

  @Test
  public void unaTareaQueVenceHoyEstaProximaAVencer() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", 0, 10, HOY);

    // ejecucion y validacion
    assertThat(
      CalculadorEstadoActividad.calcular(tarea, HOY),
      is(EstadoActividad.PROXIMA_A_VENCER)
    );
  }

  @Test
  public void unaTareaQueVenceEnCuatroDiasNoEstaProximaAVencerTodavia() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", 0, 10, HOY.plusDays(4));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void laVencidaGanaAunqueTengaHorasParciales() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", "PARCIAL", 4, 10, HOY.minusDays(1));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.VENCIDA));
  }

  @Test
  public void unParcialConHorasIntermediasYSinUrgenciaDeFechaDaParcialmenteCompletada() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", "PARCIAL", 4, 10, HOY.plusDays(20));

    // ejecucion y validacion
    assertThat(
      CalculadorEstadoActividad.calcular(tarea, HOY),
      is(EstadoActividad.PARCIALMENTE_COMPLETADA)
    );
  }

  @Test
  public void laComparacionDeTipoEsInsensibleAMayusculas() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", "parcial", 4, 10, HOY.plusDays(20));

    // ejecucion y validacion
    assertThat(
      CalculadorEstadoActividad.calcular(tarea, HOY),
      is(EstadoActividad.PARCIALMENTE_COMPLETADA)
    );
  }

  @Test
  public void unTpConLasMismasHorasIntermediasDaEnTiempoIgualQueEnTareas() {
    // preparacion: mismo escenario que un Parcial "parcialmente completado", pero de tipo TP.
    // /tareas no mira las horas para un TP, así que el calendario tampoco debería hacerlo.
    Tarea tarea = tarea("PENDIENTE", "TP", 4, 10, HOY.plusDays(20));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void unaTareaSinTipoConHorasParcialesNoEsParcialmenteCompletada() {
    // preparacion: sin tipo cargado, no se puede asumir que es un Parcial.
    Tarea tarea = tarea("PENDIENTE", 4, 10, HOY.plusDays(20));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void sinNingunaHoraRealizadaNoEsParcialmenteCompletada() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", "PARCIAL", 0, 10, HOY.plusDays(20));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void conTodasLasHorasRealizadasPeroSinMarcarComoCompletadaNoEsParcial() {
    // preparacion: hizo el 100% de las horas pero no la marcó como completada.
    Tarea tarea = tarea("PENDIENTE", "PARCIAL", 10, 10, HOY.plusDays(20));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void sinHorasRealizadasCargadasNoEsParcialmenteCompletada() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", "PARCIAL", null, 10, HOY.plusDays(20));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void sinHorasPlanificadasCargadasNoEsParcialmenteCompletada() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", "PARCIAL", 4, null, HOY.plusDays(20));

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void sinFechaDeVencimientoYSinHorasDaEnTiempo() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", null, null, null);

    // ejecucion y validacion
    assertThat(CalculadorEstadoActividad.calcular(tarea, HOY), is(EstadoActividad.EN_TIEMPO));
  }

  @Test
  public void sinFechaDeVencimientoPeroConHorasParcialesDeUnParcialDaParcialmenteCompletada() {
    // preparacion
    Tarea tarea = tarea("PENDIENTE", "PARCIAL", 4, 10, null);

    // ejecucion y validacion
    assertThat(
      CalculadorEstadoActividad.calcular(tarea, HOY),
      is(EstadoActividad.PARCIALMENTE_COMPLETADA)
    );
  }
}
