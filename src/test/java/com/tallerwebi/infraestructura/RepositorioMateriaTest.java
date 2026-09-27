package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.Materia;
import com.tallerwebi.dominio.RepositorioMateria;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.MateriaNoEncontrada;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.persistence.Query;
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
public class RepositorioMateriaTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioMateria repositorioMateria;

  @BeforeEach
  public void init() {
    repositorioMateria = new RepositorioMateriaImpl(sessionFactory);
  }

  private Materia dadoQueTengoUnaMateria(String nombre) {
    Materia materia = new Materia();
    materia.setNombre(nombre);
    return materia;
  }

  private void dadoQueExisteLaMateria(Materia materia) {
    this.sessionFactory.getCurrentSession().persist(materia);
  }

  private void cuandoGuardoUnaMateria(Materia materia) {
    repositorioMateria.guardar(materia);
  }

  private Materia cuandoBuscoUnaMateriaPorNombre(String nombre) {
    return repositorioMateria.buscarPorNombre(nombre);
  }

  private void cuandoModificoUnaMateria(Materia materia) {
    repositorioMateria.modificar(materia);
  }

  private void entoncesSeGuardoLaMateria(String nombre, Materia materiaEsperada) {
    String hql = "FROM Materia WHERE LOWER(nombre) = LOWER(:nombre)";
    Query query = this.sessionFactory.getCurrentSession().createQuery(hql, Materia.class);
    query.setParameter("nombre", nombre);
    Materia materiaObtenida = (Materia) query.getSingleResult();
    this.entoncesLaMateriaObtenidaEsCorrecta(materiaObtenida, materiaEsperada);
  }

  private void entoncesLaMateriaObtenidaEsCorrecta(
    Materia materiaObtenida,
    Materia materiaEsperada
  ) {
    assertThat(materiaObtenida.getNombre(), is(equalTo(materiaEsperada.getNombre())));
  }

  private void entoncesLaMateriaObtenidaEsNull(Materia obtenida) {
    assertThat(obtenida, is(nullValue()));
  }

  private void entoncesSeLanzaUnaMateriaNoEncontrada(Materia materia) {
    assertThrows(
      MateriaNoEncontrada.class,
      () -> {
        this.cuandoModificoUnaMateria(materia);
      }
    );
  }

  private List<Materia> listaDeMaterias() {
    return repositorioMateria.obtenerTodas();
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnaNuevaMateria() {
    String nombre = "Taller Web I";
    // preparacion
    Materia materia = this.dadoQueTengoUnaMateria(nombre);

    // ejecucion
    this.cuandoGuardoUnaMateria(materia);

    // validacion
    this.entoncesSeGuardoLaMateria(nombre, materia);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarUnaMateriaIgnorandoMayusculasYMinusculas() {
    String nombre = "Taller Web I";
    // preparacion
    Materia materia = this.dadoQueTengoUnaMateria(nombre);

    dadoQueExisteLaMateria(materia);
    Materia materiaObtenida = cuandoBuscoUnaMateriaPorNombre("TALLER WEB I");

    assertThat(materiaObtenida.getNombre(), is(equalTo(nombre)));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerTodasLasMaterias() {
    String nombre1 = "Taller Web I";
    String nombre2 = "Base de Datos II";
    String nombre3 = "Programación Web I";
    String nombre4 = "Visualización e Interfaces";

    // preparacion
    Materia materia1 = this.dadoQueTengoUnaMateria(nombre1);
    Materia materia2 = this.dadoQueTengoUnaMateria(nombre2);
    Materia materia3 = this.dadoQueTengoUnaMateria(nombre3);
    Materia materia4 = this.dadoQueTengoUnaMateria(nombre4);

    // ejecucion
    this.cuandoGuardoUnaMateria(materia1);
    this.cuandoGuardoUnaMateria(materia2);
    this.cuandoGuardoUnaMateria(materia3);
    this.cuandoGuardoUnaMateria(materia4);

    List<Materia> materiasObtenidas = this.listaDeMaterias();

    // validacion
    assertThat(materiasObtenidas.size(), is(equalTo(4)));

    assertThat(materiasObtenidas, hasItem(materia1));
    assertThat(materiasObtenidas, hasItem(materia2));
    assertThat(materiasObtenidas, hasItem(materia3));
    assertThat(materiasObtenidas, hasItem(materia4));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverNullSiUnaMateriaEsEliminada() {
    String nombre = "Taller Web I";
    // preparacion
    Materia materia = this.dadoQueTengoUnaMateria(nombre);

    this.dadoQueExisteLaMateria(materia);

    repositorioMateria.eliminar(materia); //cuando se elimina la materia de la BD no significa que se vuelva null sino que la variable sigue apuntando a una direccion de la memoria

    Materia materiaObtenida = repositorioMateria.buscarPorNombre(nombre);

    entoncesLaMateriaObtenidaEsNull(materiaObtenida);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaModificarLaMateria() {
    String nombreOriginal = "Taller Web I";
    String nombreNuevo = "Taller Web Avanzado";

    Materia materia = this.dadoQueTengoUnaMateria(nombreOriginal);

    this.dadoQueExisteLaMateria(materia);

    Integer id = materia.getId();

    materia.setNombre(nombreNuevo);

    // ejecucion
    this.cuandoModificoUnaMateria(materia);

    // validacion
    Materia materiaModificada = this.sessionFactory.getCurrentSession().find(Materia.class, id);

    assertThat(materiaModificada.getNombre(), is(equalTo(nombreNuevo)));
  }
}
