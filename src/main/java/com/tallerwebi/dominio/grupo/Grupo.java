package com.tallerwebi.dominio.grupo;

import com.tallerwebi.dominio.Usuario;
import jakarta.persistence.*;

@Entity
public class Grupo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;

  // el usuario que creó el grupo. Es el administrador y también cuenta como miembro.
  @ManyToOne
  private Usuario creador;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Usuario getCreador() {
    return creador;
  }

  public void setCreador(Usuario creador) {
    this.creador = creador;
  }

  // Compara por id porque el usuario de la sesión y el de la base son objetos distintos.
  public boolean esAdministradoPor(Usuario usuario) {
    return (
      creador != null &&
      usuario != null &&
      creador.getId() != null &&
      creador.getId().equals(usuario.getId())
    );
  }
}
