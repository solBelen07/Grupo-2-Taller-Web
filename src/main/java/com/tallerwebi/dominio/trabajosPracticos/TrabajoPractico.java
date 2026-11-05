package com.tallerwebi.dominio.trabajosPracticos;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
public class TrabajoPractico {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String materia;
  private String descripcion;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
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

  @SuppressWarnings("all")
  public Long getId() {
    return id;
  }

  @SuppressWarnings("all")
  public void setId(Long id) {
    this.id = id;
  }

  @SuppressWarnings("all")
  public String getNombre() {
    return nombre;
  }

  @SuppressWarnings("all")
  public void setNombre(String nombre) {
    this.nombre = nombre;
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
  public String getDescripcion() {
    return descripcion;
  }

  @SuppressWarnings("all")
  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  @SuppressWarnings("all")
  public LocalDate getFechaEntrega() {
    return fechaEntrega;
  }

  @SuppressWarnings("all")
  public void setFechaEntrega(LocalDate fechaEntrega) {
    this.fechaEntrega = fechaEntrega;
  }

  @SuppressWarnings("all")
  public TipoTrabajo getTipo() {
    return tipo;
  }

  @SuppressWarnings("all")
  public void setTipo(TipoTrabajo tipo) {
    this.tipo = tipo;
  }

  @SuppressWarnings("all")
  public EstadoTP getEstado() {
    return estado;
  }

  @SuppressWarnings("all")
  public void setEstado(EstadoTP estado) {
    this.estado = estado;
  }

  @SuppressWarnings("all")
  public List<DisponibilidadHoraria> getDisponibilidades() {
    return disponibilidades;
  }

  @SuppressWarnings("all")
  public void setDisponibilidades(List<DisponibilidadHoraria> disponibilidades) {
    this.disponibilidades = disponibilidades;
  }
}
