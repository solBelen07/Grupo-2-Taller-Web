package com.tallerwebi.presentacion.perfil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.catalogo.ServicioCatalogo;
import com.tallerwebi.dominio.perfil.DatosCambioPassword;
import com.tallerwebi.dominio.perfil.DatosPerfil;
import com.tallerwebi.dominio.perfil.FotoPerfil;
import com.tallerwebi.dominio.perfil.ServicioPerfil;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class ControladorPerfilTest {

  private static final String EMAIL = "ana@test.com";

  private ControladorPerfil controlador;
  private ServicioPerfil servicioPerfilMock;
  private ServicioCatalogo servicioCatalogoMock;
  private RedirectAttributes flashMock;
  private MockHttpServletRequest requestConSesion;
  private Usuario usuario;

  @BeforeEach
  public void init() {
    servicioPerfilMock = mock(ServicioPerfil.class);
    servicioCatalogoMock = mock(ServicioCatalogo.class);
    flashMock = mock(RedirectAttributes.class);
    controlador = new ControladorPerfil(servicioPerfilMock, servicioCatalogoMock);

    usuario = new Usuario();
    usuario.setId(7L);
    usuario.setEmail(EMAIL);
    usuario.setNombre("Ana");
    usuario.setApellido("Pérez");

    requestConSesion = new MockHttpServletRequest();
    requestConSesion.getSession().setAttribute("USUARIO", usuario);

    when(servicioCatalogoMock.obtenerUniversidades()).thenReturn(List.of());
    when(servicioCatalogoMock.obtenerCarreras()).thenReturn(List.of());
    when(servicioCatalogoMock.obtenerIntereses()).thenReturn(List.of());
  }

  // ---------- ver perfil ----------

  @Test
  public void verPerfilSinSesionDeberiaRedirigirALogin() {
    ModelAndView mav = controlador.verPerfil(new MockHttpServletRequest());

    assertThat(mav.getViewName(), equalTo("redirect:/login"));
    verifyNoInteractions(servicioPerfilMock);
  }

  @Test
  public void verPerfilConSesionDeberiaCargarLaVistaConDatosYCatalogos() {
    when(servicioPerfilMock.obtenerUsuario(EMAIL)).thenReturn(usuario);
    when(servicioPerfilMock.tieneFoto(7L)).thenReturn(true);

    ModelAndView mav = controlador.verPerfil(requestConSesion);

    assertThat(mav.getViewName(), equalTo("paginas/perfil/perfil"));
    assertThat(mav.getModel().get("perfil"), sameInstance(usuario));
    assertThat(mav.getModel().get("datos"), instanceOf(DatosPerfil.class));
    assertThat(mav.getModel().get("cambioPassword"), instanceOf(DatosCambioPassword.class));
    assertThat(mav.getModel().get("tieneFoto"), equalTo(true));
    assertThat(mav.getModel().containsKey("universidades"), is(true));
    assertThat(mav.getModel().containsKey("carreras"), is(true));
    assertThat(mav.getModel().containsKey("intereses"), is(true));
    assertThat(((DatosPerfil) mav.getModel().get("datos")).getNombre(), equalTo("Ana"));
  }

  // ---------- datos ----------

  @Test
  public void guardarDatosSinSesionDeberiaRedirigirALogin() {
    ModelAndView mav = controlador.guardarDatos(
      new DatosPerfil(),
      new MockHttpServletRequest(),
      flashMock
    );

    assertThat(mav.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void guardarDatosValidosDeberiaActualizarSesionYAvisar() {
    DatosPerfil datos = new DatosPerfil();
    Usuario actualizado = new Usuario();
    actualizado.setEmail(EMAIL);
    when(servicioPerfilMock.actualizarDatos(EMAIL, datos)).thenReturn(actualizado);

    ModelAndView mav = controlador.guardarDatos(datos, requestConSesion, flashMock);

    assertThat(mav.getViewName(), equalTo("redirect:/perfil"));
    assertThat(requestConSesion.getSession().getAttribute("USUARIO"), sameInstance(actualizado));
    verify(flashMock).addFlashAttribute("mensaje", "Datos actualizados");
  }

  @Test
  public void guardarDatosInvalidosDeberiaMostrarElErrorYRedirigir() {
    DatosPerfil datos = new DatosPerfil();
    when(servicioPerfilMock.actualizarDatos(EMAIL, datos))
      .thenThrow(new IllegalArgumentException("Completá nombre y apellido"));

    ModelAndView mav = controlador.guardarDatos(datos, requestConSesion, flashMock);

    assertThat(mav.getViewName(), equalTo("redirect:/perfil"));
    verify(flashMock).addFlashAttribute("error", "Completá nombre y apellido");
  }

  // ---------- password ----------

  @Test
  public void cambiarPasswordSinSesionDeberiaRedirigirALogin() {
    ModelAndView mav = controlador.cambiarPassword(
      new DatosCambioPassword(),
      new MockHttpServletRequest(),
      flashMock
    );

    assertThat(mav.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void cambiarPasswordValidaDeberiaAvisar() {
    DatosCambioPassword cambio = new DatosCambioPassword();

    ModelAndView mav = controlador.cambiarPassword(cambio, requestConSesion, flashMock);

    assertThat(mav.getViewName(), equalTo("redirect:/perfil"));
    verify(servicioPerfilMock).cambiarPassword(EMAIL, cambio);
    verify(flashMock).addFlashAttribute("mensaje", "Contraseña actualizada");
  }

  @Test
  public void cambiarPasswordInvalidaDeberiaMostrarElError() {
    DatosCambioPassword cambio = new DatosCambioPassword();
    doThrow(new IllegalArgumentException("La contraseña actual no es correcta"))
      .when(servicioPerfilMock)
      .cambiarPassword(EMAIL, cambio);

    controlador.cambiarPassword(cambio, requestConSesion, flashMock);

    verify(flashMock).addFlashAttribute("error", "La contraseña actual no es correcta");
  }

  // ---------- foto ----------

  @Test
  public void subirFotoSinSesionDeberiaRedirigirALogin() {
    MockMultipartFile archivo = new MockMultipartFile(
      "foto",
      "a.png",
      "image/png",
      new byte[] { 1 }
    );

    ModelAndView mav = controlador.subirFoto(archivo, new MockHttpServletRequest(), flashMock);

    assertThat(mav.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void subirFotoValidaDeberiaGuardarlaYAvisar() {
    byte[] contenido = { 1, 2, 3 };
    MockMultipartFile archivo = new MockMultipartFile("foto", "a.png", "image/png", contenido);

    ModelAndView mav = controlador.subirFoto(archivo, requestConSesion, flashMock);

    assertThat(mav.getViewName(), equalTo("redirect:/perfil"));
    verify(servicioPerfilMock).guardarFoto(EMAIL, "image/png", contenido);
    verify(flashMock).addFlashAttribute("mensaje", "Foto actualizada");
  }

  @Test
  public void subirFotoInvalidaDeberiaMostrarElError() {
    MockMultipartFile archivo = new MockMultipartFile(
      "foto",
      "a.html",
      "text/html",
      new byte[] { 1 }
    );
    doThrow(new IllegalArgumentException("La foto debe ser JPG, PNG o WEBP"))
      .when(servicioPerfilMock)
      .guardarFoto(eq(EMAIL), eq("text/html"), any(byte[].class));

    controlador.subirFoto(archivo, requestConSesion, flashMock);

    verify(flashMock).addFlashAttribute("error", "La foto debe ser JPG, PNG o WEBP");
  }

  @Test
  public void subirFotoQueNoSePuedeLeerDeberiaMostrarElError() throws IOException {
    org.springframework.web.multipart.MultipartFile archivo = mock(
      org.springframework.web.multipart.MultipartFile.class
    );
    when(archivo.getContentType()).thenReturn("image/png");
    when(archivo.getBytes()).thenThrow(new IOException("falla"));

    controlador.subirFoto(archivo, requestConSesion, flashMock);

    verify(flashMock).addFlashAttribute("error", "No se pudo leer la imagen");
  }

  @Test
  public void eliminarFotoSinSesionDeberiaRedirigirALogin() {
    ModelAndView mav = controlador.eliminarFoto(new MockHttpServletRequest(), flashMock);

    assertThat(mav.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void eliminarFotoDeberiaEliminarYAvisar() {
    ModelAndView mav = controlador.eliminarFoto(requestConSesion, flashMock);

    assertThat(mav.getViewName(), equalTo("redirect:/perfil"));
    verify(servicioPerfilMock).eliminarFoto(EMAIL);
    verify(flashMock).addFlashAttribute("mensaje", "Foto eliminada");
  }

  @Test
  public void verFotoSinSesionDeberiaResponder401() {
    ResponseEntity<byte[]> respuesta = controlador.verFoto(new MockHttpServletRequest());

    assertThat(respuesta.getStatusCode().value(), equalTo(401));
  }

  @Test
  public void verFotoInexistenteDeberiaResponder404() {
    when(servicioPerfilMock.obtenerFoto(EMAIL)).thenReturn(null);

    assertThat(controlador.verFoto(requestConSesion).getStatusCode().value(), equalTo(404));
  }

  @Test
  public void verFotoExistenteDeberiaDevolverLosBytesConSuTipoYNosniff() {
    byte[] contenido = { 9, 8, 7 };
    when(servicioPerfilMock.obtenerFoto(EMAIL))
      .thenReturn(new FotoPerfil(7L, "image/png", contenido));

    ResponseEntity<byte[]> respuesta = controlador.verFoto(requestConSesion);

    assertThat(respuesta.getStatusCode().value(), equalTo(200));
    assertThat(respuesta.getBody(), equalTo(contenido));
    assertThat(respuesta.getHeaders().getContentType().toString(), equalTo("image/png"));
    assertThat(respuesta.getHeaders().getFirst("X-Content-Type-Options"), equalTo("nosniff"));
  }
}
