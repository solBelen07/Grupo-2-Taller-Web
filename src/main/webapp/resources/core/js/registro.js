import {
  validarEmail,
  validarPassword,
  alternarVisibilidadPassword
} from "./registro_funciones.js";

const btnRegistroNode = document.getElementById("btn-registro");
const nombreNode = document.getElementById("nombre");
const apellidoNode = document.getElementById("apellido");
const emailNode = document.getElementById("email");
const passwordNode = document.getElementById("password");
const universidadIdNode = document.getElementById("universidadId");
const carreraIdNode = document.getElementById("carreraId");

const nombreErrorNode = document.getElementById("nombre-error");
const apellidoErrorNode = document.getElementById("apellido-error");
const emailErrorNode = document.getElementById("email-error");
const passwordErrorNode = document.getElementById("password-error");

const btnTogglePasswordNode = document.getElementById("btn-toggle-password");
const iconEyeNode = document.getElementById("icon-eye");
const capsLockWarningNode = document.getElementById("caps-lock-warning");

function validarRegistroEnTiempoReal() {
  const nombreVal = nombreNode.value.trim();
  const apellidoVal = apellidoNode.value.trim();
  const emailVal = emailNode.value.trim();
  const passwordVal = passwordNode.value.trim();
  const universidadVal = universidadIdNode.value;
  const carreraVal = carreraIdNode.value;

  const nombreOk = nombreVal.length > 0;
  const apellidoOk = apellidoVal.length > 0;
  const emailOk = validarEmail(emailVal);
  const passwordOk = validarPassword(passwordVal);
  const universidadOk = universidadVal !== "";
  const carreraOk = carreraVal !== "";

  nombreErrorNode.style.display = (nombreNode.value.length > 0 && !nombreOk) ? "block" : "none";
  apellidoErrorNode.style.display = (apellidoNode.value.length > 0 && !apellidoOk) ? "block" : "none";
  emailErrorNode.style.display = (emailVal.length > 0 && !emailOk) ? "block" : "none";
  passwordErrorNode.style.display = (passwordVal.length > 0 && !passwordOk) ? "block" : "none";

  const formularioValido = nombreOk && apellidoOk && emailOk && passwordOk && universidadOk && carreraOk;
  btnRegistroNode.disabled = !formularioValido;
}

const campos = [nombreNode, apellidoNode, emailNode, passwordNode, universidadIdNode, carreraIdNode];
campos.forEach(campo => {
  if (campo) {
    campo.addEventListener("input", validarRegistroEnTiempoReal);
    campo.addEventListener("change", validarRegistroEnTiempoReal);
  }
});

if (passwordNode) {
  passwordNode.addEventListener("keydown", (event) => {
    const capsLockIsOn = event.getModifierState("CapsLock");
    if (capsLockWarningNode) capsLockWarningNode.style.display = capsLockIsOn ? "block" : "none";
  });
  passwordNode.addEventListener("blur", () => {
    if (capsLockWarningNode) capsLockWarningNode.style.display = "none";
  });
}

if (btnTogglePasswordNode) {
  btnTogglePasswordNode.addEventListener("click", () => {
    alternarVisibilidadPassword(passwordNode, iconEyeNode);
  });
}

document.addEventListener("DOMContentLoaded", validarRegistroEnTiempoReal);