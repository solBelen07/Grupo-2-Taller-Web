package com.tallerwebi.presentacion.parcial;

import java.time.LocalDate;
import java.time.LocalTime;

public class DatosParcial {

  private String nombre;
  private Long materiaId;
  private LocalDate fecha;
  private LocalTime horario;
  private Integer cantidadDiasEstudio;
  private Integer horasPorDia;
  private String temas;

  public DatosParcial() {}

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Long getMateriaId() {
    return materiaId;
  }

  public void setMateriaId(Long materiaId) {
    this.materiaId = materiaId;
  }

  public LocalDate getFecha() {
    return fecha;
  }

  public void setFecha(LocalDate fecha) {
    this.fecha = fecha;
  }

  public LocalTime getHorario() {
    return horario;
  }

  public void setHorario(LocalTime horario) {
    this.horario = horario;
  }

  public Integer getCantidadDiasEstudio() {
    return cantidadDiasEstudio;
  }

  public void setCantidadDiasEstudio(Integer cantidadDiasEstudio) {
    this.cantidadDiasEstudio = cantidadDiasEstudio;
  }

  public Integer getHorasPorDia() {
    return horasPorDia;
  }

  public void setHorasPorDia(Integer horasPorDia) {
    this.horasPorDia = horasPorDia;
  }

  public String getTemas() {
    return temas;
  }

  public void setTemas(String temas) {
    this.temas = temas;
  }
}
