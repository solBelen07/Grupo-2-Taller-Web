package com.tallerwebi.dominio.trabajosPracticos;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Getter
@Setter
@NoArgsConstructor
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
}
