package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.invitacion.Estado;
import com.tallerwebi.dominio.invitacion.Invitacion;
import com.tallerwebi.dominio.invitacion.RepositorioInvitacion;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
@Transactional
@Rollback
public class RepositorioInvitacionTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioInvitacion repositorioInvitacion;

  @BeforeEach
  public void init() {
    repositorioInvitacion = new RepositorioInvitacionImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void enviarInvitacionDeberiaPersistirInvitacion() {
    Invitacion nuevaInvitacion = new Invitacion();
    nuevaInvitacion.setVigente(true);

    repositorioInvitacion.enviarInvitacion(nuevaInvitacion);

    Invitacion invitacionGuardada = sessionFactory
      .getCurrentSession()
      .get(Invitacion.class, nuevaInvitacion.getId());

    assertNotNull(invitacionGuardada);
    assertTrue(invitacionGuardada.getVigente());
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarInvitacionesDeUnReceptorPorNombre() {
    Usuario receptor = new Usuario();
    receptor.setEmail("receptor@test.com");

    sessionFactory.getCurrentSession().save(receptor);

    Invitacion invitacion = new Invitacion();
    invitacion.setReceptor(receptor);
    invitacion.setVigente(true);

    sessionFactory.getCurrentSession().save(invitacion);

    List<Invitacion> resultados = repositorioInvitacion.buscarInvitacionesPorReceptor(
      "receptor@test.com"
    );

    assertThat(resultados, not(empty()));
    assertThat(resultados.size(), equalTo(1));
    assertThat(resultados.get(0).getReceptor().getEmail(), equalTo("receptor@test.com"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaNoEstarVigenteUnaInvitacion() {
    Usuario emisor = this.dadoQueExisteUnUsuario("emisor@test.com");
    Usuario receptor = this.dadoQueExisteUnUsuario("receptor@test.com");
    Grupo grupo = this.dadoQueExisteUnGrupo("grupo-test");
    Invitacion invitacion = dadoQueExisteUnaInvitacionVigente(emisor, receptor, grupo);

    cuandoSeAceptaLaInvitacion(invitacion);

    entoncesLaInvitacionEsInvalidaEnElRepositorio();
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaCrearseConEstadoPendienteLaInvitacion() {
    Usuario emisor = this.dadoQueExisteUnUsuario("emisor@test.com");
    Usuario receptor = this.dadoQueExisteUnUsuario("receptor@test.com");
    Grupo grupo = this.dadoQueExisteUnGrupo("grupo-test");
    Invitacion invitacion = dadoQueExisteUnaInvitacionVigente(emisor, receptor, grupo);
    entoncesTieneEstadoPendiente(invitacion);
    entoncesLaInvitacionEsPendienteEnElRepositorio();
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaCambiarElEstadoAAceptada() {
    Usuario emisor = this.dadoQueExisteUnUsuario("emisor@test.com");
    Usuario receptor = this.dadoQueExisteUnUsuario("receptor@test.com");
    Grupo grupo = this.dadoQueExisteUnGrupo("grupo-test");
    Invitacion invitacion = dadoQueExisteUnaInvitacionVigente(emisor, receptor, grupo);
    cuandoSeAceptaLaInvitacion(invitacion);
    entoncesTieneEstadoAceptada(invitacion);
    entoncesLaInvitacionEsAceptadaEnElRepositorio();
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaCambiarElEstadoARechazada() {
    Usuario emisor = this.dadoQueExisteUnUsuario("emisor@test.com");
    Usuario receptor = this.dadoQueExisteUnUsuario("receptor@test.com");
    Grupo grupo = this.dadoQueExisteUnGrupo("grupo-test");
    Invitacion invitacion = dadoQueExisteUnaInvitacionVigente(emisor, receptor, grupo);
    cuandoSeRechazaLaInvitacion(invitacion);
    entoncesTieneEstadoRechazada(invitacion);
    entoncesLaInvitacionEsRechazadaEnElRepositorio();
  }

  private Usuario dadoQueExisteUnUsuario(String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    this.sessionFactory.getCurrentSession().save(usuario);
    return usuario;
  }

  private Grupo dadoQueExisteUnGrupo(String nombreGrupo) {
    Grupo grupo = new Grupo();
    grupo.setNombre(nombreGrupo);
    this.sessionFactory.getCurrentSession().save(grupo);
    return grupo;
  }

  private Invitacion dadoQueExisteUnaInvitacionVigente(
    Usuario emisor,
    Usuario receptor,
    Grupo grupo
  ) {
    Invitacion invitacion = new Invitacion(emisor, receptor, grupo);
    invitacion.setVigente(true);
    this.sessionFactory.getCurrentSession().persist(invitacion);
    return invitacion;
  }

  private void cuandoSeAceptaLaInvitacion(Invitacion invitacion) {
    repositorioInvitacion.cambiarEstado(invitacion, Estado.ACEPTADA);
    this.sessionFactory.getCurrentSession().flush();
  }

  private void cuandoSeRechazaLaInvitacion(Invitacion invitacion) {
    repositorioInvitacion.cambiarEstado(invitacion, Estado.RECHAZADA);
    this.sessionFactory.getCurrentSession().flush();
  }

  private void entoncesLaInvitacionEsInvalidaEnElRepositorio() {
    String sql = "from Invitacion where vigente is false";
    Invitacion invitacionObtenida =
      this.sessionFactory.getCurrentSession().createQuery(sql, Invitacion.class).getSingleResult();

    assertFalse(invitacionObtenida.getVigente());
  }

  private void entoncesLaInvitacionEsAceptadaEnElRepositorio() {
    String sql = "from Invitacion where estado LIKE 'ACEPTADA'";
    Invitacion invitacionObtenida =
      this.sessionFactory.getCurrentSession().createQuery(sql, Invitacion.class).getSingleResult();

    assertEquals(invitacionObtenida.getEstadoInvitacion(), Estado.ACEPTADA);
  }

  private void entoncesLaInvitacionEsPendienteEnElRepositorio() {
    String sql = "from Invitacion where estado LIKE 'PENDIENTE'";
    Invitacion invitacionObtenida =
      this.sessionFactory.getCurrentSession().createQuery(sql, Invitacion.class).getSingleResult();

    assertEquals(invitacionObtenida.getEstadoInvitacion(), Estado.PENDIENTE);
  }

  private void entoncesLaInvitacionEsRechazadaEnElRepositorio() {
    String sql = "from Invitacion where estado LIKE 'RECHAZADA'";
    Invitacion invitacionObtenida =
      this.sessionFactory.getCurrentSession().createQuery(sql, Invitacion.class).getSingleResult();

    assertEquals(invitacionObtenida.getEstadoInvitacion(), Estado.RECHAZADA);
  }

  private void entoncesTieneEstadoRechazada(Invitacion invitacion) {
    assertEquals(invitacion.getEstadoInvitacion(), Estado.RECHAZADA);
  }

  private void entoncesTieneEstadoAceptada(Invitacion invitacion) {
    assertEquals(invitacion.getEstadoInvitacion(), Estado.ACEPTADA);
  }

  private void entoncesTieneEstadoPendiente(Invitacion invitacion) {
    assertEquals(invitacion.getEstadoInvitacion(), Estado.PENDIENTE);
  }
}
