package com.tallerwebi.punta_a_punta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaFormularioGrupo;
import com.tallerwebi.punta_a_punta.vistas.VistaGrupos;
import com.tallerwebi.punta_a_punta.vistas.VistaInvitarGrupo;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VistaGrupoE2E {

  private static final String EMAIL_USUARIO = "test@unlam.edu.ar";
  private static final String CLAVE_USUARIO = "test";
  private static final String EMAIL_INVITADO = "user6@test.com";
  private static final String NOMBRE_GRUPO = "grupo-test";

  static Playwright playwright;
  static Browser browser;
  BrowserContext context;
  Page page;
  VistaLogin vistaLogin;
  VistaGrupos vistaGrupos;
  VistaFormularioGrupo vistaFormularioGrupo;
  VistaInvitarGrupo vistaInvitarGrupo;

  @BeforeAll
  static void abrirNavegador() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch();
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
  void deberiaCrearUnGrupoYVerloEnElListado() throws MalformedURLException {
    dadoQueElUsuarioInicioSesionCon(EMAIL_USUARIO, CLAVE_USUARIO);
    dadoQueElUsuarioEstaEnLaVistaDeGrupos();
    cuandoElUsuarioCreaUnGrupoLlamado(NOMBRE_GRUPO);
    entoncesDeberiaSerRedirigidoALaVistaDeGrupos();
    entoncesDeberiaVerElMensajeDeExito(
      "Creaste el grupo " + NOMBRE_GRUPO + ". Sos su administrador/a."
    );
    entoncesDeberiaVerElGrupoEnElListado(NOMBRE_GRUPO);
  }

  @Test
  void deberiaCrearUnGrupoEInvitarAUnUsuarioExistente() throws MalformedURLException {
    dadoQueElUsuarioInicioSesionCon(EMAIL_USUARIO, CLAVE_USUARIO);
    dadoQueElUsuarioEstaEnLaVistaDeGrupos();
    dadoQueElUsuarioCreoUnGrupoLlamado(NOMBRE_GRUPO);
    cuandoElUsuarioInvitaAlGrupo(NOMBRE_GRUPO, EMAIL_INVITADO);
    entoncesDeberiaSerRedirigidoALaVistaDeGrupos();
    entoncesDeberiaVerElMensajeDeExito("Invitación enviada");
  }

  @Test
  void deberiaDarUnErrorAlInvitarAUnUsuarioQueNoExiste() throws MalformedURLException {
    dadoQueElUsuarioInicioSesionCon(EMAIL_USUARIO, CLAVE_USUARIO);
    dadoQueElUsuarioEstaEnLaVistaDeGrupos();
    dadoQueElUsuarioCreoUnGrupoLlamado(NOMBRE_GRUPO);
    cuandoElUsuarioInvitaAlGrupo(NOMBRE_GRUPO, "noexiste@unlam.edu.ar");
    entoncesDeberiaSeguirEnLaVistaDeInvitar();
    entoncesDeberiaVerElErrorDeInvitacion("Invitación inválida");
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

  private void dadoQueElUsuarioEstaEnLaVistaDeGrupos() {
    vistaGrupos = VistaGrupos.navegar(page);
    assertThat(vistaGrupos.obtenerTituloPrincipal(), equalToCompressingWhiteSpace("Grupos"));
  }

  private void dadoQueElUsuarioCreoUnGrupoLlamado(String nombre) {
    cuandoElUsuarioCreaUnGrupoLlamado(nombre);
  }

  private void cuandoElUsuarioCreaUnGrupoLlamado(String nombre) {
    vistaFormularioGrupo = vistaGrupos.darClickEnCrearGrupo();
    vistaFormularioGrupo.escribirNombreDelGrupo(nombre);
    vistaGrupos = vistaFormularioGrupo.darClickEnCrearGrupo();
  }

  private void cuandoElUsuarioInvitaAlGrupo(String nombreGrupo, String emailInvitado) {
    vistaInvitarGrupo = vistaGrupos.darClickEnInvitarAlGrupo(nombreGrupo);
    vistaInvitarGrupo.escribirEmailDelInvitado(emailInvitado);
    vistaGrupos = vistaInvitarGrupo.darClickEnEnviarInvitacion();
  }

  private void entoncesDeberiaSerRedirigidoALaVistaDeGrupos() throws MalformedURLException {
    URL url = vistaGrupos.obtenerURLActual();
    assertThat(url.getPath(), matchesPattern("^/spring/grupos(?:;jsessionid=[^/\\s]+)?$"));
  }

  private void entoncesDeberiaSeguirEnLaVistaDeInvitar() throws MalformedURLException {
    URL url = vistaInvitarGrupo.obtenerURLActual();
    assertThat(
      url.getPath(),
      matchesPattern("^/spring/grupos/.+/invitar(?:;jsessionid=[^/\\s]+)?$")
    );
  }

  private void entoncesDeberiaVerElMensajeDeExito(String mensaje) {
    assertThat(vistaGrupos.obtenerMensajeDeExito(), equalToCompressingWhiteSpace(mensaje));
  }

  private void entoncesDeberiaVerElGrupoEnElListado(String nombre) {
    List<String> nombres = vistaGrupos.obtenerNombresDeLosGrupos();
    assertThat(nombres, hasItem(nombre));
  }

  private void entoncesDeberiaVerElErrorDeInvitacion(String mensaje) {
    assertThat(vistaInvitarGrupo.obtenerMensajeDeError(), equalToCompressingWhiteSpace(mensaje));
  }
}
