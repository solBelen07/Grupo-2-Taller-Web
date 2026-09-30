package com.tallerwebi.dominio.calendario;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Reparte en columnas los segmentos de un día que se superponen para que se dibujen lado a lado en
 * lugar de taparse. Dos segmentos que solo se tocan (uno termina cuando el otro empieza) no se
 * consideran superpuestos.
 */
public final class DisposicionDeBloques {

  private DisposicionDeBloques() {}

  /**
   * Calcula la posición de cada segmento.
   *
   * @param segmentos segmentos de un mismo día, en cualquier orden
   * @param minutoInicioGrilla minuto del día en que empieza la grilla visible
   */
  public static List<BloqueHorario> disponer(List<SegmentoDia> segmentos, int minutoInicioGrilla) {
    List<SegmentoDia> ordenados = new ArrayList<>(segmentos);
    ordenados.sort(
      Comparator
        .comparingInt(SegmentoDia::minutoInicio)
        .thenComparing(Comparator.comparingInt(SegmentoDia::minutoFin).reversed())
        .thenComparing(SegmentoDia::titulo)
    );

    List<BloqueHorario> resultado = new ArrayList<>();
    Grupo grupo = new Grupo();
    for (SegmentoDia segmento : ordenados) {
      if (!grupo.estaVacio() && segmento.minutoInicio() >= grupo.fin()) {
        resultado.addAll(grupo.cerrar(minutoInicioGrilla));
        grupo = new Grupo();
      }
      grupo.agregar(segmento);
    }
    resultado.addAll(grupo.cerrar(minutoInicioGrilla));
    return resultado;
  }

  /** Conjunto de segmentos conectados por superposición. */
  private static final class Grupo {

    private final List<SegmentoDia> segmentos = new ArrayList<>();
    private final List<Integer> columnasAsignadas = new ArrayList<>();
    private final List<Integer> finPorColumna = new ArrayList<>();
    private int ultimoFin = Integer.MIN_VALUE;

    boolean estaVacio() {
      return segmentos.isEmpty();
    }

    int fin() {
      return ultimoFin;
    }

    void agregar(SegmentoDia segmento) {
      int columna = primeraColumnaLibre(segmento.minutoInicio());
      if (columna == finPorColumna.size()) {
        finPorColumna.add(segmento.minutoFin());
      } else {
        finPorColumna.set(columna, segmento.minutoFin());
      }
      segmentos.add(segmento);
      columnasAsignadas.add(columna);
      ultimoFin = Math.max(ultimoFin, segmento.minutoFin());
    }

    List<BloqueHorario> cerrar(int minutoInicioGrilla) {
      List<BloqueHorario> bloques = new ArrayList<>();
      for (int i = 0; i < segmentos.size(); i++) {
        SegmentoDia segmento = segmentos.get(i);
        bloques.add(
          new BloqueHorario(
            segmento,
            segmento.minutoInicio() - minutoInicioGrilla,
            segmento.duracion(),
            columnasAsignadas.get(i),
            finPorColumna.size()
          )
        );
      }
      return bloques;
    }

    private int primeraColumnaLibre(int minutoInicio) {
      for (int columna = 0; columna < finPorColumna.size(); columna++) {
        if (finPorColumna.get(columna) <= minutoInicio) {
          return columna;
        }
      }
      return finPorColumna.size();
    }
  }
}
