package com.tallerwebi.dominio.grupo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.tallerwebi.dominio.Usuario;
import org.junit.jupiter.api.Test;

public class GrupoTest {

  @Test
  public void elCreadorEsElAdministradorDelGrupo() {
    Grupo grupo = new Grupo();
    grupo.setCreador(usuarioConId(1L));

    assertThat(grupo.esAdministradoPor(usuarioConId(1L)), equalTo(true));
  }

  @Test
  public void otroUsuarioNoEsAdministrador() {
    Grupo grupo = new Grupo();
    grupo.setCreador(usuarioConId(1L));

    assertThat(grupo.esAdministradoPor(usuarioConId(2L)), equalTo(false));
  }

  @Test
  public void unGrupoSinCreadorNoTieneAdministrador() {
    assertThat(new Grupo().esAdministradoPor(usuarioConId(1L)), equalTo(false));
  }

  @Test
  public void sinUsuarioLogueadoNoEsAdministrador() {
    Grupo grupo = new Grupo();
    grupo.setCreador(usuarioConId(1L));

    assertThat(grupo.esAdministradoPor(null), equalTo(false));
  }

  private Usuario usuarioConId(Long id) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    return usuario;
  }
}
