package com.tallerwebi.dominio.calendario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Algo que sabe aportar eventos al calendario (Parciales, Trabajos Prácticos, y lo que se sume
 * mañana). El servicio del calendario recibe todas las fuentes y no sabe cuáles son: sumar una
 * nueva es escribir una clase que implemente esta interfaz, sin tocar el servicio.
 *
 */

@FunctionalInterface
public interface FuenteDeEventos {
  /** Eventos que se solapan con el período (desde, hasta) */
  List<EventoConEstado> eventosEntre(
    Long usuarioId,
    LocalDateTime desde,
    LocalDateTime hasta,
    LocalDate hoy
  );
}
