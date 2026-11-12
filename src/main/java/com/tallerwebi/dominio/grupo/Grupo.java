package com.tallerwebi.dominio.grupo;

import com.tallerwebi.dominio.Usuario;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Grupo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;

  // el usuario que creó el grupo. Es el administrador y también cuenta como miembro.
  @ManyToOne
  private Usuario creador;

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
