/**
DOCU DE JASMINE: https://jasmine.github.io/api/5.8/global
**/
import {
  calcularMinutosDesdeMedianoche,
  calcularScrollObjetivo,
  calcularTopDeLineaAhora,
} from "../calendario_funciones.js";

describe("Calendario Funciones", function() {
  describe("calcularMinutosDesdeMedianoche", function() {
    it("debe devolver 0 a la medianoche", function() {
      expect(calcularMinutosDesdeMedianoche(new Date(2026, 8, 28, 0, 0))).toBe(0);
    });

    it("debe devolver los minutos transcurridos en el día", function() {
      expect(calcularMinutosDesdeMedianoche(new Date(2026, 8, 28, 18, 30))).toBe(18 * 60 + 30);
    });
  });

  describe("calcularTopDeLineaAhora", function() {
    it("debe devolver los minutos desde el inicio de la grilla cuando 'ahora' es visible", function() {
      expect(calcularTopDeLineaAhora(18 * 60, 7, 15)).toBe(11 * 60);
    });

    it("debe devolver null cuando 'ahora' es anterior al inicio de la grilla", function() {
      expect(calcularTopDeLineaAhora(6 * 60, 7, 15)).toBeNull();
    });

    it("debe devolver null cuando 'ahora' es posterior al fin de la grilla", function() {
      expect(calcularTopDeLineaAhora(23 * 60, 7, 15)).toBeNull();
    });

    it("debe incluir el límite exacto del fin de la grilla", function() {
      expect(calcularTopDeLineaAhora(22 * 60, 7, 15)).toBe(15 * 60);
    });
  });

  describe("calcularScrollObjetivo", function() {
    it("debe centrar el scroll 90 minutos antes de 'ahora' cuando es visible", function() {
      expect(calcularScrollObjetivo(11 * 60, 7)).toBe(11 * 60 - 90);
    });

    it("debe no bajar de cero aunque 'ahora' esté cerca del inicio", function() {
      expect(calcularScrollObjetivo(30, 7)).toBe(0);
    });

    it("debe no bajar de cero cuando 'ahora' no es visible y la grilla arranca cerca de las 8", function() {
      expect(calcularScrollObjetivo(null, 7)).toBe(0);
    });

    it("debe apuntar a las 8:00 relativas al inicio de una grilla que empieza más tarde", function() {
      expect(calcularScrollObjetivo(null, 6)).toBe((8 - 6) * 60 - 90);
    });
  });
});
