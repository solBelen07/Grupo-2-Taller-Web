package com.tallerwebi.infraestructura.perfil;

import com.tallerwebi.dominio.perfil.FotoPerfil;
import com.tallerwebi.dominio.perfil.RepositorioFotoPerfil;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioFotoPerfil")
public class RepositorioFotoPerfilImpl implements RepositorioFotoPerfil {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioFotoPerfilImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public FotoPerfil buscar(Long usuarioId) {
    return sessionFactory.getCurrentSession().get(FotoPerfil.class, usuarioId);
  }

  @Override
  public void guardar(FotoPerfil foto) {
    sessionFactory.getCurrentSession().merge(foto);
  }

  @Override
  public void eliminar(Long usuarioId) {
    FotoPerfil foto = buscar(usuarioId);
    if (foto != null) {
      sessionFactory.getCurrentSession().remove(foto);
    }
  }
}
