package com.tallerwebi.infraestructura.sesionEstudio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    Materia materia = dadoQueExisteUnaMateria();

    Parcial parcial = dadoQueExisteUnParcial(materia);

    SesionEstudio sesionEstudio = dadoQueTengoUnaSesionDeEstudio(
      parcial,
      "Spring MVC",
      LocalDate.of(2026, 10, 10),
      2
    );

    repositorioSesionEstudio.guardar(sesionEstudio);

    assertNotNull(sesionEstudio.getId());
  }

  @Test
  public void queSePuedaBuscarUnaSesionDeEstudioPorId() {
    Materia materia = dadoQueExisteUnaMateria();

    Parcial parcial = dadoQueExisteUnParcial(materia);

    SesionEstudio sesionEstudio = dadoQueExisteUnaSesionDeEstudio(parcial);

    SesionEstudio sesionBuscada = repositorioSesionEstudio.buscarPorId(sesionEstudio.getId());

    assertNotNull(sesionBuscada);

    assertEquals(sesionEstudio.getId(), sesionBuscada.getId());

    assertEquals("Spring MVC", sesionBuscada.getTema());
  }

  @Test
  public void queSePuedaModificarUnaSesionDeEstudio() {
    Materia materia = dadoQueExisteUnaMateria();

    Parcial parcial = dadoQueExisteUnParcial(materia);

    SesionEstudio sesionEstudio = dadoQueExisteUnaSesionDeEstudio(parcial);

    sesionEstudio.setTema("Spring MVC y Thymeleaf");
    sesionEstudio.setCantidadHoras(4);
    sesionEstudio.setCompletada(true);

    repositorioSesionEstudio.modificar(sesionEstudio);

    sessionFactory.getCurrentSession().flush();

    sessionFactory.getCurrentSession().clear();

    SesionEstudio sesionModificada = repositorioSesionEstudio.buscarPorId(sesionEstudio.getId());

    assertEquals("Spring MVC y Thymeleaf", sesionModificada.getTema());

    assertEquals(4, sesionModificada.getCantidadHoras());

    assertEquals(true, sesionModificada.getCompletada());
  }

  @Test
  public void queSePuedaEliminarUnaSesionDeEstudio() {
    Materia materia = dadoQueExisteUnaMateria();

    Parcial parcial = dadoQueExisteUnParcial(materia);

    SesionEstudio sesionEstudio = dadoQueExisteUnaSesionDeEstudio(parcial);

    Integer idSesion = sesionEstudio.getId();

    repositorioSesionEstudio.eliminar(sesionEstudio);

    sessionFactory.getCurrentSession().flush();

    sessionFactory.getCurrentSession().clear();

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

  @Test
  public void buscarUnaSesionInexistenteDeberiaDevolverNull() {
    assertNull(repositorioSesionEstudio.buscarPorId(-1));
  }

  @Test
  public void eliminarPorMateriaIdDeberiaEliminarSoloLasSesionesDeEsaMateria() {
    Materia materiaA = dadoQueExisteUnaMateria();
    Materia materiaB = dadoQueExisteUnaMateria();
    Parcial primerParcialA = dadoQueExisteUnParcial(materiaA);
    Parcial segundoParcialA = dadoQueExisteUnParcial(materiaA);
    Parcial parcialB = dadoQueExisteUnParcial(materiaB);

    SesionEstudio sesionA1 = dadoQueExisteUnaSesionDeEstudio(primerParcialA);
    SesionEstudio sesionA2 = dadoQueExisteUnaSesionDeEstudio(segundoParcialA);
    SesionEstudio sesionB = dadoQueExisteUnaSesionDeEstudio(parcialB);

    Integer idA1 = sesionA1.getId();
    Integer idA2 = sesionA2.getId();
    Integer idB = sesionB.getId();

    sessionFactory.getCurrentSession().flush();
    repositorioSesionEstudio.eliminarPorMateriaId(materiaA.getId());
    sessionFactory.getCurrentSession().clear();

    assertNull(repositorioSesionEstudio.buscarPorId(idA1));
    assertNull(repositorioSesionEstudio.buscarPorId(idA2));
    assertNotNull(repositorioSesionEstudio.buscarPorId(idB));
  }

  @Test
  public void eliminarPorMateriaIdSinSesionesNoDeberiaEliminarSesionesDeOtraMateria() {
    Materia materiaSinSesiones = dadoQueExisteUnaMateria();
    Materia otraMateria = dadoQueExisteUnaMateria();
    Parcial parcial = dadoQueExisteUnParcial(otraMateria);
    SesionEstudio sesion = dadoQueExisteUnaSesionDeEstudio(parcial);
    Integer idSesion = sesion.getId();

    sessionFactory.getCurrentSession().flush();
    repositorioSesionEstudio.eliminarPorMateriaId(materiaSinSesiones.getId());
    sessionFactory.getCurrentSession().clear();

    assertNotNull(repositorioSesionEstudio.buscarPorId(idSesion));
  }

  @Test
  public void unaSesionConEstadoNuloPuedeSerLaProximaSesion() {
    Usuario usuario = dadoQueExisteUnUsuario("proxima-nula@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate manana = LocalDate.now().plusDays(1);

    SesionEstudio sinEstado = guardarSesionConDatos(
      parcial,
      usuario,
      "Sin estado",
      manana,
      2,
      null,
      null
    );
    guardarSesion(parcial, usuario, "Pendiente posterior", manana.plusDays(1), false);

    SesionEstudio proxima = repositorioSesionEstudio.buscarProximaSesionEstudio(usuario.getId());

    assertNotNull(proxima);
    assertEquals(sinEstado.getId(), proxima.getId());
  }

  @Test
  public void siTodasLasSesionesFuturasEstanCompletadasNoHayProximaSesion() {
    Usuario usuario = dadoQueExisteUnUsuario("todas-completas@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    guardarSesion(parcial, usuario, "Completa hoy", LocalDate.now(), true);
    guardarSesion(parcial, usuario, "Completa mañana", LocalDate.now().plusDays(1), true);

    assertNull(repositorioSesionEstudio.buscarProximaSesionEstudio(usuario.getId()));
  }

  @Test
  public void laProximaSesionDebeSerDelUsuarioSolicitadoAunqueOtroTengaUnaAnterior() {
    Usuario usuario = dadoQueExisteUnUsuario("proximo-propio@prueba.com");
    Usuario otroUsuario = dadoQueExisteUnUsuario("proximo-ajeno@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    guardarSesion(parcial, otroUsuario, "Del otro", LocalDate.now(), false);
    guardarSesion(parcial, usuario, "Propia", LocalDate.now().plusDays(2), false);

    assertEquals(
      "Propia",
      repositorioSesionEstudio.buscarProximaSesionEstudio(usuario.getId()).getTema()
    );
  }

  @Test
  public void calcularHorasCompletadasDeberiaSumarSoloLasCompletadasDelUsuario() {
    Usuario usuario = dadoQueExisteUnUsuario("horas-propias@prueba.com");
    Usuario otroUsuario = dadoQueExisteUnUsuario("horas-otro@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate hoy = LocalDate.now();

    guardarSesionConDatos(parcial, usuario, "Tema 1", hoy, 2, true, hoy);
    guardarSesionConDatos(parcial, usuario, "Tema 2", hoy, 3, true, hoy);
    guardarSesionConDatos(parcial, usuario, "Pendiente", hoy, 7, false, null);
    guardarSesionConDatos(parcial, usuario, "Estado nulo", hoy, 6, null, null);
    guardarSesionConDatos(parcial, otroUsuario, "De otro", hoy, 9, true, hoy);

    assertEquals(5L, repositorioSesionEstudio.calcularHorasCompletadas(usuario.getId()));
  }

  @Test
  public void calcularHorasCompletadasSinSesionesDeberiaDevolverCero() {
    Usuario usuario = dadoQueExisteUnUsuario("sin-horas@prueba.com");

    assertEquals(0L, repositorioSesionEstudio.calcularHorasCompletadas(usuario.getId()));
  }

  @Test
  public void calcularHorasCompletadasConSoloPendientesDeberiaDevolverCero() {
    Usuario usuario = dadoQueExisteUnUsuario("solo-pendientes@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate hoy = LocalDate.now();
    guardarSesionConDatos(parcial, usuario, "Pendiente", hoy, 4, false, null);
    guardarSesionConDatos(parcial, usuario, "Sin estado", hoy, 5, null, null);

    assertEquals(0L, repositorioSesionEstudio.calcularHorasCompletadas(usuario.getId()));
  }

  @Test
  public void calcularHorasCompletadasDeberiaIgnorarHorasNulas() {
    Usuario usuario = dadoQueExisteUnUsuario("horas-nulas@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate hoy = LocalDate.now();
    guardarSesionConDatos(parcial, usuario, "Con horas", hoy, 2, true, hoy);
    guardarSesionConDatos(parcial, usuario, "Sin horas", hoy, null, true, hoy);

    assertEquals(2L, repositorioSesionEstudio.calcularHorasCompletadas(usuario.getId()));
  }

  @Test
  public void calcularHorasCompletadasDeOtroUsuarioNoDeberiaSumarse() {
    Usuario usuario = dadoQueExisteUnUsuario("usuario-sin-horas@prueba.com");
    Usuario otro = dadoQueExisteUnUsuario("otro-con-horas@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate hoy = LocalDate.now();
    guardarSesionConDatos(parcial, otro, "Otro usuario", hoy, 8, true, hoy);

    assertEquals(0L, repositorioSesionEstudio.calcularHorasCompletadas(usuario.getId()));
  }

  @Test
  public void existeSesionCompletadaEnFechaDeberiaDevolverTrueSiCompletoEseDia() {
    Usuario usuario = dadoQueExisteUnUsuario("racha-hoy@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate fecha = LocalDate.now();
    guardarSesionConDatos(parcial, usuario, "Terminado", fecha, 2, true, fecha);

    assertTrue(repositorioSesionEstudio.existeSesionCompletadaEnFecha(usuario.getId(), fecha));
  }

  @Test
  public void existeSesionCompletadaEnFechaDeberiaDevolverFalseSinSesiones() {
    Usuario usuario = dadoQueExisteUnUsuario("racha-sin-sesiones@prueba.com");

    assertFalse(
      repositorioSesionEstudio.existeSesionCompletadaEnFecha(usuario.getId(), LocalDate.now())
    );
  }

  @Test
  public void unaSesionPendienteNoDeberiaContarComoCompletadaEnFecha() {
    Usuario usuario = dadoQueExisteUnUsuario("racha-pendiente@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate fecha = LocalDate.now();
    guardarSesionConDatos(parcial, usuario, "Pendiente", fecha, 2, false, fecha);

    assertFalse(repositorioSesionEstudio.existeSesionCompletadaEnFecha(usuario.getId(), fecha));
  }

  @Test
  public void unaSesionDeOtroUsuarioNoDeberiaContarComoCompletadaEnFecha() {
    Usuario usuario = dadoQueExisteUnUsuario("racha-usuario@prueba.com");
    Usuario otro = dadoQueExisteUnUsuario("racha-otro@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate fecha = LocalDate.now();
    guardarSesionConDatos(parcial, otro, "De otro", fecha, 2, true, fecha);

    assertFalse(repositorioSesionEstudio.existeSesionCompletadaEnFecha(usuario.getId(), fecha));
  }

  @Test
  public void unaSesionCompletadaEnOtraFechaNoDeberiaContarHoy() {
    Usuario usuario = dadoQueExisteUnUsuario("racha-ayer@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate hoy = LocalDate.now();
    guardarSesionConDatos(parcial, usuario, "Ayer", hoy.minusDays(2), 2, true, hoy.minusDays(1));

    assertFalse(repositorioSesionEstudio.existeSesionCompletadaEnFecha(usuario.getId(), hoy));
  }

  @Test
  public void unaSesionSinFechaCompletadoNoDeberiaContarEnLaRacha() {
    Usuario usuario = dadoQueExisteUnUsuario("racha-sin-fecha@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate hoy = LocalDate.now();
    guardarSesionConDatos(parcial, usuario, "Sin fecha", hoy, 2, true, null);

    assertFalse(repositorioSesionEstudio.existeSesionCompletadaEnFecha(usuario.getId(), hoy));
  }

  @Test
  public void debeUsarFechaCompletadoYNoFechaPlanificadaParaLaRacha() {
    Usuario usuario = dadoQueExisteUnUsuario("racha-fecha-real@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate hoy = LocalDate.now();
    guardarSesionConDatos(parcial, usuario, "Completada hoy", hoy.minusDays(4), 2, true, hoy);

    assertTrue(repositorioSesionEstudio.existeSesionCompletadaEnFecha(usuario.getId(), hoy));
    assertFalse(
      repositorioSesionEstudio.existeSesionCompletadaEnFecha(usuario.getId(), hoy.minusDays(4))
    );
  }

  @Test
  public void variasSesionesCompletadasEnLaFechaDeberianDevolverTrue() {
    Usuario usuario = dadoQueExisteUnUsuario("racha-varias@prueba.com");
    Parcial parcial = dadoQueExisteUnParcial(dadoQueExisteUnaMateria());
    LocalDate fecha = LocalDate.now();
    guardarSesionConDatos(parcial, usuario, "Tema 1", fecha, 1, true, fecha);
    guardarSesionConDatos(parcial, usuario, "Tema 2", fecha, 1, true, fecha);

    assertTrue(repositorioSesionEstudio.existeSesionCompletadaEnFecha(usuario.getId(), fecha));
  }

  private SesionEstudio guardarSesionConDatos(
    Parcial parcial,
    Usuario usuario,
    String tema,
    LocalDate fecha,
    Integer horas,
    Boolean completada,
    LocalDate fechaCompletado
  ) {
    SesionEstudio sesion = dadoQueTengoUnaSesionDeEstudio(parcial, tema, fecha, horas);
    sesion.setUsuario(usuario);
    sesion.setCompletada(completada);
    sesion.setFechaCompletado(fechaCompletado);
    repositorioSesionEstudio.guardar(sesion);
    return sesion;
  }
}
