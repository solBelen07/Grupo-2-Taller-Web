const MINUTOS_POR_HORA = 60;
const HORA_DE_LECTURA_INICIAL = 8;
const MARGEN_SUPERIOR_EN_MINUTOS = 90;

export function calcularMinutosDesdeMedianoche(fecha) {
  return fecha.getHours() * MINUTOS_POR_HORA + fecha.getMinutes();
}

/**
 * Minutos desde el inicio de la grilla hasta "ahora", o null si la hora actual
 * cae fuera del rango visible de la grilla semanal.
 */
export function calcularTopDeLineaAhora(minutosDesdeMedianoche, horaInicioGrilla, horas) {
  const top = minutosDesdeMedianoche - horaInicioGrilla * MINUTOS_POR_HORA;
  const visible = top >= 0 && top <= horas * MINUTOS_POR_HORA;
  return visible ? top : null;
}

/** A dónde scrollear la semana: cerca de "ahora", o de las 8:00 si "ahora" no es visible. */
export function calcularScrollObjetivo(topLineaAhora, horaInicioGrilla) {
  const objetivo =
    topLineaAhora === null
      ? (HORA_DE_LECTURA_INICIAL - horaInicioGrilla) * MINUTOS_POR_HORA
      : topLineaAhora;
  return Math.max(0, objetivo - MARGEN_SUPERIOR_EN_MINUTOS);
}
