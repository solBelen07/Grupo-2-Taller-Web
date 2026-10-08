package com.tallerwebi.dominio.calendario;

/**
 * Materia de la leyenda del calendario, con la cantidad de eventos del período mostrado.
 *
 * @param nombre nombre de la materia
 * @param color color de la materia en formato
 * @param cantidadEventos cantidad de eventos de la materia en el período mostrado
 */
public record ResumenMateria(String nombre, String color, int cantidadEventos) {}
