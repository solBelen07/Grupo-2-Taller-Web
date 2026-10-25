package com.tallerwebi.infraestructura.logro;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.logro.Logro;
import com.tallerwebi.dominio.logro.LogroObtenido;
import com.tallerwebi.dominio.logro.RepositorioLogroObtenido;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
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
public class RepositorioLogroObtenidoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioLogroObtenido repositorioLogroObtenido;
  private Usuario usuario;
  private Logro popular;

  @BeforeEach
  public void init() {
    repositorioLogroObtenido = new RepositorioLogroObtenidoImpl(sessionFactory);
    usuario = new Usuario();
    usuario.setEmail("usuario@prueba.com");
    popular = new Logro();
    popular.setNombre("Popular");
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarYEncontrarLosLogrosObtenidosDeUnUsuario() {
    dadoQueExisten(usuario, popular);
    LocalDate fecha = LocalDate.of(2026, 10, 1);

    repositorioLogroObtenido.guardar(new LogroObtenido(usuario, popular, fecha));
    List<LogroObtenido> obtenidos = repositorioLogroObtenido.buscarPorUsuario(usuario.getId());

    assertThat(obtenidos.size(), equalTo(1));
    assertThat(obtenidos.get(0).getLogro().getNombre(), equalTo("Popular"));
    assertThat(obtenidos.get(0).getFechaObtencion(), equalTo(fecha));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaDevolverLosLogrosDeOtroUsuario() {
    Usuario otro = new Usuario();
    otro.setEmail("otro@prueba.com");
    dadoQueExisten(usuario, otro, popular);
    repositorioLogroObtenido.guardar(new LogroObtenido(otro, popular, LocalDate.now()));

    assertThat(repositorioLogroObtenido.buscarPorUsuario(usuario.getId()).size(), equalTo(0));
  }

  @Test
  @Transactional
  @Rollback
  public void existeDeberiaSerVerdaderoSiElUsuarioYaTieneElLogro() {
    dadoQueExisten(usuario, popular);
    repositorioLogroObtenido.guardar(new LogroObtenido(usuario, popular, LocalDate.now()));

    assertThat(repositorioLogroObtenido.existe(usuario.getId(), "popular"), equalTo(true));
  }

  @Test
  @Transactional
  @Rollback
  public void existeDeberiaSerFalsoSiElUsuarioNoTieneElLogro() {
    dadoQueExisten(usuario, popular);

    assertThat(repositorioLogroObtenido.existe(usuario.getId(), "Popular"), equalTo(false));
  }

  private void dadoQueExisten(Object... entidades) {
    for (Object entidad : entidades) {
      sessionFactory.getCurrentSession().persist(entidad);
    }
  }
}
