package com.tallerwebi.dominio.tecnicasestudio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlanDeVuelo {

  @Column(name = "materia", nullable = false)
  private String materia;

  @Column(name = "origen", nullable = false)
  private String origen;

  @Column(name = "destino", nullable = false)
  private String destino;

  @Column(name = "objetivo", nullable = false)
  private String objetivo;
}
