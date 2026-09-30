package com.tallerwebi.dominio.calendario;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.tallerwebi.dominio.TipoEvento;
import java.util.List;
import org.junit.jupiter.api.Test;

public class DisposicionDeBloquesTest {

  private static SegmentoDia segmento(String titulo, int desdeMinuto, int hastaMinuto) {
    return new SegmentoDia(
      null,
      titulo,
      TipoEvento.CLASE,
      "Materia",
      "#2F6FDE",
      desdeMinuto,
      hastaMinuto,
      false,
      false,
      null
    );
  }

  private static BloqueHorario bloque(List<BloqueHorario> bloques, String titulo) {
    return bloques
      .stream()
      .filter(b -> b.segmento().titulo().equals(titulo))
      .findFirst()
      .orElseThrow();
  }

  @Test
  public void deberiaDevolverListaVaciaSinSegmentos() {
    // ejecucion y validacion
    assertThat(DisposicionDeBloques.disponer(List.of(), 420), is(empty()));
  }

  @Test
  public void deberiaPosicionarUnBloqueRelativoAlInicioDeLaGrilla() {
    // preparacion y ejecucion
    List<BloqueHorario> bloques = DisposicionDeBloques.disponer(
      List.of(segmento("A", 18 * 60, 20 * 60)),
      7 * 60
    );

    // validacion
    assertThat(bloques, hasSize(1));
    assertThat(bloques.get(0).top(), is(11 * 60));
    assertThat(bloques.get(0).alto(), is(120));
    assertThat(bloques.get(0).columna(), is(0));
    assertThat(bloques.get(0).columnas(), is(1));
  }

  @Test
  public void deberiaUsarUnaSolaColumnaSiNoHaySuperposicion() {
    // preparacion y ejecucion
    List<BloqueHorario> bloques = DisposicionDeBloques.disponer(
      List.of(segmento("A", 480, 540), segmento("B", 600, 660)),
      0
    );

    // validacion
    assertThat(bloque(bloques, "A").columnas(), is(1));
    assertThat(bloque(bloques, "B").columnas(), is(1));
  }

  @Test
  public void noDeberiaConsiderarSuperpuestosLosBloquesQueSoloSeTocan() {
    // preparacion y ejecucion
    List<BloqueHorario> bloques = DisposicionDeBloques.disponer(
      List.of(segmento("A", 480, 540), segmento("B", 540, 600)),
      0
    );

    // validacion
    assertThat(bloque(bloques, "A").columnas(), is(1));
    assertThat(bloque(bloques, "B").columnas(), is(1));
    assertThat(bloque(bloques, "B").columna(), is(0));
  }

  @Test
  public void deberiaRepartirEnDosColumnasDosBloquesSuperpuestos() {
    // preparacion y ejecucion
    List<BloqueHorario> bloques = DisposicionDeBloques.disponer(
      List.of(segmento("Clase", 19 * 60, 21 * 60), segmento("Reunión", 20 * 60, 22 * 60)),
      0
    );

    // validacion
    assertThat(bloque(bloques, "Clase").columna(), is(0));
    assertThat(bloque(bloques, "Reunión").columna(), is(1));
    assertThat(bloque(bloques, "Clase").columnas(), is(2));
    assertThat(bloque(bloques, "Reunión").columnas(), is(2));
  }

  @Test
  public void deberiaReutilizarUnaColumnaLiberadaDentroDelMismoGrupo() {
    // preparacion y ejecucion: A (8-10) y B (9-12) se superponen; C (10-11) solo se superpone
    // con B y entra en la columna de A.
    List<BloqueHorario> bloques = DisposicionDeBloques.disponer(
      List.of(segmento("A", 480, 600), segmento("B", 540, 720), segmento("C", 600, 660)),
      0
    );

    // validacion
    assertThat(bloque(bloques, "A").columna(), is(0));
    assertThat(bloque(bloques, "B").columna(), is(1));
    assertThat(bloque(bloques, "C").columna(), is(0));
    assertThat(bloque(bloques, "C").columnas(), is(2));
  }

  @Test
  public void deberiaSepararGruposIndependientesDeSuperposicion() {
    // preparacion y ejecucion
    List<BloqueHorario> bloques = DisposicionDeBloques.disponer(
      List.of(segmento("A", 480, 600), segmento("B", 540, 660), segmento("C", 900, 960)),
      0
    );

    // validacion
    assertThat(bloque(bloques, "A").columnas(), is(2));
    assertThat(bloque(bloques, "C").columnas(), is(1));
    assertThat(bloque(bloques, "C").columna(), is(0));
  }

  @Test
  public void deberiaRepartirEnTresColumnasTresBloquesSimultaneos() {
    // preparacion y ejecucion
    List<BloqueHorario> bloques = DisposicionDeBloques.disponer(
      List.of(segmento("A", 600, 660), segmento("B", 600, 660), segmento("C", 600, 660)),
      0
    );

    // validacion
    assertThat(bloques, hasSize(3));
    assertThat(bloque(bloques, "A").columnas(), is(3));
    assertThat(bloque(bloques, "B").columnas(), is(3));
    assertThat(bloque(bloques, "C").columnas(), is(3));
  }
}
