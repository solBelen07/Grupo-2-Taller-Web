package com.tallerwebi.dominio.recomendacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class MatrizCompetenciasTest {

  private static final double DELTA = 0.001;

  @Test
  public void deberiaDevolverElNivelDeUnaCompetenciaQueExiste() {
    MatrizCompetencias matriz = new MatrizCompetencias(Map.of("Algebra", 80.0));

    assertThat(matriz.nivelEn("Algebra"), closeTo(80.0, DELTA));
  }

  @Test
  public void deberiaDevolverNivelCeroParaUnaCompetenciaQueNoTiene() {
    MatrizCompetencias matriz = new MatrizCompetencias(Map.of("Algebra", 80.0));

    assertThat(matriz.nivelEn("Fisica"), closeTo(0.0, DELTA));
  }

  @Test
  public void deberiaCalcularElCumplimientoGeneralComoPromedioDeSusNiveles() {
    MatrizCompetencias matriz = new MatrizCompetencias(Map.of("Algebra", 100.0, "Fisica", 50.0));

    assertThat(matriz.cumplimientoGeneral(), closeTo(75.0, DELTA));
  }

  @Test
  public void deberiaTenerCumplimientoGeneralCeroSiNoTieneCompetencias() {
    MatrizCompetencias matriz = new MatrizCompetencias(new HashMap<>());

    assertThat(matriz.cumplimientoGeneral(), closeTo(0.0, DELTA));
  }

  @Test
  public void deberiaListarLasCompetenciasOrdenadasAlfabeticamente() {
    MatrizCompetencias matriz = new MatrizCompetencias(
      Map.of("Quimica", 10.0, "Algebra", 20.0, "Fisica", 30.0)
    );

    assertThat(matriz.getCompetencias(), contains("Algebra", "Fisica", "Quimica"));
  }

  @Test
  public void deberiaNoTenerCompetenciasCuandoSeCreaConUnMapaVacio() {
    MatrizCompetencias matriz = new MatrizCompetencias(new HashMap<>());

    assertThat(matriz.getCompetencias(), empty());
    assertThat(matriz.getNiveles().size(), equalTo(0));
  }

  @Test
  public void deberiaIgnorarLosCambiosPosterioresAlMapaConElQueSeCreo() {
    Map<String, Double> original = new HashMap<>();
    original.put("Algebra", 40.0);
    MatrizCompetencias matriz = new MatrizCompetencias(original);

    original.put("Fisica", 90.0);
    original.put("Algebra", 0.0);

    assertThat(matriz.getCompetencias(), contains("Algebra"));
    assertThat(matriz.nivelEn("Algebra"), closeTo(40.0, DELTA));
  }

  @Test
  public void deberiaExponerLosNivelesSinPermitirModificarlos() {
    MatrizCompetencias matriz = new MatrizCompetencias(Map.of("Algebra", 40.0));
    Map<String, Double> niveles = matriz.getNiveles();

    assertThrows(UnsupportedOperationException.class, () -> niveles.put("Fisica", 10.0));
  }
}
