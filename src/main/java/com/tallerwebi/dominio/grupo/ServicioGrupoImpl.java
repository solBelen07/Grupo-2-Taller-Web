package com.tallerwebi.dominio.grupo;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("servicioGrupo")
@Transactional
public class ServicioGrupoImpl implements ServicioGrupo {

  private RepositorioGrupo repositorioGrupo;

  public ServicioGrupoImpl(RepositorioGrupo repositorioGrupo) {
    this.repositorioGrupo = repositorioGrupo;
  }

  @Override
  public Grupo buscarPorNombre(String nombre) throws GrupoNoEncontrado {
    if (repositorioGrupo.buscar(nombre) == null) {
      throw new GrupoNoEncontrado();
    }
    return repositorioGrupo.buscar(nombre);
  }

  @Override
  public List<Grupo> listarGrupos() {
    return repositorioGrupo.listar();
  }

  @Override
  public Integer cantidadParticipantes(String nombre) throws GrupoNoEncontrado {
    if (repositorioGrupo.buscar(nombre) == null) {
      throw new GrupoNoEncontrado();
    }
    return repositorioGrupo.cantidadParticipantes(nombre);
  }

  @Override
  public List<Grupo> listarGruposDelUsuario(String email) {
    return repositorioGrupo.listarGruposPorUsuario(email);
  }

  @Override
  public List<Usuario> listarIntegrantes(String nombre) throws GrupoNoEncontrado {
    if (repositorioGrupo.buscar(nombre) == null) {
      throw new GrupoNoEncontrado();
    }
    return repositorioGrupo.listarIntegrantes(nombre);
  }
}
