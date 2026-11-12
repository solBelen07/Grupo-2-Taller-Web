package com.tallerwebi.punta_a_punta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaBoleto;
import com.tallerwebi.punta_a_punta.vistas.VistaDashboard;
import com.tallerwebi.punta_a_punta.vistas.VistaFocusFlight;
import com.tallerwebi.punta_a_punta.vistas.VistaFormularioParcial;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import com.tallerwebi.punta_a_punta.vistas.VistaTimer;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VistaFocusFlightE2E {

  private static final String EMAIL_USUARIO = "test@unlam.edu.ar";
  private static final String CLAVE_USUARIO = "test";
  private static final int DURACION_VUELO_MINUTOS = 60;
  private static final int DIAS_HASTA_SESIONES_DEL_SEED = 10;

  static Playwright playwright;
  static Browser browser;
  BrowserContext context;
  Page page;
  VistaLogin vistaLogin;
  VistaDashboard vistaDashboard;

  LocalDate hoy;
  LocalDate maniana;

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

    hoy = LocalDate.now();
    maniana = hoy.plusDays(1);
    ReiniciarDB.moverSesionesPendientesA(hoy.plusDays(DIAS_HASTA_SESIONES_DEL_SEED));

    context = browser.newContext();
    page = context.newPage();
    vistaLogin = new VistaLogin(page);
  }

  @AfterEach
  void cerrarContexto() {
    context.close();
  }

  @Test
  void alPlanificarUnParcialParaManianaYCompletarUnVueloSeActualizanLaProximaSesionYLasHoras()
    throws MalformedURLException {
    dadoQueElUsuarioInicioSesionCon(EMAIL_USUARIO, CLAVE_USUARIO);
    long horasIniciales = dadoQueElUsuarioTieneRegistradasCiertasHorasDeEstudio();

    long idVuelo = cuandoElUsuarioIniciaUnVueloDe(DURACION_VUELO_MINUTOS);
    entoncesLaProximaSesionDelDashboardEsPosteriorA(maniana);

    cuandoElUsuarioPlanificaUnParcialPara(
      maniana,
      1,
      3,
      List.of("Capas del proyecto", "Implementaciones", "Integraciones")
    );
    cuandoTerminaElVuelo(idVuelo, DURACION_VUELO_MINUTOS);

    entoncesLaProximaSesionDelDashboardEs(hoy);
    entoncesLasHorasDeEstudioRegistradasSon(horasIniciales + DURACION_VUELO_MINUTOS / 60);
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

  private long dadoQueElUsuarioTieneRegistradasCiertasHorasDeEstudio() {
    vistaDashboard = VistaDashboard.navegar(page);
    return vistaDashboard.obtenerHorasDeEstudio();
  }

  private long cuandoElUsuarioIniciaUnVueloDe(int minutos) throws MalformedURLException {
    VistaFocusFlight vistaFocusFlight = VistaFocusFlight.navegar(page);
    vistaFocusFlight.escribirDestino("ESP");
    vistaFocusFlight.seleccionarDuracion(minutos);
    vistaFocusFlight.seleccionarPrimeraMateria();
    vistaFocusFlight.escribirObjetivo("Repasar para el parcial");

    VistaBoleto vistaBoleto = vistaFocusFlight.darClickEnIniciarVuelo();
    VistaTimer vistaTimer = vistaBoleto.darClickEnEmbarcar();

    assertThat(
      vistaTimer.obtenerURLActual().getPath(),
      matchesPattern("^/spring/focusflight/timer/\\d+(?:;jsessionid=[^/\\s]+)?$")
    );
    return vistaTimer.obtenerIdDelVuelo();
  }

  private void cuandoElUsuarioPlanificaUnParcialPara(
    LocalDate fecha,
    int diasDeEstudio,
    int horasPorDia,
    List<String> temas
  ) {
    VistaFormularioParcial vistaFormulario = VistaFormularioParcial.navegar(page);
    vistaFormulario.escribirNombre("1er Parcial - Unidades 1, 2 y 3");
    vistaFormulario.seleccionarPrimeraMateria();
    vistaFormulario.escribirFecha(fecha);
    vistaFormulario.escribirHorario("10:00");
    vistaFormulario.escribirCantidadDeDias(diasDeEstudio);
    vistaFormulario.escribirHorasPorDia(horasPorDia);
    vistaFormulario.escribirTemas(temas);
    vistaFormulario.darClickEnCrearPlanificacion();
  }

  private void cuandoTerminaElVuelo(long idVuelo, int duracionMinutos) {
    ReiniciarDB.adelantarVuelosEnCurso(duracionMinutos + 1);
    VistaTimer.navegar(page, idVuelo);
  }

  private void entoncesLaProximaSesionDelDashboardEsPosteriorA(LocalDate fecha) {
    vistaDashboard = VistaDashboard.navegar(page);
    LocalDate proximaSesion = vistaDashboard.obtenerFechaProximaSesion(hoy);
    assertThat(proximaSesion, notNullValue());
    assertThat(proximaSesion, greaterThan(fecha));
  }

  private void entoncesLaProximaSesionDelDashboardEs(LocalDate fecha) {
    vistaDashboard = VistaDashboard.navegar(page);
    assertThat(vistaDashboard.obtenerFechaProximaSesion(hoy), equalTo(fecha));
  }

  private void entoncesLasHorasDeEstudioRegistradasSon(long horas) {
    assertThat(vistaDashboard.obtenerHorasDeEstudio(), equalTo(horas));
  }
}
