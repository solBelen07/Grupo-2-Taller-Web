package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoExistente;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoExistente;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NoEsAdministradorDelGrupo;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NoEsAdministradorDelGrupo;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NoEsIntegranteDelGrupo;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NombreDeGrupoInvalido;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NombreDeGrupoInvalido;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.SalidaDeGrupo;
import com.tallerwebi.dominio.grupo.ServicioGrupo;
import com.tallerwebi.presentacion.grupo.ControladorGrupo;
import com.tallerwebi.presentacion.grupo.DatosGrupo;
import com.tallerwebi.presentacion.grupo.DatosIntegrante;
import com.tallerwebi.presentacion.grupo.DatosIntegrante;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

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
    ModelAndView modelAndView = controladorGrupo.verGrupo(nombre, requestConUsuarioLogueado());
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

    ModelAndView modelAndView = controladorGrupo.verGrupo("grupo2", requestConUsuarioLogueado());

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

  @Test
  public void elDetalleDelGrupoDeberiaMostrarLosIntegrantesReales() throws GrupoNoEncontrado {
    Usuario integrante = new Usuario();
    integrante.setNombre("Juan");
    integrante.setApellido("Mendez");
    integrante.setEmail(EMAIL_USUARIO);
    when(servicioGrupoMock.buscarPorNombre("grupo1")).thenReturn(grupoMock);
    when(servicioGrupoMock.cantidadParticipantes("grupo1")).thenReturn(1);
    when(servicioGrupoMock.listarIntegrantes("grupo1")).thenReturn(List.of(integrante));

    ModelAndView mav = controladorGrupo.verGrupo("grupo1", requestConUsuarioLogueado());

    DatosGrupo datos = (DatosGrupo) mav.getModel().get("datosGrupo");
    assertEquals(1, datos.getIntegrantes().size());
    DatosIntegrante primero = datos.getIntegrantes().get(0);
    assertEquals("Juan", primero.getNombre());
    assertEquals("Mendez", primero.getApellido());
    assertEquals(EMAIL_USUARIO, primero.getEmail());
  }

  @Test
  public void siElServicioNoDevuelveIntegrantesLaListaQuedaVacia() throws GrupoNoEncontrado {
    when(servicioGrupoMock.buscarPorNombre("grupo1")).thenReturn(grupoMock);
    when(servicioGrupoMock.listarIntegrantes("grupo1")).thenReturn(null);

    ModelAndView mav = controladorGrupo.verGrupo("grupo1", requestConUsuarioLogueado());

    DatosGrupo datos = (DatosGrupo) mav.getModel().get("datosGrupo");
    assertEquals(0, datos.getIntegrantes().size());
  }

  // ABM de grupo

  @Test
  public void irANuevoGrupoDeberiaMostrarElFormularioVacio() {
    ModelAndView mav = controladorGrupo.irANuevoGrupo(requestConUsuarioLogueado());

    assertEquals("paginas/grupo/grupo-formulario", mav.getViewName());
    assertEquals(null, mav.getModel().get("nombreOriginal"));
  }

  @Test
  public void irANuevoGrupoSinLoginDeberiaRedirigirAlLogin() {
    ModelAndView mav = controladorGrupo.irANuevoGrupo(new MockHttpServletRequest());

    assertEquals("redirect:/login", mav.getViewName());
  }

  @Test
  public void crearGrupoDeberiaUsarAlUsuarioLogueadoComoCreadorYVolverALaLista() throws Exception {
    MockHttpServletRequest request = requestConUsuarioLogueado();
    Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
    Grupo creado = new Grupo();
    creado.setNombre("Equipo Web");
    when(servicioGrupoMock.crearGrupo("Equipo Web", usuario)).thenReturn(creado);
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    ModelAndView mav = controladorGrupo.crearGrupo("Equipo Web", request, redirect);

    assertEquals("redirect:/grupos", mav.getViewName());
    verify(servicioGrupoMock).crearGrupo("Equipo Web", usuario);
    assertEquals(
      "Creaste el grupo Equipo Web. Sos su administrador/a.",
      redirect.getFlashAttributes().get("exito")
    );
  }

  @Test
  public void crearGrupoConNombreRepetidoDeberiaVolverAlFormularioConElError() throws Exception {
    when(servicioGrupoMock.crearGrupo(eq("grupo1"), any(Usuario.class)))
      .thenThrow(new GrupoExistente());

    ModelAndView mav = controladorGrupo.crearGrupo(
      "grupo1",
      requestConUsuarioLogueado(),
      new RedirectAttributesModelMap()
    );

    assertEquals("paginas/grupo/grupo-formulario", mav.getViewName());
    assertEquals("Ya existe un grupo con ese nombre", mav.getModel().get("error"));
    assertEquals("grupo1", mav.getModel().get("nombre"));
  }

  @Test
  public void crearGrupoSinLoginDeberiaRedirigirAlLoginSinCrearNada() {
    ModelAndView mav = controladorGrupo.crearGrupo(
      "grupo",
      new MockHttpServletRequest(),
      new RedirectAttributesModelMap()
    );

    assertEquals("redirect:/login", mav.getViewName());
    verifyNoInteractions(servicioGrupoMock);
  }

  @Test
  public void unIntegrantePuedeAbrirElFormularioDeEdicion() throws Exception {
    Grupo grupo = new Grupo();
    grupo.setNombre("grupo1");
    when(servicioGrupoMock.buscarGrupoDeIntegrante(eq("grupo1"), any(Usuario.class)))
      .thenReturn(grupo);

    ModelAndView mav = controladorGrupo.irAEditarGrupo(
      "grupo1",
      requestConUsuarioLogueado(),
      new RedirectAttributesModelMap()
    );

    assertEquals("paginas/grupo/grupo-formulario", mav.getViewName());
    assertEquals("grupo1", mav.getModel().get("nombreOriginal"));
  }

  @Test
  public void quienNoEsIntegranteNoPuedeAbrirLaEdicion() throws Exception {
    when(servicioGrupoMock.buscarGrupoDeIntegrante(eq("grupo1"), any(Usuario.class)))
      .thenThrow(new NoEsIntegranteDelGrupo());
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    ModelAndView mav = controladorGrupo.irAEditarGrupo(
      "grupo1",
      requestConUsuarioLogueado(),
      redirect
    );

    assertEquals("redirect:/grupos", mav.getViewName());
    assertEquals(
      "Solo los integrantes del grupo pueden hacer esto",
      redirect.getFlashAttributes().get("error")
    );
  }

  @Test
  public void editarUnGrupoQueNoExisteDeberiaVolverALaListaConError() throws Exception {
    when(servicioGrupoMock.buscarGrupoDeIntegrante(eq("x"), any(Usuario.class)))
      .thenThrow(new GrupoNoEncontrado());
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    ModelAndView mav = controladorGrupo.irAEditarGrupo("x", requestConUsuarioLogueado(), redirect);

    assertEquals("redirect:/grupos", mav.getViewName());
    assertEquals("El grupo no existe", redirect.getFlashAttributes().get("error"));
  }

  @Test
  public void guardarLaEdicionDeberiaModificarElGrupoYVolverAlDetalleConElNombreNuevo()
    throws Exception {
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    ModelAndView mav = controladorGrupo.editarGrupo(
      "viejo",
      "  nuevo  ",
      requestConUsuarioLogueado(),
      redirect
    );

    assertEquals("redirect:/grupos/{nombre}", mav.getViewName());
    assertEquals("nuevo", redirect.get("nombre"));
    verify(servicioGrupoMock).modificarGrupo(eq("viejo"), eq("  nuevo  "), any(Usuario.class));
    assertEquals("Nombre del grupo actualizado", redirect.getFlashAttributes().get("exito"));
  }

  @Test
  public void guardarLaEdicionConNombreInvalidoDeberiaVolverAlFormulario() throws Exception {
    doThrow(new NombreDeGrupoInvalido("El nombre del grupo es obligatorio"))
      .when(servicioGrupoMock)
      .modificarGrupo(eq("viejo"), eq(""), any(Usuario.class));

    ModelAndView mav = controladorGrupo.editarGrupo(
      "viejo",
      "",
      requestConUsuarioLogueado(),
      new RedirectAttributesModelMap()
    );

    assertEquals("paginas/grupo/grupo-formulario", mav.getViewName());
    assertEquals("viejo", mav.getModel().get("nombreOriginal"));
    assertEquals("El nombre del grupo es obligatorio", mav.getModel().get("error"));
  }

  @Test
  public void guardarLaEdicionSinSerIntegranteDeberiaVolverALaListaConError() throws Exception {
    doThrow(new NoEsIntegranteDelGrupo())
      .when(servicioGrupoMock)
      .modificarGrupo(eq("grupo1"), eq("otro"), any(Usuario.class));
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    ModelAndView mav = controladorGrupo.editarGrupo(
      "grupo1",
      "otro",
      requestConUsuarioLogueado(),
      redirect
    );

    assertEquals("redirect:/grupos", mav.getViewName());
    assertEquals(
      "Solo los integrantes del grupo pueden hacer esto",
      redirect.getFlashAttributes().get("error")
    );
  }

  @Test
  public void guardarLaEdicionDeUnGrupoQueNoExisteDeberiaVolverALaListaConError() throws Exception {
    doThrow(new GrupoNoEncontrado())
      .when(servicioGrupoMock)
      .modificarGrupo(eq("x"), eq("y"), any(Usuario.class));
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    ModelAndView mav = controladorGrupo.editarGrupo(
      "x",
      "y",
      requestConUsuarioLogueado(),
      redirect
    );

    assertEquals("redirect:/grupos", mav.getViewName());
    assertEquals("El grupo no existe", redirect.getFlashAttributes().get("error"));
  }

  @Test
  public void editarSinLoginDeberiaRedirigirAlLogin() {
    assertEquals(
      "redirect:/login",
      controladorGrupo
        .irAEditarGrupo("grupo1", new MockHttpServletRequest(), new RedirectAttributesModelMap())
        .getViewName()
    );
    assertEquals(
      "redirect:/login",
      controladorGrupo
        .editarGrupo("grupo1", "x", new MockHttpServletRequest(), new RedirectAttributesModelMap())
        .getViewName()
    );
  }

  @Test
  public void elAdministradorPuedeEliminarElGrupo() throws Exception {
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    ModelAndView mav = controladorGrupo.eliminarGrupo(
      "grupo1",
      requestConUsuarioLogueado(),
      redirect
    );

    assertEquals("redirect:/grupos", mav.getViewName());
    verify(servicioGrupoMock).eliminarGrupo(eq("grupo1"), any(Usuario.class));
    assertEquals("Grupo eliminado", redirect.getFlashAttributes().get("exito"));
  }

  @Test
  public void eliminarSinSerAdministradorDeberiaMostrarError() throws Exception {
    doThrow(new NoEsAdministradorDelGrupo())
      .when(servicioGrupoMock)
      .eliminarGrupo(eq("grupo1"), any(Usuario.class));
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    controladorGrupo.eliminarGrupo("grupo1", requestConUsuarioLogueado(), redirect);

    assertEquals(
      "Solo el administrador del grupo puede hacer esto",
      redirect.getFlashAttributes().get("error")
    );
  }

  @Test
  public void eliminarUnGrupoQueNoExisteDeberiaMostrarError() throws Exception {
    doThrow(new GrupoNoEncontrado())
      .when(servicioGrupoMock)
      .eliminarGrupo(eq("x"), any(Usuario.class));
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    controladorGrupo.eliminarGrupo("x", requestConUsuarioLogueado(), redirect);

    assertEquals("El grupo no existe", redirect.getFlashAttributes().get("error"));
  }

  @Test
  public void eliminarSinLoginDeberiaRedirigirAlLogin() {
    ModelAndView mav = controladorGrupo.eliminarGrupo(
      "grupo1",
      new MockHttpServletRequest(),
      new RedirectAttributesModelMap()
    );

    assertEquals("redirect:/login", mav.getViewName());
    verifyNoInteractions(servicioGrupoMock);
  }

  @Test
  public void enElDetalleElCreadorDeberiaFigurarComoAdministrador() throws GrupoNoEncontrado {
    Usuario creador = new Usuario();
    creador.setId(1L);
    creador.setEmail("creador@unlam.edu.ar");
    Usuario miembro = new Usuario();
    miembro.setId(2L);
    miembro.setEmail("miembro@unlam.edu.ar");
    Grupo grupo = new Grupo();
    grupo.setNombre("grupo1");
    grupo.setCreador(creador);
    when(servicioGrupoMock.buscarPorNombre("grupo1")).thenReturn(grupo);
    when(servicioGrupoMock.listarIntegrantes("grupo1")).thenReturn(List.of(creador, miembro));

    DatosGrupo datos = (DatosGrupo) controladorGrupo
      .verGrupo("grupo1", requestConUsuarioLogueado())
      .getModel()
      .get("datosGrupo");

    assertEquals(true, datos.getIntegrantes().get(0).isAdministrador());
    assertEquals(false, datos.getIntegrantes().get(1).isAdministrador());
  }

  // Panel Opciones: salir del grupo

  @Test
  public void verElDetalleSinLoginDeberiaRedirigirAlLogin() {
    assertEquals(
      "redirect:/login",
      controladorGrupo.verGrupo("grupo1", new MockHttpServletRequest()).getViewName()
    );
  }

  @Test
  public void elDetalleDeberiaIndicarSiSoyIntegranteYAdministrador() throws GrupoNoEncontrado {
    MockHttpServletRequest request = requestConUsuarioLogueado();
    Usuario yo = (Usuario) request.getSession().getAttribute("USUARIO");
    yo.setId(1L);
    Grupo grupo = new Grupo();
    grupo.setNombre("grupo1");
    grupo.setCreador(yo);
    when(servicioGrupoMock.buscarPorNombre("grupo1")).thenReturn(grupo);
    when(servicioGrupoMock.esIntegrante(grupo, yo)).thenReturn(true);

    DatosGrupo datos = (DatosGrupo) controladorGrupo
      .verGrupo("grupo1", request)
      .getModel()
      .get("datosGrupo");

    assertEquals(true, datos.isSoyIntegrante());
    assertEquals(true, datos.isSoyAdministrador());
  }

  @Test
  public void unMiembroQueSaleVuelveALaListaConMensaje() throws Exception {
    when(servicioGrupoMock.salirDelGrupo(eq("grupo1"), any(Usuario.class)))
      .thenReturn(SalidaDeGrupo.deMiembro());
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    ModelAndView mav = controladorGrupo.salirDelGrupo(
      "grupo1",
      requestConUsuarioLogueado(),
      redirect
    );

    assertEquals("redirect:/grupos", mav.getViewName());
    assertEquals("Saliste del grupo.", redirect.getFlashAttributes().get("exito"));
  }

  @Test
  public void siSaleElAdministradorElMensajeDiceQuienEsElNuevoAdministrador() throws Exception {
    Usuario nuevo = new Usuario();
    nuevo.setNombre("Usuario1");
    nuevo.setApellido("Test");
    when(servicioGrupoMock.salirDelGrupo(eq("grupo1"), any(Usuario.class)))
      .thenReturn(SalidaDeGrupo.conNuevoAdministrador(nuevo));
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    controladorGrupo.salirDelGrupo("grupo1", requestConUsuarioLogueado(), redirect);

    assertEquals(
      "Saliste del grupo. Ahora Usuario1 Test es administrador/a.",
      redirect.getFlashAttributes().get("exito")
    );
  }

  @Test
  public void siSaleElUnicoIntegranteElMensajeAvisaQueSeElimino() throws Exception {
    when(servicioGrupoMock.salirDelGrupo(eq("grupo1"), any(Usuario.class)))
      .thenReturn(SalidaDeGrupo.conGrupoEliminado());
    RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

    controladorGrupo.salirDelGrupo("grupo1", requestConUsuarioLogueado(), redirect);

    assertEquals(
      "Saliste del grupo. Como eras el único integrante, el grupo se eliminó.",
      redirect.getFlashAttributes().get("exito")
    );
  }

  @Test
  public void salirSinSerIntegranteOSinGrupoDeberiaMostrarError() throws Exception {
    when(servicioGrupoMock.salirDelGrupo(eq("ajeno"), any(Usuario.class)))
      .thenThrow(new NoEsIntegranteDelGrupo());
    when(servicioGrupoMock.salirDelGrupo(eq("x"), any(Usuario.class)))
      .thenThrow(new GrupoNoEncontrado());
    RedirectAttributesModelMap r1 = new RedirectAttributesModelMap();
    RedirectAttributesModelMap r2 = new RedirectAttributesModelMap();

    controladorGrupo.salirDelGrupo("ajeno", requestConUsuarioLogueado(), r1);
    controladorGrupo.salirDelGrupo("x", requestConUsuarioLogueado(), r2);

    assertEquals(
      "Solo los integrantes del grupo pueden hacer esto",
      r1.getFlashAttributes().get("error")
    );
    assertEquals("El grupo no existe", r2.getFlashAttributes().get("error"));
  }

  @Test
  public void salirSinLoginDeberiaRedirigirAlLogin() {
    ModelAndView mav = controladorGrupo.salirDelGrupo(
      "grupo1",
      new MockHttpServletRequest(),
      new RedirectAttributesModelMap()
    );

    assertEquals("redirect:/login", mav.getViewName());
  }
}
