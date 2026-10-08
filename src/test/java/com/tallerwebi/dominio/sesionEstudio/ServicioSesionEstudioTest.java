package com.tallerwebi.dominio.sesionEstudio;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioSesionEstudioTest {

  private RepositorioSesionEstudio repositorioSesionEstudioMock;
  private ServicioSesionEstudio servicioSesionEstudio;

  @BeforeEach
  public void init() {
    repositorioSesionEstudioMock = mock(RepositorioSesionEstudio.class);

    servicioSesionEstudio = new ServicioSesionEstudioImpl(repositorioSesionEstudioMock);
  }

  @Test
  public void queSePuedaCrearUnaSesionDeEstudio() {
    // preparacion
    SesionEstudio sesionEstudio = new SesionEstudio();

    // ejecucion
    servicioSesionEstudio.crearSesion(sesionEstudio);

    // validacion
    verify(repositorioSesionEstudioMock, times(1)).guardar(sesionEstudio);
  }

  @Test
  public void queSePuedaEditarUnaSesionDeEstudio() {
    // preparacion
    SesionEstudio sesionEstudio = new SesionEstudio();

    sesionEstudio.setTema("Spring MVC");

    // ejecucion
    servicioSesionEstudio.editarSesion(sesionEstudio);

    // validacion
    verify(repositorioSesionEstudioMock, times(1)).modificar(sesionEstudio);
  }

  @Test
  public void queSePuedaEliminarUnaSesionDeEstudio() {
    // preparacion
    SesionEstudio sesionEstudio = new SesionEstudio();

    // ejecucion
    servicioSesionEstudio.eliminarSesion(sesionEstudio);

    // validacion
    verify(repositorioSesionEstudioMock, times(1)).eliminar(sesionEstudio);
  }

  @Test
  public void queSePuedaMarcarUnaSesionComoCompletada() {
    // preparacion
    Integer id = 1;

    SesionEstudio sesionEstudio = new SesionEstudio();

    sesionEstudio.setCompletada(false);

    when(repositorioSesionEstudioMock.buscarPorId(id)).thenReturn(sesionEstudio);

    // ejecucion
    servicioSesionEstudio.cambiarEstado(id);

    // validacion
    assertTrue(sesionEstudio.getCompletada());

    verify(repositorioSesionEstudioMock, times(1)).buscarPorId(id);

    verify(repositorioSesionEstudioMock, times(1)).modificar(sesionEstudio);
  }

  @Test
  public void queSePuedaMarcarUnaSesionCompletadaComoNoCompletada() {
    // preparacion
    Integer id = 1;

    SesionEstudio sesionEstudio = new SesionEstudio();

    sesionEstudio.setCompletada(true);

    when(repositorioSesionEstudioMock.buscarPorId(id)).thenReturn(sesionEstudio);

    // ejecucion
    servicioSesionEstudio.cambiarEstado(id);

    // validacion
    assertFalse(sesionEstudio.getCompletada());

    verify(repositorioSesionEstudioMock, times(1)).buscarPorId(id);

    verify(repositorioSesionEstudioMock, times(1)).modificar(sesionEstudio);
  }

  @Test
  public void queNoSeModifiqueUnaSesionSiNoExiste() {
    // preparacion
    Integer id = 1;

    when(repositorioSesionEstudioMock.buscarPorId(id)).thenReturn(null);

    // ejecucion
    servicioSesionEstudio.cambiarEstado(id);

    // validacion
    verify(repositorioSesionEstudioMock, times(1)).buscarPorId(id);

    verify(repositorioSesionEstudioMock, never()).modificar(any(SesionEstudio.class));
  }
}
