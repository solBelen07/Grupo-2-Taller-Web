package com.tallerwebi.dominio.invitacion;

import com.tallerwebi.dominio.Usuario;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.grupo.Grupo;
import jakarta.persistence.*;

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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Usuario getEmisor() {
    return emisor;
  }

  public void setEmisor(Usuario emisor) {
    this.emisor = emisor;
  }

  public Usuario getReceptor() {
    return receptor;
  }

  public void setReceptor(Usuario receptor) {
    this.receptor = receptor;
  }

  public Grupo getGrupo() {
    return grupo;
  }

  public void setGrupo(Grupo grupo) {
    this.grupo = grupo;
  }

  public Boolean getVigente() {
    return vigente;
  }

  public void setVigente(Boolean vigente) {
    this.vigente = vigente;
  }

  public Estado getEstadoInvitacion() {
    return estado;
  }

  public void setEstadoInvitacion(Estado estado) {
    this.estado = estado;
  }
}
