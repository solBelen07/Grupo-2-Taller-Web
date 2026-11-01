/**
 DOCU DE JASMINE: https://jasmine.github.io/api/5.8/global
 **/
import {
  validarEmail,
  validarPassword,
  alternarVisibilidadPassword
} from "../registro_funciones.js";

describe("Registro Funciones", function () {

  describe("validarEmail", function () {
    it("debe devolver true con un email válido", function () {
      expect(validarEmail("test@unlam.edu.ar")).toBe(true);
    });

    it("debe ignorar espacios al principio y al final del email", function () {
      expect(validarEmail("  test@unlam.edu.ar  ")).toBe(true);
    });

    it("debe devolver false si el email no tiene arroba", function () {
      expect(validarEmail("testunlam.edu.ar")).toBe(false);
    });

    it("debe devolver false si el email no tiene dominio con punto", function () {
      expect(validarEmail("test@unlam")).toBe(false);
    });

    it("debe devolver false si el email tiene espacios en el medio", function () {
      expect(validarEmail("te st@unlam.edu.ar")).toBe(false);
    });

    it("debe devolver false con un email vacío", function () {
      expect(validarEmail("")).toBe(false);
    });

    it("debe devolver false con un email null", function () {
      expect(validarEmail(null)).toBe(false);
    });

    it("debe devolver false con un email undefined", function () {
      expect(validarEmail(undefined)).toBe(false);
    });
  });

  describe("validarPassword", function () {
    it("debe devolver true con 8 caracteres", function () {
      expect(validarPassword("12345678")).toBe(true);
    });

    it("debe devolver true con más de 8 caracteres", function () {
      expect(validarPassword("clave12345")).toBe(true);
    });

    it("debe devolver false con 7 caracteres", function () {
      expect(validarPassword("1234567")).toBe(false);
    });

    it("debe devolver false con menos de 8 caracteres", function () {
      expect(validarPassword("12345")).toBe(false);
    });

    it("debe ignorar espacios al principio y al final de la contraseña", function () {
      expect(validarPassword("  1234567  ")).toBe(false);
    });

    it("debe devolver false con una contraseña vacía", function () {
      expect(validarPassword("")).toBe(false);
    });

    it("debe devolver false con una contraseña null", function () {
      expect(validarPassword(null)).toBe(false);
    });

    it("debe devolver false con una contraseña undefined", function () {
      expect(validarPassword(undefined)).toBe(false);
    });
  });

  describe("alternarVisibilidadPassword", function () {
    let input;
    let icono;

    beforeEach(function () {
      input = {type: "password"};
      icono = {
        classList: jasmine.createSpyObj("classList", ["add", "remove"])
      };
    });

    it("debe mostrar la contraseña y poner el ícono tachado si estaba oculta", function () {
      alternarVisibilidadPassword(input, icono);

      expect(input.type).toBe("text");
      expect(icono.classList.remove).toHaveBeenCalledWith("fa-eye");
      expect(icono.classList.add).toHaveBeenCalledWith("fa-eye-slash");
    });

    it("debe ocultar la contraseña y restaurar el ícono si estaba visible", function () {
      input.type = "text";

      alternarVisibilidadPassword(input, icono);

      expect(input.type).toBe("password");
      expect(icono.classList.remove).toHaveBeenCalledWith("fa-eye-slash");
      expect(icono.classList.add).toHaveBeenCalledWith("fa-eye");
    });

    it("no debe fallar ni tocar el ícono si falta el input", function () {
      expect(function () {
        alternarVisibilidadPassword(null, icono);
      }).not.toThrow();
      expect(icono.classList.add).not.toHaveBeenCalled();
    });

    it("no debe cambiar el tipo del input si falta el ícono", function () {
      alternarVisibilidadPassword(input, null);

      expect(input.type).toBe("password");
    });
  });
});