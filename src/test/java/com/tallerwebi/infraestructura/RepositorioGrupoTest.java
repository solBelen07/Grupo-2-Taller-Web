package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.RepositorioGrupo;
import com.tallerwebi.dominio.invitacion.Estado;
import com.tallerwebi.dominio.invitacion.Invitacion;
import com.tallerwebi.dominio.invitacion.RepositorioInvitacion;
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
}
