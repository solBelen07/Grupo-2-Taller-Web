package com.tallerwebi.dominio.catalogo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCatalogoImplTest {

  private ServicioCatalogoImpl servicioCatalogo;
  private RepositorioCatalogo repositorioCatalogoMock;

  @BeforeEach
  public void init() {
    repositorioCatalogoMock = mock(RepositorioCatalogo.class);
    servicioCatalogo = new ServicioCatalogoImpl(repositorioCatalogoMock);
  }

  @Test
  public void obtenerUniversidadesDeberiaDevolverLasDelRepositorio() {
    // Preparación
    List<Universidad> universidades = List.of(new Universidad("UNLaM"), new Universidad("UBA"));
    when(repositorioCatalogoMock.obtenerUniversidades()).thenReturn(universidades);

    // Ejecución
    List<Universidad> resultado = servicioCatalogo.obtenerUniversidades();

    // Validación
    assertThat(resultado, sameInstance(universidades));
    verify(repositorioCatalogoMock, times(1)).obtenerUniversidades();
  }

  @Test
  public void obtenerCarrerasDeberiaDevolverLasDelRepositorio() {
    List<Carrera> carreras = List.of(new Carrera("Ingeniería en Sistemas"));
    when(repositorioCatalogoMock.obtenerCarreras()).thenReturn(carreras);

    List<Carrera> resultado = servicioCatalogo.obtenerCarreras();

    assertThat(resultado, sameInstance(carreras));
    verify(repositorioCatalogoMock, times(1)).obtenerCarreras();
  }

  @Test
  public void obtenerInteresesDeberiaDevolverLosDelRepositorio() {
    List<Interes> intereses = List.of(new Interes("Java"), new Interes("SQL"));
    when(repositorioCatalogoMock.obtenerIntereses()).thenReturn(intereses);

    List<Interes> resultado = servicioCatalogo.obtenerIntereses();

    assertThat(resultado, sameInstance(intereses));
    verify(repositorioCatalogoMock, times(1)).obtenerIntereses();
  }

  @Test
  public void obtenerCatalogosSinDatosDeberiaDevolverListasVacias() {
    when(repositorioCatalogoMock.obtenerUniversidades()).thenReturn(List.of());
    when(repositorioCatalogoMock.obtenerCarreras()).thenReturn(List.of());
    when(repositorioCatalogoMock.obtenerIntereses()).thenReturn(List.of());

    assertThat(servicioCatalogo.obtenerUniversidades(), empty());
    assertThat(servicioCatalogo.obtenerCarreras(), empty());
    assertThat(servicioCatalogo.obtenerIntereses(), empty());
  }
}
