package com.tallerwebi.infraestructura.logro;

import com.tallerwebi.dominio.logro.LogroObtenido;
import com.tallerwebi.dominio.logro.RepositorioLogroObtenido;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository("repositorioLogroObtenido")
public class RepositorioLogroObtenidoImpl implements RepositorioLogroObtenido {

  private final SessionFactory sessionFactory;

  public RepositorioLogroObtenidoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(LogroObtenido logroObtenido) {
    sessionFactory.getCurrentSession().persist(logroObtenido);
  }

  @Override
  public List<LogroObtenido> buscarPorUsuario(Long idUsuario) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from LogroObtenido lo where lo.usuario.id = :idUsuario order by lo.fechaObtencion",
        LogroObtenido.class
      )
      .setParameter("idUsuario", idUsuario)
      .list();
  }

  @Override
  public boolean existe(Long idUsuario, String nombreLogro) {
    Long cantidad = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(lo) from LogroObtenido lo " +
        "where lo.usuario.id = :idUsuario and lower(lo.logro.nombre) = lower(:nombreLogro)",
        Long.class
      )
      .setParameter("idUsuario", idUsuario)
      .setParameter("nombreLogro", nombreLogro)
      .uniqueResult();
    return cantidad != null && cantidad > 0;
  }
}
