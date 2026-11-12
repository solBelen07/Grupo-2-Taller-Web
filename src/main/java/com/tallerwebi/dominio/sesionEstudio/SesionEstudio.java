package com.tallerwebi.dominio.sesionEstudio;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.parcial.Parcial;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SesionEstudio {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String tema;

  private LocalDate fecha;

  private Integer cantidadHoras;

  private Boolean completada = false;

  @ManyToOne
  @JoinColumn(name = "parcial_id", nullable = false)
  private Parcial parcial;

  @ManyToOne
  @JoinColumn(name = "usuario")
  private Usuario usuario;

  @Column(name = "fecha_completado")
  private LocalDate fechaCompletado;
}
