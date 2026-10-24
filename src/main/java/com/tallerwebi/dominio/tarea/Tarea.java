package com.tallerwebi.dominio.tarea;

import com.tallerwebi.dominio.grupo.Grupo;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

  // Grupo al que pertenece la tarea (GRP-02). Permite agrupar las tareas y calcular
  // el progreso de cada grupo por separado.
  @ManyToOne
  private Grupo grupo;

  @SuppressWarnings("all")
  public Long getId() {
    return id;
  }

  @SuppressWarnings("all")
  public void setId(Long id) {
    this.id = id;
  }

  @SuppressWarnings("all")
  public String getTitulo() {
    return titulo;
  }

  @SuppressWarnings("all")
  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  @SuppressWarnings("all")
  public String getMateria() {
    return materia;
  }

  @SuppressWarnings("all")
  public void setMateria(String materia) {
    this.materia = materia;
  }

  @SuppressWarnings("all")
  public String getEstado() {
    return estado;
  }

  @SuppressWarnings("all")
  public void setEstado(String estado) {
    this.estado = estado;
  }

  @SuppressWarnings("all")
  public Integer getHorasPlanificadas() {
    return horasPlanificadas;
  }

  @SuppressWarnings("all")
  public void setHorasPlanificadas(Integer horasPlanificadas) {
    this.horasPlanificadas = horasPlanificadas;
  }

  @SuppressWarnings("all")
  public Integer getHorasRealizadas() {
    return horasRealizadas;
  }

  @SuppressWarnings("all")
  public void setHorasRealizadas(Integer horasRealizadas) {
    this.horasRealizadas = horasRealizadas;
  }

  @SuppressWarnings("all")
  public String getResponsable() {
    return responsable;
  }

  @SuppressWarnings("all")
  public void setResponsable(String responsable) {
    this.responsable = responsable;
  }

  @SuppressWarnings("all")
  public String getTipo() {
    return tipo;
  }

  @SuppressWarnings("all")
  public void setTipo(String tipo) {
    this.tipo = tipo;
  }

  @SuppressWarnings("all")
  public LocalDate getFechaVencimiento() {
    return fechaVencimiento;
  }

  @SuppressWarnings("all")
  public void setFechaVencimiento(LocalDate fechaVencimiento) {
    this.fechaVencimiento = fechaVencimiento;
  }

  @SuppressWarnings("all")
  public Grupo getGrupo() {
    return grupo;
  }

  @SuppressWarnings("all")
  public void setGrupo(Grupo grupo) {
    this.grupo = grupo;
  }
}
