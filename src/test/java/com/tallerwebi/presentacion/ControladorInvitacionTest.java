package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.InvitacionInvalida;
import com.tallerwebi.dominio.invitacion.Invitacion;
import com.tallerwebi.dominio.invitacion.ServicioInvitacion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class ControladorInvitacionTest {

  private ServicioInvitacion servicioInvitacionMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private RedirectAttributes redirectAttributesMock;

  private ControladorInvitacion controladorInvitacion;

  private DatosInvitacion datosInvitacion;
  private Usuario usuarioLogueado;
  private String nombreGrupo;

  @BeforeEach
  public void init() {
    servicioInvitacionMock = mock(ServicioInvitacion.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    redirectAttributesMock = mock(RedirectAttributes.class);

    controladorInvitacion = new ControladorInvitacion(servicioInvitacionMock);

    usuarioLogueado = new Usuario();
    usuarioLogueado.setEmail("emisor@test.com");
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioLogueado);

    nombreGrupo = "grupo-test";
    datosInvitacion = new DatosInvitacion();
    datosInvitacion.setReceptor("receptor@test.com");
    datosInvitacion.setGrupo(nombreGrupo);
    datosInvitacion.setEmisor(usuarioLogueado.getEmail());
  }

  @Test
  public void aceptarInvitacionDeberiaRecargarPantallaDeInvitacionConMensajeDeExito()
    throws InvitacionInvalida {
    dadoQueElUsuarioTieneInvitacionesPendientes();
    ModelAndView vistaInvitacionesYMensajeDeExito = cuandoAceptaLaInvitacion();
    entoncesRedirigeAInvitacionesConMensajeDeExito(vistaInvitacionesYMensajeDeExito);
  }

  @Test
  public void rechazarInvitacionDeberiaRecargarPantallaDeInvitacionConMensajeDeExito()
    throws InvitacionInvalida {
    dadoQueElUsuarioTieneInvitacionesPendientes();
    ModelAndView vistaInvitacionesYMensajeDeExito = cuandoRechazaLaInvitacion();
    entoncesRedirigeAInvitacionesConMensajeDeExito(vistaInvitacionesYMensajeDeExito);
  }

  @Test
  public void deberiaDevolverALaPantallaDeGruposSiLaInvitacionEsValida() throws InvitacionInvalida {
    dadoQueLaInvitacionEsValida();
    ModelAndView vistaDeGruposYMjeDeExito = cuandoEnviaUnaInvitacionAUnUsuario();
    entoncesLaInvitacionSeEnviaConExitoYRedirigeAGrupos(vistaDeGruposYMjeDeExito);
  }

  @Test
  public void deberiaDevolverALaPantallaDeInvitacionConErrorSiLaInvitacionEsInvalida()
    throws InvitacionInvalida {
    dadoQueLaInvitacionEsInvalida();
    ModelAndView vistaDeInvitacionInvalida = cuandoEnviaUnaInvitacionAUnUsuario();
    entoncesRedirigeAlFormularioDeInvitacionConError(vistaDeInvitacionInvalida);
  }

  @Test
  public void deberiaDevolverPantallaDeInvitaciones() {
    ModelAndView vistaDeInvitaciones = cuandoSolicitaVerLasInvitaciones();
    entoncesDevuelveLaVistaDeInvitaciones(vistaDeInvitaciones);
  }

  @Test
  public void deberiaRedirigirAlLoginSiElUsuarioNoEstaLogueadoAlIntentarInvitar() {
    dadoQueElUsuarioNoEstaLogueado();
    ModelAndView vistaDeLogin = cuandoEnviaUnaInvitacionAUnUsuario();
    entoncesRedirigeAlLogin(vistaDeLogin);
  }

  private void dadoQueElUsuarioNoEstaLogueado() {
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
  }

  private void dadoQueElUsuarioTieneInvitacionesPendientes() throws InvitacionInvalida {
    doNothing().when(servicioInvitacionMock).aceptarInvitacion(datosInvitacion);
  }

  private void dadoQueLaInvitacionEsValida() throws InvitacionInvalida {
    Invitacion invitacionCreada = new Invitacion();

    when(servicioInvitacionMock.validarInvitacion(datosInvitacion)).thenReturn(invitacionCreada);
  }

  private void dadoQueLaInvitacionEsInvalida() throws InvitacionInvalida {
    when(servicioInvitacionMock.validarInvitacion(datosInvitacion))
      .thenThrow(new InvitacionInvalida());
  }

  private ModelAndView cuandoAceptaLaInvitacion() {
    return controladorInvitacion.aceptar(datosInvitacion, requestMock, redirectAttributesMock);
  }

  private ModelAndView cuandoRechazaLaInvitacion() {
    return controladorInvitacion.rechazar(datosInvitacion, requestMock, redirectAttributesMock);
  }

  private ModelAndView cuandoEnviaUnaInvitacionAUnUsuario() {
    return controladorInvitacion.validarInvitacion(
      nombreGrupo,
      datosInvitacion,
      requestMock,
      redirectAttributesMock
    );
  }

  private ModelAndView cuandoSolicitaVerLasInvitaciones() {
    return controladorInvitacion.verInvitaciones(requestMock);
  }

  private void entoncesRedirigeAInvitacionesConMensajeDeExito(ModelAndView vista) {
    assertEquals("redirect:/invitaciones", vista.getViewName());
  }

  private void entoncesRedirigeAlLogin(ModelAndView mav) {
    assertEquals("redirect:/login", mav.getViewName());
  }

  private void entoncesLaInvitacionSeEnviaConExitoYRedirigeAGrupos(
    ModelAndView vistaDeGrupoConMensajeDeExito
  ) {
    assertEquals("redirect:/grupos", vistaDeGrupoConMensajeDeExito.getViewName());
    verify(redirectAttributesMock, times(1)).addFlashAttribute(eq("exito"), anyString());
  }

  private void entoncesRedirigeAlFormularioDeInvitacionConError(
    ModelAndView vistaDeInvitacionConError
  ) {
    assertEquals("redirect:/grupos/grupo-test/invitar", vistaDeInvitacionConError.getViewName());
    verify(redirectAttributesMock, times(1)).addFlashAttribute(eq("error"), anyString());
  }

  private void entoncesDevuelveLaVistaDeInvitaciones(ModelAndView vistaDeInvitaciones) {
    assertEquals("invitaciones", vistaDeInvitaciones.getViewName());
  }
}
