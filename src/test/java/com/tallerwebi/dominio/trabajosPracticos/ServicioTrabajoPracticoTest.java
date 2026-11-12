package com.tallerwebi.dominio.trabajosPracticos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioTrabajoPracticoTest {

  private RepositorioTrabajoPractico repositorioTrabajoPracticoMock;
  private ServicioTrabajoPractico servicioTrabajoPractico;

  @BeforeEach
  public void init() {
    this.repositorioTrabajoPracticoMock = mock(RepositorioTrabajoPractico.class);
    this.servicioTrabajoPractico =
      new ServicioTrabajoPracticoImpl(this.repositorioTrabajoPracticoMock);
  }

  @Test
  void crearTrabajoPracticoDeberiaCrearloYGuardarloEnElRepositorio() {
    TrabajoPractico tpNuevo = new TrabajoPractico(
      "TP Arquitectura",
      "Desarrollo de Software",
      LocalDateTime.now().plusDays(7),
      TipoTrabajo.GRUPAL,
      "Descripción del TP"
    );

    when(repositorioTrabajoPracticoMock.save(any(TrabajoPractico.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    TrabajoPractico resultado = servicioTrabajoPractico.crearTrabajoPractico(tpNuevo);

    assertNotNull(resultado);
    assertEquals("TP Arquitectura", resultado.getNombre());
    assertEquals(EstadoTP.PENDIENTE, resultado.getEstado());
    verify(repositorioTrabajoPracticoMock, times(1)).save(any(TrabajoPractico.class));
  }

  @Test
  void deberiaCambiarElEstadoDelTrabajoPracticoExitosamente() {
    Long idTp = 1L;
    TrabajoPractico tpExistente = new TrabajoPractico(
      "TP Arquitectura",
      "Desarrollo de Software",
      LocalDateTime.now().plusDays(7),
      TipoTrabajo.GRUPAL,
      "Descripción del TP"
    );

    assertEquals(EstadoTP.PENDIENTE, tpExistente.getEstado());

    when(repositorioTrabajoPracticoMock.buscarPorId((long) idTp)).thenReturn(tpExistente);

    when(repositorioTrabajoPracticoMock.save(any(TrabajoPractico.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    TrabajoPractico resultado = servicioTrabajoPractico.cambiarEstado(idTp, EstadoTP.EN_CURSO);

    assertNotNull(resultado);
    assertEquals(EstadoTP.EN_CURSO, resultado.getEstado());

    verify(repositorioTrabajoPracticoMock, times(1)).buscarPorId((long) idTp);
    verify(repositorioTrabajoPracticoMock, times(1)).save(any(TrabajoPractico.class));
  }

  @Test
  void deberiaRetornarListaDeTrabajosPracticos() {
    List<TrabajoPractico> listaSimulada = List.of(
      new TrabajoPractico("TP 1", "Matemática", LocalDateTime.now(), TipoTrabajo.GRUPAL, "Desc 1"),
      new TrabajoPractico("TP 2", "Física", LocalDateTime.now(), TipoTrabajo.INDIVIDUAL, "Desc 2")
    );

    when(repositorioTrabajoPracticoMock.obtenerTodos()).thenReturn(listaSimulada);

    List<TrabajoPractico> resultado = servicioTrabajoPractico.obtenerTodos();

    assertNotNull(resultado);
    assertEquals(2, resultado.size());
    assertEquals("TP 1", resultado.getFirst().getNombre());
    verify(repositorioTrabajoPracticoMock, times(1)).obtenerTodos();
  }

  @Test
  void deberiaBuscarUnTrabajoPracticoPorId() {
    Long idTp = 1L;

    TrabajoPractico tp = new TrabajoPractico(
      "TP Arquitectura",
      "Desarrollo de Software",
      LocalDateTime.now().plusDays(7),
      TipoTrabajo.GRUPAL,
      "Descripción del TP"
    );

    when(repositorioTrabajoPracticoMock.buscarPorId(idTp)).thenReturn(tp);

    TrabajoPractico resultado = servicioTrabajoPractico.buscarPorId(idTp);

    assertNotNull(resultado);
    assertSame(tp, resultado);

    verify(repositorioTrabajoPracticoMock, times(1)).buscarPorId(idTp);
  }

  @Test
  void deberiaDevolverNullSiElTrabajoPracticoNoExiste() {
    Long idTp = 99L;

    when(repositorioTrabajoPracticoMock.buscarPorId(idTp)).thenReturn(null);
    TrabajoPractico resultado = servicioTrabajoPractico.buscarPorId(idTp);
    assertNull(resultado);
    verify(repositorioTrabajoPracticoMock, times(1)).buscarPorId(idTp);
  }

  @Test
  void deberiaEliminarUnTrabajoPracticoExistente() {
    Long idTp = 1L;

    TrabajoPractico tp = new TrabajoPractico(
      "TP Arquitectura",
      "Desarrollo de Software",
      LocalDateTime.now().plusDays(7),
      TipoTrabajo.GRUPAL,
      "Descripción del TP"
    );

    when(repositorioTrabajoPracticoMock.buscarPorId(idTp)).thenReturn(tp);
    servicioTrabajoPractico.eliminar(idTp);
    verify(repositorioTrabajoPracticoMock, times(1)).buscarPorId(idTp);
    verify(repositorioTrabajoPracticoMock, times(1)).eliminar(tp);
  }

  @Test
  void deberiaLanzarExcepcionAlEliminarUnTrabajoPracticoInexistente() {
    Long idTp = 99L;

    when(repositorioTrabajoPracticoMock.buscarPorId(idTp)).thenReturn(null);

    RuntimeException excepcion = assertThrows(
      RuntimeException.class,
      () -> servicioTrabajoPractico.eliminar(idTp)
    );

    assertEquals("Trabajo práctico no encontrado para eliminar", excepcion.getMessage());
    verify(repositorioTrabajoPracticoMock, times(1)).buscarPorId(idTp);
    verify(repositorioTrabajoPracticoMock, never()).eliminar(any(TrabajoPractico.class));
  }

  @Test
  void deberiaLanzarExcepcionAlCambiarEstadoDeTrabajoPracticoInexistente() {
    Long idTp = 99L;

    when(repositorioTrabajoPracticoMock.buscarPorId(idTp)).thenReturn(null);

    RuntimeException excepcion = assertThrows(
      RuntimeException.class,
      () -> servicioTrabajoPractico.cambiarEstado(idTp, EstadoTP.EN_CURSO)
    );
    assertEquals("Trabajo práctico no encontrado", excepcion.getMessage());
    verify(repositorioTrabajoPracticoMock, times(1)).buscarPorId(idTp);
    verify(repositorioTrabajoPracticoMock, never()).save(any(TrabajoPractico.class));
  }
}
