package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.logro.ServicioLogro;
import com.tallerwebi.presentacion.logro.ControladorLogro;
import com.tallerwebi.presentacion.logro.DatosLogro;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLogroTest {

  private ControladorLogro controladorLogro;
  private ServicioLogro servicioLogro;
  private HttpSession request;

  @BeforeEach
  public void inicializar() {
    request = mock(HttpSession.class);
    servicioLogro = mock(ServicioLogro.class);
    controladorLogro = new ControladorLogro(servicioLogro);
  }

  @Test
  public void irALogrosDeberiaDevolverVistaDeLogros() {
    dadoQueExisteUnUsuarioEnSesion();
    when(servicioLogro.obtenerProgresos(any())).thenReturn(List.of());
    ModelAndView resultado = controladorLogro.mostrarLogros(request);
    assert resultado.getViewName().equals("paginas/logro/logros");
  }

  @Test
  public void irALogrosSinUsuarioDeberiaRedirigirALogin() {
    when(request.getAttribute("USUARIO")).thenReturn(null);
    ModelAndView resultado = controladorLogro.mostrarLogros(request);
    assert resultado.getViewName().equals("redirect:/login");
  }

  @Test
  public void irALogrosConUsuarioDeberiaLlamarAServicioLogro() {
    dadoQueExisteUnUsuarioEnSesion();
    when(servicioLogro.obtenerProgresos(any())).thenReturn(List.of());
    controladorLogro.mostrarLogros(request);
    assert servicioLogro.obtenerProgresos(any()) != null;
  }

  @Test
  public void irALogrosConUsuarioDeberiaPasarLogrosAModelo() {
    dadoQueExisteUnUsuarioEnSesion();
    dadoQueElUsuarioTieneLogrosObtenidos();
    ModelAndView resultado = controladorLogro.mostrarLogros(request);
    assert resultado.getModel().containsKey("logros");
  }

  @Test
  public void irALogrosConLogrosObtenidosDeberiaMarcarLogrosComoObtenidos() {
    dadoQueExisteUnUsuarioEnSesion();
    dadoQueElUsuarioTieneLogrosObtenidos();

    ModelAndView resultado = controladorLogro.mostrarLogros(request);
    List<?> logros = (List<?>) resultado.getModel().get("logros");
    for (Object logro : logros) {
      assert logro instanceof DatosLogro;
      DatosLogro datosLogro = (DatosLogro) logro;
      assertTrue(datosLogro.isObtenido());
    }
  }

  @Test
  public void irALogrosConLogrosNoObtenidosDeberiaMarcarLogrosComoNoObtenidos() {
    dadoQueExisteUnUsuarioEnSesion();
    dadoQueElUsuarioTieneLogrosNoObtenidos();

    ModelAndView resultado = controladorLogro.mostrarLogros(request);
    List<?> logros = (List<?>) resultado.getModel().get("logros");
    for (Object logro : logros) {
      assert logro instanceof DatosLogro;
      DatosLogro datosLogro = (DatosLogro) logro;
      assertFalse(datosLogro.isObtenido());
    }
  }

  private void dadoQueExisteUnUsuarioEnSesion() {
    Usuario usuario = new Usuario();
    when(request.getAttribute("USUARIO")).thenReturn(usuario);
  }

  private void dadoQueElUsuarioTieneLogrosObtenidos() {
    when(servicioLogro.obtenerLogros()).thenReturn(List.of());
    when(servicioLogro.obtenerProgresos(any())).thenReturn(List.of());
  }

  private void dadoQueElUsuarioTieneLogrosNoObtenidos() {
    when(servicioLogro.obtenerLogros()).thenReturn(List.of());
    when(servicioLogro.obtenerProgresos(any())).thenReturn(null);
  }
}
