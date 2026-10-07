package com.tallerwebi.infraestructura.parcial;

import com.tallerwebi.dominio.parcial.Parcial;
import com.tallerwebi.dominio.parcial.RepositorioParcial;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@SuppressWarnings("CPD-START")
@Repository
public class RepositorioParcialImpl implements RepositorioParcial {

  private final SessionFactory sessionFactory;

  public RepositorioParcialImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Parcial parcial) {
    sessionFactory.getCurrentSession().save(parcial);
  }

  @Override
  public Parcial buscarPorId(Integer id) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT DISTINCT p " +
        "FROM Parcial p " +
        "LEFT JOIN FETCH p.sesiones " +
        "WHERE p.id = :id",
        Parcial.class
      )
      .setParameter("id", id)
      .uniqueResult();
  }

  @Override
  public List<Parcial> obtenerTodos() {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT DISTINCT p " + "FROM Parcial p " + "LEFT JOIN FETCH p.sesiones",
        Parcial.class
      )
      .getResultList();
  }

  @Override
  public void modificar(Parcial parcial) {
    sessionFactory.getCurrentSession().update(parcial);
  }

  @Override
  public void eliminar(Parcial parcial) {
    sessionFactory.getCurrentSession().remove(parcial);
  }

  @Override
  public void eliminarPorMateriaId(Integer materiaId) {
    String hql = "delete from Parcial p where p.materia.id = :materiaId";
    sessionFactory
      .getCurrentSession()
      .createQuery(hql)
      .setParameter("materiaId", materiaId)
      .executeUpdate();
  }
}
