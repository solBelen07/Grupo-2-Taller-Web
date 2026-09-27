package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tallerwebi.dominio.Invitacion;
import com.tallerwebi.dominio.RepositorioInvitacion;
import com.tallerwebi.dominio.Usuario;
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
}
