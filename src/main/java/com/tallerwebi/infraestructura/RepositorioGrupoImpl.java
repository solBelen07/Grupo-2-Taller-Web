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

  private static final String PARAM_ACEPTADA = "aceptada";
  private static final String PARAM_GRUPO = "grupo";

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
    // - Como invitado: invitaciones ACEPTADAS (pertenece) o PENDIENTES (fue invitado).
    //   Las RECHAZADAS y las ABANDONADAS (salió del grupo) no.
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct g from Grupo g left join g.creador c " +
        "where c.email = :email " +
        "or g in (select i.grupo from Invitacion i " +
        "         where i.receptor.email = :email and i.estado in (:estadosVisibles))",
        Grupo.class
      )
      .setParameter("email", email)
      .setParameter("estadosVisibles", List.of(Estado.ACEPTADA, Estado.PENDIENTE))
      .list();
  }

  @Override
  public Integer cantidadParticipantes(String nombre) {
    // cuenta los usuarios confirmados del grupo, sin repetir:
    // - el creador del grupo, y
    // - los que aceptaron una invitación (receptor con estado ACEPTADA).
    // Las invitaciones PENDIENTES, RECHAZADAS o ABANDONADAS no suman.
    Long count = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(distinct u) from Usuario u " +
        "where u in (select g.creador from Grupo g where g.nombre = :nombreGrupo) " +
        "or u in (select i.receptor from Invitacion i " +
        "            where i.grupo.nombre = :nombreGrupo and i.estado = :aceptada)",
        Long.class
      )
      .setParameter("nombreGrupo", nombre)
      .setParameter(PARAM_ACEPTADA, Estado.ACEPTADA)
      .uniqueResult();

    return count != null ? count.intValue() : 0;
  }

  @Override
  public List<Usuario> listarIntegrantes(String nombre) {
    // Mismo criterio que cantidadParticipantes: el creador y los que aceptaron una invitación.
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select distinct u from Usuario u " +
        "where u in (select g.creador from Grupo g where g.nombre = :nombreGrupo) " +
        "or u in (select i.receptor from Invitacion i " +
        "            where i.grupo.nombre = :nombreGrupo and i.estado = :aceptada) " +
        "order by u.nombre",
        Usuario.class
      )
      .setParameter("nombreGrupo", nombre)
      .setParameter(PARAM_ACEPTADA, Estado.ACEPTADA)
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
      .setParameter(PARAM_GRUPO, grupo)
      .executeUpdate();
    sessionFactory
      .getCurrentSession()
      .createMutationQuery("update Tarea t set t.grupo = null where t.grupo = :grupo")
      .setParameter(PARAM_GRUPO, grupo)
      .executeUpdate();
    sessionFactory.getCurrentSession().remove(grupo);
  }

  @Override
  public Usuario buscarIntegranteMasAntiguo(Grupo grupo, Usuario excluido) {
    // El primero que acepto una invitacion (la de id más chico), sin contar al excluido.
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select i.receptor from Invitacion i " +
        "where i.grupo = :grupo and i.estado = :aceptada and i.receptor.id <> :idExcluido " +
        "order by i.id",
        Usuario.class
      )
      .setParameter(PARAM_GRUPO, grupo)
      .setParameter(PARAM_ACEPTADA, Estado.ACEPTADA)
      .setParameter("idExcluido", excluido.getId())
      .setMaxResults(1)
      .uniqueResult();
  }

  @Override
  public void quitarIntegrante(Grupo grupo, Usuario usuario) {
    // No se borra la invitacion: pasa a ABANDONADA, asi queda el historial.
    sessionFactory
      .getCurrentSession()
      .createMutationQuery(
        "update Invitacion i set i.estado = :abandonada, i.vigente = false " +
        "where i.grupo = :grupo and i.receptor.id = :idUsuario and i.estado = :aceptada"
      )
      .setParameter("abandonada", Estado.ABANDONADA)
      .setParameter(PARAM_GRUPO, grupo)
      .setParameter("idUsuario", usuario.getId())
      .setParameter(PARAM_ACEPTADA, Estado.ACEPTADA)
      .executeUpdate();
  }
}
