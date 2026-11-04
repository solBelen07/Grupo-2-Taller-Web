package com.tallerwebi.infraestructura.sesionEstudio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.parcial.Parcial;
import com.tallerwebi.dominio.sesionEstudio.RepositorioSesionEstudio;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import com.tallerwebi.infraestructura.SesionEstudio.RepositorioSesionEstudioImpl;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import java.time.LocalDate;
import java.time.LocalTime;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
@Transactional
@Rollback
public class RepositorioSesionEstudioTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioSesionEstudio repositorioSesionEstudio;

  @BeforeEach
  public void init() {
    repositorioSesionEstudio = new RepositorioSesionEstudioImpl(sessionFactory);
  }

  @Test
  public void queSePuedaGuardarUnaSesionDeEstudio() {
    // preparacion
    Materia materia = dadoQueExisteUnaMateria();

    Parcial parcial = dadoQueExisteUnParcial(materia);

    SesionEstudio sesionEstudio = dadoQueTengoUnaSesionDeEstudio(
      parcial,
      "Spring MVC",
      LocalDate.of(2026, 10, 10),
      2
    );

    // ejecucion
    repositorioSesionEstudio.guardar(sesionEstudio);

    // validacion
    assertNotNull(sesionEstudio.getId());
  }

  @Test
  public void queSePuedaBuscarUnaSesionDeEstudioPorId() {
    // preparacion
    Materia materia = dadoQueExisteUnaMateria();

    Parcial parcial = dadoQueExisteUnParcial(materia);

    SesionEstudio sesionEstudio = dadoQueExisteUnaSesionDeEstudio(parcial);

    // ejecucion
    SesionEstudio sesionBuscada = repositorioSesionEstudio.buscarPorId(sesionEstudio.getId());

    // validacion
    assertNotNull(sesionBuscada);

    assertEquals(sesionEstudio.getId(), sesionBuscada.getId());

    assertEquals("Spring MVC", sesionBuscada.getTema());
  }

  @Test
  public void queSePuedaModificarUnaSesionDeEstudio() {
    // preparacion
    Materia materia = dadoQueExisteUnaMateria();

    Parcial parcial = dadoQueExisteUnParcial(materia);

    SesionEstudio sesionEstudio = dadoQueExisteUnaSesionDeEstudio(parcial);

    sesionEstudio.setTema("Spring MVC y Thymeleaf");
    sesionEstudio.setCantidadHoras(4);
    sesionEstudio.setCompletada(true);

    // ejecucion
    repositorioSesionEstudio.modificar(sesionEstudio);

    /*
     * Forzamos a Hibernate a ejecutar los cambios
     * y después limpiamos la sesión para asegurarnos
     * de volver a consultar desde la base de datos.
     */
    sessionFactory.getCurrentSession().flush();

    sessionFactory.getCurrentSession().clear();

    // validacion
    SesionEstudio sesionModificada = repositorioSesionEstudio.buscarPorId(sesionEstudio.getId());

    assertEquals("Spring MVC y Thymeleaf", sesionModificada.getTema());

    assertEquals(4, sesionModificada.getCantidadHoras());

    assertEquals(true, sesionModificada.getCompletada());
  }

  @Test
  public void queSePuedaEliminarUnaSesionDeEstudio() {
    // preparacion
    Materia materia = dadoQueExisteUnaMateria();

    Parcial parcial = dadoQueExisteUnParcial(materia);

    SesionEstudio sesionEstudio = dadoQueExisteUnaSesionDeEstudio(parcial);

    Integer idSesion = sesionEstudio.getId();

    // ejecucion
    repositorioSesionEstudio.eliminar(sesionEstudio);

    sessionFactory.getCurrentSession().flush();

    sessionFactory.getCurrentSession().clear();

    // validacion
    SesionEstudio sesionEliminada = repositorioSesionEstudio.buscarPorId(idSesion);

    assertNull(sesionEliminada);
  }

  private Materia dadoQueExisteUnaMateria() {
    Materia materia = new Materia();

    materia.setNombre("Taller Web I");
    materia.setDescripcion("Materia de desarrollo web");
    materia.setDocente("Profesor");

    Session session = sessionFactory.getCurrentSession();

    session.persist(materia);

    return materia;
  }

  private Parcial dadoQueExisteUnParcial(Materia materia) {
    Parcial parcial = new Parcial();

    parcial.setNombre("Primer Parcial");
    parcial.setMateria(materia);

    parcial.setFecha(LocalDate.of(2026, 10, 20));

    parcial.setHorario(LocalTime.of(18, 0));

    parcial.setCantidadDiasEstudio(5);
    parcial.setHorasPorDia(2);

    Session session = sessionFactory.getCurrentSession();

    session.persist(parcial);

    return parcial;
  }

  private SesionEstudio dadoQueTengoUnaSesionDeEstudio(
    Parcial parcial,
    String tema,
    LocalDate fecha,
    Integer cantidadHoras
  ) {
    SesionEstudio sesionEstudio = new SesionEstudio();

    sesionEstudio.setParcial(parcial);
    sesionEstudio.setTema(tema);
    sesionEstudio.setFecha(fecha);
    sesionEstudio.setCantidadHoras(cantidadHoras);

    return sesionEstudio;
  }

  private SesionEstudio dadoQueExisteUnaSesionDeEstudio(Parcial parcial) {
    SesionEstudio sesionEstudio = dadoQueTengoUnaSesionDeEstudio(
      parcial,
      "Spring MVC",
      LocalDate.of(2026, 10, 15),
      2
    );

    repositorioSesionEstudio.guardar(sesionEstudio);

    return sesionEstudio;
  }

  // próxima sesion

  @Test
  public void laProximaSesionEsLaPrimeraSinCompletarDeHoyEnAdelanteDelUsuario() {
    Usuario usuario = dadoQueExisteUnUsuario("dsh02@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate hoy = LocalDate.now();
    guardarSesion(parcial, usuario, "Pasada", hoy.minusDays(1), false);
    guardarSesion(parcial, usuario, "Completada", hoy.plusDays(1), true);
    guardarSesion(parcial, usuario, "Lejana", hoy.plusDays(5), false);
    guardarSesion(parcial, usuario, "La proxima", hoy.plusDays(2), false);

    SesionEstudio proxima = repositorioSesionEstudio.buscarProximaSesionEstudio(usuario.getId());

    assertEquals("La proxima", proxima.getTema());
  }

  @Test
  public void unaSesionDeHoySinCompletarCuentaComoProxima() {
    Usuario usuario = dadoQueExisteUnUsuario("dsh02@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    guardarSesion(parcial, usuario, "Hoy", LocalDate.now(), false);
    guardarSesion(parcial, usuario, "Mañana", LocalDate.now().plusDays(1), false);

    assertEquals(
      "Hoy",
      repositorioSesionEstudio.buscarProximaSesionEstudio(usuario.getId()).getTema()
    );
  }

  @Test
  public void conVariasSesionesElMismoDiaNoFallaYDevuelveUna() {
    Usuario usuario = dadoQueExisteUnUsuario("dsh02@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate manana = LocalDate.now().plusDays(1);
    guardarSesion(parcial, usuario, "Primera", manana, false);
    guardarSesion(parcial, usuario, "Segunda", manana, false);

    assertEquals(
      "Primera",
      repositorioSesionEstudio.buscarProximaSesionEstudio(usuario.getId()).getTema()
    );
  }

  @Test
  public void noDeberiaDevolverSesionesDeOtroUsuario() {
    Usuario usuario = dadoQueExisteUnUsuario("dsh02@prueba.com");
    Usuario otro = dadoQueExisteUnUsuario("otro-dsh02@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    guardarSesion(parcial, otro, "Del otro", LocalDate.now().plusDays(1), false);

    assertNull(repositorioSesionEstudio.buscarProximaSesionEstudio(usuario.getId()));
  }

  @Test
  public void siNoHaySesionesFuturasDeberiaDevolverNull() {
    Usuario usuario = dadoQueExisteUnUsuario("dsh02@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    guardarSesion(parcial, usuario, "Pasada", LocalDate.now().minusDays(3), false);

    assertNull(repositorioSesionEstudio.buscarProximaSesionEstudio(usuario.getId()));
  }

  private Usuario dadoQueExisteUnUsuario(String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private void guardarSesion(
    Parcial parcial,
    Usuario usuario,
    String tema,
    LocalDate fecha,
    boolean completada
  ) {
    SesionEstudio sesion = dadoQueTengoUnaSesionDeEstudio(parcial, tema, fecha, 2);
    sesion.setUsuario(usuario);
    sesion.setCompletada(completada);
    repositorioSesionEstudio.guardar(sesion);
  }
}
