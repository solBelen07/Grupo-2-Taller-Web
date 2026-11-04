package com.tallerwebi.infraestructura.tecnicasestudio;

import com.tallerwebi.dominio.tecnicasestudio.Estado;
import com.tallerwebi.dominio.tecnicasestudio.Pomodoro;
import com.tallerwebi.dominio.tecnicasestudio.RepositorioPomodoro;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("RepositorioPomodoro")
public class RepositorioPomodoroImpl implements RepositorioPomodoro {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioPomodoroImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Pomodoro pomodoro) {
    this.sessionFactory.getCurrentSession().persist(pomodoro);
  }

  @Override
  public void actualizar(Pomodoro pomodoro) {
    boolean existe =
      pomodoro.getId() != null &&
      this.sessionFactory.getCurrentSession().find(Pomodoro.class, pomodoro.getId()) != null;

    if (!existe) throw new RuntimeException("Sesion no encontrada");

    this.sessionFactory.getCurrentSession().merge(pomodoro);
  }

  @Override
  public Optional<Pomodoro> buscarPorId(Long id) {
    return Optional.ofNullable(this.sessionFactory.getCurrentSession().find(Pomodoro.class, id));
  }

  @Override
  public Optional<Pomodoro> buscarPorUsuarioYEstado(Long idUsuario, Estado estado) {
    return this.sessionFactory.getCurrentSession()
      .createQuery(
        "FROM Pomodoro p WHERE p.usuario.id = :idUsuario AND p.estado = :estado " +
        "ORDER BY p.fechaInicio DESC",
        Pomodoro.class
      )
      .setParameter("idUsuario", idUsuario)
      .setParameter("estado", estado)
      .setMaxResults(1)
      .uniqueResultOptional();
  }

  @Override
  public long calcularMinutosCompletados(Long idUsuario) {
    Long total =
      this.sessionFactory.getCurrentSession()
        .createQuery(
          "SELECT SUM(p.duracionMinutos) FROM Pomodoro p WHERE p.usuario.id = :idUsuario AND p.estado = :estado",
          Long.class
        )
        .setParameter("idUsuario", idUsuario)
        .setParameter("estado", Estado.COMPLETADO)
        .uniqueResult();
    return total != null ? total : 0L;
  }

  @Override
  public boolean existePomodoroCompletadoEnFecha(Long idUsuario, LocalDate fecha) {
    LocalDateTime inicioDia = fecha.atStartOfDay();
    LocalDateTime finDia = fecha.plusDays(1).atStartOfDay();

    Long cantidad = sessionFactory
      .getCurrentSession()
      .createQuery(
        "SELECT COUNT(p.id) FROM Pomodoro p " +
        "WHERE p.usuario.id = :idUsuario " +
        "AND p.estado = :estado " +
        "AND p.fechaFin >= :inicioDia " +
        "AND p.fechaFin < :finDia",
        Long.class
      )
      .setParameter("idUsuario", idUsuario)
      .setParameter("estado", Estado.COMPLETADO)
      .setParameter("inicioDia", inicioDia)
      .setParameter("finDia", finDia)
      .uniqueResult();

    return cantidad != null && cantidad > 0;
  }
}
