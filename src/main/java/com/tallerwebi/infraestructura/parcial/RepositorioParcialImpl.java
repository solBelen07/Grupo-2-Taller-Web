package com.tallerwebi.infraestructura.parcial;

import com.tallerwebi.dominio.parcial.Parcial;
import com.tallerwebi.dominio.parcial.RepositorioParcial;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioParcialImpl implements RepositorioParcial {

  private final SessionFactory sessionFactory;

  public RepositorioParcialImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Parcial parcial) {
    Session session = sessionFactory.getCurrentSession();
    session.persist(parcial);
  }

  @Override
  public Parcial buscarPorId(Integer id) {
    Session session = sessionFactory.getCurrentSession();
    return session.get(Parcial.class, id);
  }

  @Override
  public List<Parcial> obtenerTodos() {
    Session session = sessionFactory.getCurrentSession();

    return session.createQuery("FROM Parcial", Parcial.class).getResultList();
  }

  @Override
  public void modificar(Parcial parcial) {
    Session session = sessionFactory.getCurrentSession();
    session.merge(parcial);
  }

  @Override
  public void eliminar(Parcial parcial) {
    Session session = sessionFactory.getCurrentSession();
    session.remove(parcial);
  }
}
