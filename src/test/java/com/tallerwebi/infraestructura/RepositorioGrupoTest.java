package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.tallerwebi.dominio.Grupo;
import com.tallerwebi.dominio.RepositorioGrupo;
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
public class RepositorioGrupoTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioGrupo repositorioGrupo;
  private Grupo grupo;

  @BeforeEach
  public void init() {
    repositorioGrupo = new RepositorioGrupoImpl(sessionFactory);
    grupo = new Grupo();
    grupo.setNombre("grupo-existente");
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
}
