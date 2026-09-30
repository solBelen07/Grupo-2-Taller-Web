package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.dominio.materia.Materia;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class EventoTest {

  private static final Long USUARIO = 1L;
  private static final LocalDateTime INICIO = LocalDateTime.of(2026, 9, 28, 18, 0);
  private static final LocalDateTime FIN = LocalDateTime.of(2026, 9, 28, 20, 0);

  private static Materia materia() {
    Materia materia = new Materia();
    materia.setNombre("Bases de Datos");
    materia.setColor("#8A4FD0");
    return materia;
  }

  @Test
  public void elConstructorProtegidoParaHibernateDejaLosCamposSinAsignar() {
    // ejecucion
    Evento evento = new Evento();

    // validacion
    assertThat(evento.getId(), is(nullValue()));
  }

  @Test
  public void deberiaCrearUnEventoValido() {
    // preparacion y ejecucion
    Materia materia = materia();
    Evento evento = new Evento(USUARIO, materia, TipoEvento.PARCIAL, "  Parcial 1  ", INICIO, FIN);

    // validacion
    assertThat(evento.getUsuarioId(), is(USUARIO));
    assertThat(evento.getTitulo(), is("Parcial 1"));
    assertThat(evento.getTipo(), is(TipoEvento.PARCIAL));
    assertThat(evento.getMateria(), is(materia));
    assertThat(evento.getInicio(), is(INICIO));
    assertThat(evento.getFin(), is(FIN));
  }

  @Test
  public void deberiaRechazarUnUsuarioNulo() {
    // ejecucion y validacion
    assertThrows(
      IllegalArgumentException.class,
      () -> new Evento(null, materia(), TipoEvento.CLASE, "Clase", INICIO, FIN)
    );
  }

  @Test
  public void deberiaRechazarUnFinAnteriorAlInicio() {
    // ejecucion y validacion
    assertThrows(
      IllegalArgumentException.class,
      () -> new Evento(USUARIO, materia(), TipoEvento.CLASE, "Clase", FIN, INICIO)
    );
  }

  @Test
  public void deberiaRechazarUnEventoSinDuracion() {
    // ejecucion y validacion
    assertThrows(
      IllegalArgumentException.class,
      () -> new Evento(USUARIO, materia(), TipoEvento.CLASE, "Clase", INICIO, INICIO)
    );
  }

  @Test
  public void deberiaRechazarUnTituloVacio() {
    // ejecucion y validacion
    assertThrows(
      IllegalArgumentException.class,
      () -> new Evento(USUARIO, materia(), TipoEvento.CLASE, "   ", INICIO, FIN)
    );
  }

  @Test
  public void deberiaRechazarUnTituloNulo() {
    // ejecucion y validacion
    assertThrows(
      IllegalArgumentException.class,
      () -> new Evento(USUARIO, materia(), TipoEvento.CLASE, null, INICIO, FIN)
    );
  }

  @Test
  public void deberiaRechazarDatosObligatoriosNulos() {
    // ejecucion y validacion
    assertThrows(
      IllegalArgumentException.class,
      () -> new Evento(USUARIO, null, TipoEvento.CLASE, "Clase", INICIO, FIN)
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> new Evento(USUARIO, materia(), null, "Clase", INICIO, FIN)
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> new Evento(USUARIO, materia(), TipoEvento.CLASE, "Clase", null, FIN)
    );
    assertThrows(
      IllegalArgumentException.class,
      () -> new Evento(USUARIO, materia(), TipoEvento.CLASE, "Clase", INICIO, null)
    );
  }

  @Test
  public void unEventoNuevoNoTieneTareaVinculadaHastaQueSeLaAsignen() {
    // preparacion y ejecucion
    Evento evento = new Evento(USUARIO, materia(), TipoEvento.TRABAJO_PRACTICO, "TP", INICIO, FIN);

    // validacion
    assertThat(evento.getTareaId(), is(nullValue()));

    // ejecucion
    evento.vincularTarea(42L);

    // validacion
    assertThat(evento.getTareaId(), is(42L));
  }
}
