package com.tallerwebi.presentacion.grupo;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DatosGrupo {

  private String nombre;
  private Integer cantidadDeIntegrantes;
  private List<DatosIntegrante> integrantes = new ArrayList<>();
  private boolean soyIntegrante;
  private boolean soyAdministrador;

  public DatosGrupo(String nombre, Integer cantidadDeIntegrantes) {
    this.nombre = nombre;
    this.cantidadDeIntegrantes = cantidadDeIntegrantes;
  }
}
