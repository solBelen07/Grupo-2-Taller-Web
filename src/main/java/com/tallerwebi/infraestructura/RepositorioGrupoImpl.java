package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.RepositorioGrupo;
import com.tallerwebi.dominio.invitacion.Estado;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository("repositorioGrupo")
public class RepositorioGrupoImpl implements RepositorioGrupo {

  private SessionFactory sessionFactory;

  public RepositorioGrupoImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Grupo buscar(String nombre) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Grupo where nombre = :nombre", Grupo.class)
      .setParameter("nombre", nombre)
      .setMaxResults(1)
      .uniqueResult();
  }

  @Override
  public List<Grupo> listar() {
    return sessionFactory.getCurrentSession().createQuery("from Grupo", Grupo.class).list();
  }

  @Override
  public List<Grupo> listarGruposPorUsuario(String email) {
    // grupos a los que el usuario pertenece o fue invitado.
    // - Como creador: es el administrador del grupo.
    // - Como invitado: invitaciones ACEPTADAS (pertenece) o PENDIENTES (fue invitado). Las RECHAZADAS no.
    // - Como emisor: si invitó a alguien a un grupo, es porque forma parte de ese grupo.
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct g from Grupo g left join g.creador c " +
        "where c.email = :email " +
        "or g in (select i.grupo from Invitacion i " +
        "         where (i.receptor.email = :email and i.estado in (:estadosVisibles)) " +
        "         or i.emisor.email = :email)",
        Grupo.class
      )
      .setParameter("email", email)
      .setParameter("estadosVisibles", List.of(Estado.ACEPTADA, Estado.PENDIENTE))
      .list();
  }

  @Override
  public Integer cantidadParticipantes(String nombre) {
    // cuenta los usuarios confirmados del grupo, sin repetir:
    // - el creador del grupo,
    // - los que aceptaron una invitación (receptor con estado ACEPTADA), y
    // - los que invitaron a alguien (emisor): si invitaron, forman parte del grupo.
    // Las invitaciones PENDIENTES o RECHAZADAS no suman.
    Long count = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(distinct u) from Usuario u " +
        "where u in (select g.creador from Grupo g where g.nombre = :nombreGrupo) " +
        "or u in (select i.receptor from Invitacion i " +
        "            where i.grupo.nombre = :nombreGrupo and i.estado = :aceptada) " +
        "or u in (select i.emisor from Invitacion i where i.grupo.nombre = :nombreGrupo)",
        Long.class
      )
      .setParameter("nombreGrupo", nombre)
      .setParameter("aceptada", Estado.ACEPTADA)
      .uniqueResult();

    return count != null ? count.intValue() : 0;
  }

  @Override
  public List<Usuario> listarIntegrantes(String nombre) {
    // Mismo criterio que cantidadParticipantes: el creador, los que aceptaron
    // una invitación y los que invitaron a alguien al grupo, sin repetir.
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct u from Usuario u " +
        "where u in (select g.creador from Grupo g where g.nombre = :nombreGrupo) " +
        "or u in (select i.receptor from Invitacion i " +
        "            where i.grupo.nombre = :nombreGrupo and i.estado = :aceptada) " +
        "or u in (select i.emisor from Invitacion i where i.grupo.nombre = :nombreGrupo) " +
        "order by u.nombre",
        Usuario.class
      )
      .setParameter("nombreGrupo", nombre)
      .setParameter("aceptada", Estado.ACEPTADA)
      .list();
  }

  @Override
  public void guardar(Grupo grupo) {
    sessionFactory.getCurrentSession().persist(grupo);
  }

  @Override
  public void modificar(Grupo grupo) {
    sessionFactory.getCurrentSession().merge(grupo);
  }

  @Override
  public void eliminar(Grupo grupo) {
    // Antes de borrar el grupo hay que soltar lo que lo referencia en la base:
    // sus invitaciones se borran y sus tareas quedan "Sin grupo".
    sessionFactory
      .getCurrentSession()
      .createMutationQuery("delete from Invitacion i where i.grupo = :grupo")
      .setParameter("grupo", grupo)
      .executeUpdate();
    sessionFactory
      .getCurrentSession()
      .createMutationQuery("update Tarea t set t.grupo = null where t.grupo = :grupo")
      .setParameter("grupo", grupo)
      .executeUpdate();
    sessionFactory.getCurrentSession().remove(grupo);
  }
}
