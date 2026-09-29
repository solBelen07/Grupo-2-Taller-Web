package com.tallerwebi.dominio.tarea;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Tarea {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String titulo;
  private String materia;
  private String estado;
  private Integer horasRealizadas;
  private Integer horasPlanificadas;
  private String responsable;

  // Indica si la tarea corresponde a un parcial o a un TP.
  // Se utiliza para aplicar el indicador de seguimiento correspondiente.
  private String tipo;
  // Fecha límite de entrega de la tarea.
  //  para determinar si un TP está en tiempo, próximo a vencer o atrasado.
  private LocalDate fechaVencimiento;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getMateria() {
    return materia;
  }

  public void setMateria(String materia) {
    this.materia = materia;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public Integer getHorasPlanificadas() {
    return horasPlanificadas;
  }

  public void setHorasPlanificadas(Integer horasPlanificadas) {
    this.horasPlanificadas = horasPlanificadas;
  }

  public Integer getHorasRealizadas() {
    return horasRealizadas;
  }

  public void setHorasRealizadas(Integer horasRealizadas) {
    this.horasRealizadas = horasRealizadas;
  }

  public String getResponsable() {
    return responsable;
  }

  public void setResponsable(String responsable) {
    this.responsable = responsable;
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
  }

  public LocalDate getFechaVencimiento() {
    return fechaVencimiento;
  }

  public void setFechaVencimiento(LocalDate fechaVencimiento) {
    this.fechaVencimiento = fechaVencimiento;
  }
}
