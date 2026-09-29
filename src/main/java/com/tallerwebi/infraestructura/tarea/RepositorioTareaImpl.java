package com.tallerwebi.infraestructura.tarea;

import com.tallerwebi.dominio.tarea.RepositorioTarea;
import com.tallerwebi.dominio.tarea.Tarea;
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
