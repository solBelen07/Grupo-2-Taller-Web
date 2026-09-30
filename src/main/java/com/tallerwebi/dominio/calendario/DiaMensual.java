package com.tallerwebi.dominio.calendario;

import java.time.LocalDate;
import java.util.List;

/**
 * Celda de un día en la vista mensual.
 *
 * @param fecha fecha de la celda
 * @param delMesActual si la fecha pertenece al mes que se está mostrando
 * @param esHoy si la fecha es la de hoy
 * @param eventos segmentos de eventos que caen en este día
 */
public record DiaMensual(
  LocalDate fecha,
  boolean delMesActual,
  boolean esHoy,
  List<SegmentoDia> eventos
) {}
