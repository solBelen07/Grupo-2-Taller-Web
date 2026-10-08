package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.excepcionMateria.MateriaExistente;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.presentacion.materia.ControladorMateria;
import com.tallerwebi.presentacion.materia.DatosMateria;
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
  public void queAlIngresarANuevaMateriaSeMuestreElFormulario() {
    ModelAndView mav = controladorMateria.nuevaMateria();

    assertThat(mav.getViewName(), equalTo("materia-formulario"));
    assertThat(mav.getModel().get("datosMateria"), instanceOf(DatosMateria.class));
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
  public void queNoSePuedaCrearUnaMateriaSiYaExiste() throws MateriaExistente {
    DatosMateria datosMateria = new DatosMateria();

    datosMateria.setNombre("Taller Web I");

    doThrow(new MateriaExistente()).when(servicioMateriaMock).crearMateria(any(Materia.class));

    ModelAndView mav = controladorMateria.guardarMateria(datosMateria);

    assertThat(mav.getViewName(), equalTo("materia-formulario"));
    assertThat(mav.getModel().get("error"), equalTo("Ya existe una materia con ese nombre"));
    assertThat(mav.getModel().get("datosMateria"), equalTo(datosMateria));
    verify(servicioMateriaMock, times(1)).crearMateria(any(Materia.class));
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

    assertThat(mav.getViewName(), equalTo("paginas/materia/materias"));
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

    assertThat(mav.getViewName(), equalTo("paginas/materia/materia-detalle"));

    assertThat(mav.getModel().get("materia"), equalTo(materia));

    verify(servicioMateriaMock, times(1)).buscarMateriaPorId(id);
  }

  @Test
  public void queAlEditarUnaMateriaSeCarguenSusDatosEnElFormulario() {
    Integer id = 1;

    Materia materia = new Materia();

    materia.setId(id);
    materia.setNombre("Taller Web I");
    materia.setDescripcion("Materia de desarrollo web");
    materia.setDocente("Spizzirri");
    materia.setColor("#8B5CF6");
    materia.setDias("Lunes y Miércoles");
    materia.setHorario("18:00 - 22:00");
    materia.setMaterialBibliografico("Documentación Spring");

    when(servicioMateriaMock.buscarMateriaPorId(id)).thenReturn(materia);

    ModelAndView mav = controladorMateria.editarMateria(id);

    DatosMateria datosMateria = (DatosMateria) mav.getModel().get("datosMateria");

    assertThat(mav.getViewName(), equalTo("materia-formulario"));

    assertThat(datosMateria.getId(), equalTo(id));

    assertThat(datosMateria.getNombre(), equalTo("Taller Web I"));

    assertThat(datosMateria.getDescripcion(), equalTo("Materia de desarrollo web"));

    assertThat(datosMateria.getDocente(), equalTo("Spizzirri"));

    assertThat(datosMateria.getColor(), equalTo("#8B5CF6"));

    assertThat(datosMateria.getDias(), equalTo("Lunes y Miércoles"));

    assertThat(datosMateria.getHorario(), equalTo("18:00 - 22:00"));

    assertThat(datosMateria.getMaterialBibliografico(), equalTo("Documentación Spring"));

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

    assertThat(materiaModificada.getColor(), equalTo("#FF0000"));

    assertThat(materiaModificada.getDias(), equalTo("Viernes"));

    assertThat(materiaModificada.getHorario(), equalTo("19:00 - 23:00"));

    assertThat(materiaModificada.getMaterialBibliografico(), equalTo("Nuevo material"));

    assertThat(mav.getViewName(), equalTo("redirect:/materias"));
  }

  @Test
  public void queAlEliminarUnaMateriaYaNoAparezcaEnLaLista() {
    String nombre = "Taller Web I";

    Materia materia = new Materia();

    materia.setNombre(nombre);

    when(servicioMateriaMock.obtenerMaterias()).thenReturn(List.of(materia));

    ModelAndView antesDeEliminar = controladorMateria.listarMaterias();

    List<Materia> materiasAntes = (List<Materia>) antesDeEliminar.getModel().get("materias");

    assertThat(materiasAntes, hasItem(materia));

    ModelAndView mav = controladorMateria.eliminarMateria(nombre);

    verify(servicioMateriaMock, times(1)).eliminarMateria(nombre);

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

    assertThat(mav.getViewName(), equalTo("paginas/materia/materias"));
    assertThat(materiaObtenida, equalTo(materiaEsperada));
    assertThat(materiaObtenida.getNombre(), equalTo(nombre));
    assertThat(mav.getModel().get("nombreBuscado"), equalTo(nombre));
    verify(servicioMateriaMock, times(1)).buscarMateriaPorNombre(nombre);
  }
}
