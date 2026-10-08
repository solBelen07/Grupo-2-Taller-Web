package com.tallerwebi.dominio.calendario;

import java.time.LocalDate;
import java.util.List;

/**
 * Vista mensual: grilla de semanas completas que cubre el mes.
 *
 * @param titulo por ejemplo septiembre de 2026
 * @param anterior primer día del mes anterior (para navegar)
 * @param siguiente primer día del mes siguiente (para navegar)
 * @param hoy fecha actual según el reloj de la aplicación
 * @param semanas filas de la grilla, de la primera a la última semana del mes
 * @param materias materias del usuario con la cantidad de eventos que aparecen en la grilla
 * @param totalEventos cantidad de eventos distintos que aparecen en la grilla
 */
public record CalendarioMensual(
  String titulo,
  LocalDate anterior,
  LocalDate siguiente,
  LocalDate hoy,
  List<SemanaMensual> semanas,
  List<ResumenMateria> materias,
  int totalEventos
) {
  /** Indica si no hay ningún evento en la grilla. */
  public boolean vacio() {
    return totalEventos == 0;
  }
}
