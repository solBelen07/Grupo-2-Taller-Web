describe("validarPassword", function() {
    it("debe devolver true con 8 caracteres", function() {
        expect(validarPassword("12345678")).toBe(true);
    });

    it("debe devolver true con más de 8 caracteres", function() {
        expect(validarPassword("clave12345")).toBe(true);
    });

    it("debe devolver false con 7 caracteres", function() {
        expect(validarPassword("1234567")).toBe(false);
    });

    it("debe devolver false con menos de 8 caracteres", function() {
        expect(validarPassword("12345")).toBe(false);
    });

    it("debe ignorar espacios al principio y al final", function() {
        expect(validarPassword("  1234567  ")).toBe(false);
    });

    it("debe devolver false con string vacío", function() {
        expect(validarPassword("")).toBe(false);
    });

    it("debe devolver false con null", function() {
        expect(validarPassword(null)).toBe(false);
    });

    it("debe devolver false con undefined", function() {
        expect(validarPassword(undefined)).toBe(false);
    });
});