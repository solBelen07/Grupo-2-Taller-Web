import {
  calcularMinutosDesdeMedianoche,
  calcularScrollObjetivo,
  calcularTopDeLineaAhora,
} from "./calendario_funciones.js";

const UN_MINUTO_EN_MS = 60000;

function dibujarLineaAhora(grilla) {
  const columnaHoy = grilla.querySelector(".columna-dia[data-hoy='true']");
  if (!columnaHoy) {
    return null;
  }
  let linea = columnaHoy.querySelector(".linea-ahora");
  if (!linea) {
    linea = document.createElement("div");
    linea.className = "linea-ahora";
    linea.setAttribute("aria-hidden", "true");
    columnaHoy.appendChild(linea);
  }
  const horaInicio = Number(grilla.dataset.horaInicio);
  const horas = Number(getComputedStyle(grilla).getPropertyValue("--horas"));
  const minutosAhora = calcularMinutosDesdeMedianoche(new Date());
  const top = calcularTopDeLineaAhora(minutosAhora, horaInicio, horas);
  linea.hidden = top === null;
  if (top !== null) {
    linea.style.setProperty("--top", String(top));
  }
  return top;
}

function iniciarVistaSemanal() {
  const contenedor = document.querySelector("[data-semana-scroll]");
  const grilla = document.querySelector("[data-semana-grid]");
  if (!contenedor || !grilla) {
    return;
  }
  const horaInicio = Number(grilla.dataset.horaInicio);
  const top = dibujarLineaAhora(grilla);
  contenedor.scrollTop = calcularScrollObjetivo(top, horaInicio);
  window.setInterval(() => dibujarLineaAhora(grilla), UN_MINUTO_EN_MS);
}

document.addEventListener("DOMContentLoaded", iniciarVistaSemanal);
