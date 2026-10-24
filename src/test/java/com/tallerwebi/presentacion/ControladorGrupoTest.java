package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.ServicioGrupo;
import com.tallerwebi.presentacion.grupo.ControladorGrupo;
import com.tallerwebi.presentacion.grupo.DatosGrupo;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class ControladorGrupoTest {

  private ControladorGrupo controladorGrupo;
  private ServicioGrupo servicioGrupoMock;
  private Grupo grupoMock;
  private static final String EMAIL_USUARIO = "test@unlam.edu.ar";

  @BeforeEach
  public void init() {
    servicioGrupoMock = mock(ServicioGrupo.class);
    controladorGrupo = new ControladorGrupo(servicioGrupoMock);
    grupoMock = mock(Grupo.class);
    when(grupoMock.getNombre()).thenReturn("grupo1");
  }

  @Test
  public void irAGrupoDeberiaRetornarVistaGrupo() throws GrupoNoEncontrado {
    String nombre = "grupo1";
    grupoMock.setNombre(nombre);
    when(servicioGrupoMock.buscarPorNombre(nombre)).thenReturn(grupoMock);
    ModelAndView modelAndView = controladorGrupo.verGrupo(nombre);
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("paginas/grupo/grupo-detalle"));
  }

  @Test
  public void irAGruposDeberiaRetornarVistaGruposYDatosGrupo() {
    when(servicioGrupoMock.listarGruposDelUsuario(EMAIL_USUARIO))
      .thenReturn(java.util.Arrays.asList(grupoMock));
    ModelAndView modelAndView = controladorGrupo.verGrupos(requestConUsuarioLogueado());
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("paginas/grupo/grupos"));
    assertThat(modelAndView.getModel().get("datosGrupo"), instanceOf(List.class));
  }

  @Test
  public void irAGruposDeberiaRetornarVistaGrupos() {
    ModelAndView modelAndView = controladorGrupo.verGrupos(requestConUsuarioLogueado());

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("paginas/grupo/grupos"));
  }

  @Test
  public void irAGrupoQueNoExisteDeberiaRetornarVistaGrupoDetalleConError()
    throws GrupoNoEncontrado {
    doThrow(GrupoNoEncontrado.class).when(servicioGrupoMock).buscarPorNombre("grupo2");

    ModelAndView modelAndView = controladorGrupo.verGrupo("grupo2");

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("paginas/grupo/grupo-detalle"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("El grupo no existe")
    );
  }

  @Test
  public void alHacerClicEnInvitarDebeLlevarALaPantallaDeInvitacion() throws GrupoNoEncontrado {
    grupoMock.setNombre("grupo-existente");
    when(servicioGrupoMock.buscarPorNombre("grupo-existente")).thenReturn(grupoMock);
    ModelAndView modelAndView = controladorGrupo.irAInvitar("grupo-existente");
    DatosGrupo datosGrupo = (DatosGrupo) modelAndView.getModel().get("datosGrupo");

    assertEquals("paginas/grupo/grupo-invitar", modelAndView.getViewName());
    assertEquals(datosGrupo, modelAndView.getModel().get("datosGrupo"));
  }

  @Test
  public void alAccederAInvitacionDeGrupoQueNoExisteDebeMostrarError() throws GrupoNoEncontrado {
    doThrow(GrupoNoEncontrado.class).when(servicioGrupoMock).buscarPorNombre("grupo-inexistente");

    ModelAndView modelAndView = controladorGrupo.irAInvitar("grupo-inexistente");

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("paginas/grupo/grupo-invitar"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("El grupo no existe")
    );
  }

  @Test
  public void irAGruposSinEstarLogueadoDeberiaRedirigirAlLogin() {
    ModelAndView modelAndView = controladorGrupo.verGrupos(new MockHttpServletRequest());

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    verifyNoInteractions(servicioGrupoMock);
  }

  @Test
  public void irAGruposDeberiaListarSoloLosGruposDelUsuarioLogueado() {
    List<Grupo> gruposDelUsuario = java.util.Arrays.asList(grupoMock);
    when(servicioGrupoMock.listarGruposDelUsuario(EMAIL_USUARIO)).thenReturn(gruposDelUsuario);

    ModelAndView modelAndView = controladorGrupo.verGrupos(requestConUsuarioLogueado());

    assertEquals(gruposDelUsuario, modelAndView.getModel().get("datosGrupo"));
    verify(servicioGrupoMock).listarGruposDelUsuario(EMAIL_USUARIO);
    verify(servicioGrupoMock, never()).listarGrupos();
  }

  private MockHttpServletRequest requestConUsuarioLogueado() {
    Usuario usuario = new Usuario();
    usuario.setEmail(EMAIL_USUARIO);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.getSession().setAttribute("USUARIO", usuario);
    return request;
  }
}
