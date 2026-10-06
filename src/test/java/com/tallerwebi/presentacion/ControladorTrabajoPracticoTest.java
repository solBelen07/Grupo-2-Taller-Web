package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.EstadoTP;
import com.tallerwebi.dominio.ServicioTrabajoPractico;
import com.tallerwebi.dominio.TrabajoPractico;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorTrabajoPracticoTest {

  private ControladorTrabajoPractico controladorTrabajoPractico;
  private ServicioTrabajoPractico servicioTrabajoPracticoMock;
  private TrabajoPractico trabajoPracticoMock;

  @BeforeEach
  public void init() {
    servicioTrabajoPracticoMock = mock(ServicioTrabajoPractico.class);
    trabajoPracticoMock = mock(TrabajoPractico.class);
    controladorTrabajoPractico = new ControladorTrabajoPractico(servicioTrabajoPracticoMock);
  }

  @Test
  public void listarTrabajosPracticosDeberiaRetornarVistaConListaDeTps() {
    // Preparación
    when(trabajoPracticoMock.getEstado()).thenReturn(EstadoTP.PENDIENTE);
    List<TrabajoPractico> listaTps = new ArrayList<>();
    listaTps.add(trabajoPracticoMock);
    when(servicioTrabajoPracticoMock.obtenerTodos()).thenReturn(listaTps);

    // Ejecución
    ModelAndView modelAndView = controladorTrabajoPractico.listarTrabajosPracticos(null);

    // Validación
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("paginas/trabajos-practicos"));
    assertThat((List<?>) modelAndView.getModel().get("pendientes"), hasSize(1));
    assertThat((List<?>) modelAndView.getModel().get("enCurso"), empty());
    assertThat((List<?>) modelAndView.getModel().get("finalizados"), empty());
    verify(servicioTrabajoPracticoMock, times(1)).obtenerTodos();
  }

  @Test
  public void listarTrabajosPracticosConFiltroMateriaDeberiaRetornarVistaFiltrada() {
    // Preparación
    String materiaFiltro = "Taller Web";
    List<TrabajoPractico> listaTps = new ArrayList<>();
    when(servicioTrabajoPracticoMock.buscarPorMateria(materiaFiltro)).thenReturn(listaTps);

    // Ejecución
    ModelAndView modelAndView = controladorTrabajoPractico.listarTrabajosPracticos(materiaFiltro);

    // Validación
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("paginas/trabajos-practicos"));
    assertThat(modelAndView.getModel().get("pendientes"), notNullValue());
    assertThat(modelAndView.getModel().get("materiaBuscada"), equalTo(materiaFiltro));
    verify(servicioTrabajoPracticoMock, times(1)).buscarPorMateria(materiaFiltro);
  }

  @Test
  public void irANuevoTrabajoPracticoDeberiaRetornarVistaConTrabajoPracticoVacio() {
    // Ejecución
    ModelAndView modelAndView = controladorTrabajoPractico.irANuevoTrabajoPractico();

    // Validación
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("paginas/nuevo-trabajo-practico"));
    assertThat(modelAndView.getModel().get("trabajoPractico"), instanceOf(TrabajoPractico.class));
  }

  @Test
  public void guardarTrabajoPracticoDeberiaLlamarAlServicioYRedirigir() {
    // Ejecución
    ModelAndView modelAndView = controladorTrabajoPractico.guardarTrabajoPractico(
      trabajoPracticoMock
    );

    // Validación
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/trabajos-practicos"));
    verify(servicioTrabajoPracticoMock, times(1)).crearTrabajoPractico(trabajoPracticoMock);
  }

  @Test
  public void editarTrabajoPracticoDeberiaRetornarVistaConElTrabajoPracticoBuscado() {
    // Preparación
    Long id = 1L;
    when(servicioTrabajoPracticoMock.buscarPorId(id)).thenReturn(trabajoPracticoMock);

    // Ejecución
    ModelAndView modelAndView = controladorTrabajoPractico.editarTrabajoPractico(id);

    // Validación
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("paginas/nuevo-trabajo-practico"));
    assertThat(modelAndView.getModel().get("trabajoPractico"), notNullValue());
    verify(servicioTrabajoPracticoMock, times(1)).buscarPorId(id);
  }

  @Test
  public void eliminarTrabajoPracticoDeberiaLlamarAlServicioYRedirigir() {
    // Preparación
    Long id = 1L;

    // Ejecución
    String vistaRedireccion = controladorTrabajoPractico.eliminarTrabajoPractico(id);

    // Validación
    assertThat(vistaRedireccion, equalToIgnoringCase("redirect:/trabajos-practicos"));
    verify(servicioTrabajoPracticoMock, times(1)).eliminar(id);
  }
}
