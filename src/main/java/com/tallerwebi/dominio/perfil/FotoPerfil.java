package com.tallerwebi.dominio.perfil;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class FotoPerfil {

  @Id
  private Long usuarioId;

  private String tipoContenido;

  @Lob
  private byte[] datos;

  public FotoPerfil() {}

  public FotoPerfil(Long usuarioId, String tipoContenido, byte[] datos) {
    this.usuarioId = usuarioId;
    this.tipoContenido = tipoContenido;
    this.datos = datos.clone();
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public String getTipoContenido() {
    return tipoContenido;
  }

  public byte[] getDatos() {
    return datos.clone();
  }
}
