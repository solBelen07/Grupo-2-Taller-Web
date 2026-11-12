package com.tallerwebi.dominio.trabajosPracticos;

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
public class DisponibilidadHoraria {

  private String dia;
  private int horas;
}
