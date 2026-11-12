package com.tallerwebi.presentacion.parcial;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.parcial.Parcial;
import com.tallerwebi.dominio.parcial.ServicioParcial;
import com.tallerwebi.dominio.racha.ServicioRacha;
import com.tallerwebi.dominio.sesionEstudio.ServicioSesionEstudio;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class ControladorParcialTest {

  private ServicioMateria servicioMateriaMock;
  private ServicioParcial servicioParcialMock;
  private ServicioSesionEstudio servicioSesionEstudioMock;
  private ServicioRacha servicioRachaMock;
  private Usuario usuarioLogueado = new Usuario();

  private ControladorParcial controladorParcial;

  @BeforeEach
  public void init() {
    servicioMateriaMock = mock(ServicioMateria.class);

    servicioParcialMock = mock(ServicioParcial.class);

    servicioSesionEstudioMock = mock(ServicioSesionEstudio.class);

    servicioRachaMock = mock(ServicioRacha.class);

    controladorParcial =
      new ControladorParcial(
        servicioMateriaMock,
        servicioParcialMock,
        servicioSesionEstudioMock,
        servicioRachaMock
      );
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

    ModelAndView mav = controladorParcial.guardarParcial(datosParcial, requestConUsuario());

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

    controladorParcial.guardarParcial(datosParcial, requestConUsuario());

    verify(servicioParcialMock, times(1))
      .agregarTema(5, "Spring MVC", LocalDate.of(2026, 10, 17), 2, usuarioLogueado);

    verify(servicioParcialMock, times(1))
      .agregarTema(5, "Hibernate", LocalDate.of(2026, 10, 18), 2, usuarioLogueado);

    verify(servicioParcialMock, times(1))
      .agregarTema(5, "Mockito", LocalDate.of(2026, 10, 19), 2, usuarioLogueado);
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

    controladorParcial.guardarParcial(datosParcial, requestConUsuario());

    verify(servicioParcialMock, times(1))
      .agregarTema(7, "Spring MVC", LocalDate.of(2026, 10, 17), 2, usuarioLogueado);

    verify(servicioParcialMock, times(1))
      .agregarTema(anyInt(), anyString(), any(LocalDate.class), anyInt(), any(Usuario.class));
  }

  @Test
  public void queSePuedaCambiarElEstadoDeUnaSesion() {
    Integer idSesion = 3;
    MockHttpServletRequest request = new MockHttpServletRequest();

    ModelAndView mav = controladorParcial.cambiarEstadoSesion(idSesion, request);

    verify(servicioSesionEstudioMock, times(1)).cambiarEstado(idSesion);

    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
  }

  @Test
  public void queAlCambiarElEstadoDeUnaSesionSeRegistreLaRachaDelUsuarioLogueado() {
    Integer idSesion = 3;
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.getSession().setAttribute("USUARIO", usuario);
    when(servicioSesionEstudioMock.cambiarEstado(idSesion)).thenReturn(true);

    ModelAndView mav = controladorParcial.cambiarEstadoSesion(idSesion, request);

    InOrder orden = inOrder(servicioSesionEstudioMock, servicioRachaMock);
    orden.verify(servicioSesionEstudioMock, times(1)).cambiarEstado(idSesion);
    orden.verify(servicioRachaMock, times(1)).recalcularRacha(usuario);

    assertThat((Usuario) request.getSession().getAttribute("USUARIO"), sameInstance(usuario));
    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
  }

  @Test
  public void queAlCambiarElEstadoDeUnaSesionNoSeRegistreLaRachaSiNoHayUsuarioEnSesion() {
    Integer idSesion = 3;
    MockHttpServletRequest request = new MockHttpServletRequest();

    ModelAndView mav = controladorParcial.cambiarEstadoSesion(idSesion, request);

    verify(servicioSesionEstudioMock, times(1)).cambiarEstado(idSesion);
    verifyNoInteractions(servicioRachaMock);
    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
  }

  @Test
  public void queAlDesmarcarUnaSesionSeRecalculeLaRacha() {
    Integer idSesion = 3;
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.getSession().setAttribute("USUARIO", usuario);
    when(servicioSesionEstudioMock.cambiarEstado(idSesion)).thenReturn(false);

    ModelAndView mav = controladorParcial.cambiarEstadoSesion(idSesion, request);

    verify(servicioRachaMock, times(1)).recalcularRacha(usuario);
    verify(servicioRachaMock, never()).registrarRacha(any(Usuario.class));
    assertThat(request.getSession().getAttribute("USUARIO"), sameInstance(usuario));
    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
  }

  private MockHttpServletRequest requestConUsuario() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.getSession().setAttribute("USUARIO", usuarioLogueado);
    return request;
  }

  @Test
  public void guardarUnParcialSinEstarLogueadoDeberiaRedirigirAlLogin() {
    ModelAndView mav = controladorParcial.guardarParcial(
      new DatosParcial(),
      new MockHttpServletRequest()
    );

    assertThat(mav.getViewName(), equalTo("redirect:/login"));
    verifyNoInteractions(
      servicioMateriaMock,
      servicioParcialMock,
      servicioSesionEstudioMock,
      servicioRachaMock
    );
  }

  // --- Validaciones de guardarParcial ---

  @Test
  public void queNoSeGuardeUnParcialCuandoLosTemasSonNulos() {
    DatosParcial datos = datosValidos(3, 2, null);
    comprobarFormularioConError(datos);
  }

  @Test
  public void queNoSeGuardeUnParcialCuandoTodosLosTemasEstanVacios() {
    DatosParcial datos = datosValidos(3, 2, java.util.Arrays.asList(null, "", "   ", "\t"));
    comprobarFormularioConError(datos);
  }

  @Test
  public void queNoSeGuardeUnParcialCuandoLosDiasSonNulos() {
    DatosParcial datos = datosValidos(3, 2, List.of("Tema 1"));
    datos.setCantidadDiasEstudio(null);
    comprobarFormularioConError(datos);
  }

  @Test
  public void queNoSeGuardeUnParcialCuandoLosDiasSonCero() {
    DatosParcial datos = datosValidos(0, 2, List.of("Tema 1"));
    comprobarFormularioConError(datos);
  }

  @Test
  public void queNoSeGuardeUnParcialCuandoLosDiasSonNegativos() {
    DatosParcial datos = datosValidos(-1, 2, List.of("Tema 1"));
    comprobarFormularioConError(datos);
  }

  @Test
  public void queNoSeGuardeUnParcialCuandoLasHorasSonNulas() {
    DatosParcial datos = datosValidos(3, 2, List.of("Tema 1"));
    datos.setHorasPorDia(null);
    comprobarFormularioConError(datos);
  }

  @Test
  public void queNoSeGuardeUnParcialCuandoLasHorasSonCero() {
    DatosParcial datos = datosValidos(3, 0, List.of("Tema 1"));
    comprobarFormularioConError(datos);
  }

  @Test
  public void queNoSeGuardeUnParcialCuandoLasHorasSonNegativas() {
    DatosParcial datos = datosValidos(3, -2, List.of("Tema 1"));
    comprobarFormularioConError(datos);
  }

  @Test
  public void queNoSeGuardeUnParcialCuandoHayMasTemasQueHorasDisponibles() {
    // Un día de dos horas no alcanza para tres temas de al menos una hora.
    DatosParcial datos = datosValidos(1, 2, List.of("Tema 1", "Tema 2", "Tema 3"));
    comprobarFormularioConError(datos);
  }

  @Test
  public void queSePermitaGuardarElMaximoDeTemasSegunDiasYHoras() {
    DatosParcial datos = datosValidos(2, 2, List.of("Tema 1", "Tema 2", "Tema 3", "Tema 4"));
    prepararGuardadoParcial(21);

    ModelAndView mav = controladorParcial.guardarParcial(datos, requestConUsuario());

    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
    verify(servicioParcialMock).crearParcial(any(Parcial.class));
    verify(servicioParcialMock)
      .agregarTema(21, "Tema 1", LocalDate.of(2026, 10, 18), 1, usuarioLogueado);
    verify(servicioParcialMock)
      .agregarTema(21, "Tema 2", LocalDate.of(2026, 10, 18), 1, usuarioLogueado);
    verify(servicioParcialMock)
      .agregarTema(21, "Tema 3", LocalDate.of(2026, 10, 19), 1, usuarioLogueado);
    verify(servicioParcialMock)
      .agregarTema(21, "Tema 4", LocalDate.of(2026, 10, 19), 1, usuarioLogueado);
    verify(servicioParcialMock, times(4))
      .agregarTema(anyInt(), anyString(), any(LocalDate.class), anyInt(), any(Usuario.class));
  }

  @Test
  public void queSeRepartanLosTemasSobrantesYLasHorasSobrantes() {
    DatosParcial datos = datosValidos(
      3,
      3,
      List.of("Tema 1", "Tema 2", "Tema 3", "Tema 4", "Tema 5")
    );
    prepararGuardadoParcial(22);

    ModelAndView mav = controladorParcial.guardarParcial(datos, requestConUsuario());

    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
    InOrder orden = inOrder(servicioParcialMock);
    orden.verify(servicioParcialMock).crearParcial(any(Parcial.class));
    orden
      .verify(servicioParcialMock)
      .agregarTema(22, "Tema 1", LocalDate.of(2026, 10, 17), 2, usuarioLogueado);
    orden
      .verify(servicioParcialMock)
      .agregarTema(22, "Tema 2", LocalDate.of(2026, 10, 17), 1, usuarioLogueado);
    orden
      .verify(servicioParcialMock)
      .agregarTema(22, "Tema 3", LocalDate.of(2026, 10, 18), 2, usuarioLogueado);
    orden
      .verify(servicioParcialMock)
      .agregarTema(22, "Tema 4", LocalDate.of(2026, 10, 18), 1, usuarioLogueado);
    orden
      .verify(servicioParcialMock)
      .agregarTema(22, "Tema 5", LocalDate.of(2026, 10, 19), 3, usuarioLogueado);
    orden.verifyNoMoreInteractions();
  }

  @Test
  public void queSiHayMenosTemasQueDiasSoloSeCreenSesionesParaDiasConTemas() {
    DatosParcial datos = datosValidos(5, 3, List.of("Tema 1", "Tema 2"));
    prepararGuardadoParcial(23);

    ModelAndView mav = controladorParcial.guardarParcial(datos, requestConUsuario());

    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
    verify(servicioParcialMock)
      .agregarTema(23, "Tema 1", LocalDate.of(2026, 10, 15), 3, usuarioLogueado);
    verify(servicioParcialMock)
      .agregarTema(23, "Tema 2", LocalDate.of(2026, 10, 16), 3, usuarioLogueado);
    verify(servicioParcialMock, times(2))
      .agregarTema(anyInt(), anyString(), any(LocalDate.class), anyInt(), any(Usuario.class));
  }

  @Test
  public void queSeEliminenTemasVaciosYSeQuitenEspaciosAlGuardar() {
    DatosParcial datos = datosValidos(
      2,
      2,
      java.util.Arrays.asList("  Spring MVC  ", "   ", null, " Hibernate ")
    );
    prepararGuardadoParcial(24);

    ModelAndView mav = controladorParcial.guardarParcial(datos, requestConUsuario());

    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
    verify(servicioParcialMock)
      .agregarTema(24, "Spring MVC", LocalDate.of(2026, 10, 18), 2, usuarioLogueado);
    verify(servicioParcialMock)
      .agregarTema(24, "Hibernate", LocalDate.of(2026, 10, 19), 2, usuarioLogueado);
    verify(servicioParcialMock, times(2))
      .agregarTema(anyInt(), anyString(), any(LocalDate.class), anyInt(), any(Usuario.class));
  }

  @Test
  public void queUnSoloTemaRecibaTodasLasHorasDelDia() {
    DatosParcial datos = datosValidos(1, 4, List.of("Tema único"));
    prepararGuardadoParcial(25);

    ModelAndView mav = controladorParcial.guardarParcial(datos, requestConUsuario());

    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
    verify(servicioParcialMock)
      .agregarTema(25, "Tema único", LocalDate.of(2026, 10, 19), 4, usuarioLogueado);
    verify(servicioParcialMock, times(1))
      .agregarTema(anyInt(), anyString(), any(LocalDate.class), anyInt(), any(Usuario.class));
  }

  @Test
  public void queSiElServicioDevuelveNullNoSeRecalculeLaRacha() {
    MockHttpServletRequest request = requestConUsuario();
    when(servicioSesionEstudioMock.cambiarEstado(8)).thenReturn(null);

    ModelAndView mav = controladorParcial.cambiarEstadoSesion(8, request);

    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
    verify(servicioSesionEstudioMock).cambiarEstado(8);
    verifyNoInteractions(servicioRachaMock);
    assertThat(request.getSession().getAttribute("USUARIO"), sameInstance(usuarioLogueado));
  }

  @Test
  public void queNoSeRecalculeLaRachaSinUsuarioAunqueSeCompleteLaSesion() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    when(servicioSesionEstudioMock.cambiarEstado(8)).thenReturn(true);

    ModelAndView mav = controladorParcial.cambiarEstadoSesion(8, request);

    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
    verify(servicioSesionEstudioMock).cambiarEstado(8);
    verifyNoInteractions(servicioRachaMock);
  }

  @Test
  public void queAlDesmarcarUnaSesionPrimeroCambieEstadoYDespuesRecalculeLaRacha() {
    MockHttpServletRequest request = requestConUsuario();
    when(servicioSesionEstudioMock.cambiarEstado(9)).thenReturn(false);

    ModelAndView mav = controladorParcial.cambiarEstadoSesion(9, request);

    assertThat(mav.getViewName(), equalTo("redirect:/parciales"));
    InOrder orden = inOrder(servicioSesionEstudioMock, servicioRachaMock);
    orden.verify(servicioSesionEstudioMock).cambiarEstado(9);
    orden.verify(servicioRachaMock).recalcularRacha(usuarioLogueado);
    verify(servicioRachaMock, never()).registrarRacha(any(Usuario.class));
    assertThat(request.getSession().getAttribute("USUARIO"), sameInstance(usuarioLogueado));
  }

  private DatosParcial datosValidos(int dias, int horas, List<String> temas) {
    DatosParcial datos = new DatosParcial();
    datos.setNombre("Primer Parcial");
    datos.setMateriaId(1L);
    datos.setFecha(LocalDate.of(2026, 10, 20));
    datos.setHorario(LocalTime.of(18, 0));
    datos.setCantidadDiasEstudio(dias);
    datos.setHorasPorDia(horas);
    datos.setTemas(temas);
    return datos;
  }

  private void prepararGuardadoParcial(int idParcial) {
    Materia materia = new Materia();
    materia.setId(1);
    when(servicioMateriaMock.buscarMateriaPorId(1)).thenReturn(materia);
    doAnswer(invocation -> {
        Parcial parcial = invocation.getArgument(0);
        parcial.setId(idParcial);
        return null;
      })
      .when(servicioParcialMock)
      .crearParcial(any(Parcial.class));
  }

  private void comprobarFormularioConError(DatosParcial datos) {
    List<Materia> materias = List.of(new Materia());
    when(servicioMateriaMock.obtenerMaterias()).thenReturn(materias);

    ModelAndView mav = controladorParcial.guardarParcial(datos, requestConUsuario());

    assertThat(mav.getViewName(), equalTo("paginas/materia/parcial-formulario"));
    assertThat(mav.getModel().get("datosParcial"), sameInstance(datos));
    assertThat(mav.getModel().get("materias"), sameInstance(materias));
    assertThat((String) mav.getModel().get("error"), containsString("Ingresá datos válidos"));
    verify(servicioMateriaMock).obtenerMaterias();
    verify(servicioMateriaMock, never()).buscarMateriaPorId(anyInt());
    verifyNoInteractions(servicioParcialMock, servicioSesionEstudioMock, servicioRachaMock);
  }
}
