package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaFormularioGrupo extends VistaWeb {

  public VistaFormularioGrupo(Page page) {
    super(page);
  }

  public void escribirNombreDelGrupo(String nombre) {
    this.escribirEnElElemento("#nombre", nombre);
  }

  public VistaGrupos darClickEnCrearGrupo() {
    this.darClickEnElElemento("#btn-guardar-grupo");
    return new VistaGrupos(page);
  }

  public String obtenerMensajeDeError() {
    return this.obtenerTextoDelElemento("div.alert-danger");
  }
}
