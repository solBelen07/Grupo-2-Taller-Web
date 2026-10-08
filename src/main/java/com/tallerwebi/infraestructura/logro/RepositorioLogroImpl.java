package com.tallerwebi.infraestructura.logro;

import com.tallerwebi.dominio.logro.Logro;
import com.tallerwebi.dominio.logro.RepositorioLogro;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository("repositorioLogro")
public class RepositorioLogroImpl implements RepositorioLogro {

  private SessionFactory sessionFactory;

  @Override
  public Logro buscar(String nombre) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Logro where LOWER(nombre) = LOWER(:nombre)", Logro.class)
      .setParameter("nombre", nombre)
      .uniqueResult();
  }

  public List<Logro> listar() {
    return sessionFactory.getCurrentSession().createQuery("from Logro", Logro.class).list();
  }

  public RepositorioLogroImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }
}
