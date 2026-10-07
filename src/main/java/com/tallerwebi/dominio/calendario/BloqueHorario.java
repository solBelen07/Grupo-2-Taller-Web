package com.tallerwebi.dominio.calendario;

/**
 * Bloque posicionado en la grilla horaria de la vista semanal.
 *
 * @param segmento porción del evento que se dibuja
 * @param top minutos desde el inicio de la grilla hasta el comienzo del bloque
 * @param alto duración del bloque en minutos
 * @param columna columna (0-based) que ocupa entre los bloques que se superponen
 * @param columnas cantidad de columnas del grupo de bloques superpuestos
 */
public record BloqueHorario(SegmentoDia segmento, int top, int alto, int columna, int columnas) {

}
