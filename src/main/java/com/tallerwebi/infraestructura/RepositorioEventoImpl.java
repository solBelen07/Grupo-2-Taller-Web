package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Evento;
import com.tallerwebi.dominio.RepositorioEvento;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** Persistencia de eventos con Hibernate  */
@Repository("repositorioEvento")
public class RepositorioEventoImpl implements RepositorioEvento {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioEventoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public List<Evento> buscarEnRango(Long usuarioId, LocalDateTime desde, LocalDateTime hasta) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select e from Evento e join fetch e.materia " +
        "where e.usuarioId = :usuarioId and e.inicio < :hasta and e.fin > :desde " +
        "order by e.inicio, e.id",
        Evento.class
      )
      .setParameter("usuarioId", usuarioId)
      .setParameter("desde", desde)
      .setParameter("hasta", hasta)
      .getResultList();
  }

  @Override
  public void guardar(Evento evento) {
    sessionFactory.getCurrentSession().persist(evento);
  }

  @Override
  public void eliminarPorMateriaId(Integer materiaId) {
    String hql = "delete from Evento e where e.materia.id = :materiaId";
    sessionFactory
      .getCurrentSession()
      .createQuery(hql)
      .setParameter("materiaId", materiaId)
      .executeUpdate();
  }
}
