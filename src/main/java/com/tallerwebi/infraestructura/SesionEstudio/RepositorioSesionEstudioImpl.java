package com.tallerwebi.infraestructura.SesionEstudio;

import com.tallerwebi.dominio.sesionEstudio.RepositorioSesionEstudio;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import java.time.LocalDate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

//@SuppressWarnings("CPD-START")
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

  @Override
  public void eliminarPorMateriaId(Integer materiaId) {
    String hql = "delete from SesionEstudio s where s.parcial.materia.id = :materiaId";
    sessionFactory
      .getCurrentSession()
      .createQuery(hql)
      .setParameter("materiaId", materiaId)
      .executeUpdate();
  }

  @Override
  public SesionEstudio buscarProximaSesionEstudio(Long idUsuario) {
    // la próxima sesión SIN COMPLETAR del usuario, de hoy en adelante.
    // Antes buscaba solo las de hoy y con uniqueResult(): si había dos el mismo día, fallaba.
    // Ahora se ordena por fecha y se toma la primera (setMaxResults(1)).
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from SesionEstudio s " +
        "where s.usuario.id = :idUsuario and s.fecha >= :hoy " +
        "and (s.completada = false or s.completada is null) " +
        "order by s.fecha asc, s.id asc",
        SesionEstudio.class
      )
      .setParameter("idUsuario", idUsuario)
      .setParameter("hoy", LocalDate.now())
      .setMaxResults(1)
      .uniqueResult();
  }
}
