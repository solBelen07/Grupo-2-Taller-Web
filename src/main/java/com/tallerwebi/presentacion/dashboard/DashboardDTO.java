package com.tallerwebi.presentacion.dashboard;

import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;

public class DashboardDTO {

  private Long horasEstudio;
  private Double porcentajeCumplimiento;
  private Long cantidadMaterias;
  private CalendarioSemanal calendarioSemanal;
  private SesionEstudio proximaSesion;
  private Integer racha;
  private Integer recordRacha;

  public DashboardDTO(
    Long horasEstudio,
    Double porcentajeCumplimiento,
    Long cantidadMaterias,
    CalendarioSemanal calendarioSemanal,
    SesionEstudio proximaSesion,
    Integer racha,
    Integer recordRacha
  ) {
    this.horasEstudio = horasEstudio;
    this.porcentajeCumplimiento = porcentajeCumplimiento;
    this.cantidadMaterias = cantidadMaterias;
    this.calendarioSemanal = calendarioSemanal;
    this.proximaSesion = proximaSesion;
    this.racha = racha;
    this.recordRacha = recordRacha;
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

  public Integer getRacha() {
    return racha;
  }

  public void setRacha(Integer racha) {
    this.racha = racha;
  }

  public Integer getRecordRacha() {
    return recordRacha;
  }

  public void setRecordRacha(Integer recordRacha) {
    this.recordRacha = recordRacha;
  }
}
