const formulario = document.getElementById("ia-formulario");
const urlApi = formulario.dataset.apiUrl;
const entrada = document.getElementById("ia-pregunta");
const mensajes = document.getElementById("ia-mensajes");
const botonEnviar = document.getElementById("ia-enviar");
const sugerencias = document.getElementById("ia-sugerencias");

function agregarMensaje(texto, tipo) {
  const mensaje = document.createElement("div");
  mensaje.className = "ia-mensaje " + tipo;

  const etiqueta = document.createElement("div");
  etiqueta.className = "ia-etiqueta";
  etiqueta.textContent = tipo === "usuario" ? "yo" : "FocusFlight IA";

  const contenido = document.createElement("div");

  if (tipo === "usuario") {
    contenido.textContent = texto;
  } else {
    contenido.className = "contenido-markdown";
    contenido.innerHTML = texto;
  }

  mensaje.appendChild(etiqueta);
  mensaje.appendChild(contenido);
  mensajes.appendChild(mensaje);

  mensajes.scrollTop = mensajes.scrollHeight;

  return mensaje;
}

async function enviarPregunta(pregunta) {
  const texto = pregunta.trim();

  if (!texto || botonEnviar.disabled) {
    return;
  }

  agregarMensaje(texto, "usuario");

  entrada.value = "";
  botonEnviar.disabled = true;
  entrada.disabled = true;
  sugerencias.hidden = true;

  const mensajeEspera = agregarMensaje(
    "Estoy preparando tu respuesta...",
    "asistente"
  );

  try {
    const respuestaHttp = await fetch(urlApi, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        pregunta: texto,
        reglaAdicional: "Sos el asistente de estudio de FocusFlight. Respondé en español rioplatense, con claridad, paciencia y ejemplos. Ayudá al estudiante a comprender temas, practicar y organizar el estudio. Si faltan datos para crear un plan, preguntá por la materia, los temas y la fecha del examen. No inventes información académica."
      })
    });

    if (!respuestaHttp.ok) {
      const detalle = await respuestaHttp.text();
      throw new Error(detalle || "El servidor respondió con un error.");
    }

    const datos = await respuestaHttp.json();

    mensajeEspera.lastChild.innerHTML =
      datos.respuesta || "No se recibió una respuesta. Probá nuevamente.";

  } catch (error) {
    mensajeEspera.lastChild.innerHTML =
      "No pude obtener una respuesta. Revisá que el servidor esté funcionando e intentá nuevamente.";

    console.error("Error al consultar Gemini:", error);

  } finally {
    botonEnviar.disabled = false;
    entrada.disabled = false;
    entrada.focus();
    mensajes.scrollTop = mensajes.scrollHeight;
  }
}

formulario.addEventListener("submit", function (evento) {
  evento.preventDefault();
  enviarPregunta(entrada.value);
});

sugerencias.addEventListener("click", function (evento) {
  const boton = evento.target.closest("[data-pregunta]");

  if (boton) {
    enviarPregunta(boton.dataset.pregunta);
  }
});

entrada.addEventListener("keydown", function (evento) {
  if (evento.key === "Enter" && !evento.shiftKey) {
    evento.preventDefault();
    formulario.requestSubmit();
  }
});

// ELEMENTOS DE EJERCITACIÓN
const contenedorEjercicios = document.getElementById("contenedor-ejercicios");
const botonGenerar = document.getElementById("generar-ejercicios");
const botonCorregir = document.getElementById("corregir-ejercicios");
const preguntasEjercicios = document.getElementById("preguntas-ejercicios");
const resultadoEjercicios = document.getElementById("resultado-ejercicios");

const urlGenerar = contenedorEjercicios.dataset.generarUrl;
const urlCorregir = contenedorEjercicios.dataset.corregirUrl;

// GENERAR EJERCICIOS
botonGenerar.addEventListener("click", async function () {

  const materiaId = document.getElementById("materia").value;
  const tema = document.getElementById("tema").value.trim();
  const cantidad = Number(document.getElementById("cantidad").value);

  if (!materiaId || !tema) {
    resultadoEjercicios.textContent =
      "Seleccioná una materia e ingresá el tema que querés practicar.";
    contenedorEjercicios.hidden = false;
    return;
  }

  botonGenerar.disabled = true;
  botonGenerar.textContent = "Generando ejercicios...";

  resultadoEjercicios.textContent = "";
  preguntasEjercicios.replaceChildren();
  contenedorEjercicios.hidden = false;

  try {
    const respuestaHttp = await fetch(urlGenerar, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        materiaId: Number(materiaId),
        tema: tema,
        cantidad: cantidad
      })
    });

    const texto = await respuestaHttp.text();
    let datos;

    try {
      datos = JSON.parse(texto);
    } catch (error) {
      throw new Error(texto || "No se pudo procesar la respuesta.");
    }

    if (!respuestaHttp.ok) {
      throw new Error(typeof datos === "string" ? datos : "No se pudieron generar los ejercicios.");
    }

    mostrarPreguntas(datos);

  } catch (error) {
    resultadoEjercicios.textContent = error.message;
    console.error("Error al generar ejercicios:", error);

  } finally {
    botonGenerar.disabled = false;
    botonGenerar.innerHTML =
      '<i class="fa-solid fa-wand-magic-sparkles me-2"></i> Generar ejercicios';
  }
});

