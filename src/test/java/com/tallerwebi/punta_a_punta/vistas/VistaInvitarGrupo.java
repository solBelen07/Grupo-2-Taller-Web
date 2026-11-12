package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaInvitarGrupo extends VistaWeb {

  public VistaInvitarGrupo(Page page) {
    super(page);
  }

  public String obtenerNombreDelGrupo() {
    return this.obtenerTextoDelElemento("main p strong");
  }

  public void escribirEmailDelInvitado(String email) {
    this.escribirEnElElemento("#receptor", email);
  }

  public VistaGrupos darClickEnEnviarInvitacion() {
    this.darClickEnElElemento("#btn-enviar-invitacion");
    return new VistaGrupos(page);
  }

  public String obtenerMensajeDeError() {
    return this.obtenerTextoDelElemento("div.alert-danger");
  }
}
