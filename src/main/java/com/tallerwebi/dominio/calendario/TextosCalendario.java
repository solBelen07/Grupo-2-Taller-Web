package com.tallerwebi.dominio.calendario;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

/** Textos en español para el calendario. */
public final class TextosCalendario {

  private static final String[] MESES = {
    "enero",
    "febrero",
    "marzo",
    "abril",
    "mayo",
    "junio",
    "julio",
    "agosto",
    "septiembre",
    "octubre",
    "noviembre",
    "diciembre",
  };
  private static final String[] DIAS = {
    "Lunes",
    "Martes",
    "Miércoles",
    "Jueves",
    "Viernes",
    "Sábado",
    "Domingo",
  };
  private static final int LETRAS_ABREVIATURA = 3;

  private TextosCalendario() {}

  /** Nombre del mes en minúsculas, por ejemplo {@code septiembre}. */
  public static String nombreMes(Month mes) {
    return MESES[mes.getValue() - 1];
  }

  /** Nombre del día, por ejemplo {@code Miércoles}. */
  public static String nombreDia(DayOfWeek dia) {
    return DIAS[dia.getValue() - 1];
  }

  /** Abreviatura de tres letras, por ejemplo {@code Mié}. */
  public static String abreviaturaDia(DayOfWeek dia) {
    return nombreDia(dia).substring(0, LETRAS_ABREVIATURA);
  }

  /** Por ejemplo {@code septiembre de 2026}. */
  public static String tituloMes(YearMonth mes) {
    return nombreMes(mes.getMonth()) + " de " + mes.getYear();
  }

  /** Por ejemplo {@code 28 sep – 4 oct 2026}, o con ambos años si la semana cruza de año. */
  public static String tituloSemana(LocalDate lunes) {
    LocalDate domingo = lunes.plusDays(6);
    String inicio = lunes.getDayOfMonth() + " " + abreviaturaMes(lunes.getMonth());
    String fin = domingo.getDayOfMonth() + " " + abreviaturaMes(domingo.getMonth());
    if (lunes.getYear() == domingo.getYear()) {
      return inicio + " – " + fin + " " + domingo.getYear();
    }
    return inicio + " " + lunes.getYear() + " – " + fin + " " + domingo.getYear();
  }

  private static String abreviaturaMes(Month mes) {
    return nombreMes(mes).substring(0, LETRAS_ABREVIATURA);
  }
}
