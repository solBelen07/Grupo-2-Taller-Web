package com.tallerwebi.infraestructura.registro;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Interes;
import com.tallerwebi.dominio.catalogo.Universidad;
import com.tallerwebi.dominio.registro.RepositorioRegistro;
import com.tallerwebi.infraestructura.registro.RepositorioRegistroImpl;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { HibernateTestConfig.class })
@Transactional
public class RepositorioRegistroImplTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioRegistro repositorioRegistro;
  private Universidad universidad;
  private Carrera carrera;
  private Interes java;
  private Interes sql;
  private Interes docker;

  @BeforeEach
  public void init() {
    repositorioRegistro = new RepositorioRegistroImpl(sessionFactory);

    universidad = new Universidad("UNLaM Repo Test");
    carrera = new Carrera("Ingeniería Repo Test");
    java = new Interes("Java Repo Test");
    sql = new Interes("SQL Repo Test");
    docker = new Interes("Docker Repo Test");

    sessionFactory.getCurrentSession().persist(universidad);
    sessionFactory.getCurrentSession().persist(carrera);
    sessionFactory.getCurrentSession().persist(java);
    sessionFactory.getCurrentSession().persist(sql);
    sessionFactory.getCurrentSession().persist(docker);
  }

  @Test
  public void buscarUniversidadConIdExistenteDeberiaDevolverLaUniversidad() {
    Universidad encontrada = repositorioRegistro.buscarUniversidad(universidad.getId());

    assertThat(encontrada, notNullValue());
    assertThat(encontrada.getNombre(), equalTo("UNLaM Repo Test"));
  }

  @Test
  public void buscarUniversidadConIdInexistenteDeberiaDevolverNull() {
    assertThat(repositorioRegistro.buscarUniversidad(999999L), nullValue());
  }

  @Test
  public void buscarCarreraConIdExistenteDeberiaDevolverLaCarrera() {
    Carrera encontrada = repositorioRegistro.buscarCarrera(carrera.getId());

    assertThat(encontrada, notNullValue());
    assertThat(encontrada.getNombre(), equalTo("Ingeniería Repo Test"));
  }

  @Test
  public void buscarCarreraConIdInexistenteDeberiaDevolverNull() {
    assertThat(repositorioRegistro.buscarCarrera(999999L), nullValue());
  }

  @Test
  public void buscarInteresesConIdsExistentesDeberiaDevolverSoloLosPedidos() {
    List<Interes> encontrados = repositorioRegistro.buscarIntereses(
      List.of(java.getId(), sql.getId())
    );

    assertThat(encontrados, hasSize(2));
    assertThat(
      encontrados.stream().map(Interes::getNombre).toList(),
      containsInAnyOrder("Java Repo Test", "SQL Repo Test")
    );
  }

  @Test
  public void buscarInteresesIgnoraLosIdsInexistentes() {
    List<Interes> encontrados = repositorioRegistro.buscarIntereses(List.of(java.getId(), 999999L));

    assertThat(encontrados, hasSize(1));
    assertThat(encontrados.get(0).getNombre(), equalTo("Java Repo Test"));
  }

  @Test
  public void buscarInteresesConListaVaciaDeberiaDevolverListaVacia() {
    assertThat(repositorioRegistro.buscarIntereses(List.of()), empty());
  }

  @Test
  public void buscarInteresesConNullDeberiaDevolverListaVacia() {
    assertThat(repositorioRegistro.buscarIntereses(null), empty());
  }
}
