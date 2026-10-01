package com.tallerwebi.dominio.calendario;

import java.time.LocalDate;
import java.util.List;

/**
 * Fila de la vista mensual: una semana ISO completa (lunes a domingo).
 *
 * @param numero número de semana ISO
 * @param lunes fecha del lunes con el que arranca la semana
 * @param dias los 7 días de la semana, de lunes a domingo
 */
public record SemanaMensual(int numero, LocalDate lunes, List<DiaMensual> dias) {}
