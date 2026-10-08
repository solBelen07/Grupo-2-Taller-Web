package com.tallerwebi.infraestructura.trabajosPracticos;

import com.tallerwebi.dominio.trabajosPracticos.RepositorioTrabajoPractico;
import com.tallerwebi.dominio.trabajosPracticos.TipoTrabajo;
import com.tallerwebi.dominio.trabajosPracticos.TrabajoPractico;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioTrabajoPractico")
public class RepositorioTrabajoPracticoImpl implements RepositorioTrabajoPractico {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioTrabajoPracticoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public TrabajoPractico save(TrabajoPractico trabajoPractico) {
    if (trabajoPractico.getId() == null) {
      sessionFactory.getCurrentSession().persist(trabajoPractico);
      return trabajoPractico;
    }
    return sessionFactory.getCurrentSession().merge(trabajoPractico);
  }

  @Override
  public TrabajoPractico buscarPorId(Long id) {
    TrabajoPractico tp = sessionFactory.getCurrentSession().get(TrabajoPractico.class, id);
    if (tp != null) {
      Hibernate.initialize(tp.getDisponibilidades());
    }
    return tp;
  }

  @Override
  public List<TrabajoPractico> obtenerTodos() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM TrabajoPractico", TrabajoPractico.class)
      .list();
  }

  @Override
  public List<TrabajoPractico> buscarPorMateria(String materia) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM TrabajoPractico tp WHERE tp.materia = :materia", TrabajoPractico.class)
      .setParameter("materia", materia)
      .list();
  }

  @Override
  public List<TrabajoPractico> buscarPorNombre(String nombre) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM TrabajoPractico tp WHERE tp.nombre = :nombre", TrabajoPractico.class)
      .setParameter("nombre", nombre)
      .list();
  }

  @Override
  public List<TrabajoPractico> buscarPorTipo(TipoTrabajo tipo) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM TrabajoPractico tp WHERE tp.tipo = :tipo", TrabajoPractico.class)
      .setParameter("tipo", tipo)
      .list();
  }

  @Override
  public void eliminar(TrabajoPractico trabajoPractico) {
    sessionFactory.getCurrentSession().remove(trabajoPractico);
  }
}
