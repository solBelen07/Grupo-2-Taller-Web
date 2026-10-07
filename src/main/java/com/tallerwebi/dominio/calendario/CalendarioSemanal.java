package com.tallerwebi.dominio.calendario;

import java.time.LocalDate;
import java.util.List;

/**
 * Vista semanal con bloques de horas.
 *
 * @param titulo por ejemplo (28 sep – 4 oct 2026)
 * @param anterior lunes de la semana anterior (para navegar)
 * @param siguiente lunes de la semana siguiente (para navegar)
 * @param hoy fecha actual según el reloj de la aplicación
 * @param dias los 7 días de la semana, de lunes a domingo
 * @param horaInicio primera hora visible de la grilla (0 a 23)
 * @param horaFin hora en que termina la grilla (1 a 24, exclusiva)
 * @param etiquetasHoras una etiqueta por cada hora de la grilla, por ejemplo 07:00
 * @param materias materias del usuario con la cantidad de eventos que aparecen en la semana
 * @param totalEventos cantidad de eventos distintos de la semana
 */
public record CalendarioSemanal(
  String titulo,
  LocalDate anterior,
  LocalDate siguiente,
  LocalDate hoy,
  List<DiaSemanal> dias,
  int horaInicio,
  int horaFin,
  List<String> etiquetasHoras,
  List<ResumenMateria> materias,
  int totalEventos
) {
  /** Indica si no hay ningún evento en la semana. */
  public boolean vacio() {

    return totalEventos == 0;
  }
}
