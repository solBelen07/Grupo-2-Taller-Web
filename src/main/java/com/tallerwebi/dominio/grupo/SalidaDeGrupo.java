package com.tallerwebi.dominio.grupo;

import com.tallerwebi.dominio.Usuario;

// Cuenta que paso cuando alguien salio de un grupo, para poder mostrar el mensaje correcto.
public class SalidaDeGrupo {

  private final Usuario nuevoAdministrador;
  private final boolean grupoEliminado;

  private SalidaDeGrupo(Usuario nuevoAdministrador, boolean grupoEliminado) {
    this.nuevoAdministrador = nuevoAdministrador;
    this.grupoEliminado = grupoEliminado;
  }

  // Salio un integrante comun.
  public static SalidaDeGrupo deMiembro() {
    return new SalidaDeGrupo(null, false);
  }

  // Salio el administrador y la administración pasó a otro integrante.
  public static SalidaDeGrupo conNuevoAdministrador(Usuario nuevoAdministrador) {
    return new SalidaDeGrupo(nuevoAdministrador, false);
  }

  // Salió el administrador y no quedaba nadie más: el grupo se eliminó.
  public static SalidaDeGrupo conGrupoEliminado() {
    return new SalidaDeGrupo(null, true);
  }

  public Usuario getNuevoAdministrador() {
    return nuevoAdministrador;
  }

  public boolean isGrupoEliminado() {
    return grupoEliminado;
  }
}
