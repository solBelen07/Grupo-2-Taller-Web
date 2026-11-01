import {
  validarEmail,
  validarPassword,
  validarCamposDeLogin,
  alternarVisibilidadPassword
} from "./login_funciones.js";

const btnLoginNode = document.getElementById("btn-login");
const inputEmailNode = document.getElementById("email");
const inputPasswordNode = document.getElementById("password");
const btnTogglePasswordNode = document.getElementById("btn-toggle-password");
const iconEyeNode = document.getElementById("icon-eye");

const emailErrorNode = document.getElementById("email-error");
const passwordErrorNode = document.getElementById("password-error");
const capsLockWarningNode = document.getElementById("caps-lock-warning");

function validarFormularioEnTiempoReal() {
  const emailValue = inputEmailNode.value;
  const passwordValue = inputPasswordNode.value;

  const emailEsValido = validarEmail(emailValue);
  const passwordEsValido = validarPassword(passwordValue);

  if (emailValue.length > 0 && !emailEsValido) {
    emailErrorNode.style.display = "block";
  } else {
    emailErrorNode.style.display = "none";
  }

  // Mostrar u ocultar mensaje de error de la contraseña en vivo
  if (passwordValue.length > 0 && !passwordEsValido) {
    passwordErrorNode.style.display = "block";
  } else {
    passwordErrorNode.style.display = "none";
  }

  // Aquí usamos la función validarCamposDeLogin para el botón
  const formularioValido = validarCamposDeLogin(emailValue, passwordValue);
  btnLoginNode.disabled = !formularioValido;
}

inputEmailNode.addEventListener("input", validarFormularioEnTiempoReal);
inputPasswordNode.addEventListener("input", validarFormularioEnTiempoReal);

inputPasswordNode.addEventListener("keydown", (event) => {
  const capsLockIsOn = event.getModifierState("CapsLock");
  if (capsLockWarningNode) {
    capsLockWarningNode.style.display = capsLockIsOn ? "block" : "none";
  }
});

inputPasswordNode.addEventListener("blur", () => {
  if (capsLockWarningNode) capsLockWarningNode.style.display = "none";
});

if (btnTogglePasswordNode) {
  btnTogglePasswordNode.addEventListener("click", () => {
    alternarVisibilidadPassword(inputPasswordNode, iconEyeNode);
  });
}

document.addEventListener("DOMContentLoaded", validarFormularioEnTiempoReal);