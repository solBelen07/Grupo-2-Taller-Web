package com.tallerwebi.presentacion.tecnicasestudio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UsuarioDTO {

  private Long id;
  private String nombre;
  private String apellido;
}
