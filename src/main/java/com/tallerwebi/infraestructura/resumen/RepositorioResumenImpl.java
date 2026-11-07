package com.tallerwebi.infraestructura.resumen;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.resumen.RepositorioResumen;
import com.tallerwebi.dominio.resumen.Resumen;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository("RepositorioResumen")
public class RepositorioResumenImpl implements RepositorioResumen {

  private final SessionFactory sessionFactory;

  public RepositorioResumenImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Resumen resumen) {
    sessionFactory.getCurrentSession().persist(resumen);
  }

  @Override
  public void actualizar(Resumen resumen) {
    sessionFactory.getCurrentSession().merge(resumen);
  }

  @Override
  public List<Resumen> buscarPorUsuario(Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "FROM Resumen r WHERE r.usuario.id = :usuarioId ORDER BY r.fechaCreacion DESC, r.id DESC",
        Resumen.class
      )
      .setParameter("usuarioId", usuarioId)
      .getResultList();
  }

  @Override
  public Resumen buscarPorIdYUsuario(Long id, Long usuarioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("FROM Resumen r WHERE r.id = :id AND r.usuario.id = :usuarioId", Resumen.class)
      .setParameter("id", id)
      .setParameter("usuarioId", usuarioId)
      .uniqueResult();
  }

  @Override
  public Resumen buscarPorUsuarioYMarca(Long usuarioId, String marca) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "FROM Resumen r WHERE r.usuario.id = :usuarioId AND r.marca = :marca",
        Resumen.class
      )
      .setParameter("usuarioId", usuarioId)
      .setParameter("marca", marca)
      .setMaxResults(1)
      .uniqueResult();
  }
}
