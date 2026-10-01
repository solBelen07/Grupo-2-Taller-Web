package com.tallerwebi.infraestructura.SesionEstudio;

import com.tallerwebi.dominio.sesionEstudio.RepositorioSesionEstudio;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
@SuppressWarnings("CPD-START")
@Repository
public class RepositorioSesionEstudioImpl implements RepositorioSesionEstudio {

  private final SessionFactory sessionFactory;

  public RepositorioSesionEstudioImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(SesionEstudio sesionEstudio) {
    sessionFactory.getCurrentSession().persist(sesionEstudio);
  }

  @Override
  public void modificar(SesionEstudio sesionEstudio) {
    sessionFactory.getCurrentSession().update(sesionEstudio);
  }

  @Override
  public void eliminar(SesionEstudio sesionEstudio) {
    sessionFactory.getCurrentSession().remove(sesionEstudio);
  }

  @Override
  public SesionEstudio buscarPorId(Integer id) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from SesionEstudio where id = :id", SesionEstudio.class)
      .setParameter("id", id)
      .uniqueResult();
  }
}
