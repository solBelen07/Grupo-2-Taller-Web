package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.tallerwebi.dominio.Evento;
import com.tallerwebi.dominio.RepositorioEvento;
import com.tallerwebi.dominio.TipoEvento;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
@Transactional
public class RepositorioEventoTest {

  private static final Long USUARIO = 1L;
  private static final Long OTRO_USUARIO = 2L;
  private static final LocalDate LUNES = LocalDate.of(2026, 9, 28);

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioEvento repositorioEvento;
  private Materia analisis;
  private Materia fisica;

  @BeforeEach
  public void init() {
    this.repositorioEvento = new RepositorioEventoImpl(this.sessionFactory);
    this.analisis = dadoQueExisteLaMateria("Análisis II");
    this.fisica = dadoQueExisteLaMateria("Física II");
  }

  private Materia dadoQueExisteLaMateria(String nombre) {
    Materia materia = new Materia();
    materia.setNombre(nombre);
    materia.setColor("#2F6FDE");
    this.sessionFactory.getCurrentSession().persist(materia);
    return materia;
  }

  private Evento dadoQueGuardoUnEvento(
    Long usuario,
    Materia materia,
    String titulo,
    LocalDateTime inicio,
    LocalDateTime fin
  ) {
    Evento evento = new Evento(usuario, materia, TipoEvento.CLASE, titulo, inicio, fin);
    this.repositorioEvento.guardar(evento);
    return evento;
  }

  private List<String> titulos(List<Evento> eventos) {
    return eventos.stream().map(Evento::getTitulo).toList();
  }

  private List<Evento> cuandoBuscoLaSemana(Long usuario) {
    this.sessionFactory.getCurrentSession().flush();
    return this.repositorioEvento.buscarEnRango(
        usuario,
        LUNES.atStartOfDay(),
        LUNES.plusDays(7).atStartOfDay()
      );
  }

  @Test
  public void guardarUnEventoLeAsignaUnId() {
    // ejecucion
    Evento evento = dadoQueGuardoUnEvento(
      USUARIO,
      this.analisis,
      "Clase",
      LUNES.atTime(8, 0),
      LUNES.atTime(10, 0)
    );
    this.sessionFactory.getCurrentSession().flush();

    // validacion
    assertThat(evento.getId(), is(notNullValue()));
  }

  @Test
  public void deberiaEncontrarLosEventosDelUsuarioDentroDelRango() {
    // preparacion
    dadoQueGuardoUnEvento(USUARIO, this.analisis, "Clase", LUNES.atTime(8, 0), LUNES.atTime(10, 0));

    // ejecucion
    List<Evento> eventos = cuandoBuscoLaSemana(USUARIO);

    // validacion
    assertThat(titulos(eventos), contains("Clase"));
    assertThat(eventos.get(0).getMateria().getNombre(), is("Análisis II"));
  }

  @Test
  public void noDeberiaTraerEventosDeOtroUsuario() {
    // preparacion
    dadoQueGuardoUnEvento(
      OTRO_USUARIO,
      this.analisis,
      "Ajeno",
      LUNES.atTime(8, 0),
      LUNES.atTime(10, 0)
    );

    // ejecucion y validacion
    assertThat(cuandoBuscoLaSemana(USUARIO), empty());
  }

  @Test
  public void noDeberiaTraerEventosFueraDelRango() {
    // preparacion
    dadoQueGuardoUnEvento(
      USUARIO,
      this.analisis,
      "Antes",
      LUNES.minusDays(3).atTime(8, 0),
      LUNES.minusDays(3).atTime(10, 0)
    );
    dadoQueGuardoUnEvento(
      USUARIO,
      this.analisis,
      "Despues",
      LUNES.plusDays(10).atTime(8, 0),
      LUNES.plusDays(10).atTime(10, 0)
    );

    // ejecucion y validacion
    assertThat(cuandoBuscoLaSemana(USUARIO), empty());
  }

  @Test
  public void deberiaTraerUnEventoQueCruzaElInicioDelRango() {
    // preparacion: empieza el domingo a la noche y termina el lunes a la madrugada
    dadoQueGuardoUnEvento(
      USUARIO,
      this.analisis,
      "Nocturno",
      LUNES.minusDays(1).atTime(23, 0),
      LUNES.atTime(1, 0)
    );

    // ejecucion y validacion
    assertThat(titulos(cuandoBuscoLaSemana(USUARIO)), contains("Nocturno"));
  }

  @Test
  public void noDeberiaTraerUnEventoQueTerminaJustoCuandoEmpiezaElRango() {
    // preparacion
    dadoQueGuardoUnEvento(
      USUARIO,
      this.analisis,
      "Termina a las 00:00",
      LUNES.minusDays(1).atTime(22, 0),
      LUNES.atStartOfDay()
    );

    // ejecucion y validacion
    assertThat(cuandoBuscoLaSemana(USUARIO), empty());
  }

  @Test
  public void noDeberiaTraerUnEventoQueEmpiezaJustoCuandoTerminaElRango() {
    // preparacion
    dadoQueGuardoUnEvento(
      USUARIO,
      this.analisis,
      "Empieza a las 00:00",
      LUNES.plusDays(7).atStartOfDay(),
      LUNES.plusDays(7).atTime(2, 0)
    );

    // ejecucion y validacion
    assertThat(cuandoBuscoLaSemana(USUARIO), empty());
  }

  @Test
  public void deberiaOrdenarLosEventosPorInicio() {
    // preparacion: se guardan en desorden
    dadoQueGuardoUnEvento(
      USUARIO,
      this.analisis,
      "Tarde",
      LUNES.atTime(18, 0),
      LUNES.atTime(20, 0)
    );
    dadoQueGuardoUnEvento(
      USUARIO,
      this.fisica,
      "Temprano",
      LUNES.atTime(8, 0),
      LUNES.atTime(10, 0)
    );
    dadoQueGuardoUnEvento(
      USUARIO,
      this.analisis,
      "Otro dia",
      LUNES.plusDays(2).atTime(9, 0),
      LUNES.plusDays(2).atTime(11, 0)
    );

    // ejecucion y validacion
    assertThat(titulos(cuandoBuscoLaSemana(USUARIO)), contains("Temprano", "Tarde", "Otro dia"));
  }

  @Test
  public void eliminarPorMateriaBorraSoloLosEventosDeEsaMateria() {
    // preparacion
    dadoQueGuardoUnEvento(
      USUARIO,
      this.analisis,
      "De Analisis",
      LUNES.atTime(8, 0),
      LUNES.atTime(10, 0)
    );
    dadoQueGuardoUnEvento(
      USUARIO,
      this.fisica,
      "De Fisica",
      LUNES.atTime(12, 0),
      LUNES.atTime(14, 0)
    );
    this.sessionFactory.getCurrentSession().flush();

    // ejecucion
    this.repositorioEvento.eliminarPorMateriaId(this.analisis.getId());
    this.sessionFactory.getCurrentSession().clear();

    // validacion
    List<Evento> restantes = cuandoBuscoLaSemana(USUARIO);
    assertThat(restantes, hasSize(1));
    assertThat(restantes.get(0).getTitulo(), is("De Fisica"));
  }
}
