package com.tallerwebi.dominio.parcial;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.sesionEstudio.ServicioSesionEstudio;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioParcialTest {

  private RepositorioParcial repositorioParcialMock;
  private ServicioSesionEstudio servicioSesionEstudioMock;
  private ServicioParcial servicioParcial;

  @BeforeEach
  public void init() {
    repositorioParcialMock = mock(RepositorioParcial.class);
    servicioSesionEstudioMock = mock(ServicioSesionEstudio.class);

    servicioParcial = new ServicioParcialImpl(repositorioParcialMock, servicioSesionEstudioMock);
  }

  @Test
  public void queSePuedaCrearUnParcial() {
    Parcial parcial = new Parcial();

    servicioParcial.crearParcial(parcial);

    verify(repositorioParcialMock, times(1)).guardar(parcial);
  }

  @Test
  public void queSePuedaBuscarUnParcialPorId() {
    Parcial parcial = new Parcial();
    Integer id = 1;

    when(repositorioParcialMock.buscarPorId(id)).thenReturn(parcial);

    Parcial parcialBuscado = servicioParcial.buscarParcialPorId(id);

    assertNotNull(parcialBuscado);
    assertEquals(parcial, parcialBuscado);

    verify(repositorioParcialMock, times(1)).buscarPorId(id);
  }

  @Test
  public void queSePuedanObtenerTodosLosParciales() {
    Parcial parcial1 = new Parcial();
    Parcial parcial2 = new Parcial();

    List<Parcial> parcialesEsperados = Arrays.asList(parcial1, parcial2);

    when(repositorioParcialMock.obtenerTodos()).thenReturn(parcialesEsperados);

    List<Parcial> parcialesObtenidos = servicioParcial.obtenerParciales();

    assertNotNull(parcialesObtenidos);
    assertEquals(2, parcialesObtenidos.size());
    assertEquals(parcialesEsperados, parcialesObtenidos);

    verify(repositorioParcialMock, times(1)).obtenerTodos();
  }

  @Test
  public void queSePuedaEditarUnParcial() {
    Parcial parcial = new Parcial();

    servicioParcial.editarParcial(parcial);

    verify(repositorioParcialMock, times(1)).modificar(parcial);
  }

  @Test
  public void queSePuedaEliminarUnParcialExistente() {
    Parcial parcial = new Parcial();
    Integer id = 1;

    when(repositorioParcialMock.buscarPorId(id)).thenReturn(parcial);

    servicioParcial.eliminarParcial(id);

    verify(repositorioParcialMock, times(1)).buscarPorId(id);

    verify(repositorioParcialMock, times(1)).eliminar(parcial);
  }

  @Test
  public void queNoSeElimineUnParcialSiNoExiste() {
    Integer id = 1;

    when(repositorioParcialMock.buscarPorId(id)).thenReturn(null);

    servicioParcial.eliminarParcial(id);

    verify(repositorioParcialMock, times(1)).buscarPorId(id);

    verify(repositorioParcialMock, never()).eliminar(any(Parcial.class));
  }

  @Test
  public void queSePuedaAgregarUnTemaAlParcial() {
    Integer parcialId = 1;
    Usuario usuario = new Usuario();
    Parcial parcial = new Parcial();

    String tema = "Hibernate";
    LocalDate fecha = LocalDate.of(2026, 10, 15);
    Integer cantidadHoras = 2;

    when(repositorioParcialMock.buscarPorId(parcialId)).thenReturn(parcial);

    servicioParcial.agregarTema(parcialId, tema, fecha, cantidadHoras, usuario);

    assertEquals(1, parcial.getSesiones().size());

    SesionEstudio sesion = parcial.getSesiones().get(0);

    assertEquals("Hibernate", sesion.getTema());
    assertEquals(fecha, sesion.getFecha());
    assertEquals(2, sesion.getCantidadHoras());
    assertFalse(sesion.getCompletada());

    assertEquals(parcial, sesion.getParcial());
    assertEquals(usuario, sesion.getUsuario());

    verify(servicioSesionEstudioMock, times(1)).crearSesion(sesion);
  }

  @Test
  public void queNoSeAgregueUnTemaSiElParcialNoExiste() {
    Integer parcialId = 1;

    when(repositorioParcialMock.buscarPorId(parcialId)).thenReturn(null);

    servicioParcial.agregarTema(
      parcialId,
      "Hibernate",
      LocalDate.of(2026, 10, 15),
      2,
      new Usuario()
    );

    verify(repositorioParcialMock, times(1)).buscarPorId(parcialId);

    verify(servicioSesionEstudioMock, never()).crearSesion(any(SesionEstudio.class));
  }

  @Test
  public void deberiaDevolverCeroPorcentajeCuandoLasSesionesSonNulas() {
    Parcial parcial = new Parcial();
    parcial.setSesiones(null);

    Integer resultado = parcial.getPorcentajeCompletado();

    assertEquals(0, resultado);
  }

  @Test
  public void deberiaDevolverCeroPorcentajeCuandoNoHaySesiones() {
    Parcial parcial = new Parcial();
    parcial.setSesiones(List.of());

    Integer resultado = parcial.getPorcentajeCompletado();

    assertEquals(0, resultado);
  }

  @Test
  public void deberiaCalcularPorcentajeDeSesionesCompletadas() {
    Parcial parcial = new Parcial();

    SesionEstudio sesion1 = new SesionEstudio();
    sesion1.setCompletada(true);

    SesionEstudio sesion2 = new SesionEstudio();
    sesion2.setCompletada(false);

    SesionEstudio sesion3 = new SesionEstudio();
    sesion3.setCompletada(true);

    SesionEstudio sesion4 = new SesionEstudio();
    sesion4.setCompletada(null);

    parcial.setSesiones(Arrays.asList(sesion1, sesion2, sesion3, sesion4));

    Integer resultado = parcial.getPorcentajeCompletado();

    assertEquals(50, resultado);
  }

  @Test
  public void deberiaDevolverCienPorCientoSiTodasLasSesionesEstanCompletadas() {
    Parcial parcial = new Parcial();

    SesionEstudio sesion1 = new SesionEstudio();
    sesion1.setCompletada(true);

    SesionEstudio sesion2 = new SesionEstudio();
    sesion2.setCompletada(true);

    parcial.setSesiones(Arrays.asList(sesion1, sesion2));

    assertEquals(100, parcial.getPorcentajeCompletado());
  }

  @Test
  public void deberiaDevolverCeroPorcentajeSiNingunaSesionEstaCompletada() {
    Parcial parcial = new Parcial();

    SesionEstudio sesion1 = new SesionEstudio();
    sesion1.setCompletada(false);

    SesionEstudio sesion2 = new SesionEstudio();
    sesion2.setCompletada(null);

    parcial.setSesiones(Arrays.asList(sesion1, sesion2));

    assertEquals(0, parcial.getPorcentajeCompletado());
  }

  @Test
  public void deberiaDevolverCeroCantidadSesionesCuandoLaListaEsNula() {
    Parcial parcial = new Parcial();
    parcial.setSesiones(null);

    Integer resultado = parcial.getCantidadSesiones();

    assertEquals(0, resultado);
  }

  @Test
  public void deberiaDevolverCeroCantidadSesionesCuandoLaListaEstaVacia() {
    Parcial parcial = new Parcial();
    parcial.setSesiones(List.of());

    Integer resultado = parcial.getCantidadSesiones();

    assertEquals(0, resultado);
  }

  @Test
  public void deberiaDevolverLaCantidadTotalDeSesiones() {
    Parcial parcial = new Parcial();

    SesionEstudio sesion1 = new SesionEstudio();
    SesionEstudio sesion2 = new SesionEstudio();
    SesionEstudio sesion3 = new SesionEstudio();

    parcial.setSesiones(Arrays.asList(sesion1, sesion2, sesion3));

    Integer resultado = parcial.getCantidadSesiones();

    assertEquals(3, resultado);
  }

  @Test
  public void deberiaDevolverCeroSesionesCompletadasCuandoLaListaEsNula() {
    Parcial parcial = new Parcial();
    parcial.setSesiones(null);

    Long resultado = parcial.getCantidadSesionesCompletadas();

    assertEquals(0L, resultado);
  }

  @Test
  public void deberiaContarSolamenteLasSesionesCompletadas() {
    Parcial parcial = new Parcial();

    SesionEstudio sesion1 = new SesionEstudio();
    sesion1.setCompletada(true);

    SesionEstudio sesion2 = new SesionEstudio();
    sesion2.setCompletada(false);

    SesionEstudio sesion3 = new SesionEstudio();
    sesion3.setCompletada(true);

    SesionEstudio sesion4 = new SesionEstudio();
    sesion4.setCompletada(null);

    parcial.setSesiones(Arrays.asList(sesion1, sesion2, sesion3, sesion4));

    Long resultado = parcial.getCantidadSesionesCompletadas();

    assertEquals(2L, resultado);
  }
}
