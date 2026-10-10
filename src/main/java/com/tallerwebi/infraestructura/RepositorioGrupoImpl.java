package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.RepositorioGrupo;
import com.tallerwebi.dominio.invitacion.Estado;
import java.util.List;
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
      .setMaxResults(1)
      .uniqueResult();
  }

  @Override
  public List<Grupo> listar() {
    return sessionFactory.getCurrentSession().createQuery("from Grupo", Grupo.class).list();
  }

  @Override
  public List<Grupo> listarGruposPorUsuario(String email) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select i.grupo from Invitacion i where i.receptor.email = :email and i.estado = :estadoAceptada",
        Grupo.class
      )
      .setParameter("email", email)
      .setParameter("estadoAceptada", Estado.ACEPTADA)
      .list();
  }
}
