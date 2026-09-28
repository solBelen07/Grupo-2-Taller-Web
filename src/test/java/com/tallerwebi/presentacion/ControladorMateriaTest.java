package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Materia;
import com.tallerwebi.dominio.ServicioMateria;
import com.tallerwebi.dominio.excepcion.MateriaExistente;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.ModelAndView;

public class ControladorMateriaTest {

  private ServicioMateria servicioMateriaMock;
  private ControladorMateria controladorMateria;

  @BeforeEach
  public void init() {
    this.servicioMateriaMock = mock(ServicioMateria.class);
    this.controladorMateria = new ControladorMateria(this.servicioMateriaMock);
  }

  @Test
  public void queSePuedaCrearUnaMateriaCompletandoTodosLosDatos() throws MateriaExistente {
    DatosMateria datosMateria = new DatosMateria();

    datosMateria.setNombre("Taller Web I");
    datosMateria.setDescripcion("Materia de desarrollo web");
    datosMateria.setDocente("Spizzirri");
    datosMateria.setColor("#8B5CF6");
    datosMateria.setDias("Lunes y Miércoles");
    datosMateria.setHorario("18:00 - 22:00");
    datosMateria.setMaterialBibliografico("Documentación Spring");

    ModelAndView mav = controladorMateria.guardarMateria(datosMateria);

    ArgumentCaptor<Materia> materiaCaptor = ArgumentCaptor.forClass(Materia.class);

    verify(servicioMateriaMock, times(1)).crearMateria(materiaCaptor.capture());

    Materia materiaCreada = materiaCaptor.getValue();

    assertThat(materiaCreada.getNombre(), equalTo("Taller Web I"));
    assertThat(materiaCreada.getDescripcion(), equalTo("Materia de desarrollo web"));
    assertThat(materiaCreada.getDocente(), equalTo("Spizzirri"));
    assertThat(materiaCreada.getColor(), equalTo("#8B5CF6"));
    assertThat(materiaCreada.getDias(), equalTo("Lunes y Miércoles"));
    assertThat(materiaCreada.getHorario(), equalTo("18:00 - 22:00"));
    assertThat(materiaCreada.getMaterialBibliografico(), equalTo("Documentación Spring"));
    assertThat(mav.getViewName(), equalTo("redirect:/materias"));
  }

  @Test
  public void queAlCrearUnaMateriaAparezcaEnLaListaDeMaterias() throws MateriaExistente {
    DatosMateria datosMateria = new DatosMateria();

    datosMateria.setNombre("Taller Web I");

    controladorMateria.guardarMateria(datosMateria);

    ArgumentCaptor<Materia> materiaCaptor = ArgumentCaptor.forClass(Materia.class);

    verify(servicioMateriaMock).crearMateria(materiaCaptor.capture());

    Materia materiaCreada = materiaCaptor.getValue();

    when(servicioMateriaMock.obtenerMaterias()).thenReturn(List.of(materiaCreada));

    ModelAndView mav = controladorMateria.listarMaterias();

    List<Materia> materias = (List<Materia>) mav.getModel().get("materias");

    assertThat(mav.getViewName(), equalTo("materias"));
    assertThat(materias, hasItem(materiaCreada));
    assertThat(materias.size(), equalTo(1));
  }

  @Test
  public void queAlApretarUnaMateriaMuestreElDetalleDeLaMisma() {
    Integer id = 1;

    Materia materia = new Materia();
    materia.setId(id);
    materia.setNombre("Taller Web I");
    materia.setDocente("Spizzirri");

    when(servicioMateriaMock.buscarMateriaPorId(id)).thenReturn(materia);

    ModelAndView mav = controladorMateria.verMateria(id);

    assertThat(mav.getViewName(), equalTo("materia-detalle"));

    assertThat(mav.getModel().get("materia"), equalTo(materia));

    verify(servicioMateriaMock, times(1)).buscarMateriaPorId(id);
  }

  @Test
  public void queAlEditarLaMateriaSeGuardenLosCambios() {
    DatosMateria datosMateria = new DatosMateria();

    datosMateria.setId(1);
    datosMateria.setNombre("Taller Web I Modificado");
    datosMateria.setDescripcion("Nueva descripción");
    datosMateria.setDocente("Nuevo docente");
    datosMateria.setColor("#FF0000");
    datosMateria.setDias("Viernes");
    datosMateria.setHorario("19:00 - 23:00");
    datosMateria.setMaterialBibliografico("Nuevo material");

    ModelAndView mav = controladorMateria.guardarMateriaModificada(datosMateria);

    ArgumentCaptor<Materia> materiaCaptor = ArgumentCaptor.forClass(Materia.class);

    verify(servicioMateriaMock, times(1)).editarMateria(materiaCaptor.capture());

    Materia materiaModificada = materiaCaptor.getValue();

    assertThat(materiaModificada.getId(), equalTo(1));

    assertThat(materiaModificada.getNombre(), equalTo("Taller Web I Modificado"));

    assertThat(materiaModificada.getDescripcion(), equalTo("Nueva descripción"));

    assertThat(materiaModificada.getDocente(), equalTo("Nuevo docente"));

    assertThat(materiaModificada.getDias(), equalTo("Viernes"));

    assertThat(materiaModificada.getHorario(), equalTo("19:00 - 23:00"));

    assertThat(mav.getViewName(), equalTo("redirect:/materias"));
  }

  @Test
  public void queAlEliminarUnaMateriaYaNoAparezcaEnLaLista() {
    String nombre = "Taller Web I";

    Materia materia = new Materia();
    materia.setNombre(nombre);

    // Simulamos que inicialmente existe
    when(servicioMateriaMock.obtenerMaterias()).thenReturn(List.of(materia));

    ModelAndView antesDeEliminar = controladorMateria.listarMaterias();

    List<Materia> materiasAntes = (List<Materia>) antesDeEliminar.getModel().get("materias");

    assertThat(materiasAntes, hasItem(materia));

    // Eliminamos
    ModelAndView mav = controladorMateria.eliminarMateria(nombre);

    verify(servicioMateriaMock, times(1)).eliminarMateria(nombre);

    // Simulamos el estado luego de eliminar
    when(servicioMateriaMock.obtenerMaterias()).thenReturn(List.of());

    ModelAndView despuesDeEliminar = controladorMateria.listarMaterias();

    List<Materia> materiasDespues = (List<Materia>) despuesDeEliminar.getModel().get("materias");

    assertThat(materiasDespues, not(hasItem(materia)));

    assertThat(materiasDespues.isEmpty(), is(true));

    assertThat(mav.getViewName(), equalTo("redirect:/materias"));
  }

  @Test
  public void queAlBuscarUnaMateriaDevuelvaLoQueBusco() {
    String nombre = "Taller Web I";

    Materia materiaEsperada = new Materia();
    materiaEsperada.setNombre(nombre);
    materiaEsperada.setDocente("Spizzirri");

    when(servicioMateriaMock.buscarMateriaPorNombre(nombre)).thenReturn(materiaEsperada);

    ModelAndView mav = controladorMateria.buscarMateria(nombre);

    Materia materiaObtenida = (Materia) mav.getModel().get("materiaBuscada");

    assertThat(mav.getViewName(), equalTo("materias"));

    assertThat(materiaObtenida, equalTo(materiaEsperada));

    assertThat(materiaObtenida.getNombre(), equalTo(nombre));

    assertThat(mav.getModel().get("nombreBuscado"), equalTo(nombre));

    verify(servicioMateriaMock, times(1)).buscarMateriaPorNombre(nombre);
  }
}
