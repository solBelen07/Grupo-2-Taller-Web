package com.tallerwebi.presentacion.grupo;

public class DatosInvitacion {

  private String emisor;
  private String receptor;
  private String grupo;

  public String getEmisor() {
    return emisor;
  }

  public void setEmisor(String emisor) {
    this.emisor = emisor;
  }

  public String getReceptor() {
    return receptor;
  }

  public void setReceptor(String receptor) {
    this.receptor = receptor;
  }

  public String getGrupo() {
    return grupo;
  }

  public void setGrupo(String grupo) {
    this.grupo = grupo;
  }

  public DatosInvitacion(String emisor, String receptor, String grupo) {
    this.emisor = emisor;
    this.receptor = receptor;
    this.grupo = grupo;
  }

  public DatosInvitacion() {}
}
