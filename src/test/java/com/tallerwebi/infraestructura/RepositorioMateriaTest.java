package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.excepcion.excepcionMateria.MateriaNoEncontrada;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import com.tallerwebi.infraestructura.materia.RepositorioMateriaImpl;
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

  @Test
  @Transactional
  @Rollback
  public void deberiaLanzarMateriaNoEncontradaAlModificarUnaMateriaInexistente() {
    Materia materia = dadoQueTengoUnaMateria("Inexistente");
    materia.setId(-1);

    entoncesSeLanzaUnaMateriaNoEncontrada(materia);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarUnaMateriaPorId() {
    Materia materia = dadoQueTengoUnaMateria("Materia por ID");
    dadoQueExisteLaMateria(materia);
    Integer id = materia.getId();
    prepararLecturaDesdeBaseDeDatos();

    Materia obtenida = repositorioMateria.buscarPorId(id);

    assertThat(obtenida.getId(), is(equalTo(id)));
    entoncesLaMateriaObtenidaEsCorrecta(obtenida, materia);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverNullAlBuscarUnIdInexistente() {
    entoncesLaMateriaObtenidaEsNull(repositorioMateria.buscarPorId(-1));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverNullAlBuscarUnNombreInexistente() {
    entoncesLaMateriaObtenidaEsNull(
      repositorioMateria.buscarPorNombre("Nombre inexistente de prueba")
    );
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerSoloLasMateriasDelUsuario() {
    Usuario usuario = dadoQueExisteUnUsuarioDePrueba("propietario-lista@test.com");
    Usuario otro = dadoQueExisteUnUsuarioDePrueba("otro-lista@test.com");
    Materia propia = dadoQueExisteUnaMateriaDeUsuarios("Propia", usuario);
    Materia compartida = dadoQueExisteUnaMateriaDeUsuarios("Compartida", usuario, otro);
    dadoQueExisteUnaMateriaDeUsuarios("Ajena", otro);
    prepararLecturaDesdeBaseDeDatos();

    List<Materia> obtenidas = repositorioMateria.obtenerPorUsuario(usuario.getId());

    assertThat(obtenidas.size(), is(equalTo(2)));
    List<Integer> ids = obtenidas.stream().map(Materia::getId).toList();
    assertThat(ids, hasItem(propia.getId()));
    assertThat(ids, hasItem(compartida.getId()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaObtenerListaVaciaParaUsuarioSinMaterias() {
    Usuario usuario = dadoQueExisteUnUsuarioDePrueba("sin-materias@test.com");
    Usuario otro = dadoQueExisteUnUsuarioDePrueba("con-materia@test.com");
    dadoQueExisteUnaMateriaDeUsuarios("Ajena", otro);
    prepararLecturaDesdeBaseDeDatos();

    assertThat(repositorioMateria.obtenerPorUsuario(usuario.getId()).isEmpty(), is(true));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarPorNombreYUsuarioIgnorandoMayusculas() {
    Usuario usuario = dadoQueExisteUnUsuarioDePrueba("nombre-propio@test.com");
    Usuario otro = dadoQueExisteUnUsuarioDePrueba("nombre-ajeno@test.com");
    Materia propia = dadoQueExisteUnaMateriaDeUsuarios("Materia Compartida", usuario, otro);
    dadoQueExisteUnaMateriaDeUsuarios("Materia Compartida", otro);
    prepararLecturaDesdeBaseDeDatos();

    Materia obtenida = repositorioMateria.buscarPorNombreYUsuario(
      "MATERIA COMPARTIDA",
      usuario.getId()
    );

    assertThat(obtenida.getId(), is(equalTo(propia.getId())));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaEncontrarPorNombreUnaMateriaDeOtroUsuario() {
    Usuario usuario = dadoQueExisteUnUsuarioDePrueba("consulta-nombre@test.com");
    Usuario otro = dadoQueExisteUnUsuarioDePrueba("duenio-nombre@test.com");
    dadoQueExisteUnaMateriaDeUsuarios("Privada", otro);
    prepararLecturaDesdeBaseDeDatos();

    entoncesLaMateriaObtenidaEsNull(
      repositorioMateria.buscarPorNombreYUsuario("Privada", usuario.getId())
    );
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarPorIdYUsuarioUnaMateriaCompartida() {
    Usuario usuario = dadoQueExisteUnUsuarioDePrueba("id-propio@test.com");
    Usuario otro = dadoQueExisteUnUsuarioDePrueba("id-compartido@test.com");
    Materia materia = dadoQueExisteUnaMateriaDeUsuarios("Compartida por ID", usuario, otro);
    prepararLecturaDesdeBaseDeDatos();

    Materia obtenida = repositorioMateria.buscarPorIdYUsuario(materia.getId(), usuario.getId());

    assertThat(obtenida.getId(), is(equalTo(materia.getId())));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaEncontrarPorIdUnaMateriaDeOtroUsuario() {
    Usuario usuario = dadoQueExisteUnUsuarioDePrueba("consulta-id@test.com");
    Usuario otro = dadoQueExisteUnUsuarioDePrueba("duenio-id@test.com");
    Materia materia = dadoQueExisteUnaMateriaDeUsuarios("Ajena por ID", otro);
    prepararLecturaDesdeBaseDeDatos();

    entoncesLaMateriaObtenidaEsNull(
      repositorioMateria.buscarPorIdYUsuario(materia.getId(), usuario.getId())
    );
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaContarSoloLasMateriasDelUsuario() {
    Usuario usuario = dadoQueExisteUnUsuarioDePrueba("conteo-propio@test.com");
    Usuario otro = dadoQueExisteUnUsuarioDePrueba("conteo-ajeno@test.com");
    dadoQueExisteUnaMateriaDeUsuarios("Propia para conteo", usuario);
    dadoQueExisteUnaMateriaDeUsuarios("Compartida para conteo", usuario, otro);
    dadoQueExisteUnaMateriaDeUsuarios("Ajena para conteo", otro);
    prepararLecturaDesdeBaseDeDatos();

    Long cantidad = repositorioMateria.obtenerCantidadMateriasPorUsuario(usuario.getId());

    assertThat(cantidad, is(equalTo(2L)));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaContarCeroParaUsuarioSinMaterias() {
    Usuario usuario = dadoQueExisteUnUsuarioDePrueba("conteo-vacio@test.com");
    Usuario otro = dadoQueExisteUnUsuarioDePrueba("conteo-otro@test.com");
    dadoQueExisteUnaMateriaDeUsuarios("Solo del otro usuario", otro);
    prepararLecturaDesdeBaseDeDatos();

    assertThat(
      repositorioMateria.obtenerCantidadMateriasPorUsuario(usuario.getId()),
      is(equalTo(0L))
    );
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaModificarUnaMateriaDesvinculadaDeLaSesion() {
    Materia materia = dadoQueTengoUnaMateria("Antes de modificar");
    dadoQueExisteLaMateria(materia);
    Integer id = materia.getId();
    prepararLecturaDesdeBaseDeDatos();
    materia.setNombre("Despues de modificar");

    cuandoModificoUnaMateria(materia);
    prepararLecturaDesdeBaseDeDatos();

    Materia obtenida = sessionFactory.getCurrentSession().find(Materia.class, id);
    assertThat(obtenida.getNombre(), is(equalTo("Despues de modificar")));
  }

  private Usuario dadoQueExisteUnUsuarioDePrueba(String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("password-prueba");
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private Materia dadoQueExisteUnaMateriaDeUsuarios(String nombre, Usuario... usuarios) {
    Materia materia = dadoQueTengoUnaMateria(nombre);
    for (Usuario usuario : usuarios) {
      materia.getUsuarios().add(usuario);
    }
    dadoQueExisteLaMateria(materia);
    return materia;
  }

  private void prepararLecturaDesdeBaseDeDatos() {
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();
  }
}