// MOSTRAR LAS PREGUNTAS
function mostrarPreguntas(preguntas) {

  preguntasEjercicios.replaceChildren();
  resultadoEjercicios.textContent = "";

  if (!Array.isArray(preguntas) || preguntas.length === 0) {
    resultadoEjercicios.textContent =
      "No se recibieron preguntas. Intentá nuevamente.";
    botonCorregir.hidden = true;
    return;
  }

  preguntas.forEach(function (pregunta, indice) {

    const grupo = document.createElement("fieldset");
    grupo.className = "mb-4";

    const titulo = document.createElement("legend");
    titulo.className = "h5";
    titulo.textContent = (indice + 1) + ". " + pregunta.enunciado;

    grupo.appendChild(titulo);

    pregunta.opciones.forEach(function (opcion, indiceOpcion) {

      const etiqueta = document.createElement("label");
      etiqueta.className = "d-block border rounded p-2 mb-2";

      const radio = document.createElement("input");
      radio.type = "radio";
      radio.name = "respuesta-" + indice;
      radio.value = indiceOpcion;
      radio.className = "form-check-input me-2";

      etiqueta.appendChild(radio);
      etiqueta.appendChild(document.createTextNode(opcion));

      grupo.appendChild(etiqueta);
    });

    preguntasEjercicios.appendChild(grupo);
  });

  botonCorregir.hidden = false;
}

// CORREGIR EJERCICIOS
botonCorregir.addEventListener("click", async function () {

  const preguntas = preguntasEjercicios.querySelectorAll("fieldset");
  const respuestas = [];

  for (let i = 0; i < preguntas.length; i++) {

    const seleccionada = preguntasEjercicios.querySelector(
      'input[name="respuesta-' + i + '"]:checked'
    );

    if (!seleccionada) {
      resultadoEjercicios.textContent =
        "Respondé todas las preguntas antes de corregir.";
      return;
    }

    respuestas.push(Number(seleccionada.value));
  }

  botonCorregir.disabled = true;
  botonCorregir.textContent = "Corrigiendo...";

  try {
    const respuestaHttp = await fetch(urlCorregir, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        respuestas: respuestas
      })
    });

    const texto = await respuestaHttp.text();
    let datos;

    try {
      datos = JSON.parse(texto);
    } catch (error) {
      throw new Error(texto || "No se pudo procesar la corrección.");
    }

    if (!respuestaHttp.ok) {
      throw new Error(typeof datos === "string" ? datos : "No se pudieron corregir los ejercicios.");
    }

    mostrarResultado(datos);

  } catch (error) {
    resultadoEjercicios.textContent = error.message;
    console.error("Error al corregir ejercicios:", error);

  } finally {
    botonCorregir.disabled = false;
    botonCorregir.textContent = "Corregir respuestas";
  }
});

// MOSTRAR RESULTADO
function mostrarResultado(datos) {

  resultadoEjercicios.replaceChildren();

  const titulo = document.createElement("h3");
  titulo.className = "h4";
  titulo.textContent = "¡Terminaste tu práctica!";

  const puntaje = document.createElement("p");
  puntaje.className = "fs-5";
  puntaje.textContent =
    "Resultado: " + datos.correctas + " de " + datos.total +
    " respuestas correctas (" + datos.porcentaje + "%).";

  resultadoEjercicios.appendChild(titulo);
  resultadoEjercicios.appendChild(puntaje);

  datos.correcciones.forEach(function (correccion, indice) {

    const detalle = document.createElement("div");
    detalle.className = "border rounded p-3 mb-3";

    const pregunta = document.createElement("p");
    pregunta.className = "fw-bold";
    pregunta.textContent = (indice + 1) + ". " + correccion.enunciado;

    const estado = document.createElement("p");
    estado.textContent = correccion.esCorrecta ?
      "¡Correcto!" : "Incorrecto";

    const respuestaElegida = document.createElement("p");
    respuestaElegida.textContent =
      "Tu respuesta: " + correccion.respuestaElegida;

    const respuestaCorrecta = document.createElement("p");
    respuestaCorrecta.textContent =
      "Respuesta correcta: " + correccion.respuestaCorrecta;

    const explicacion = document.createElement("p");
    explicacion.textContent =
      "Explicación: " + (correccion.explicacion || "Sin explicación disponible.");

    detalle.append(
      pregunta,
      estado,
      respuestaElegida,
      respuestaCorrecta,
      explicacion
    );

    resultadoEjercicios.appendChild(detalle);
  });

  botonCorregir.hidden = true;
}