package com.tallerwebi.dominio;

import java.time.LocalDateTime;
import java.util.List;

/** Acceso a los eventos de todas las materias de un alumno. */
public interface RepositorioEvento {
  /**
   * Devuelve los eventos que se solapan con el rango (desde, hasta), ordenados por inicio.
   */
  List<Evento> buscarEnRango(Long usuarioId, LocalDateTime desde, LocalDateTime hasta);

  /** Persiste un evento nuevo. */
  void guardar(Evento evento);
  void eliminarPorMateriaId(Integer materiaId);
}
