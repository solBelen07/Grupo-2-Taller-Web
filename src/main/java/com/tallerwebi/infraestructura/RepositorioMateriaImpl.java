package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Materia;
import com.tallerwebi.dominio.RepositorioMateria;
import com.tallerwebi.dominio.excepcion.MateriaNoEncontrada;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioMateria")
public class RepositorioMateriaImpl implements RepositorioMateria {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioMateriaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Materia materia) {
    sessionFactory.getCurrentSession().persist(materia);
  }

  @Override
  public List<Materia> obtenerTodas() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Materia", Materia.class)
      .getResultList();
  }

  @Override
  public Materia buscarPorNombre(String nombre) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Materia where LOWER(nombre) = LOWER(:nombre)", Materia.class)
      .setParameter("nombre", nombre)
      .uniqueResult();
  }

  @Override
  public void modificar(Materia materia) {
    Materia existente = sessionFactory
      .getCurrentSession()
      .createQuery("from Materia where id = :id", Materia.class)
      .setParameter("id", materia.getId())
      .uniqueResult();
    if (existente == null) {
      throw new MateriaNoEncontrada();
    }
    sessionFactory.getCurrentSession().merge(materia);
  }

  @Override
  public void eliminar(Materia materia) {
    sessionFactory.getCurrentSession().remove(materia);
  }
}
