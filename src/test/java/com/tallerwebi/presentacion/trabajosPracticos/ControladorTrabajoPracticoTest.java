package com.tallerwebi.presentacion.trabajosPracticos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.trabajosPracticos.EstadoTP;
import com.tallerwebi.dominio.trabajosPracticos.ServicioTrabajoPractico;
import com.tallerwebi.dominio.trabajosPracticos.TrabajoPractico;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class ControladorTrabajoPracticoTest {

  private ControladorTrabajoPractico controlador;
  private ServicioTrabajoPractico servicioTrabajoPracticoMock;
  private ServicioMateria servicioMateriaMock;
  private TrabajoPractico trabajoPracticoMock;

  @BeforeEach
  public void init() {
    servicioTrabajoPracticoMock = mock(ServicioTrabajoPractico.class);
    servicioMateriaMock = mock(ServicioMateria.class);
    trabajoPracticoMock = mock(TrabajoPractico.class);
    controlador = new ControladorTrabajoPractico(servicioTrabajoPracticoMock, servicioMateriaMock);
  }

  // ---------- listado ----------

  @Test
  public void listarDeberiaRepartirLosTpsEnLasTresColumnas() {
    TrabajoPractico pendiente = mock(TrabajoPractico.class);
    TrabajoPractico enCurso = mock(TrabajoPractico.class);
    TrabajoPractico finalizado = mock(TrabajoPractico.class);
    when(pendiente.getEstado()).thenReturn(EstadoTP.PENDIENTE);
    when(enCurso.getEstado()).thenReturn(EstadoTP.EN_CURSO);
    when(finalizado.getEstado()).thenReturn(EstadoTP.FINALIZADO);
    when(servicioTrabajoPracticoMock.obtenerTodos())
      .thenReturn(new ArrayList<>(List.of(pendiente, enCurso, finalizado)));

    ModelAndView mav = controlador.listarTrabajosPracticos(null, new MockHttpServletRequest());

    assertThat(mav.getViewName(), equalToIgnoringCase("paginas/trabajos-practicos"));
    assertThat((List<?>) mav.getModel().get("pendientes"), hasSize(1));
    assertThat((List<?>) mav.getModel().get("enCurso"), hasSize(1));
    assertThat((List<?>) mav.getModel().get("finalizados"), hasSize(1));
    verify(servicioTrabajoPracticoMock, times(1)).obtenerTodos();
  }

  @Test
  public void listarConFiltroDeMateriaDeberiaBuscarPorMateriaYConservarElFiltro() {
    when(servicioTrabajoPracticoMock.buscarPorMateria("Taller Web I"))
      .thenReturn(new ArrayList<>());

    ModelAndView mav = controlador.listarTrabajosPracticos(
      "Taller Web I",
      new MockHttpServletRequest()
    );

    assertThat(mav.getModel().get("materiaBuscada"), equalTo("Taller Web I"));
    verify(servicioTrabajoPracticoMock, times(1)).buscarPorMateria("Taller Web I");
    verify(servicioTrabajoPracticoMock, never()).obtenerTodos();
  }

  @Test
  public void listarConFiltroVacioDeberiaTraerTodos() {
    when(servicioTrabajoPracticoMock.obtenerTodos()).thenReturn(new ArrayList<>());

    controlador.listarTrabajosPracticos("", new MockHttpServletRequest());

    verify(servicioTrabajoPracticoMock, times(1)).obtenerTodos();
    verify(servicioTrabajoPracticoMock, never()).buscarPorMateria("");
  }

  @Test
  public void listarSinSesionDeberiaTenerMateriasVacias() {
    when(servicioTrabajoPracticoMock.obtenerTodos()).thenReturn(new ArrayList<>());

    ModelAndView mav = controlador.listarTrabajosPracticos(null, new MockHttpServletRequest());

    assertThat((List<?>) mav.getModel().get("materias"), empty());
    verifyNoInteractions(servicioMateriaMock);
  }

  @Test
  public void listarConSesionDeberiaCargarLosNombresDeLasMateriasDelUsuario() {
    when(servicioTrabajoPracticoMock.obtenerTodos()).thenReturn(new ArrayList<>());
    stubMateriasDelUsuario(7L, "Taller Web I", "Base de Datos II");

    ModelAndView mav = controlador.listarTrabajosPracticos(null, requestConUsuario(7L));

    assertThat(
      mav.getModel().get("materias"),
      equalTo(List.of("Taller Web I", "Base de Datos II"))
    );
  }

  @Test
  public void listarConAtributoDeSesionQueNoEsUsuarioDeberiaTenerMateriasVacias() {
    when(servicioTrabajoPracticoMock.obtenerTodos()).thenReturn(new ArrayList<>());
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.getSession().setAttribute("USUARIO", "no soy un usuario");

    ModelAndView mav = controlador.listarTrabajosPracticos(null, request);

    assertThat((List<?>) mav.getModel().get("materias"), empty());
  }

  // ---------- formulario ----------

  @Test
  public void irANuevoDeberiaRetornarFormularioConOpcionesYMateriasDelUsuario() {
    stubMateriasDelUsuario(7L, "Taller Web I");

    ModelAndView mav = controlador.irANuevoTrabajoPractico(requestConUsuario(7L));

    assertThat(mav.getViewName(), equalToIgnoringCase("paginas/nuevo-trabajo-practico"));
    assertThat(mav.getModel().get("trabajoPractico"), instanceOf(TrabajoPractico.class));
    assertThat(mav.getModel().get("tipos"), notNullValue());
    assertThat(mav.getModel().get("estados"), notNullValue());
    assertThat(mav.getModel().get("materias"), equalTo(List.of("Taller Web I")));
  }

  @Test
  public void editarDeberiaRetornarFormularioConElTpBuscadoYLasMaterias() {
    when(servicioTrabajoPracticoMock.buscarPorId(1L)).thenReturn(trabajoPracticoMock);
    stubMateriasDelUsuario(7L, "Base de Datos II");

    ModelAndView mav = controlador.editarTrabajoPractico(1L, requestConUsuario(7L));

    assertThat(mav.getViewName(), equalToIgnoringCase("paginas/nuevo-trabajo-practico"));
    assertThat(mav.getModel().get("trabajoPractico"), sameInstance(trabajoPracticoMock));
    assertThat(mav.getModel().get("materias"), equalTo(List.of("Base de Datos II")));
    verify(servicioTrabajoPracticoMock, times(1)).buscarPorId(1L);
  }

  // ---------- acciones ----------

  @Test
  public void guardarDeberiaLlamarAlServicioYRedirigir() {
    ModelAndView mav = controlador.guardarTrabajoPractico(trabajoPracticoMock);

    assertThat(mav.getViewName(), equalToIgnoringCase("redirect:/trabajos-practicos"));
    verify(servicioTrabajoPracticoMock, times(1)).crearTrabajoPractico(trabajoPracticoMock);
  }

  @Test
  public void avanzarEstadoDeberiaLlamarAlServicioYRedirigir() {
    String vista = controlador.avanzarEstado(5L);

    assertThat(vista, equalToIgnoringCase("redirect:/trabajos-practicos"));
    verify(servicioTrabajoPracticoMock, times(1)).avanzarEstado(5L);
  }

  @Test
  public void eliminarDeberiaLlamarAlServicioYRedirigir() {
    String vista = controlador.eliminarTrabajoPractico(1L);

    assertThat(vista, equalToIgnoringCase("redirect:/trabajos-practicos"));
    verify(servicioTrabajoPracticoMock, times(1)).eliminar(1L);
  }

  // ---------- helpers ----------

  private MockHttpServletRequest requestConUsuario(Long id) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.getSession().setAttribute("USUARIO", usuario);
    return request;
  }

  private void stubMateriasDelUsuario(Long idUsuario, String... nombres) {
    List<Materia> materias = new ArrayList<>();
    for (String nombre : nombres) {
      Materia materia = mock(Materia.class);
      when(materia.getNombre()).thenReturn(nombre);
      materias.add(materia);
    }
    when(servicioMateriaMock.obtenerMateriasPorUsuario(idUsuario)).thenReturn(materias);
  }
}
