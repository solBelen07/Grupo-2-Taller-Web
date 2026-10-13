package com.tallerwebi.presentacion.parcial;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.parcial.Parcial;
import com.tallerwebi.dominio.parcial.ServicioParcial;
import com.tallerwebi.dominio.sesionEstudio.ServicioSesionEstudio;
import com.tallerwebi.presentacion.parcial.ControladorParcial;
import com.tallerwebi.presentacion.parcial.DatosParcial;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.ModelAndView;

public class ControladorParcialTest {

  private ServicioMateria servicioMateriaMock;
  private ServicioParcial servicioParcialMock;
  private ServicioSesionEstudio servicioSesionEstudioMock;

  private ControladorParcial controladorParcial;

  @BeforeEach
  public void init() {
    servicioMateriaMock = mock(ServicioMateria.class);

    servicioParcialMock = mock(ServicioParcial.class);

    servicioSesionEstudioMock = mock(ServicioSesionEstudio.class);

    controladorParcial =
      new ControladorParcial(servicioMateriaMock, servicioParcialMock, servicioSesionEstudioMock);
  }

  @Test
  public void queAlIngresarANuevoParcialSeMuestreElFormularioConLasMaterias() {
    Materia materia1 = new Materia();
    materia1.setNombre("Taller Web I");

    Materia materia2 = new Materia();
    materia2.setNombre("Base de Datos II");

    List<Materia> materias = List.of(materia1, materia2);

    when(servicioMateriaMock.obtenerMaterias()).thenReturn(materias);

    ModelAndView mav = controladorParcial.nuevoParcial();

    assertThat(mav.getViewName(), equalTo("paginas/materia/parcial-formulario"));

    assertThat(mav.getModel().get("materias"), equalTo(materias));

    assertThat(mav.getModel().get("datosParcial") instanceof DatosParcial, equalTo(true));

    verify(servicioMateriaMock, times(1)).obtenerMaterias();
  }

  @Test
  public void queSePuedanListarLosParciales() {
    Parcial parcial1 = new Parcial();
    Parcial parcial2 = new Parcial();

    parcial1.setNombre("Primer Parcial");
    parcial2.setNombre("Segundo Parcial");

    List<Parcial> parciales = List.of(parcial1, parcial2);

    when(servicioParcialMock.obtenerParciales()).thenReturn(parciales);

    ModelAndView mav = controladorParcial.listarParciales();

    assertThat(mav.getViewName(), equalTo("parciales"));

    assertThat(mav.getModel().get("parciales"), equalTo(parciales));

    verify(servicioParcialMock, times(1)).obtenerParciales();
  }

  @Test
  public void queSePuedaGuardarUnParcialConTodosSusDatos() {
    DatosParcial datosParcial = new DatosParcial();

    datosParcial.setNombre("Primer Parcial");
    datosParcial.setMateriaId(1L);

    datosParcial.setFecha(LocalDate.of(2026, 10, 20));

    datosParcial.setHorario(LocalTime.of(18, 0));

    datosParcial.setCantidadDiasEstudio(5);
    datosParcial.setHorasPorDia(2);

    datosParcial.setTemas(List.of("Spring MVC", "Hibernate"));

    Materia materia = new Materia();
    materia.setId(1);
    materia.setNombre("Taller Web I");

    when(servicioMateriaMock.buscarMateriaPorId(1)).thenReturn(materia);

    doAnswer(invocation -> {
        Parcial parcial = invocation.getArgument(0);

        parcial.setId(10);

        return null;
      })
      .when(servicioParcialMock)
      .crearParcial(any(Parcial.class));

    ModelAndView mav = controladorParcial.guardarParcial(datosParcial);

    ArgumentCaptor<Parcial> parcialCaptor = ArgumentCaptor.forClass(Parcial.class);

    verify(servicioParcialMock, times(1)).crearParcial(parcialCaptor.capture());

    Parcial parcialCreado = parcialCaptor.getValue();

    assertThat(parcialCreado.getNombre(), equalTo("Primer Parcial"));

    assertThat(parcialCreado.getMateria(), equalTo(materia));

    assertThat(parcialCreado.getFecha(), equalTo(LocalDate.of(2026, 10, 20)));

    assertThat(parcialCreado.getHorario(), equalTo(LocalTime.of(18, 0)));

    assertThat(parcialCreado.getCantidadDiasEstudio(), equalTo(5));

    assertThat(parcialCreado.getHorasPorDia(), equalTo(2));

    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
  }

