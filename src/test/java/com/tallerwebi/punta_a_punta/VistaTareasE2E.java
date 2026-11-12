package com.tallerwebi.punta_a_punta;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.tallerwebi.punta_a_punta.vistas.VistaTareas;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VistaTareasE2E {

  static Playwright playwright;
  static Browser browser;

  BrowserContext context;
  Page page;
  VistaTareas vistaTareas;

  @BeforeAll
  static void abrirNavegador() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch();
  }

  @AfterAll
  static void cerrarNavegador() {
    browser.close();
    playwright.close();
  }

  @BeforeEach
  void crearContextoYPagina() {
    context = browser.newContext();
    page = context.newPage();
    vistaTareas = new VistaTareas(page);
  }

  @AfterEach
  void cerrarContexto() {
    context.close();
  }

  @Test
  void deberiaCrearUnaTareaYMostrarlaEnElSeguimiento() {
    page.navigate("http://localhost:8080/spring/login");

    page.locator("#email").fill("test@unlam.edu.ar");
    page.locator("#password").fill("test");
    page.locator("#btn-login").click();

    page.waitForURL("**/spring/dashboard**");

    page.navigate("http://localhost:8080/spring/tareas");

    assertTrue(
      vistaTareas.obtenerTitulo().contains("Seguimiento de tareas"),
      "No se pudo ingresar al seguimiento de tareas"
    );

    vistaTareas.darClickEnNuevaTarea();

    assertTrue(
      vistaTareas.obtenerTitulo().contains("Crear Nueva Tarea"),
      "No se abrió el formulario para crear una tarea"
    );

    String titulo = "Prueba E2E Tareas " + System.nanoTime();

    vistaTareas.escribirTitulo(titulo);
    vistaTareas.seleccionarGrupo("grupo1");
    vistaTareas.seleccionarMateria("Taller Web I");
    vistaTareas.seleccionarTipo("TP");
    vistaTareas.escribirResponsable("test@unlam.edu.ar");
    vistaTareas.seleccionarEstado("PENDIENTE");

    vistaTareas.escribirFechaVencimiento(LocalDate.now().plusDays(10).toString());

    vistaTareas.escribirHorasPlanificadas("10");
    vistaTareas.escribirHorasRealizadas("4");

    vistaTareas.darClickEnGuardar();

    page.waitForURL("**/spring/tareas**");

    assertTrue(
      vistaTareas.obtenerContenido().contains(titulo),
      "La tarea creada debería aparecer en el seguimiento"
    );
  }
}
