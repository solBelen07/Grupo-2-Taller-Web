document.addEventListener("DOMContentLoaded", function () {
    const datos = document.getElementById("timer-datos");
    const timerDisplay = document.getElementById("timer-display");
    const progressFill = document.getElementById("flight-progress-fill");
    const percentText = document.getElementById("flight-percent-text");
    const timerRing = document.getElementById("timer-progress-ring");
    const formCancelar = document.getElementById("form-cancelar");

    if (!datos || !timerDisplay) return;

    const MAX_CIRCUNFERENCIA = 326; // Perímetro del círculo SVG
    const INTERVALO_SINCRONIZACION_MS = 10000;

    const segundosTotales = (parseInt(datos.dataset.duracionTotalMin, 10) || 25) * 60;
    const urlEstado = datos.dataset.urlEstado;
    const urlInicio = datos.dataset.urlInicio;

    // El servidor es la fuente de verdad: estos valores se refrescan en cada sincronización.
    let estado = datos.dataset.estado;
    let segundosServidor = parseInt(datos.dataset.segundosIniciales, 10) || 0;
    let referenciaLocal = Date.now();

    let intervaloReloj = null;
    let intervaloSincronizacion = null;
    let finalizando = false;

    function formatearTiempo(totalSegundos) {
        const segundos = Math.max(0, totalSegundos);
        const min = Math.floor(segundos / 60);
        const seg = segundos % 60;
        const mm = min < 10 ? "0" + min : min;
        const ss = seg < 10 ? "0" + seg : seg;
        return `${mm}:${ss}`;
    }

    // Solo ilustrativo: parte del valor del servidor y descuenta el tiempo local transcurrido.
    // Si el vuelo no está en curso (pausado o finalizado) el tiempo no corre.
    function calcularSegundosActuales() {
        if (estado !== "EN_CURSO") return segundosServidor;
        const transcurridos = Math.floor((Date.now() - referenciaLocal) / 1000);
        return Math.max(0, segundosServidor - transcurridos);
    }

    function renderizar(segundosActuales) {
        timerDisplay.textContent = formatearTiempo(segundosActuales);

        const consumidos = segundosTotales - segundosActuales;
        const porcentaje = Math.min(100, Math.max(0, Math.floor((consumidos / segundosTotales) * 100)));

        if (progressFill) progressFill.style.width = `${porcentaje}%`;
        if (percentText) percentText.textContent = `${porcentaje}%`;
        if (timerRing) {
            timerRing.style.strokeDashoffset = MAX_CIRCUNFERENCIA - (porcentaje / 100) * MAX_CIRCUNFERENCIA;
        }
    }

    function detenerRelojes() {
        clearInterval(intervaloReloj);
        clearInterval(intervaloSincronizacion);
    }

    function terminar(estadoFinal) {
        if (finalizando) return;
        finalizando = true;
        detenerRelojes();

        if (estadoFinal === "COMPLETADO") {
            renderizar(0);
            alert("¡Aterrizaje completado con éxito! Ganaste +3 puntos.");
        }
        window.location.href = urlInicio;
    }

    function aplicarEstadoServidor(respuesta) {
        if (finalizando) return;

        if (respuesta.estado !== estado) {
            const sigueActiva = respuesta.estado === "EN_CURSO" || respuesta.estado === "PAUSADO";
            if (sigueActiva) {
                // Cambió desde otra pestaña: se recarga para mostrar los botones correctos.
                window.location.reload();
            } else {
                terminar(respuesta.estado);
            }
            return;
        }

        segundosServidor = respuesta.segundosRestantes;
        referenciaLocal = Date.now();
    }

    function sincronizar() {
        return fetch(urlEstado, { headers: { Accept: "application/json" }, cache: "no-store" })
            .then(function (respuesta) {
                return respuesta.json();
            })
            .then(function (respuesta) {
                aplicarEstadoServidor(respuesta);
                return respuesta;
            })
            .catch(function () {
                // Sin conexión: se sigue con el reloj local hasta la próxima sincronización.
                return null;
            });
    }

    function iniciarReloj() {
        clearInterval(intervaloReloj);
        intervaloReloj = setInterval(actualizarInterfaz, 1000);
    }

    function alLlegarACero() {
        clearInterval(intervaloReloj);
        // Se consulta al servidor para que cierre la sesión y asigne los puntos.
        sincronizar().then(function (respuesta) {
            const sigueEnCurso = respuesta && respuesta.estado === "EN_CURSO" && respuesta.segundosRestantes > 0;
            if (sigueEnCurso) {
                iniciarReloj();
            } else {
                terminar("COMPLETADO");
            }
        });
    }

    function actualizarInterfaz() {
        const segundosActuales = calcularSegundosActuales();
        renderizar(segundosActuales);

        if (estado === "EN_CURSO" && segundosActuales <= 0) alLlegarACero();
    }

    if (formCancelar) {
        formCancelar.addEventListener("submit", function (evento) {
            const confirmado = confirm("¿Seguro que quieres cancelar el vuelo? Perderás 2 puntos.");
            if (!confirmado) evento.preventDefault();
        });
    }

    document.addEventListener("visibilitychange", function () {
        if (!document.hidden) sincronizar();
    });

    if (estado === "COMPLETADO" || estado === "CANCELADO") {
        terminar(estado);
        return;
    }

    actualizarInterfaz();
    iniciarReloj();
    intervaloSincronizacion = setInterval(sincronizar, INTERVALO_SINCRONIZACION_MS);
});
