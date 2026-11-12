package com.tallerwebi.presentacion.dashboard;

import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.calendario.EventoConEstado;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DashboardDTO {

  private Long horasEstudio;
  private Double porcentajeCumplimiento;
  private Long cantidadMaterias;
  private CalendarioSemanal calendarioSemanal;
  private SesionEstudio proximaSesion;
  private Integer racha;
  private Integer recordRacha;
  private List<EventoConEstado> proximosVencimientos = new ArrayList<>();

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
}
