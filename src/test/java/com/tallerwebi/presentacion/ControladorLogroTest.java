package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.logro.Logro;
import com.tallerwebi.dominio.logro.LogroObtenido;
import com.tallerwebi.dominio.logro.ServicioLogro;
import com.tallerwebi.presentacion.logro.ControladorLogro;
import com.tallerwebi.presentacion.logro.DatosLogro;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLogroTest {

  private ControladorLogro controladorLogro;
  private ServicioLogro servicioLogro;
  private HttpSession session;
  private Usuario usuario;

  @BeforeEach
  public void inicializar() {
    session = mock(HttpSession.class);
    servicioLogro = mock(ServicioLogro.class);
    controladorLogro = new ControladorLogro(servicioLogro);
    usuario = new Usuario();
  }

  @Test
  public void irALogrosDeberiaDevolverVistaDeLogros() {
    dadoQueExisteUnUsuarioEnSesion();
    when(servicioLogro.obtenerLogros()).thenReturn(List.of());

    ModelAndView resultado = controladorLogro.mostrarLogros(session);

    assertEquals("paginas/logro/logros", resultado.getViewName());
  }

  @Test
  public void irALogrosSinUsuarioDeberiaRedirigirALogin() {
    when(session.getAttribute("USUARIO")).thenReturn(null);

    ModelAndView resultado = controladorLogro.mostrarLogros(session);

    assertEquals("redirect:/login", resultado.getViewName());
    verifyNoInteractions(servicioLogro);
  }

  @Test
  public void irALogrosDeberiaOtorgarLosLogrosAlcanzadosDelUsuario() {
    dadoQueExisteUnUsuarioEnSesion();
    when(servicioLogro.obtenerLogros()).thenReturn(List.of());

    controladorLogro.mostrarLogros(session);

    verify(servicioLogro).otorgarLogrosAlcanzados(usuario);
    verify(servicioLogro).obtenerLogrosObtenidos(usuario);
  }

  @Test
  public void unLogroObtenidoDeberiaMostrarseComoObtenidoConSuFecha() {
    dadoQueExisteUnUsuarioEnSesion();
    LocalDate fecha = LocalDate.of(2026, 10, 1);
    Logro popular = logro("Popular");
    when(servicioLogro.obtenerLogros()).thenReturn(List.of(popular));
    when(servicioLogro.obtenerLogrosObtenidos(any()))
      .thenReturn(List.of(new LogroObtenido(usuario, popular, fecha)));

    DatosLogro datos = primerLogro(controladorLogro.mostrarLogros(session));

    assertTrue(datos.isObtenido());
    assertEquals(fecha, datos.getFechaObtencion());
    assertEquals("fa-user-plus", datos.getIcono());
  }

  @Test
  public void unLogroNoObtenidoDeberiaMostrarseBloqueadoYSinFecha() {
    dadoQueExisteUnUsuarioEnSesion();
    when(servicioLogro.obtenerLogros()).thenReturn(List.of(logro("Maratonista")));
    when(servicioLogro.obtenerLogrosObtenidos(any())).thenReturn(List.of());

    DatosLogro datos = primerLogro(controladorLogro.mostrarLogros(session));

    assertFalse(datos.isObtenido());
    assertNull(datos.getFechaObtencion());
    assertEquals("fa-person-running", datos.getIcono());
  }

  @Test
  public void siElServicioNoDevuelveObtenidosTodosQuedanBloqueados() {
    dadoQueExisteUnUsuarioEnSesion();
    when(servicioLogro.obtenerLogros()).thenReturn(List.of(logro("Otro")));
    when(servicioLogro.obtenerLogrosObtenidos(any())).thenReturn(null);

    DatosLogro datos = primerLogro(controladorLogro.mostrarLogros(session));

    assertFalse(datos.isObtenido());
    assertEquals("fa-trophy", datos.getIcono());
  }

  private DatosLogro primerLogro(ModelAndView resultado) {
    List<?> logros = (List<?>) resultado.getModel().get("logros");
    return (DatosLogro) logros.get(0);
  }

  private void dadoQueExisteUnUsuarioEnSesion() {
    when(session.getAttribute("USUARIO")).thenReturn(usuario);
  }

  private Logro logro(String nombre) {
    Logro logro = new Logro();
    logro.setNombre(nombre);
    logro.setDescripcion("descripcion");
    return logro;
  }

  @Test
  public void cadaLogroTieneSuIconoYLosDesconocidosUnTrofeo() {
    when(session.getAttribute("USUARIO")).thenReturn(usuario);
    when(servicioLogro.obtenerLogros())
      .thenReturn(List.of(logroLlamado("Líder de grupo"), logroLlamado("Otro")));

    ModelAndView mav = controladorLogro.mostrarLogros(session);

    @SuppressWarnings("unchecked")
    List<DatosLogro> logros = (List<DatosLogro>) mav.getModel().get("logros");
    assertThat(logros.get(0).getIcono(), equalTo("fa-crown"));
    assertThat(logros.get(1).getIcono(), equalTo("fa-trophy"));
  }

  private static Logro logroLlamado(String nombre) {
    Logro logro = new Logro();
    logro.setNombre(nombre);
    return logro;
  }
}
