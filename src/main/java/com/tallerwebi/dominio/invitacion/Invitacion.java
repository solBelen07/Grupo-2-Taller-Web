package com.tallerwebi.dominio.invitacion;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.grupo.Grupo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
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

  public Invitacion(Usuario emisor, Usuario receptor, Grupo grupo) {
    this.emisor = emisor;
    this.receptor = receptor;
    this.grupo = grupo;
  }

  public Estado getEstadoInvitacion() {
    return estado;
  }

  public void setEstadoInvitacion(Estado estado) {
    this.estado = estado;
  }
}
