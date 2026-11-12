package com.tallerwebi.dominio.recomendacion;

import com.tallerwebi.dominio.Usuario;
import java.util.List;

public class Recomendado {

  private final Usuario companiero;
  private final double compatibilidad;
  private final double cumplimiento;
  private final double aporteEnGrupos;
  private final List<String> aportaEn;

  public Recomendado(
    Usuario companiero,
    double compatibilidad,
    double cumplimiento,
    double aporteEnGrupos,
    List<String> aportaEn
  ) {
    this.companiero = companiero;
    this.compatibilidad = compatibilidad;
    this.cumplimiento = cumplimiento;
    this.aporteEnGrupos = aporteEnGrupos;
    this.aportaEn = aportaEn;
  }

  public Usuario getCompaniero() {
    return this.companiero;
  }

  public double getCompatibilidad() {
    return compatibilidad;
  }

  public double getCumplimiento() {
    return cumplimiento;
  }

  public double getAporteEnGrupos() {
    return aporteEnGrupos;
  }

  public List<String> getAportaEn() {
    return aportaEn;
  }
}
