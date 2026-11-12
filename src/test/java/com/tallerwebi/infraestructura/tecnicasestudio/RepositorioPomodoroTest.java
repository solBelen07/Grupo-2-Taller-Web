package com.tallerwebi.infraestructura.tecnicasestudio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.tecnicasestudio.Estado;
import com.tallerwebi.dominio.tecnicasestudio.PlanDeVuelo;
import com.tallerwebi.dominio.tecnicasestudio.Pomodoro;
import com.tallerwebi.dominio.tecnicasestudio.RepositorioPomodoro;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
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
public class RepositorioPomodoroTest {

  private static final LocalDateTime INICIO = LocalDateTime.of(2026, 9, 30, 10, 0, 0);
  private static final LocalDate DIA = LocalDate.of(2026, 11, 10);

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioPomodoro repositorioPomodoro;

  @BeforeEach
  public void init() {
    this.repositorioPomodoro = new RepositorioPomodoroImpl(this.sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarYRecuperarUnPomodoroPorId() {
    Usuario usuario = this.dadoQueExisteElUsuario("ana@test.com");
    Pomodoro pomodoro = this.crearPomodoro(usuario, Estado.EN_CURSO, 25);
    pomodoro.setFechaInicio(INICIO);

    this.repositorioPomodoro.guardar(pomodoro);
    this.forzarLecturaDesdeLaBaseDeDatos();

    Optional<Pomodoro> recuperado = this.repositorioPomodoro.buscarPorId(pomodoro.getId());

    assertThat(recuperado.isPresent(), is(true));
    assertThat(recuperado.get().getId(), equalTo(pomodoro.getId()));
    assertThat(recuperado.get().getUsuario().getId(), equalTo(usuario.getId()));
    assertThat(recuperado.get().getEstado(), equalTo(Estado.EN_CURSO));
    assertThat(recuperado.get().getDuracionMinutos(), equalTo(25));
    assertThat(recuperado.get().getFechaInicio(), equalTo(INICIO));
    assertThat(recuperado.get().getPlan().getMateria(), equalTo("Taller Web I"));
    assertThat(recuperado.get().getPlan().getOrigen(), equalTo("ARG"));
    assertThat(recuperado.get().getPlan().getDestino(), equalTo("ESP"));
    assertThat(recuperado.get().getPlan().getObjetivo(), equalTo("Repasar patrones"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverVacioAlBuscarUnPomodoroInexistente() {
    Optional<Pomodoro> recuperado = this.repositorioPomodoro.buscarPorId(9999L);

    assertThat(recuperado.isPresent(), is(false));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaActualizarElEstadoYLasFechasDeUnPomodoro() {
    Usuario usuario = this.dadoQueExisteElUsuario("ana@test.com");
    Pomodoro pomodoro = this.dadoQueExisteElPomodoro(usuario, Estado.EN_CURSO, 25);

    pomodoro.setEstado(Estado.PAUSADO);
    pomodoro.setFechaPausa(INICIO.plusMinutes(5));
    this.repositorioPomodoro.actualizar(pomodoro);
    this.forzarLecturaDesdeLaBaseDeDatos();

    Pomodoro recuperado = this.repositorioPomodoro.buscarPorId(pomodoro.getId()).orElseThrow();
    assertThat(recuperado.getEstado(), equalTo(Estado.PAUSADO));
    assertThat(recuperado.getFechaPausa(), equalTo(INICIO.plusMinutes(5)));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaLanzarExcepcionAlActualizarUnPomodoroInexistente() {
    Usuario usuario = this.dadoQueExisteElUsuario("ana@test.com");
    Pomodoro noPersistido = this.crearPomodoro(usuario, Estado.EN_CURSO, 25);
    noPersistido.setId(9999L);

    assertThrows(RuntimeException.class, () -> this.repositorioPomodoro.actualizar(noPersistido));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarLaSesionEnCursoDeUnUsuarioEspecifico() {
    Usuario ana = this.dadoQueExisteElUsuario("ana@test.com");
    Usuario beto = this.dadoQueExisteElUsuario("beto@test.com");
    Pomodoro sesionDeAna = this.dadoQueExisteElPomodoro(ana, Estado.EN_CURSO, 25);
    this.dadoQueExisteElPomodoro(beto, Estado.EN_CURSO, 45);
    this.forzarLecturaDesdeLaBaseDeDatos();

    Optional<Pomodoro> encontrada =
      this.repositorioPomodoro.buscarPorUsuarioYEstado(ana.getId(), Estado.EN_CURSO);

    assertThat(encontrada.isPresent(), is(true));
    assertThat(encontrada.get().getId(), equalTo(sesionDeAna.getId()));
    assertThat(encontrada.get().getUsuario().getId(), equalTo(ana.getId()));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaEncontrarSesionEnCursoSiElUsuarioSoloTieneSesionesEnOtroEstado() {
    Usuario ana = this.dadoQueExisteElUsuario("ana@test.com");
    this.dadoQueExisteElPomodoro(ana, Estado.COMPLETADO, 25);
    this.dadoQueExisteElPomodoro(ana, Estado.CANCELADO, 25);
    this.dadoQueExisteElPomodoro(ana, Estado.PAUSADO, 25);

    Optional<Pomodoro> encontrada =
      this.repositorioPomodoro.buscarPorUsuarioYEstado(ana.getId(), Estado.EN_CURSO);

    assertThat(encontrada.isPresent(), is(false));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaEncontrarLaSesionEnCursoDeOtroUsuario() {
    Usuario ana = this.dadoQueExisteElUsuario("ana@test.com");
    Usuario beto = this.dadoQueExisteElUsuario("beto@test.com");
    this.dadoQueExisteElPomodoro(beto, Estado.EN_CURSO, 25);

    Optional<Pomodoro> encontrada =
      this.repositorioPomodoro.buscarPorUsuarioYEstado(ana.getId(), Estado.EN_CURSO);

    assertThat(encontrada.isPresent(), is(false));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaCalcularElTotalDeMinutosCompletadosSoloDelUsuarioIndicado() {
    Usuario ana = this.dadoQueExisteElUsuario("ana@test.com");
    Usuario beto = this.dadoQueExisteElUsuario("beto@test.com");
    this.dadoQueExisteElPomodoro(ana, Estado.COMPLETADO, 25);
    this.dadoQueExisteElPomodoro(ana, Estado.COMPLETADO, 45);
    this.dadoQueExisteElPomodoro(ana, Estado.CANCELADO, 60);
    this.dadoQueExisteElPomodoro(ana, Estado.EN_CURSO, 60);
    this.dadoQueExisteElPomodoro(beto, Estado.COMPLETADO, 60);

    long totalAna = this.repositorioPomodoro.calcularMinutosCompletados(ana.getId());
    long totalBeto = this.repositorioPomodoro.calcularMinutosCompletados(beto.getId());

    assertThat(totalAna, equalTo(70L));
    assertThat(totalBeto, equalTo(60L));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverCeroMinutosCompletadosSiElUsuarioNoTieneSesionesCompletadas() {
    Usuario ana = this.dadoQueExisteElUsuario("ana@test.com");
    this.dadoQueExisteElPomodoro(ana, Estado.CANCELADO, 25);

    assertThat(this.repositorioPomodoro.calcularMinutosCompletados(ana.getId()), equalTo(0L));
  }

  private Usuario dadoQueExisteElUsuario(String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("1234");
    this.sessionFactory.getCurrentSession().persist(usuario);
    return usuario;
  }

  private Pomodoro crearPomodoro(Usuario usuario, Estado estado, int duracionMinutos) {
    Pomodoro pomodoro = new Pomodoro();
    pomodoro.setUsuario(usuario);
    pomodoro.setEstado(estado);
    pomodoro.setDuracionMinutos(duracionMinutos);
    pomodoro.setFechaInicio(INICIO);
    pomodoro.setPlan(new PlanDeVuelo("Taller Web I", "ARG", "ESP", "Repasar patrones"));
    return pomodoro;
  }

  private Pomodoro dadoQueExisteElPomodoro(Usuario usuario, Estado estado, int duracionMinutos) {
    Pomodoro pomodoro = this.crearPomodoro(usuario, estado, duracionMinutos);
    this.sessionFactory.getCurrentSession().persist(pomodoro);
    return pomodoro;
  }

  private void forzarLecturaDesdeLaBaseDeDatos() {
    this.sessionFactory.getCurrentSession().flush();
    this.sessionFactory.getCurrentSession().clear();
  }

  // ---------- pomodoro completado en una fecha (racha) ----------

  @Test
  @Transactional
  @Rollback
  public void deberiaContarElDiaSiTerminoUnPomodoroEseDia() {
    Usuario ana = this.dadoQueExisteElUsuario("ana@test.com");
    this.dadoQueTerminoUnPomodoro(ana, Estado.COMPLETADO, DIA.atTime(15, 30));

    boolean completoUnPomodoroEseDia = this.cuandoConsultoSiCompletoUnPomodoroEl(ana, DIA);

    assertTrue(completoUnPomodoroEseDia, "Terminó un pomodoro a las 15:30, debería contar");
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaContarUnPomodoroQueTerminaJustoALaMedianoche() {
    Usuario ana = this.dadoQueExisteElUsuario("ana@test.com");
    this.dadoQueTerminoUnPomodoro(ana, Estado.COMPLETADO, DIA.atStartOfDay());

    boolean completoUnPomodoroEseDia = this.cuandoConsultoSiCompletoUnPomodoroEl(ana, DIA);

    assertTrue(completoUnPomodoroEseDia, "Las 00:00 son el comienzo del día, debería contar");
  }

  @Test
  @Transactional
  @Rollback
  public void unPomodoroQuePasaLaMedianocheCuentaParaElDiaEnQueTermina() {
    // Ana estudia del 10/11 a las 23:50 al 11/11 a las 00:20.
    Usuario ana = this.dadoQueExisteElUsuario("ana@test.com");
    LocalDate diaQueEmpezo = DIA;
    LocalDate diaQueTermino = DIA.plusDays(1);
    this.dadoQueEstudioUnPomodoroEntre(
        ana,
        diaQueEmpezo.atTime(23, 50),
        diaQueTermino.atTime(0, 20)
      );

    boolean cuentaElDiaQueEmpezo = this.cuandoConsultoSiCompletoUnPomodoroEl(ana, diaQueEmpezo);
    boolean cuentaElDiaQueTermino = this.cuandoConsultoSiCompletoUnPomodoroEl(ana, diaQueTermino);

    assertTrue(cuentaElDiaQueTermino, "Terminó el 11/11, debería contar para ese día");
    assertFalse(cuentaElDiaQueEmpezo, "Se cuenta por la hora en que terminó, no por la de inicio");
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaContarUnPomodoroCancelado() {
    Usuario ana = this.dadoQueExisteElUsuario("ana@test.com");
    this.dadoQueTerminoUnPomodoro(ana, Estado.CANCELADO, DIA.atTime(15, 30));

    boolean completoUnPomodoroEseDia = this.cuandoConsultoSiCompletoUnPomodoroEl(ana, DIA);

    assertFalse(completoUnPomodoroEseDia, "Un pomodoro cancelado no debería contar");
  }

  private boolean cuandoConsultoSiCompletoUnPomodoroEl(Usuario usuario, LocalDate dia) {
    return this.repositorioPomodoro.existePomodoroCompletadoEnFecha(usuario.getId(), dia);
  }

  private void dadoQueEstudioUnPomodoroEntre(
    Usuario usuario,
    LocalDateTime horaDeInicio,
    LocalDateTime horaDeFin
  ) {
    Pomodoro pomodoro = this.crearPomodoro(usuario, Estado.COMPLETADO, 30);
    pomodoro.setFechaInicio(horaDeInicio);
    pomodoro.setFechaFin(horaDeFin);
    this.sessionFactory.getCurrentSession().persist(pomodoro);
  }

  private void dadoQueTerminoUnPomodoro(Usuario usuario, Estado estado, LocalDateTime horaDeFin) {
    Pomodoro pomodoro = this.crearPomodoro(usuario, estado, 25);
    pomodoro.setFechaFin(horaDeFin);
    this.sessionFactory.getCurrentSession().persist(pomodoro);
  }
}
