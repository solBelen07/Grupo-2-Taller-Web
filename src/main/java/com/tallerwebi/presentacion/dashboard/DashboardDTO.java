package com.tallerwebi.presentacion.dashboard;

import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;

public class DashboardDTO {

  private Long horasEstudio;
  private Double porcentajeCumplimiento;
  private Long cantidadMaterias;
  private CalendarioSemanal calendarioSemanal;
  private SesionEstudio proximaSesion;

  public DashboardDTO(
    Long horasEstudio,
    Double porcentajeCumplimiento,
    Long cantidadMaterias,
    CalendarioSemanal calendarioSemanal,
    SesionEstudio proximaSesion
  ) {
    this.horasEstudio = horasEstudio;
    this.porcentajeCumplimiento = porcentajeCumplimiento;
    this.cantidadMaterias = cantidadMaterias;
    this.calendarioSemanal = calendarioSemanal;
    this.proximaSesion = proximaSesion;
  }

  public Long getHorasEstudio() {
    return horasEstudio;
  }

  public void setHorasEstudio(Long horasEstudio) {
    this.horasEstudio = horasEstudio;
  }

  public Double getPorcentajeCumplimiento() {
    return porcentajeCumplimiento;
  }

  public void setPorcentajeCumplimiento(Double porcentajeCumplimiento) {
    this.porcentajeCumplimiento = porcentajeCumplimiento;
  }

  public Long getCantidadMaterias() {
    return cantidadMaterias;
  }

  public void setCantidadMaterias(Long cantidadMaterias) {
    this.cantidadMaterias = cantidadMaterias;
  }

  public CalendarioSemanal getCalendarioSemanal() {
    return calendarioSemanal;
  }

  public void setCalendarioSemanal(CalendarioSemanal calendarioSemanal) {
    this.calendarioSemanal = calendarioSemanal;
  }

  public SesionEstudio getProximaSesion() {
    return proximaSesion;
  }

  public void setProximaSesion(SesionEstudio proximaSesion) {
    this.proximaSesion = proximaSesion;
  }
}
