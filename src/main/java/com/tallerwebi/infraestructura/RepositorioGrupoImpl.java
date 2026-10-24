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
    // GRP-04: grupos a los que el usuario pertenece o fue invitado.
    // - Como invitado: invitaciones ACEPTADAS (pertenece) o PENDIENTES (fue invitado). Las RECHAZADAS no.
    // - Como emisor: si invitó a alguien a un grupo, es porque forma parte de ese grupo.
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct i.grupo from Invitacion i " +
        "where (i.receptor.email = :email and i.estado in (:estadosVisibles)) " +
        "or i.emisor.email = :email",
        Grupo.class
      )
      .setParameter("email", email)
      .setParameter("estadosVisibles", List.of(Estado.ACEPTADA, Estado.PENDIENTE))
      .list();
  }

  @Override
  public Integer cantidadParticipantes(String nombre) {
    Long count = sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT COUNT(i.receptor) FROM Invitacion i WHERE i.grupo.nombre = :nombreGrupo AND i.estado = 'ACEPTADA'",
        Long.class
      )
      .setParameter("nombreGrupo", nombre)
      .uniqueResult();

    return count != null ? count.intValue() : 0;
  }
}
