package com.tallerwebi.presentacion.tecnicasestudio;

import com.tallerwebi.dominio.tecnicasestudio.Estado;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatosPomodoro {

  private String origen;
  private String destino;
  private Integer duracion;
  private Estado estado;
  private Integer materia;
  private UsuarioDTO usuario;
  private String objetivo;
}
