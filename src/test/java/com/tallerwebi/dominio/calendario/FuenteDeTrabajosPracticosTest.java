package com.tallerwebi.dominio.calendario;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Evento;
import com.tallerwebi.dominio.TipoEvento;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import com.tallerwebi.dominio.trabajosPracticos.EstadoTP;
import com.tallerwebi.dominio.trabajosPracticos.RepositorioTrabajoPractico;
import com.tallerwebi.dominio.trabajosPracticos.TrabajoPractico;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FuenteDeTrabajosPracticosTest {

  private static final Long USUARIO = 1L;
  private static final LocalDate HOY = LocalDate.of(2026, 9, 28);
  private static final LocalDateTime DESDE = LocalDate.of(2026, 9, 28).atStartOfDay();
  private static final LocalDateTime HASTA = LocalDate.of(2026, 10, 5).atStartOfDay();

  private RepositorioTrabajoPractico repositorioTpMock;
  private RepositorioMateria repositorioMateriaMock;
  private FuenteDeTrabajosPracticos fuente;
  private Materia tallerWeb;

  @BeforeEach
  public void init() {
    this.repositorioTpMock = mock(RepositorioTrabajoPractico.class);
    this.repositorioMateriaMock = mock(RepositorioMateria.class);
    this.fuente =
      new FuenteDeTrabajosPracticos(this.repositorioTpMock, this.repositorioMateriaMock);
    this.tallerWeb = new Materia();
    this.tallerWeb.setNombre("Taller Web I");
    this.tallerWeb.setColor("#2F6FDE");
    when(this.repositorioMateriaMock.obtenerTodas()).thenReturn(List.of(this.tallerWeb));
  }

  private static TrabajoPractico tp(String nombre, String materia, LocalDate entrega) {
    TrabajoPractico trabajo = new TrabajoPractico();
    trabajo.setNombre(nombre);
    trabajo.setMateria(materia);
    trabajo.setFechaEntrega(entrega);
    trabajo.setEstado(EstadoTP.PENDIENTE);
    return trabajo;
  }

  private List<EventoConEstado> traer(TrabajoPractico... trabajos) {
    when(this.repositorioTpMock.obtenerTodos()).thenReturn(List.of(trabajos));
    return this.fuente.eventosEntre(USUARIO, DESDE, HASTA, HOY);
  }

  @Test
  public void unTpSeConvierteEnUnBloqueDeUnaHoraAlaVeinteDelDiaDeEntrega() {
    List<EventoConEstado> eventos = traer(tp("TP 1", "Taller Web I", HOY.plusDays(2)));

    assertThat(eventos, hasSize(1));
    Evento evento = eventos.get(0).evento();
    assertThat(evento.getTipo(), is(TipoEvento.TRABAJO_PRACTICO));
    assertThat(evento.getTitulo(), is("TP 1"));
    assertThat(evento.getUsuarioId(), is(USUARIO));
    assertThat(evento.getMateria(), is(sameInstance(this.tallerWeb)));
    assertThat(evento.getInicio(), is(HOY.plusDays(2).atTime(20, 0)));
    assertThat(evento.getFin(), is(HOY.plusDays(2).atTime(21, 0)));
  }

  @Test
  public void sinNombreUsaUnTituloGenerico() {
    assertThat(
      traer(tp(null, "Taller Web I", HOY)).get(0).evento().getTitulo(),
      is(FuenteDeTrabajosPracticos.TITULO_GENERICO)
    );
    assertThat(
      traer(tp("  ", "Taller Web I", HOY)).get(0).evento().getTitulo(),
      is(FuenteDeTrabajosPracticos.TITULO_GENERICO)
    );
  }

  @Test
  public void unTpSinFechaDeEntregaNoSeIncluye() {
    assertThat(traer(tp("TP 1", "Taller Web I", null)), empty());
  }

  @Test
  public void unTpFueraDelPeriodoNoSeIncluye() {
    assertThat(
      traer(
        tp("Antes", "Taller Web I", HOY.minusDays(1)),
        tp("Despues", "Taller Web I", HASTA.toLocalDate())
      ),
      empty()
    );
  }

  @Test
  public void elEstadoSeCalculaConLaFechaYElEstadoDelTp() {
    TrabajoPractico finalizado = tp("Hecho", "Taller Web I", HOY.plusDays(6));
    finalizado.setEstado(EstadoTP.FINALIZADO);

    List<EventoConEstado> eventos = traer(tp("Cerca", "Taller Web I", HOY.plusDays(2)), finalizado);

    assertThat(eventos.get(0).estado(), is(EstadoActividad.PROXIMA_A_VENCER));
    assertThat(eventos.get(1).estado(), is(EstadoActividad.COMPLETADA));
  }

  private static final LocalDate HOY_ESTADOS = LocalDate.of(2026, 9, 30);

  private static TrabajoPractico tpConEstado(EstadoTP estado, LocalDate entrega) {
    TrabajoPractico trabajo = new TrabajoPractico();
    trabajo.setEstado(estado);
    trabajo.setFechaEntrega(entrega);
    return trabajo;
  }

  @Test
  public void unTpFinalizadoEstaCompletado() {
    assertThat(
      FuenteDeTrabajosPracticos.estadoDe(
        tpConEstado(EstadoTP.FINALIZADO, HOY_ESTADOS.plusDays(30)),
        HOY_ESTADOS
      ),
      is(EstadoActividad.COMPLETADA)
    );
  }

  @Test
  public void unTpFinalizadoGanaAunqueLaFechaYaHayaPasado() {
    assertThat(
      FuenteDeTrabajosPracticos.estadoDe(
        tpConEstado(EstadoTP.FINALIZADO, HOY_ESTADOS.minusDays(5)),
        HOY_ESTADOS
      ),
      is(EstadoActividad.COMPLETADA)
    );
  }

  @Test
  public void unTpNoFinalizadoConFechaPasadaEstaVencido() {
    assertThat(
      FuenteDeTrabajosPracticos.estadoDe(
        tpConEstado(EstadoTP.EN_CURSO, HOY_ESTADOS.minusDays(1)),
        HOY_ESTADOS
      ),
      is(EstadoActividad.VENCIDA)
    );
  }

  @Test
  public void unTpQueSeEntregaHoyEstaProximoAVencer() {
    assertThat(
      FuenteDeTrabajosPracticos.estadoDe(tpConEstado(EstadoTP.PENDIENTE, HOY_ESTADOS), HOY_ESTADOS),
      is(EstadoActividad.PROXIMA_A_VENCER)
    );
  }

  @Test
  public void unTpQueSeEntregaEnTresDiasEstaProximoAVencer() {
    assertThat(
      FuenteDeTrabajosPracticos.estadoDe(
        tpConEstado(EstadoTP.PENDIENTE, HOY_ESTADOS.plusDays(3)),
        HOY_ESTADOS
      ),
      is(EstadoActividad.PROXIMA_A_VENCER)
    );
  }

  @Test
  public void unTpQueSeEntregaEnCuatroDiasEstaEnTiempo() {
    assertThat(
      FuenteDeTrabajosPracticos.estadoDe(
        tpConEstado(EstadoTP.PENDIENTE, HOY_ESTADOS.plusDays(4)),
        HOY_ESTADOS
      ),
      is(EstadoActividad.EN_TIEMPO)
    );
  }

  @Test
  public void unTpSinFechaNiEstadoEstaEnTiempo() {
    assertThat(
      FuenteDeTrabajosPracticos.estadoDe(tpConEstado(null, null), HOY_ESTADOS),
      is(EstadoActividad.EN_TIEMPO)
    );
  }

  @Test
  public void usaLaMateriaRealSinImportarMayusculasNiEspacios() {
    Materia materia = traer(tp("TP 1", " taller web i ", HOY)).get(0).evento().getMateria();
    assertThat(materia, is(sameInstance(this.tallerWeb)));
  }

  @Test
  public void dosTpsDeLaMismaMateriaSinCatalogoComparteLaMateriaGris() {
    List<EventoConEstado> eventos = traer(
      tp("TP 1", "Inglés", HOY),
      tp("TP 2", "inglés", HOY.plusDays(1))
    );
    assertThat(
      eventos.get(0).evento().getMateria(),
      is(sameInstance(eventos.get(1).evento().getMateria()))
    );
  }

  @Test
  public void unTpSinMateriaUsaElNombreGenerico() {
    Materia materia = traer(tp("TP 1", null, HOY)).get(0).evento().getMateria();
    assertThat(materia.getNombre(), is(FuenteDeTrabajosPracticos.MATERIA_SIN_NOMBRE));
  }

  @Test
  public void ignoraLasMateriasDelCatalogoSinNombre() {
    when(this.repositorioMateriaMock.obtenerTodas())
      .thenReturn(List.of(new Materia(), this.tallerWeb));
    Materia materia = traer(tp("TP 1", "Taller Web I", HOY)).get(0).evento().getMateria();
    assertThat(materia, is(sameInstance(this.tallerWeb)));
  }
}
