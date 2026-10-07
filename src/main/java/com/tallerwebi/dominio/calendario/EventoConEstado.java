package com.tallerwebi.dominio.calendario;

import com.tallerwebi.dominio.Evento;

/**
 * Un {@code Evento} del calendario junto con su estado de seguimiento.
 *
 * @param evento el evento a mostrar
 * @param estado su estado de seguimiento, o null si no tiene
 */
public record EventoConEstado(Evento evento, EstadoActividad estado) {}
