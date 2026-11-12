package com.tallerwebi.dominio.recomendacion;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MatrizCompetencias {

  private final Map<String, Double> niveles;

  public MatrizCompetencias(Map<String, Double> niveles) {
    this.niveles = Collections.unmodifiableMap(new TreeMap<>(niveles));
  }

  public Map<String, Double> getNiveles() {
    return niveles;
  }

  public Set<String> getCompetencias() {
    return niveles.keySet();
  }

  public double nivelEn(String competencia) {
    return niveles.getOrDefault(competencia, 0.0);
  }

  public double cumplimientoGeneral() {
    return niveles.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
  }
}
