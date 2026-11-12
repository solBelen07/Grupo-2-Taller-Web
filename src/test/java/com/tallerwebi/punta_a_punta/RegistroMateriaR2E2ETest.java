package com.tallerwebi.punta_a_punta;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * E2E real: navegador -> Spring MVC -> MySQL -> Cloudflare R2 -> descarga.
 * Ejecutar contra una base y un bucket de pruebas, nunca contra produccion.
 * Activar con: mvn -De2e.r2=true -Dtest=RegistroMateriaR2E2ETest test
 */
@EnabledIfSystemProperty(named = "e2e.r2", matches = "true")
public class RegistroMateriaR2E2ETest {

  private static Playwright playwright;
  private static Browser browser;

  private BrowserContext context;
  private Page page;

  private final String baseUrl = System.getProperty("e2e.baseUrl", "http://localhost:8080/spring");

  @BeforeAll
  static void iniciarNavegador() {
    playwright = Playwright.create();
    browser =
      playwright
        .chromium()
        .launch(
          new BrowserType.LaunchOptions()
            .setHeadless(Boolean.parseBoolean(System.getProperty("e2e.headless", "true")))
        );
  }

  @AfterAll
  static void cerrarNavegador() {
    if (browser != null) {
      browser.close();
    }
    if (playwright != null) {
      playwright.close();
    }
  }

  @BeforeEach
  void abrirPagina() {
    context = browser.newContext();
    page = context.newPage();
  }

  @AfterEach
  void cerrarPagina() {
    if (context != null) {
      context.close();
    }
  }

  @Test
  void deberiaRegistrarLoguearCrearMateriaYRecuperarPdfDeR2()
    throws IOException, URISyntaxException {
    String identificador = UUID.randomUUID().toString().substring(0, 8);
    String email = "e2e-" + identificador + "@example.com";
    String clave = "ClaveSegura123!";
    String nombreMateria = "Materia E2E R2 " + identificador;
    URL recurso = getClass().getClassLoader().getResource("material-e2e.pdf");
    assertTrue(recurso != null, "No se encontro material-e2e.pdf en src/test/resources");
    Path pdf = Path.of(recurso.toURI());

    assertTrue(Files.isRegularFile(pdf), "Falta el archivo PDF de prueba");
    byte[] contenidoOriginal = Files.readAllBytes(pdf);

    // 1. REGISTRO: formulario real, con los campos obligatorios actuales.
    page.navigate(baseUrl + "/registro");
    page.locator("#nombre").fill("Prueba");
    page.locator("#apellido").fill("Automatizada");
    page.locator("#email").fill(email);
    page.locator("#password").fill(clave);
    seleccionarPrimerValorValido("#universidadId");
    seleccionarPrimerValorValido("#carreraId");
    page.locator("#btn-registro").click();
    comprobarRuta("/login", "registro");

    // 2. LOGIN: el controlador actual redirige a /dashboard.
    page.locator("#email").fill(email);
    page.locator("#password").fill(clave);
    page.locator("#btn-login").click();
    // Si el login falla, se informa la URL real y el mensaje de la pantalla.
    comprobarRuta("/dashboard", "inicio de sesion");

    // 3. CREACION DE MATERIA, adjuntando un archivo mediante el navegador.
    page.navigate(baseUrl + "/materias");
    page.locator("a[href*='/materias/nueva']").click();
    page.locator("#nombre").fill(nombreMateria);
    page.locator("#descripcion").fill("Materia de prueba para Cloudflare R2");
    page.locator("#docente").fill("Docente de pruebas");
    page.locator("#dias").fill("Lunes");
    page.locator("#horario").fill("18:00 - 20:00");
    page.locator("#color").fill("#4477cc");
    page.locator("#archivos").setInputFiles(pdf);
    page.locator("form[enctype='multipart/form-data'] button[type='submit']").click();
    // Si R2 falla al subir, el error se informa aqui, sin esperar 30 segundos.
    comprobarRuta("/materias", "creacion de materia y subida a R2");

    // 4. VERIFICAR que la materia existe y entrar a su detalle.
    Locator enlaceMateria = page
      .locator("a[href*='/materias/detalle']")
      .filter(new Locator.FilterOptions().setHasText(nombreMateria));
    assertEquals(1, enlaceMateria.count(), "No se encontro la materia creada");
    enlaceMateria.click();
    assertTrue(
      page.locator("h2").filter(new Locator.FilterOptions().setHasText(nombreMateria)).isVisible()
    );

    // 5. VERIFICAR R2: pedir el material desde el mismo contexto autenticado.
    // La respuesta pasa por ServicioMaterial -> AlmacenamientoR2 -> getObjectAsBytes.
    Locator enlacePdf = page
      .locator("a[href*='/materias/material/']")
      .filter(new Locator.FilterOptions().setHasText("material-e2e.pdf"));
    assertEquals(1, enlacePdf.count(), "No se guardo el material bibliografico");

    String enlace = enlacePdf.getAttribute("href");
    String urlArchivo = URI.create(page.url()).resolve(enlace).toString();
    APIResponse respuesta = context.request().get(urlArchivo);

    assertEquals(200, respuesta.status(), "No se pudo recuperar el archivo de R2");
    assertTrue(
      respuesta.headers().getOrDefault("content-type", "").contains("application/pdf"),
      "La respuesta no tiene el Content-Type esperado"
    );
    assertArrayEquals(
      contenidoOriginal,
      respuesta.body(),
      "El archivo descargado no coincide con el PDF que se subio"
    );
  }

  private void comprobarRuta(String rutaEsperada, String etapa) {
    String urlEsperada = baseUrl + rutaEsperada;
    String urlActual = page.url();
    String urlNormalizada = urlActual.replaceFirst(";jsessionid=[^/?#]*", "");
    System.out.println("E2E [" + etapa + "]: " + urlActual);
    if (!urlNormalizada.equals(urlEsperada) && !urlNormalizada.startsWith(urlEsperada + "?")) {
      Locator errores = page.locator(".login-error, .registro-error, .alert-danger");
      String errorPantalla = errores.count() > 0
        ? errores.first().innerText()
        : "Sin mensaje de error visible";
      String cuerpo = page.locator("body").innerText();
      String extracto = cuerpo.substring(0, Math.min(400, cuerpo.length()));
      try {
        Path captura = Path.of("target", "e2e-error-" + etapa.replace(' ', '-') + ".png");
        Files.createDirectories(captura.getParent());
        page.screenshot(new Page.ScreenshotOptions().setPath(captura).setFullPage(true));
        System.out.println("Captura del fallo: " + captura.toAbsolutePath());
      } catch (RuntimeException | IOException e) {
        System.out.println("No se pudo guardar la captura: " + e.getMessage());
      }
      throw new AssertionError(
        "Fallo en " +
        etapa +
        ". Esperaba " +
        urlEsperada +
        ", URL obtenida: " +
        urlActual +
        ". Error visible: " +
        errorPantalla +
        ". Pantalla: " +
        extracto
      );
    }
  }

  private void seleccionarPrimerValorValido(String selector) {
    String valor = page.locator(selector + " option:not([value=''])").first().getAttribute("value");
    assertTrue(valor != null && !valor.isBlank(), "No hay opciones disponibles: " + selector);
    page.locator(selector).selectOption(valor);
  }
}
