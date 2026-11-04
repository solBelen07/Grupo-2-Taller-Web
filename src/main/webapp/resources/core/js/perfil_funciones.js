export function hayEdicionAbierta(formulario) {
  return formulario?.querySelector("[data-edicion]:not([hidden])") != null;
}

export function abrirCampo(campo) {
  if (!campo) return;
  campo.querySelector("[data-lectura]").hidden = true;
  campo.querySelector("[data-edicion]").hidden = false;
  campo.querySelector("[data-editar]").hidden = true;
}

export function cerrarCampo(campo) {
  if (!campo) return;
  campo.querySelector("[data-lectura]").hidden = false;
  campo.querySelector("[data-edicion]").hidden = true;
  campo.querySelector("[data-editar]").hidden = false;
}

export function alternarPassword(formPassword, botonPassword, mostrar) {
  if (!formPassword || !botonPassword) return;
  formPassword.hidden = !mostrar;
  botonPassword.hidden = mostrar;
  botonPassword.setAttribute("aria-expanded", String(mostrar));
}