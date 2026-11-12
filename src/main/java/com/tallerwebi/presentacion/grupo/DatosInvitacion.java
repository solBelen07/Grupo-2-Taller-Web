package com.tallerwebi.presentacion.grupo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DatosInvitacion {

  private String emisor;
  private String receptor;
  private String grupo;
}
