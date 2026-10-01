package com.tallerwebi.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class TrabajoPractico {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String materia;
  private String descripcion;
  private LocalDate fechaEntrega;

  @ElementCollection
  private List<DisponibilidadHoraria> disponibilidades;

  @Enumerated(EnumType.STRING)
  private TipoTrabajo tipo;

  @Enumerated(EnumType.STRING)
  private EstadoTP estado;

  public TrabajoPractico(
    String nombre,
    String materia,
    LocalDateTime fechaEntrega,
    TipoTrabajo tipo,
    String descripcion
  ) {
    this.nombre = nombre;
    this.materia = materia;
    this.fechaEntrega = fechaEntrega.toLocalDate();
    this.tipo = tipo;
    this.descripcion = descripcion;
    this.disponibilidades = List.of();
    this.estado = EstadoTP.PENDIENTE;
  }

  public TrabajoPractico() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getMateria() {
    return materia;
  }

  public void setMateria(String materia) {
    this.materia = materia;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public LocalDate getFechaEntrega() {
    return fechaEntrega;
  }

  public void setFechaEntrega(LocalDate fechaEntrega) {
    this.fechaEntrega = fechaEntrega;
  }

  public TipoTrabajo getTipo() {
    return tipo;
  }

  public void setTipo(TipoTrabajo tipo) {
    this.tipo = tipo;
  }

  public EstadoTP getEstado() {
    return estado;
  }

  public void setEstado(EstadoTP estado) {
    this.estado = estado;
  }

  public List<DisponibilidadHoraria> getDisponibilidades() {
    return disponibilidades;
  }

  public void setDisponibilidades(List<DisponibilidadHoraria> disponibilidades) {
    this.disponibilidades = disponibilidades;
  }
}
