package com.tallerwebi.infraestructura.materia;

import com.tallerwebi.dominio.excepcion.excepcionMateria.MateriaNoEncontrada;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioMateria")
public class RepositorioMateriaImpl implements RepositorioMateria {

  private static final String PARAM_ID_USUARIO = "idUsuario";

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
  public List<Materia> obtenerPorUsuario(Long idUsuario) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Materia m join m.usuarios u where u.id = :idUsuario", Materia.class)
      .setParameter(PARAM_ID_USUARIO, idUsuario)
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
  public Materia buscarPorNombreYUsuario(String nombre, Long idUsuario) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct m from Materia m join m.usuarios u " +
        "where LOWER(m.nombre) = LOWER(:nombre) and u.id = :idUsuario",
        Materia.class
      )
      .setParameter("nombre", nombre)
      .setParameter(PARAM_ID_USUARIO, idUsuario)
      .uniqueResult();
  }

  @Override
  public Materia buscarPorId(Integer id) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Materia where id = :id", Materia.class)
      .setParameter("id", id)
      .uniqueResult();
  }

  @Override
  public Materia buscarPorIdYUsuario(Integer id, Long idUsuario) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct m from Materia m join m.usuarios u " +
        "where m.id = :id and u.id = :idUsuario",
        Materia.class
      )
      .setParameter("id", id)
      .setParameter(PARAM_ID_USUARIO, idUsuario)
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

  @Override
  public Long obtenerCantidadMateriasPorUsuario(Long idUsuario) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(m) from Materia m join m.usuarios u where u.id = :idUsuario",
        Long.class
      )
      .setParameter(PARAM_ID_USUARIO, idUsuario)
      .uniqueResult();
  }
}
