package com.tallerwebi.infraestructura.tecnicasestudio;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.tecnicasestudio.Estado;
import com.tallerwebi.dominio.tecnicasestudio.Pomodoro;
import com.tallerwebi.dominio.tecnicasestudio.RepositorioPomodoro;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("RepositorioPomodoro")
public class RepositorioPomodoroImpl implements RepositorioPomodoro {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioPomodoroImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Pomodoro sesion) {
    this.sessionFactory.getCurrentSession().persist(sesion);
  }

  @Override
  public void modificar(Pomodoro sesion) {
    Pomodoro existente =
      this.sessionFactory.getCurrentSession()
        .createQuery("FROM Pomodoro WHERE id = :id ", Pomodoro.class)
        .setParameter("id", Long.valueOf(sesion.getId()))
        .uniqueResult();

    if (existente == null) throw new RuntimeException("Sesion no encontrada");

    this.sessionFactory.getCurrentSession().merge(sesion);
  }

  @Override
  public Pomodoro buscarPorId(long id) {
    return this.sessionFactory.getCurrentSession()
      .createQuery("FROM Pomodoro WHERE id = :id", Pomodoro.class)
      .setParameter("id", Long.valueOf(id))
      .uniqueResult();
  }

  @Override
  public Pomodoro buscarSesionesEnCurso(Usuario usuario) {
    return this.sessionFactory.getCurrentSession()
      .createQuery("FROM Pomodoro WHERE id = :id AND estado = :estado", Pomodoro.class)
      .setParameter("id", usuario.getId())
      .setParameter("estado", Estado.EN_CURSO)
      .uniqueResult();
  }

  @Override
  public Integer obtenerTotalMinutosCompletados(Usuario usuario) {
    return (Integer) this.sessionFactory.getCurrentSession()
      .createQuery(
        "SELECT SUM(duracion) FROM Pomodoro WHERE usuario = :usuario AND estado = :estado"
      )
      .setParameter("usuario", usuario)
      .setParameter("estado", Estado.COMPLETADA)
      .uniqueResult();
  }
}
