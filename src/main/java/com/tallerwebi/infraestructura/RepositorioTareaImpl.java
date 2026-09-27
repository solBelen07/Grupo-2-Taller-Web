package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioTarea;
import com.tallerwebi.dominio.Tarea;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioTarea")
public class RepositorioTareaImpl implements RepositorioTarea {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioTareaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Tarea tarea) {
    sessionFactory.getCurrentSession().persist(tarea);
  }

  @Override
  public List<Tarea> buscarTodas() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Tarea", Tarea.class)
      .getResultList();
  }
}
