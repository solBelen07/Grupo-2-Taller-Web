package com.tallerwebi.infraestructura.logro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tallerwebi.dominio.logro.Logro;
import com.tallerwebi.dominio.logro.RepositorioLogro;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
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
public class RepositorioLogroTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioLogro repositorioLogro;
  private Logro logro;

  @BeforeEach
  public void init() {
    repositorioLogro = new RepositorioLogroImpl(sessionFactory);
    logro = new Logro();
    logro.setNombre("logro-existente");
    logro.setDescripcion("descripcion del logro");
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarUnLogroExistentePorNombre() {
    String logroBuscado = "logro-existente";
    dadoQueExisteElLogro(logro);
    Logro logroObtenido = cuandoBuscoUnLogroPorNombre(logroBuscado);
    entoncesElLogroObtenidoDeberiaSerElMismo(logro, logroObtenido);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverUnListadoDeLogros() {
    dadoQueExisteElLogro(logro);
    var logrosObtenidos = repositorioLogro.listar();
    assertEquals(1, logrosObtenidos.size());
    assertEquals(logro, logrosObtenidos.get(0));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverNullSiElLogroNoExiste() {
    String logroBuscado = "logro-no-existente";
    Logro logroObtenido = cuandoBuscoUnLogroPorNombre(logroBuscado);
    assertEquals(null, logroObtenido);
  }

  private void entoncesElLogroObtenidoDeberiaSerElMismo(Logro logro, Logro logroObtenido) {
    assertEquals(logro, logroObtenido);
  }

  private Logro cuandoBuscoUnLogroPorNombre(String logroBuscado) {
    return repositorioLogro.buscar(logroBuscado);
  }

  private void dadoQueExisteElLogro(Logro logro) {
    this.sessionFactory.getCurrentSession().persist(logro);
  }
}
