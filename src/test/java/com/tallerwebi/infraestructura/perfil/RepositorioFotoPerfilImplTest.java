package com.tallerwebi.infraestructura.perfil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.perfil.FotoPerfil;
import com.tallerwebi.dominio.perfil.RepositorioFotoPerfil;
import com.tallerwebi.integracion.config.HibernateTestConfig;
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
public class RepositorioFotoPerfilImplTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioFotoPerfil repositorio;

  @BeforeEach
  public void init() {
    repositorio = new RepositorioFotoPerfilImpl(sessionFactory);
  }

  @Test
  public void guardarYBuscarDeberiaDevolverLaFoto() {
    repositorio.guardar(new FotoPerfil(100L, "image/png", new byte[] { 1, 2, 3 }));
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    FotoPerfil encontrada = repositorio.buscar(100L);

    assertThat(encontrada, notNullValue());
    assertThat(encontrada.getTipoContenido(), equalTo("image/png"));
    assertThat(encontrada.getDatos(), equalTo(new byte[] { 1, 2, 3 }));
  }

  @Test
  public void guardarDosVecesDeberiaReemplazarLaFoto() {
    repositorio.guardar(new FotoPerfil(101L, "image/png", new byte[] { 1 }));
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    repositorio.guardar(new FotoPerfil(101L, "image/jpeg", new byte[] { 9, 9 }));
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    FotoPerfil encontrada = repositorio.buscar(101L);
    assertThat(encontrada.getTipoContenido(), equalTo("image/jpeg"));
    assertThat(encontrada.getDatos(), equalTo(new byte[] { 9, 9 }));
  }

  @Test
  public void buscarInexistenteDeberiaDevolverNull() {
    assertThat(repositorio.buscar(999999L), nullValue());
  }

  @Test
  public void eliminarDeberiaBorrarLaFoto() {
    repositorio.guardar(new FotoPerfil(102L, "image/png", new byte[] { 1 }));
    sessionFactory.getCurrentSession().flush();

    repositorio.eliminar(102L);
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    assertThat(repositorio.buscar(102L), nullValue());
  }

  @Test
  public void eliminarInexistenteNoDeberiaFallar() {
    repositorio.eliminar(999998L);

    assertThat(repositorio.buscar(999998L), nullValue());
  }
}
