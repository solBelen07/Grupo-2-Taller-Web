import {
  hayEdicionAbierta,
  abrirCampo,
  cerrarCampo,
  alternarPassword
} from "../perfil_funciones.js";

function crearCampo() {
  const partes = {
    "[data-lectura]": {hidden: false},
    "[data-edicion]": {hidden: true},
    "[data-editar]": {hidden: false}
  };
  return {
    partes,
    querySelector: (selector) => partes[selector]
  };
}

describe("Perfil Funciones", function () {

  describe("abrirCampo", function () {
    it("debe ocultar la lectura y el lápiz y mostrar la edición", function () {
      const campo = crearCampo();

      abrirCampo(campo);

      expect(campo.partes["[data-lectura]"].hidden).toBe(true);
      expect(campo.partes["[data-edicion]"].hidden).toBe(false);
      expect(campo.partes["[data-editar]"].hidden).toBe(true);
    });

    it("no debe fallar con un campo nulo", function () {
      expect(function () {
        abrirCampo(null);
      }).not.toThrow();
    });
  });

  describe("cerrarCampo", function () {
    it("debe restaurar la lectura y el lápiz y ocultar la edición", function () {
      const campo = crearCampo();
      abrirCampo(campo);

      cerrarCampo(campo);

      expect(campo.partes["[data-lectura]"].hidden).toBe(false);
      expect(campo.partes["[data-edicion]"].hidden).toBe(true);
      expect(campo.partes["[data-editar]"].hidden).toBe(false);
    });

    it("no debe fallar con un campo nulo", function () {
      expect(function () {
        cerrarCampo(null);
      }).not.toThrow();
    });
  });

  describe("hayEdicionAbierta", function () {
    it("debe devolver true si el formulario tiene una edición visible", function () {
      const formulario = {querySelector: () => ({})};
      expect(hayEdicionAbierta(formulario)).toBe(true);
    });

    it("debe devolver false si no hay ninguna edición visible", function () {
      const formulario = {querySelector: () => null};
      expect(hayEdicionAbierta(formulario)).toBe(false);
    });

    it("debe devolver false con un formulario nulo", function () {
      expect(hayEdicionAbierta(null)).toBe(false);
    });
  });

  describe("alternarPassword", function () {
    let form;
    let boton;

    beforeEach(function () {
      form = {hidden: true};
      boton = {hidden: false, setAttribute: jasmine.createSpy("setAttribute")};
    });

    it("debe mostrar el formulario y ocultar el botón", function () {
      alternarPassword(form, boton, true);

      expect(form.hidden).toBe(false);
      expect(boton.hidden).toBe(true);
      expect(boton.setAttribute).toHaveBeenCalledWith("aria-expanded", "true");
    });

    it("debe ocultar el formulario y mostrar el botón", function () {
      form.hidden = false;
      boton.hidden = true;

      alternarPassword(form, boton, false);

      expect(form.hidden).toBe(true);
      expect(boton.hidden).toBe(false);
      expect(boton.setAttribute).toHaveBeenCalledWith("aria-expanded", "false");
    });

    it("no debe fallar si falta algún nodo", function () {
      expect(function () {
        alternarPassword(null, boton, true);
      }).not.toThrow();
      expect(function () {
        alternarPassword(form, null, true);
      }).not.toThrow();
    });
  });
});