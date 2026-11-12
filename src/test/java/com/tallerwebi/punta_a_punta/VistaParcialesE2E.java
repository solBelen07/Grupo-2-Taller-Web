package com.tallerwebi.punta_a_punta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.tallerwebi.punta_a_punta.vistas.VistaDashboard;
import com.tallerwebi.punta_a_punta.vistas.VistaFormularioParcial;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import com.tallerwebi.punta_a_punta.vistas.VistaParciales;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VistaParcialesE2E {

  private static final String EMAIL_USUARIO = "test@unlam.edu.ar";
  private static final String CLAVE_USUARIO = "test";
  private static final String NOMBRE_PARCIAL = "Recuperatorio E2E de Taller Web";
  private static final int HORAS_DE_LA_SESION = 3;

  static Playwright playwright;
  static Browser browser;
  BrowserContext context;
  Page page;
  VistaLogin vistaLogin;

  @BeforeAll
  static void abrirNavegador() {
    playwright = Playwright.create();
    browser =
      playwright
        .chromium()
        .launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500));
  }

  @AfterAll
  static void cerrarNavegador() {
    playwright.close();
  }

  @BeforeEach
  void crearContextoYPagina() {
    ReiniciarDB.limpiarBaseDeDatos();
    context = browser.newContext();
    page = context.newPage();
    vistaLogin = new VistaLogin(page);
  }

  @AfterEach
  void cerrarContexto() {
    context.close();
  }

  @Test
  void unParcialParaMananaApareceUrgenteYPrimeroEnLosProximosVencimientos()
    throws MalformedURLException {
    dadoQueElUsuarioInicioSesionCon(EMAIL_USUARIO, CLAVE_USUARIO);

    cuandoElUsuarioCreaUnParcialPara(LocalDate.now().plusDays(1));

    VistaDashboard.navegar(page);
    assertThat(primerVencimientoDelDashboard(), equalTo(NOMBRE_PARCIAL));
    assertThat(elPrimerVencimientoEsUrgente(), is(true));
  }

  @Test
  void alCompletarUnaSesionDeEstudioSeSumanSusHorasYArrancaLaRacha() throws MalformedURLException {
    dadoQueElUsuarioInicioSesionCon(EMAIL_USUARIO, CLAVE_USUARIO);
    long horasAntes = VistaDashboard.navegar(page).obtenerHorasDeEstudio();
    cuandoElUsuarioCreaUnParcialPara(LocalDate.now().plusDays(5));
    VistaParciales vistaParciales = VistaParciales.navegar(page);
    int rachaAntes = vistaParciales.obtenerDiasDeRacha();

    vistaParciales.darClickEnLaPrimeraSesion(NOMBRE_PARCIAL);

    assertThat(vistaParciales.obtenerDiasDeRacha(), equalTo(rachaAntes + 1));
    long horasDespues = VistaDashboard.navegar(page).obtenerHorasDeEstudio();
    assertThat(horasDespues, equalTo(horasAntes + HORAS_DE_LA_SESION));
  }

  @Test
  void siSeDesmarcaLaSesionSeDescuentanLasHorasYLaRacha() throws MalformedURLException {
    dadoQueElUsuarioInicioSesionCon(EMAIL_USUARIO, CLAVE_USUARIO);
    long horasAntes = VistaDashboard.navegar(page).obtenerHorasDeEstudio();
    cuandoElUsuarioCreaUnParcialPara(LocalDate.now().plusDays(5));
    VistaParciales vistaParciales = VistaParciales.navegar(page);
    int rachaAntes = vistaParciales.obtenerDiasDeRacha();
    vistaParciales.darClickEnLaPrimeraSesion(NOMBRE_PARCIAL);

    // Se arrepiente: la desmarca.
    vistaParciales.darClickEnLaPrimeraSesion(NOMBRE_PARCIAL);

    assertThat(vistaParciales.obtenerDiasDeRacha(), equalTo(rachaAntes));
    long horasDespues = VistaDashboard.navegar(page).obtenerHorasDeEstudio();
    assertThat(horasDespues, equalTo(horasAntes));
  }

  private void dadoQueElUsuarioInicioSesionCon(String email, String clave)
    throws MalformedURLException {
    vistaLogin.escribirEMAIL(email);
    vistaLogin.escribirClave(clave);
    vistaLogin.darClickEnIniciarSesion();
    assertThat(
      vistaLogin.obtenerURLActual().getPath(),
      matchesPattern("^/spring/dashboard(?:;jsessionid=[^/\\s]+)?$")
    );
  }

  // un parcial con un solo tema: queda una sesion de estudio de HORAS_DE_LA_SESION horas
  private void cuandoElUsuarioCreaUnParcialPara(LocalDate fecha) {
    VistaFormularioParcial formulario = VistaFormularioParcial.navegar(page);
    formulario.escribirNombre(NOMBRE_PARCIAL);
    formulario.seleccionarPrimeraMateria();
    formulario.escribirFecha(fecha);
    formulario.escribirHorario("08:00");
    formulario.escribirCantidadDeDias(1);
    formulario.escribirHorasPorDia(HORAS_DE_LA_SESION);
    formulario.escribirTemas(List.of("Repaso general"));
    formulario.darClickEnCrearPlanificacion();
  }

  //titulo del primer elemento de la tarjeta "proximos vencimientos" del dashboard
  private String primerVencimientoDelDashboard() {
    return page.locator(".deadlines-list .deadline-item strong").first().textContent().trim();
  }

  //si el primer vencimiento esta marcado como urgente
  private boolean elPrimerVencimientoEsUrgente() {
    return page.locator(".deadlines-list .deadline-item").first().locator(".text-red").count() > 0;
  }
}
