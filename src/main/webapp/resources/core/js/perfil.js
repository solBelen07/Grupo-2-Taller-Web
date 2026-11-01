import {
  hayEdicionAbierta,
  abrirCampo,
  cerrarCampo,
  alternarPassword
} from "./perfil_funciones.js";

const formDatos = document.getElementById("form-datos");
const acciones = document.getElementById("acciones-datos");
const botonCancelarDatos = document.getElementById("cancelar-datos");
const botonPassword = document.getElementById("btn-password");
const formPassword = document.getElementById("form-password");
const botonCancelarPassword = document.getElementById("cancelar-password");

function actualizarAcciones() {
  acciones.hidden = !hayEdicionAbierta(formDatos);
}

function cerrarTodos() {
  formDatos.querySelectorAll("[data-campo]").forEach(cerrarCampo);
  formDatos.reset();
  actualizarAcciones();
}

function mostrarPassword(mostrar) {
  alternarPassword(formPassword, botonPassword, mostrar);
  if (mostrar) {
    document.getElementById("actual").focus();
  } else {
    formPassword.reset();
  }
}

if (formDatos) {
  formDatos.querySelectorAll("[data-editar]").forEach((boton) => {
    boton.addEventListener("click", () => {
      const campo = boton.closest("[data-campo]");
      abrirCampo(campo);
      const control = campo.querySelector("input, select");
      if (control) control.focus();
      actualizarAcciones();
    });
  });
  botonCancelarDatos.addEventListener("click", cerrarTodos);
}

if (botonPassword) {
  botonPassword.addEventListener("click", () => mostrarPassword(true));
  botonCancelarPassword.addEventListener("click", () => mostrarPassword(false));
}