package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.GrupoNoEncontrado;
import jakarta.transaction.Transactional;
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
}
