package com.tallerwebi.presentacion.recomendacion;

import java.util.List;

public class CompanieroDTO {

  private final String nombre;
  private final String apellido;
  private final String email;
  private final double compatibilidad;
  private final double cumplimiento;
  private final double aporteEnGrupos;
  private final List<String> aportaEn;

  public CompanieroDTO(
    String nombre,
    String apellido,
    String email,
    double compatibilidad,
    double cumplimiento,
    double aporteEnGrupos,
    List<String> aportaEn
  ) {
    this.nombre = nombre;
    this.apellido = apellido;
    this.email = email;
    this.compatibilidad = compatibilidad;
    this.cumplimiento = cumplimiento;
    this.aporteEnGrupos = aporteEnGrupos;
    this.aportaEn = aportaEn;
  }

  public String getNombre() {
    return nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public String getEmail() {
    return email;
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
