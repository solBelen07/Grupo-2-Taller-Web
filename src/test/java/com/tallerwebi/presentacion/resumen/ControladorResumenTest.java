package com.tallerwebi.presentacion.resumen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionResumen.ResumenInvalido;
import com.tallerwebi.dominio.excepcion.excepcionResumen.ResumenNoGenerado;
import com.tallerwebi.dominio.resumen.ResultadoResumen;
import com.tallerwebi.dominio.resumen.Resumen;
import com.tallerwebi.dominio.resumen.ServicioResumen;
import com.tallerwebi.presentacion.gemini.DatosGemini;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class ControladorResumenTest {

  private static final String VISTA = "paginas/resumen/resumen";

  private ServicioResumen servicioResumen;
  private ControladorResumen controlador;
  private MockHttpServletRequest request;
  private Usuario usuario;

  @BeforeEach
  void init() {
    servicioResumen = mock(ServicioResumen.class);
    controlador = new ControladorResumen(servicioResumen);

    usuario = new Usuario();
    usuario.setId(1L);
    request = new MockHttpServletRequest();
    request.getSession().setAttribute("USUARIO", usuario);

    when(servicioResumen.obtenerHistorial(1L)).thenReturn(List.of());
  }

  @Test
  void deberiaRedirigirALoginSiNoHayUsuarioAlMostrarLaPantalla() {
    request.getSession().removeAttribute("USUARIO");

    ModelAndView mav = controlador.mostrarPantallaDeResumen(request);

    assertEquals("redirect:/login", mav.getViewName());
    verify(servicioResumen, never()).obtenerHistorial(anyLong());
  }

  @Test
  void deberiaMostrarElFormularioVacioConElHistorialDelUsuario() {
    when(servicioResumen.obtenerHistorial(1L))
      .thenReturn(List.of(resumenGuardado(5L, "un texto", "<p>r</p>")));

    ModelAndView mav = controlador.mostrarPantallaDeResumen(request);

    assertEquals(VISTA, mav.getViewName());
    DatosGemini datos = (DatosGemini) mav.getModel().get("datosGemini");
    assertNull(datos.getRespuesta());
    assertNull(datos.getPregunta());
    List<HistorialResumenDTO> historial = historialDe(mav);
    assertEquals(1, historial.size());
    assertEquals(5L, historial.get(0).getId());
    assertEquals("un texto", historial.get(0).getVistaPrevia());
    assertFalse(mav.getModel().containsKey("error"));
  }

  @Test
  void deberiaRedirigirALoginSiNoHayUsuarioAlResumir() throws Exception {
    request.getSession().removeAttribute("USUARIO");

    ModelAndView mav = controlador.generarResumen(datos("texto"), request);

    assertEquals("redirect:/login", mav.getViewName());
    verify(servicioResumen, never()).resumir(any(Usuario.class), any(String.class));
  }

  @Test
  void deberiaMostrarElResumenGeneradoYMarcarloComoNoReutilizado() throws Exception {
    Resumen nuevo = resumenGuardado(8L, "texto", "<p><strong>Resumen</strong></p>");
    when(servicioResumen.resumir(usuario, "texto")).thenReturn(new ResultadoResumen(nuevo, false));
    when(servicioResumen.obtenerHistorial(1L)).thenReturn(List.of(nuevo));

    ModelAndView mav = controlador.generarResumen(datos("texto"), request);

    assertEquals(VISTA, mav.getViewName());
    DatosGemini mostrado = (DatosGemini) mav.getModel().get("datosGemini");
    assertEquals("<p><strong>Resumen</strong></p>", mostrado.getRespuesta());
    assertEquals("texto", mostrado.getPregunta());
    assertEquals(Boolean.FALSE, mav.getModel().get("reutilizado"));
    assertNull(mav.getModel().get("error"));
    assertEquals(1, historialDe(mav).size());
  }

  @Test
  void deberiaAvisarCuandoElResumenSalioDelHistorial() throws Exception {
    Resumen viejo = resumenGuardado(8L, "texto", "<p>guardado</p>");
    when(servicioResumen.resumir(usuario, "texto")).thenReturn(new ResultadoResumen(viejo, true));

    ModelAndView mav = controlador.generarResumen(datos("texto"), request);

    assertEquals(Boolean.TRUE, mav.getModel().get("reutilizado"));
    assertEquals(
      "<p>guardado</p>",
      ((DatosGemini) mav.getModel().get("datosGemini")).getRespuesta()
    );
  }

  @Test
  void deberiaMostrarElMensajeCuandoElTextoEsInvalido() throws Exception {
    when(servicioResumen.resumir(usuario, " "))
      .thenThrow(new ResumenInvalido("Ingresá el texto que querés resumir."));

    ModelAndView mav = controlador.generarResumen(datos(" "), request);

    assertEquals(VISTA, mav.getViewName());
    assertEquals("Ingresá el texto que querés resumir.", mav.getModel().get("error"));
    assertNull(mav.getModel().get("reutilizado"));
    assertNull(((DatosGemini) mav.getModel().get("datosGemini")).getRespuesta());
  }

  @Test
  void deberiaMostrarUnMensajeAmigableCuandoGeminiFalla() throws Exception {
    when(servicioResumen.resumir(usuario, "texto"))
      .thenThrow(new ResumenNoGenerado("No pudimos generar el resumen"));

    ModelAndView mav = controlador.generarResumen(datos("texto"), request);

    assertEquals(VISTA, mav.getViewName());
    String error = (String) mav.getModel().get("error");
    assertTrue(error.startsWith("No pudimos generar el resumen"));
    assertEquals("texto", ((DatosGemini) mav.getModel().get("datosGemini")).getPregunta());
  }

  @Test
  void deberiaRedirigirALoginSiNoHayUsuarioAlVerUnResumen() {
    request.getSession().removeAttribute("USUARIO");

    ModelAndView mav = controlador.verResumenDelHistorial(5L, request);

    assertEquals("redirect:/login", mav.getViewName());
    verify(servicioResumen, never()).buscarPorIdYUsuario(anyLong(), anyLong());
  }

  @Test
  void deberiaMostrarUnResumenDelHistorialConSuTextoOriginal() {
    Resumen guardado = resumenGuardado(5L, "texto original", "<p>resumen</p>");
    when(servicioResumen.buscarPorIdYUsuario(5L, 1L)).thenReturn(guardado);

    ModelAndView mav = controlador.verResumenDelHistorial(5L, request);

    assertEquals(VISTA, mav.getViewName());
    DatosGemini mostrado = (DatosGemini) mav.getModel().get("datosGemini");
    assertEquals("texto original", mostrado.getPregunta());
    assertEquals("<p>resumen</p>", mostrado.getRespuesta());
    assertFalse(mav.getModel().containsKey("reutilizado"));
  }

  @Test
  void deberiaVolverAlResumenSiElIdNoExisteONoEsDelUsuario() {
    when(servicioResumen.buscarPorIdYUsuario(99L, 1L)).thenReturn(null);

    ModelAndView mav = controlador.verResumenDelHistorial(99L, request);

    assertEquals("redirect:/resumen", mav.getViewName());
  }

  @Test
  void elModeloSiempreIncluyeElHistorialAunqueEsteVacio() {
    ModelAndView mav = controlador.mostrarPantallaDeResumen(request);

    Map<String, Object> modelo = mav.getModel();
    assertTrue(modelo.containsKey("historial"));
    assertEquals(0, historialDe(mav).size());
  }

  private DatosGemini datos(String pregunta) {
    DatosGemini datos = new DatosGemini();
    datos.setPregunta(pregunta);
    return datos;
  }

  private Resumen resumenGuardado(Long id, String texto, String html) {
    Resumen resumen = new Resumen(
      usuario,
      texto,
      "huella",
      html,
      LocalDateTime.of(2026, 11, 4, 10, 0)
    );
    resumen.setId(id);
    return resumen;
  }

  @SuppressWarnings("unchecked")
  private List<HistorialResumenDTO> historialDe(ModelAndView mav) {
    return (List<HistorialResumenDTO>) mav.getModel().get("historial");
  }
}
