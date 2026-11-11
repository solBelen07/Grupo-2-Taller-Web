package com.tallerwebi.dominio.resumen;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionResumen.ResumenInvalido;
import com.tallerwebi.dominio.excepcion.excepcionResumen.ResumenNoGenerado;
import com.tallerwebi.dominio.gemini.ServicioGemini;
import com.tallerwebi.dominio.gemini.errores.ErroresGemini;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioResumenTest {

  private static final Long ID_USUARIO = 7L;
  private static final String MARCA_HOLA_MUNDO =
    "0b894166d3336435c800bea36ff21b29eaa801a52f584c006c49289a0dcf6e2f";

  private RepositorioResumen repositorioResumen;
  private ServicioGemini servicioGemini;
  private ServicioResumenImpl servicio;
  private Usuario usuario;

  @BeforeEach
  void init() {
    repositorioResumen = mock(RepositorioResumen.class);
    servicioGemini = mock(ServicioGemini.class);
    Clock reloj = Clock.fixed(
      Instant.parse("2026-11-04T15:30:00Z"),
      ZoneId.of("America/Argentina/Buenos_Aires")
    );
    servicio = new ServicioResumenImpl(repositorioResumen, servicioGemini, reloj);

    usuario = new Usuario();
    usuario.setId(ID_USUARIO);
  }

  @Test
  void deberiaRechazarUnTextoNull() {
    assertThrows(ResumenInvalido.class, () -> servicio.resumir(usuario, null));
    verify(servicioGemini, never()).preguntar(anyString());
  }

  @Test
  void deberiaRechazarUnTextoVacioOSoloEspacios() {
    assertThrows(ResumenInvalido.class, () -> servicio.resumir(usuario, ""));
    assertThrows(ResumenInvalido.class, () -> servicio.resumir(usuario, "  \n\t "));

    verify(servicioGemini, never()).preguntar(anyString());
  }

  @Test
  void deberiaRechazarUnTextoMasLargoQueElMaximo() {
    String demasiadoLargo = "a".repeat(ServicioResumenImpl.MAXIMO_CARACTERES + 1);

    ResumenInvalido error = assertThrows(
      ResumenInvalido.class,
      () -> servicio.resumir(usuario, demasiadoLargo)
    );

    assertTrue(error.getMessage().contains(String.valueOf(ServicioResumenImpl.MAXIMO_CARACTERES)));
    verify(servicioGemini, never()).preguntar(anyString());
    verify(repositorioResumen, never()).guardar(any(Resumen.class));
  }

  @Test
  void deberiaAceptarUnTextoJustoEnElMaximo() throws Exception {
    String enElLimite = "a".repeat(ServicioResumenImpl.MAXIMO_CARACTERES);
    when(servicioGemini.preguntar(enElLimite)).thenReturn("<p>ok</p>\n");

    ResultadoResumen resultado = servicio.resumir(usuario, enElLimite);

    assertFalse(resultado.reutilizado());
  }

  @Test
  void elLargoSeMideSinContarLosEspaciosDeLosExtremos() throws Exception {
    String texto = "a".repeat(ServicioResumenImpl.MAXIMO_CARACTERES);
    when(servicioGemini.preguntar(texto)).thenReturn("<p>ok</p>\n");

    ResultadoResumen resultado = servicio.resumir(usuario, "   " + texto + "   ");

    assertFalse(resultado.reutilizado());
  }

  // ---------- texto nuevo: se consulta a Gemini y se guarda ----------

  @Test
  void deberiaConsultarAGeminiYGuardarElResumenSiElTextoEsNuevo() throws Exception {
    when(repositorioResumen.buscarPorUsuarioYMarca(eq(ID_USUARIO), anyString())).thenReturn(null);
    when(servicioGemini.preguntar("hola mundo")).thenReturn("<p>Resumen</p>\n");

    ResultadoResumen resultado = servicio.resumir(usuario, "  hola mundo \n");

    assertFalse(resultado.reutilizado());
    ArgumentCaptor<Resumen> captor = ArgumentCaptor.forClass(Resumen.class);
    verify(repositorioResumen).guardar(captor.capture());
    Resumen guardado = captor.getValue();
    assertSame(guardado, resultado.resumen());
    assertSame(usuario, guardado.getUsuario());
    assertEquals("hola mundo", guardado.getTextoOriginal());
    assertEquals("<p>Resumen</p>\n", guardado.getRespuestaHtml());
    assertEquals(MARCA_HOLA_MUNDO, guardado.getMarca());
    assertEquals(LocalDateTime.of(2026, 11, 4, 12, 30), guardado.getFechaCreacion());
    verify(repositorioResumen, never()).actualizar(any(Resumen.class));
  }

  // ---------- texto repetido ----------

  @Test
  void deberiaReutilizarElResumenGuardadoSinLlamarAGemini() throws Exception {
    Resumen existente = new Resumen(usuario, "hola mundo", MARCA_HOLA_MUNDO, "<p>viejo</p>", null);
    when(repositorioResumen.buscarPorUsuarioYMarca(ID_USUARIO, MARCA_HOLA_MUNDO))
      .thenReturn(existente);

    ResultadoResumen resultado = servicio.resumir(usuario, "hola mundo");

    assertTrue(resultado.reutilizado());
    assertSame(existente, resultado.resumen());
    verify(repositorioResumen).actualizar(existente);
    verify(servicioGemini, never()).preguntar(anyString());
    verify(repositorioResumen, never()).guardar(any(Resumen.class));
  }

  @Test
  void elMismoTextoConOtroFormatoDeEspaciosSeReconoceComoRepetido() throws Exception {
    Resumen existente = new Resumen(usuario, "hola mundo", MARCA_HOLA_MUNDO, "<p>viejo</p>", null);
    when(repositorioResumen.buscarPorUsuarioYMarca(ID_USUARIO, MARCA_HOLA_MUNDO))
      .thenReturn(existente);

    ResultadoResumen resultado = servicio.resumir(usuario, "hola \n\n   mundo");

    assertTrue(resultado.reutilizado());
    verify(servicioGemini, never()).preguntar(anyString());
  }

  @Test
  void cadaReutilizacionSeVaSumandoEnElHistorial() throws Exception {
    Resumen existente = new Resumen(usuario, "hola mundo", MARCA_HOLA_MUNDO, "<p>viejo</p>", null);
    when(repositorioResumen.buscarPorUsuarioYMarca(ID_USUARIO, MARCA_HOLA_MUNDO))
      .thenReturn(existente);

    servicio.resumir(usuario, "hola mundo");
    servicio.resumir(usuario, "hola mundo");

    verify(repositorioResumen, times(2)).actualizar(existente);
  }

  // ---------- fallas de Gemini: no se guardan ----------

  @Test
  void noDeberiaGuardarNadaSiGeminiDevuelveUnErrorDePeticion() {
    when(servicioGemini.preguntar("texto"))
      .thenReturn(ErroresGemini.ERROR_AL_ARMAR_LA_PETICION + "timeout");

    assertThrows(ResumenNoGenerado.class, () -> servicio.resumir(usuario, "texto"));

    verify(repositorioResumen, never()).guardar(any(Resumen.class));
  }

  @Test
  void noDeberiaGuardarNadaSiGeminiDevuelveUnErrorDeRespuesta() {
    when(servicioGemini.preguntar("texto"))
      .thenReturn("<p>" + ErroresGemini.ERROR_PROCESANDO_RESPUESTA + "null</p>\n");

    assertThrows(ResumenNoGenerado.class, () -> servicio.resumir(usuario, "texto"));

    verify(repositorioResumen, never()).guardar(any(Resumen.class));
  }

  @Test
  void noDeberiaGuardarNadaSiGeminiDevuelveVacioONulo() {
    when(servicioGemini.preguntar("vacio")).thenReturn("");
    when(servicioGemini.preguntar("nulo")).thenReturn(null);

    assertThrows(ResumenNoGenerado.class, () -> servicio.resumir(usuario, "vacio"));
    assertThrows(ResumenNoGenerado.class, () -> servicio.resumir(usuario, "nulo"));

    verify(repositorioResumen, never()).guardar(any(Resumen.class));
  }

  @Test
  void deberiaObtenerElHistorialDelUsuario() {
    List<Resumen> historial = List.of(new Resumen(), new Resumen());
    when(repositorioResumen.buscarPorUsuario(ID_USUARIO)).thenReturn(historial);

    assertSame(historial, servicio.obtenerHistorial(ID_USUARIO));
  }

  @Test
  void deberiaBuscarUnResumenSoloSiEsDelUsuario() {
    Resumen propio = new Resumen();
    when(repositorioResumen.buscarPorIdYUsuario(3L, ID_USUARIO)).thenReturn(propio);

    assertSame(propio, servicio.buscarPorIdYUsuario(3L, ID_USUARIO));
    assertNull(servicio.buscarPorIdYUsuario(3L, 99L));
  }

  @Test
  void laHuellaEsUnSha256EnHexadecimalDeterminista() {
    assertEquals(MARCA_HOLA_MUNDO, ServicioResumenImpl.calcularMarca("hola mundo", "SHA-256"));
    assertEquals(64, ServicioResumenImpl.calcularMarca("cualquier texto", "SHA-256").length());
  }

  @Test
  void laHuellaIgnoraLasDiferenciasDeEspaciosYSaltosDeLinea() {
    assertEquals(
      ServicioResumenImpl.calcularMarca("hola mundo", "SHA-256"),
      ServicioResumenImpl.calcularMarca("hola \t\n  mundo", "SHA-256")
    );
  }

  @Test
  void textosDistintosTienenHuellasDistintas() {
    assertNotEquals(
      ServicioResumenImpl.calcularMarca("hola mundo", "SHA-256"),
      ServicioResumenImpl.calcularMarca("hola mundo!", "SHA-256")
    );
  }

  @Test
  void deberiaFallarConUnAlgoritmoDeHuellaInexistente() {
    IllegalStateException error = assertThrows(
      IllegalStateException.class,
      () -> ServicioResumenImpl.calcularMarca("texto", "ALGORITMO-INEXISTENTE")
    );

    assertTrue(error.getCause() instanceof NoSuchAlgorithmException);
    assertTrue(error.getMessage().contains("ALGORITMO-INEXISTENTE"));
  }
}
