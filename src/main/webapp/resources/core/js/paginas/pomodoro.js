document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("form-vuelo");

    form.addEventListener("submit", function (e) {
        let esValido = true;

        document.querySelectorAll(".error-message").forEach(el => el.remove());
        document.querySelectorAll(".input-error").forEach(el => el.classList.remove("input-error"));

        const campos = [
            {name: "origen", mensaje: "Ingresa el lugar de origen."},
            {name: "destino", mensaje: "Ingresa el lugar de destino."},
            {name: "duracion", mensaje: "Selecciona la duración de la sesion."},
            {name: "materiaId", mensaje: "Selecciona una materia."},
        ];

        campos.forEach(campo => {
            const input = form.querySelector(`[name="${campo.name}"]`);
            if (input) {
                const valor = input.value.trim();

                if (!valor || valor === "Seleccionar tiempo" || valor === "") {
                    esValido = false;
                    mostrarError(input, campo.mensaje);
                }
            }
        });

        if (!esValido) {
            e.preventDefault();
        }
    });

    function mostrarError(inputElement, mensaje) {
        inputElement.classList.add("input-error");

        const spanError = document.createElement("span");
        spanError.className = "error-message";
        spanError.innerText = mensaje;

        const contenedorPadre = inputElement.closest(".input-with-icon") || inputElement;
        contenedorPadre.parentNode.insertBefore(spanError, contenedorPadre.nextSibling);
    }
});