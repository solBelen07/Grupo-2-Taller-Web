package com.tallerwebi.dominio.calendario;

import java.time.LocalDate;
import java.util.List;

/**
 * Columna de un día en la vista semanal.
 *
 * @param fecha fecha de la columna
 * @param nombre nombre del día, por ejemplo {@code Lunes}
 * @param abreviatura abreviatura de tres letras, por ejemplo {@code Lun}
 * @param esHoy si la fecha es la de hoy
 * @param bloques bloques de eventos posicionados en la grilla horaria de este día
 */
public record DiaSemanal(
  LocalDate fecha,
  String nombre,
  String abreviatura,
  boolean esHoy,
  List<BloqueHorario> bloques
) {}
