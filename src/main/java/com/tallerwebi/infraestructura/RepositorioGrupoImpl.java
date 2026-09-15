package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Grupo;
import com.tallerwebi.dominio.RepositorioGrupo;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository("repositorioGrupo")
public class RepositorioGrupoImpl implements RepositorioGrupo {

  private SessionFactory sessionFactory;

  public RepositorioGrupoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Grupo buscar(String nombre) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Grupo where nombre = :nombre", Grupo.class)
      .setParameter("nombre", nombre)
      .uniqueResult();
  }
}
