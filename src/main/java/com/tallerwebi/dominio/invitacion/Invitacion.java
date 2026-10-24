package com.tallerwebi.dominio.invitacion;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.grupo.Grupo;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Invitacion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "emisor_id")
  private Usuario emisor;

  @ManyToOne
  @JoinColumn(name = "receptor_id")
  private Usuario receptor;

  @ManyToOne
  @JoinColumn(name = "grupo_id")
  private Grupo grupo;

  private Boolean vigente = true;

  @Enumerated(EnumType.STRING)
  private Estado estado = Estado.PENDIENTE;

  // Constructores
  public Invitacion() {}

  public Invitacion(Usuario emisor, Usuario receptor, Grupo grupo) {
    this.emisor = emisor;
    this.receptor = receptor;
    this.grupo = grupo;
  }

  @SuppressWarnings("all")
  public Long getId() {
    return id;
  }

  @SuppressWarnings("all")
  public void setId(Long id) {
    this.id = id;
  }

  @SuppressWarnings("all")
  public Usuario getEmisor() {
    return emisor;
  }

  @SuppressWarnings("all")
  public void setEmisor(Usuario emisor) {
    this.emisor = emisor;
  }

  @SuppressWarnings("all")
  public Usuario getReceptor() {
    return receptor;
  }

  @SuppressWarnings("all")
  public void setReceptor(Usuario receptor) {
    this.receptor = receptor;
  }

  @SuppressWarnings("all")
  public Grupo getGrupo() {
    return grupo;
  }

  @SuppressWarnings("all")
  public void setGrupo(Grupo grupo) {
    this.grupo = grupo;
  }

  @SuppressWarnings("all")
  public Boolean getVigente() {
    return vigente;
  }

  @SuppressWarnings("all")
  public void setVigente(Boolean vigente) {
    this.vigente = vigente;
  }

  @SuppressWarnings("all")
  public Estado getEstadoInvitacion() {
    return estado;
  }

  @SuppressWarnings("all")
  public void setEstadoInvitacion(Estado estado) {
    this.estado = estado;
  }
}
