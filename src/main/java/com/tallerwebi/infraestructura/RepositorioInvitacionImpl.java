package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Invitacion;
import com.tallerwebi.dominio.RepositorioInvitacion;
import com.tallerwebi.dominio.Usuario;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository("repositorioInvitacion")
public class RepositorioInvitacionImpl implements RepositorioInvitacion {

  private SessionFactory sessionFactory;

  public Invitacion enviarInvitacion(Invitacion invitacion) {
    sessionFactory.getCurrentSession().persist(invitacion);
    return invitacion;
  }

  @Override
  public List<Invitacion> buscarInvitacionesPorReceptor(String emailReceptor) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Invitacion where receptor.email = :email", Invitacion.class)
      .setParameter("email", emailReceptor)
      .list();
  }

  public RepositorioInvitacionImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }
}
