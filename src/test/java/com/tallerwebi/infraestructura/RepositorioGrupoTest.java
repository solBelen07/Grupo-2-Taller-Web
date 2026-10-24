package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.RepositorioGrupo;
import com.tallerwebi.dominio.invitacion.Estado;
import com.tallerwebi.dominio.invitacion.Invitacion;
import com.tallerwebi.dominio.invitacion.RepositorioInvitacion;
import com.tallerwebi.dominio.tarea.Tarea;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioGrupoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioGrupo repositorioGrupo;
  private RepositorioInvitacion repositorioInvitacion;
  private Grupo grupo;
  private Usuario usuario;

  @BeforeEach
  public void init() {
    repositorioGrupo = new RepositorioGrupoImpl(sessionFactory);
    repositorioInvitacion = new RepositorioInvitacionImpl(sessionFactory);
    grupo = new Grupo();
    grupo.setNombre("grupo-existente");
    usuario = new Usuario();
    usuario.setEmail("usuario@prueba.com");
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarUnGrupoExistentePorNombre() {
    String grupoBuscado = "grupo-existente";
    dadoQueExisteElGrupo(grupo);
    Grupo grupoObtenido = cuandoBuscoUnGrupoPorNombre(grupoBuscado);
    entoncesElGrupoObtenidoDeberiaSerElMismo(grupo, grupoObtenido);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverNullSiElGrupoNoExiste() {
    String grupoBuscado = "grupo-no-existente";
    Grupo grupoObtenido = cuandoBuscoUnGrupoPorNombre(grupoBuscado);
    entoncesElGrupoObtenidoDeberiaSerNull(grupoObtenido);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaListarTodosLosGruposDeUnUsuario() {
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);

    Invitacion invitacion = new Invitacion();
    invitacion.setGrupo(grupo);
    invitacion.setEmisor(usuario);
    invitacion.setReceptor(usuario);
    invitacion.setEstadoInvitacion(Estado.ACEPTADA);

    dadoQueExisteUnaInvitacion(invitacion);

    List<Grupo> grupoObtenido = this.repositorioGrupo.listarGruposPorUsuario(usuario.getEmail());
    assertThat(grupoObtenido.size(), equalTo(1));
  }

  private void dadoQueExisteElUsuario(Usuario usuario) {
    this.sessionFactory.getCurrentSession().persist(usuario);
  }

  private void dadoQueExisteUnaInvitacion(Invitacion invitacion) {
    this.sessionFactory.getCurrentSession().persist(invitacion);
  }

  private void dadoQueExisteElGrupo(Grupo grupo) {
    this.sessionFactory.getCurrentSession().persist(grupo);
  }

  private Grupo cuandoBuscoUnGrupoPorNombre(String nombre) {
    return this.repositorioGrupo.buscar(nombre);
  }

  private void entoncesElGrupoObtenidoDeberiaSerElMismo(Grupo grupoEsperado, Grupo grupoObtenido) {
    assertThat(grupoObtenido.getId(), equalTo(grupoEsperado.getId()));
    assertThat(grupoObtenido.getNombre(), equalTo(grupoEsperado.getNombre()));
  }

  private void entoncesElGrupoObtenidoDeberiaSerNull(Grupo grupoObtenido) {
    assertThat(grupoObtenido, equalTo(null));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaIncluirLosGruposConInvitacionPendiente() {
    Usuario emisor = crearUsuario("emisor@prueba.com");
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);
    dadoQueExisteUnaInvitacion(crearInvitacion(emisor, usuario, grupo, Estado.PENDIENTE));

    List<Grupo> grupos = this.repositorioGrupo.listarGruposPorUsuario(usuario.getEmail());

    assertThat(grupos.size(), equalTo(1));
    assertThat(grupos.get(0).getNombre(), equalTo("grupo-existente"));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaIncluirLosGruposConInvitacionRechazada() {
    Usuario emisor = crearUsuario("emisor@prueba.com");
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);
    dadoQueExisteUnaInvitacion(crearInvitacion(emisor, usuario, grupo, Estado.RECHAZADA));

    List<Grupo> grupos = this.repositorioGrupo.listarGruposPorUsuario(usuario.getEmail());

    assertThat(grupos.size(), equalTo(0));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaIncluirLosGruposDondeElUsuarioInvitoAAlguien() {
    Usuario invitado = crearUsuario("invitado@prueba.com");
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, invitado, grupo, Estado.PENDIENTE));

    List<Grupo> grupos = this.repositorioGrupo.listarGruposPorUsuario(usuario.getEmail());

    assertThat(grupos.size(), equalTo(1));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaIncluirGruposDeOtrosUsuarios() {
    Usuario otro = crearUsuario("otro@prueba.com");
    Usuario emisor = crearUsuario("emisor@prueba.com");
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);
    dadoQueExisteUnaInvitacion(crearInvitacion(emisor, otro, grupo, Estado.ACEPTADA));

    List<Grupo> grupos = this.repositorioGrupo.listarGruposPorUsuario(usuario.getEmail());

    assertThat(grupos.size(), equalTo(0));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaRepetirUnGrupoAunqueTengaVariasInvitaciones() {
    Usuario otro = crearUsuario("otro@prueba.com");
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);
    dadoQueExisteUnaInvitacion(crearInvitacion(otro, usuario, grupo, Estado.ACEPTADA));
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, otro, grupo, Estado.PENDIENTE));

    List<Grupo> grupos = this.repositorioGrupo.listarGruposPorUsuario(usuario.getEmail());

    assertThat(grupos.size(), equalTo(1));
  }

  private Usuario crearUsuario(String email) {
    Usuario nuevo = new Usuario();
    nuevo.setEmail(email);
    dadoQueExisteElUsuario(nuevo);
    return nuevo;
  }

  private Invitacion crearInvitacion(Usuario emisor, Usuario receptor, Grupo grupo, Estado estado) {
    Invitacion invitacion = new Invitacion();
    invitacion.setEmisor(emisor);
    invitacion.setReceptor(receptor);
    invitacion.setGrupo(grupo);
    invitacion.setEstadoInvitacion(estado);
    return invitacion;
  }

  @Test
  @Transactional
  @Rollback
  public void laCantidadDeIntegrantesDeberiaContarAlQueInvitaYALosQueAceptaron() {
    Usuario aceptado = crearUsuario("aceptado@prueba.com");
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, aceptado, grupo, Estado.ACEPTADA));

    Integer cantidad = this.repositorioGrupo.cantidadParticipantes("grupo-existente");

    assertThat(cantidad, equalTo(2));
  }

  @Test
  @Transactional
  @Rollback
  public void laCantidadDeIntegrantesNoDeberiaContarInvitacionesPendientesNiRechazadas() {
    Usuario pendiente = crearUsuario("pendiente@prueba.com");
    Usuario rechazado = crearUsuario("rechazado@prueba.com");
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, pendiente, grupo, Estado.PENDIENTE));
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, rechazado, grupo, Estado.RECHAZADA));

    Integer cantidad = this.repositorioGrupo.cantidadParticipantes("grupo-existente");

    assertThat(cantidad, equalTo(1));
  }

  @Test
  @Transactional
  @Rollback
  public void laCantidadDeIntegrantesNoDeberiaContarDosVecesAlMismoUsuario() {
    Usuario aceptado = crearUsuario("aceptado@prueba.com");
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, aceptado, grupo, Estado.ACEPTADA));
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, aceptado, grupo, Estado.ACEPTADA));
    dadoQueExisteUnaInvitacion(crearInvitacion(aceptado, usuario, grupo, Estado.ACEPTADA));

    Integer cantidad = this.repositorioGrupo.cantidadParticipantes("grupo-existente");

    assertThat(cantidad, equalTo(2));
  }

  @Test
  @Transactional
  @Rollback
  public void laCantidadDeIntegrantesDeberiaSerCeroSiElGrupoNoTieneInvitaciones() {
    dadoQueExisteElGrupo(grupo);

    Integer cantidad = this.repositorioGrupo.cantidadParticipantes("grupo-existente");

    assertThat(cantidad, equalTo(0));
  }

  @Test
  @Transactional
  @Rollback
  public void laCantidadDeIntegrantesSoloDeberiaContarLosDeEseGrupo() {
    Grupo otroGrupo = new Grupo();
    otroGrupo.setNombre("otro-grupo");
    Usuario aceptado = crearUsuario("aceptado@prueba.com");
    Usuario deOtroGrupo = crearUsuario("otro@prueba.com");
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);
    dadoQueExisteElGrupo(otroGrupo);
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, aceptado, grupo, Estado.ACEPTADA));
    dadoQueExisteUnaInvitacion(crearInvitacion(deOtroGrupo, aceptado, otroGrupo, Estado.ACEPTADA));

    Integer cantidad = this.repositorioGrupo.cantidadParticipantes("grupo-existente");

    assertThat(cantidad, equalTo(2));
  }

  @Test
  @Transactional
  @Rollback
  public void listarIntegrantesDeberiaDevolverAlQueInvitaYALosQueAceptaronSinPendientes() {
    Usuario aceptado = crearUsuario("aceptado@prueba.com");
    Usuario pendiente = crearUsuario("pendiente@prueba.com");
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, aceptado, grupo, Estado.ACEPTADA));
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, pendiente, grupo, Estado.PENDIENTE));

    List<Usuario> integrantes = this.repositorioGrupo.listarIntegrantes("grupo-existente");

    assertThat(integrantes.size(), equalTo(2));
    assertThat(integrantes.contains(usuario), equalTo(true));
    assertThat(integrantes.contains(aceptado), equalTo(true));
    assertThat(integrantes.contains(pendiente), equalTo(false));
  }

  @Test
  @Transactional
  @Rollback
  public void listarIntegrantesNoDeberiaRepetirUsuarios() {
    Usuario aceptado = crearUsuario("aceptado@prueba.com");
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, aceptado, grupo, Estado.ACEPTADA));
    dadoQueExisteUnaInvitacion(crearInvitacion(aceptado, usuario, grupo, Estado.ACEPTADA));

    assertThat(this.repositorioGrupo.listarIntegrantes("grupo-existente").size(), equalTo(2));
  }

  @Test
  @Transactional
  @Rollback
  public void laListaDeIntegrantesYElContadorDeberianCoincidir() {
    Usuario aceptado = crearUsuario("aceptado@prueba.com");
    Usuario rechazado = crearUsuario("rechazado@prueba.com");
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, aceptado, grupo, Estado.ACEPTADA));
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, rechazado, grupo, Estado.RECHAZADA));

    assertThat(
      this.repositorioGrupo.listarIntegrantes("grupo-existente").size(),
      equalTo(this.repositorioGrupo.cantidadParticipantes("grupo-existente"))
    );
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnGrupoConSuCreador() {
    dadoQueExisteElUsuario(usuario);
    grupo.setCreador(usuario);

    this.repositorioGrupo.guardar(grupo);

    Grupo guardado = this.repositorioGrupo.buscar("grupo-existente");
    assertThat(guardado.getCreador().getEmail(), equalTo("usuario@prueba.com"));
  }

  @Test
  @Transactional
  @Rollback
  public void elCreadorVeSuGrupoAunqueTodaviaNoHayaInvitadoANadie() {
    dadoQueExisteElUsuario(usuario);
    grupo.setCreador(usuario);
    dadoQueExisteElGrupo(grupo);

    List<Grupo> grupos = this.repositorioGrupo.listarGruposPorUsuario(usuario.getEmail());

    assertThat(grupos.size(), equalTo(1));
  }

  @Test
  @Transactional
  @Rollback
  public void elCreadorCuentaComoIntegranteUnaSolaVez() {
    Usuario aceptado = crearUsuario("aceptado@prueba.com");
    dadoQueExisteElUsuario(usuario);
    grupo.setCreador(usuario);
    dadoQueExisteElGrupo(grupo);
    // el creador además invitó a alguien: igual tiene que contarse una sola vez
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, aceptado, grupo, Estado.ACEPTADA));

    assertThat(this.repositorioGrupo.cantidadParticipantes("grupo-existente"), equalTo(2));
  }

  @Test
  @Transactional
  @Rollback
  public void unGrupoRecienCreadoTieneUnIntegrante() {
    dadoQueExisteElUsuario(usuario);
    grupo.setCreador(usuario);
    dadoQueExisteElGrupo(grupo);

    assertThat(this.repositorioGrupo.cantidadParticipantes("grupo-existente"), equalTo(1));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaModificarElNombreDelGrupo() {
    dadoQueExisteElGrupo(grupo);
    grupo.setNombre("nombre-nuevo");

    this.repositorioGrupo.modificar(grupo);

    assertThat(this.repositorioGrupo.buscar("nombre-nuevo").getId(), equalTo(grupo.getId()));
    assertThat(this.repositorioGrupo.buscar("grupo-existente"), equalTo(null));
  }

  @Test
  @Transactional
  @Rollback
  public void alEliminarUnGrupoSeBorranSusInvitacionesYSusTareasQuedanSinGrupo() {
    Usuario invitado = crearUsuario("invitado@prueba.com");
    dadoQueExisteElUsuario(usuario);
    dadoQueExisteElGrupo(grupo);
    dadoQueExisteUnaInvitacion(crearInvitacion(usuario, invitado, grupo, Estado.PENDIENTE));
    Tarea tarea = new Tarea();
    tarea.setTitulo("Tarea del grupo");
    tarea.setGrupo(grupo);
    this.sessionFactory.getCurrentSession().persist(tarea);
    // como en la app: el grupo se busca en una sesión nueva y recién ahí se elimina
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();
    Grupo grupoAEliminar = this.repositorioGrupo.buscar("grupo-existente");

    this.repositorioGrupo.eliminar(grupoAEliminar);
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();

    assertThat(this.repositorioGrupo.buscar("grupo-existente"), equalTo(null));
    Long invitaciones =
      this.sessionFactory.getCurrentSession()
        .createQuery("select count(i) from Invitacion i", Long.class)
        .uniqueResult();
    assertThat(invitaciones, equalTo(0L));
    Tarea tareaGuardada = this.sessionFactory.getCurrentSession().get(Tarea.class, tarea.getId());
    assertThat(tareaGuardada.getGrupo(), equalTo(null));
  }

  @Test
  @Transactional
  @Rollback
  public void listarIntegrantesDeberiaIncluirAlCreadorAunqueNoHayaInvitadoANadie() {
    dadoQueExisteElUsuario(usuario);
    grupo.setCreador(usuario);
    dadoQueExisteElGrupo(grupo);

    List<Usuario> integrantes = this.repositorioGrupo.listarIntegrantes("grupo-existente");

    assertThat(integrantes.size(), equalTo(1));
    assertThat(integrantes.get(0).getEmail(), equalTo("usuario@prueba.com"));
  }
}
