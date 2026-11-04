export function validarEmail(email) {
  const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return regexEmail.test(email?.trim());
}

export function validarPassword(password) {
  return Boolean(password?.trim());
}

export function validarCamposDeLogin(email, password) {
  return validarEmail(email) && validarPassword(password);
}

export function alternarVisibilidadPassword(inputNode, iconNode) {
  if (!inputNode || !iconNode) return;

  const esPassword = inputNode.type === "password";
  inputNode.type = esPassword ? "text" : "password";

  if (esPassword) {
    iconNode.classList.remove("fa-eye");
    iconNode.classList.add("fa-eye-slash");
  } else {
    iconNode.classList.remove("fa-eye-slash");
    iconNode.classList.add("fa-eye");
  }
}