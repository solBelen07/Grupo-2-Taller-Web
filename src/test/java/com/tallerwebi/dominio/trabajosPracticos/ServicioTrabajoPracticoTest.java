package com.tallerwebi.dominio.trabajosPracticos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    // Arrange
    TrabajoPractico tpNuevo = new TrabajoPractico(
      "TP Arquitectura",
      "Desarrollo de Software",
      LocalDateTime.now().plusDays(7),
      TipoTrabajo.GRUPAL,
      "Descripción del TP"
    );

    when(repositorioTrabajoPracticoMock.save(any(TrabajoPractico.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    TrabajoPractico resultado = servicioTrabajoPractico.crearTrabajoPractico(tpNuevo);

    // Assert
    assertNotNull(resultado);
    assertEquals("TP Arquitectura", resultado.getNombre());
    assertEquals(EstadoTP.PENDIENTE, resultado.getEstado());
    verify(repositorioTrabajoPracticoMock, times(1)).save(any(TrabajoPractico.class));
  }

  @Test
  void deberiaCambiarElEstadoDelTrabajoPracticoExitosamente() {
    // Arrange
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

    // Act
    TrabajoPractico resultado = servicioTrabajoPractico.cambiarEstado(idTp, EstadoTP.EN_CURSO);

    // Assert
    assertNotNull(resultado);
    assertEquals(EstadoTP.EN_CURSO, resultado.getEstado());

    verify(repositorioTrabajoPracticoMock, times(1)).buscarPorId((long) idTp);
    verify(repositorioTrabajoPracticoMock, times(1)).save(any(TrabajoPractico.class));
  }

  @Test
  void deberiaRetornarListaDeTrabajosPracticos() {
    // Arrange
    List<TrabajoPractico> listaSimulada = List.of(
      new TrabajoPractico("TP 1", "Matemática", LocalDateTime.now(), TipoTrabajo.GRUPAL, "Desc 1"),
      new TrabajoPractico("TP 2", "Física", LocalDateTime.now(), TipoTrabajo.INDIVIDUAL, "Desc 2")
    );

    when(repositorioTrabajoPracticoMock.obtenerTodos()).thenReturn(listaSimulada);

    // Act
    List<TrabajoPractico> resultado = servicioTrabajoPractico.obtenerTodos();

    // Assert
    assertNotNull(resultado);
    assertEquals(2, resultado.size());
    assertEquals("TP 1", resultado.getFirst().getNombre());
    verify(repositorioTrabajoPracticoMock, times(1)).obtenerTodos();
  }
}
