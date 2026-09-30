package com.tallerwebi.dominio.parcial;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.parcial.*;
import com.tallerwebi.dominio.sesionEstudio.*;
import com.tallerwebi.dominio.sesionEstudio.ServicioSesionEstudio;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
