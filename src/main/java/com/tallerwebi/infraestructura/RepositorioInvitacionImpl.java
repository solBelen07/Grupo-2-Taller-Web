package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.invitacion.Estado;
import com.tallerwebi.dominio.invitacion.Invitacion;
import com.tallerwebi.dominio.invitacion.RepositorioInvitacion;
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
      .createQuery(
        "from Invitacion where receptor.email = :email and vigente is true",
        Invitacion.class
      )
      .setParameter("email", emailReceptor)
      .list();
  }

  @Override
  public Invitacion buscar(String emailEmisor, String emailReceptor, String nombreGrupo) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Invitacion i where i.emisor.email = :emisor and i.receptor.email = :receptor and i.grupo.nombre = :grupo",
        Invitacion.class
      )
      .setParameter("emisor", emailEmisor)
      .setParameter("receptor", emailReceptor)
      .setParameter("grupo", nombreGrupo)
      .uniqueResult();
  }

  public void cambiarEstado(Invitacion invitacion, Estado estado) {
    invitacion.setVigente(false);
    invitacion.setEstadoInvitacion(estado);
    sessionFactory.getCurrentSession().update(invitacion);
  }

  public RepositorioInvitacionImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }
}
