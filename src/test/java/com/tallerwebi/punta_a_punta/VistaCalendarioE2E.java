package com.tallerwebi.punta_a_punta;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.tallerwebi.punta_a_punta.vistas.VistaCalendario;
import com.tallerwebi.punta_a_punta.vistas.VistaLogin;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * E2E del calendario. Solo toca la tabla {@code Evento} (propia de esta funcionalidad, sin uso en
 * ningún otro test): no comparte ni modifica el reset de {@link ReiniciarDB}, que es de
 * {@code VistaLoginE2E}. Usa el usuario y las materias que ya siembra {@code data.sql} en el
 * contenedor (usuario {@code test@unlam.edu.ar} id 1; materias "Taller Web I" id 1 y "Base de
 * Datos II" id 2).
 */
public class VistaCalendarioE2E {

  private static final Long USUARIO_ID = 1L;

  static Playwright playwright;
  static Browser browser;
  BrowserContext context;
  VistaLogin vistaLogin;
  VistaCalendario vistaCalendario;

  @BeforeAll
  static void abrirNavegador() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch();
  }

  @AfterAll
  static void cerrarNavegador() {
    playwright.close();
  }

  @BeforeEach
  void crearContextoYLoguearse() {
    limpiarEventos();
    sembrarEventosDeHoy();

    context = browser.newContext();
    Page page = context.newPage();
    vistaLogin = new VistaLogin(page);
    dadoQueElUsuarioIniciaSesionCon("test@unlam.edu.ar", "test");
    vistaCalendario = new VistaCalendario(page);
  }

  @AfterEach
  void cerrarContexto() {
    context.close();
  }

  @Test
  void deberiaMostrarLaVistaMensualPorDefectoConLasMateriasDelUsuario() {
    entoncesDeberiaVerLaVistaMensual();
    entoncesDeberiaVerDosMateriasEnLaLeyenda();
  }

  @Test
  void deberiaMostrarLaVistaSemanalConBloquesDeHoras() {
    cuandoElUsuarioTocaVerSemana();
    entoncesDeberiaVerLaVistaSemanalConAlMenosUnBloque();
  }

  @Test
  void deberiaNavegarAlPeriodoSiguienteYVolverAHoy() {
    String tituloActual = vistaCalendario.obtenerTituloDelPeriodo();

    cuandoElUsuarioTocaSiguiente();
    entoncesElTituloDelPeriodoCambia(tituloActual);

    cuandoElUsuarioTocaHoy();
    entoncesElTituloDelPeriodoVuelveA(tituloActual);
  }

  private void dadoQueElUsuarioIniciaSesionCon(String email, String clave) {
    vistaLogin.escribirEMAIL(email);
    vistaLogin.escribirClave(clave);
    vistaLogin.darClickEnIniciarSesion();
  }

  private void cuandoElUsuarioTocaVerSemana() {
    vistaCalendario.darClickEnVerSemana();
  }

  private void cuandoElUsuarioTocaSiguiente() {
    vistaCalendario.darClickEnSiguiente();
  }

  private void cuandoElUsuarioTocaHoy() {
    vistaCalendario.darClickEnHoy();
  }

  private void entoncesDeberiaVerLaVistaMensual() {
    assertThat(vistaCalendario.estaVisibleLaVistaMensual(), is(true));
  }

  private void entoncesDeberiaVerDosMateriasEnLaLeyenda() {
    assertThat(vistaCalendario.contarMateriasEnLaLeyenda(), is(2));
  }

  private void entoncesDeberiaVerLaVistaSemanalConAlMenosUnBloque() {
    assertThat(vistaCalendario.estaVisibleLaVistaSemanal(), is(true));
    assertThat(vistaCalendario.contarBloquesDeLaSemana(), is(greaterThan(0)));
  }

  private void entoncesElTituloDelPeriodoCambia(String tituloAnterior) {
    assertThat(vistaCalendario.obtenerTituloDelPeriodo(), is(not(tituloAnterior)));
  }

  private void entoncesElTituloDelPeriodoVuelveA(String tituloEsperado) {
    assertThat(vistaCalendario.obtenerTituloDelPeriodo(), is(tituloEsperado));
  }

  private static void limpiarEventos() {
    ejecutarSql("DELETE FROM Evento;");
  }

  private static void sembrarEventosDeHoy() {
    String sql =
      "INSERT INTO Evento(id, usuarioId, titulo, tipo, inicio, fin, materia_id) " +
      "VALUES(null, " +
      USUARIO_ID +
      ", 'Clase de Taller Web I', 'CLASE', CONCAT(CURDATE(), ' 18:00:00'), " +
      "CONCAT(CURDATE(), ' 22:00:00'), 1);\n" +
      "INSERT INTO Evento(id, usuarioId, titulo, tipo, inicio, fin, materia_id) " +
      "VALUES(null, " +
      USUARIO_ID +
      ", 'Práctica de Base de Datos II', 'CLASE', CONCAT(CURDATE(), ' 08:00:00'), " +
      "CONCAT(CURDATE(), ' 10:00:00'), 2);";
    ejecutarSql(sql);
  }

  /** Corre SQL contra el MySQL de Docker, tocando solo la tabla Evento (propia de CAL-01). */
  private static void ejecutarSql(String sqlCommands) {
    try {
      String dbHost = System.getenv().getOrDefault("DB_HOST", "localhost");
      String dbPort = System.getenv().getOrDefault("DB_PORT", "3306");
      String dbName = System.getenv().getOrDefault("DB_NAME", "tallerwebi");
      String dbUser = System.getenv().getOrDefault("DB_USER", "user");
      String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "user");

      String comando = String.format(
        "docker exec tallerwebi-mysql mysql -h %s -P %s -u %s -p%s %s -e \"%s\"",
        dbHost,
        dbPort,
        dbUser,
        dbPassword,
        dbName,
        sqlCommands
      );

      Process process = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", comando });
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      System.err.println("Error preparando eventos para el E2E del calendario: " + e.getMessage());
      Thread.currentThread().interrupt();
    }
  }
}