  @Test
  public void queAlGuardarUnParcialSeCreenLasSesionesParaCadaTema() {
    DatosParcial datosParcial = new DatosParcial();

    datosParcial.setNombre("Primer Parcial");
    datosParcial.setMateriaId(1L);

    datosParcial.setFecha(LocalDate.of(2026, 10, 20));

    datosParcial.setHorario(LocalTime.of(18, 0));

    datosParcial.setCantidadDiasEstudio(3);
    datosParcial.setHorasPorDia(2);

    datosParcial.setTemas(List.of("Spring MVC", "Hibernate", "Mockito"));

    Materia materia = new Materia();
    materia.setId(1);

    when(servicioMateriaMock.buscarMateriaPorId(1)).thenReturn(materia);

    doAnswer(invocation -> {
        Parcial parcial = invocation.getArgument(0);

        parcial.setId(5);

        return null;
      })
      .when(servicioParcialMock)
      .crearParcial(any(Parcial.class));

    controladorParcial.guardarParcial(datosParcial);

    /*
     * Fecha parcial:
     * 20/10/2026
     *
     * 3 días de estudio:
     * fechaInicio = 17/10/2026
     */

    verify(servicioParcialMock, times(1))
      .agregarTema(5, "Spring MVC", LocalDate.of(2026, 10, 17), 2);

    verify(servicioParcialMock, times(1))
      .agregarTema(5, "Hibernate", LocalDate.of(2026, 10, 18), 2);

    verify(servicioParcialMock, times(1)).agregarTema(5, "Mockito", LocalDate.of(2026, 10, 19), 2);
  }

  @Test
  public void queNoSeCreeUnaSesionSiElTemaEstaVacioONull() {
    DatosParcial datosParcial = new DatosParcial();

    datosParcial.setNombre("Primer Parcial");
    datosParcial.setMateriaId(1L);

    datosParcial.setFecha(LocalDate.of(2026, 10, 20));

    datosParcial.setHorario(LocalTime.of(18, 0));

    datosParcial.setCantidadDiasEstudio(3);
    datosParcial.setHorasPorDia(2);

    datosParcial.setTemas(java.util.Arrays.asList("Spring MVC", "", null));

    Materia materia = new Materia();
    materia.setId(1);

    when(servicioMateriaMock.buscarMateriaPorId(1)).thenReturn(materia);

    doAnswer(invocation -> {
        Parcial parcial = invocation.getArgument(0);

        parcial.setId(7);

        return null;
      })
      .when(servicioParcialMock)
      .crearParcial(any(Parcial.class));

    controladorParcial.guardarParcial(datosParcial);

    verify(servicioParcialMock, times(1))
      .agregarTema(7, "Spring MVC", LocalDate.of(2026, 10, 17), 2);

    /*
     * Solo debe crear una sesión,
     * porque "" y null no son temas válidos.
     */
    verify(servicioParcialMock, times(1))
      .agregarTema(anyInt(), anyString(), any(LocalDate.class), anyInt());
  }

  @Test
  public void queSePuedaCambiarElEstadoDeUnaSesion() {
    Integer idSesion = 3;

    ModelAndView mav = controladorParcial.cambiarEstadoSesion(idSesion);

    verify(servicioSesionEstudioMock, times(1)).cambiarEstado(idSesion);

    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
  }
}
