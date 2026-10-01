package com.tallerwebi.dominio.calendario;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

public class TextosCalendarioTest {

  @Test
  public void deberiaArmarElTituloDelMes() {
    // ejecucion y validacion
    assertThat(TextosCalendario.tituloMes(YearMonth.of(2026, 9)), is("septiembre de 2026"));
  }

  @Test
  public void deberiaArmarElTituloDeUnaSemanaDentroDelMismoAnio() {
    // ejecucion y validacion
    assertThat(TextosCalendario.tituloSemana(LocalDate.of(2026, 9, 28)), is("28 sep – 4 oct 2026"));
  }

  @Test
  public void deberiaIncluirAmbosAniosSiLaSemanaCruzaDeAnio() {
    // ejecucion y validacion
    assertThat(
      TextosCalendario.tituloSemana(LocalDate.of(2026, 12, 28)),
      is("28 dic 2026 – 3 ene 2027")
    );
  }

  @Test
  public void deberiaDevolverNombresDeDiasEnEspaniol() {
    // ejecucion y validacion
    assertThat(TextosCalendario.nombreDia(DayOfWeek.MONDAY), is("Lunes"));
    assertThat(TextosCalendario.nombreDia(DayOfWeek.WEDNESDAY), is("Miércoles"));
    assertThat(TextosCalendario.abreviaturaDia(DayOfWeek.SATURDAY), is("Sáb"));
  }
}
