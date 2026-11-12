package com.tallerwebi.presentacion.recomendacion;

import java.util.List;
import java.util.Map;

public class RecomendacionDTO {

  private final double cumplimiento;
  private final double aporteEnGrupos;
  private final Map<String, Double> niveles;
  private final List<CompanieroDTO> companeros;

  public RecomendacionDTO(
    double cumplimiento,
    double aporteEnGrupos,
    Map<String, Double> niveles,
    List<CompanieroDTO> companeros
  ) {
    this.cumplimiento = cumplimiento;
    this.aporteEnGrupos = aporteEnGrupos;
    this.niveles = niveles;
    this.companeros = companeros;
  }

  public double getCumplimiento() {
    return cumplimiento;
  }

  public double getAporteEnGrupos() {
    return aporteEnGrupos;
  }

  public Map<String, Double> getNiveles() {
    return niveles;
  }

  public List<CompanieroDTO> getCompaneros() {
    return companeros;
  }
}
