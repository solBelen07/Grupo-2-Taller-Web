package com.tallerwebi.dominio.recomendacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import com.tallerwebi.dominio.tarea.ServicioTarea;
import com.tallerwebi.dominio.tarea.Tarea;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioRecomendacionTest {

  private static final double DELTA = 0.001;
  private static final String COMPLETADA = "COMPLETADA";
  private static final String PENDIENTE = "PENDIENTE";
  private static final String ALGEBRA = "Algebra";
  private static final String FISICA = "Fisica";

  private RepositorioMateria repositorioMateriaMock;
  private ServicioTarea servicioTareaMock;
  private ServicioRecomendacion servicioRecomendacion;

  private Usuario yo;
  private Usuario ana;
  private Usuario beto;
  private Usuario carla;
  private Materia algebra;
  private Materia fisica;
  private List<Tarea> tareas;

  @BeforeEach
  public void init() {
    this.repositorioMateriaMock = mock(RepositorioMateria.class);
    this.servicioTareaMock = mock(ServicioTarea.class);
    this.servicioRecomendacion =
      new ServicioRecomendacionImpl(this.repositorioMateriaMock, this.servicioTareaMock);

    this.yo = crearUsuario(1L, "yo@unlam.edu.ar");
    this.ana = crearUsuario(2L, "ana@unlam.edu.ar");
    this.beto = crearUsuario(3L, "beto@unlam.edu.ar");
    this.carla = crearUsuario(4L, "carla@unlam.edu.ar");

    this.algebra = crearMateria(1, ALGEBRA, yo, ana, beto);
    this.fisica = crearMateria(2, FISICA, yo, ana, carla);

    this.tareas = new ArrayList<>();
    when(this.servicioTareaMock.obtenerTodas()).thenReturn(this.tareas);

    cursa(yo, algebra, fisica);
    cursa(ana, algebra, fisica);
    cursa(beto, algebra);
    cursa(carla, fisica);
  }

  @Test
  public void deberiaCalcularElNivelDeCadaMateriaComoPorcentajeDeSusTareasCompletadas() {
    agregarTarea(ALGEBRA, yo, COMPLETADA);
    agregarTarea(ALGEBRA, yo, PENDIENTE);

    MatrizCompetencias matriz = servicioRecomendacion.obtenerMatrizDeCompetencias(yo);

    assertThat(matriz.nivelEn(ALGEBRA), closeTo(50.0, DELTA));
  }

  @Test
  public void deberiaTenerNivelCeroEnUnaMateriaSinTareasAsignadasAlUsuario() {
    MatrizCompetencias matriz = servicioRecomendacion.obtenerMatrizDeCompetencias(yo);

    assertThat(matriz.getCompetencias(), contains(ALGEBRA, FISICA));
    assertThat(matriz.nivelEn(FISICA), closeTo(0.0, DELTA));
  }

  @Test
  public void deberiaIgnorarLasTareasDeOtrosResponsablesAlArmarLaMatriz() {
    agregarTarea(ALGEBRA, yo, PENDIENTE);
    agregarTarea(ALGEBRA, ana, COMPLETADA);

    MatrizCompetencias matriz = servicioRecomendacion.obtenerMatrizDeCompetencias(yo);

    assertThat(matriz.nivelEn(ALGEBRA), closeTo(0.0, DELTA));
  }

  @Test
  public void deberiaIgnorarLasTareasDeMateriasQueElUsuarioNoCursa() {
    agregarTarea("Quimica", yo, COMPLETADA);

    MatrizCompetencias matriz = servicioRecomendacion.obtenerMatrizDeCompetencias(yo);

    assertThat(matriz.getCompetencias(), contains(ALGEBRA, FISICA));
    assertThat(matriz.cumplimientoGeneral(), closeTo(0.0, DELTA));
  }

  @Test
  public void deberiaCompararElNombreDeLaMateriaSinDistinguirMayusculas() {
    agregarTarea("ALGEBRA", yo, COMPLETADA);

    MatrizCompetencias matriz = servicioRecomendacion.obtenerMatrizDeCompetencias(yo);

    assertThat(matriz.nivelEn(ALGEBRA), closeTo(100.0, DELTA));
  }

  @Test
  public void deberiaDevolverUnaMatrizVaciaSiElUsuarioNoCursaMaterias() {
    Usuario sinMaterias = crearUsuario(9L, "solo@unlam.edu.ar");
    cursa(sinMaterias);

    MatrizCompetencias matriz = servicioRecomendacion.obtenerMatrizDeCompetencias(sinMaterias);

    assertThat(matriz.getCompetencias(), empty());
  }

  @Test
  public void deberiaNoRecomendarNadaSiElUsuarioNoCursaMaterias() {
    Usuario sinMaterias = crearUsuario(9L, "solo@unlam.edu.ar");
    cursa(sinMaterias);

    List<Recomendado> recomendados = servicioRecomendacion.recomendarCompanieros(sinMaterias);

    assertThat(recomendados, empty());
  }

  @Test
  public void deberiaNoRecomendarAlPropioUsuario() {
    agregarTarea(FISICA, carla, COMPLETADA);

    List<Recomendado> recomendados = servicioRecomendacion.recomendarCompanieros(yo);

    assertThat(emailsDe(recomendados), contains(carla.getEmail()));
  }

  @Test
  public void deberiaNoRecomendarAUnCompanieroQueNoAportaNingunaCompetenciaQueFalteAlUsuario() {
    // beto no completo nada en Algebra, asi que no suma a quien ya tiene mas nivel que el
    agregarTarea(ALGEBRA, yo, COMPLETADA);
    agregarTarea(ALGEBRA, beto, PENDIENTE);

    List<Recomendado> recomendados = servicioRecomendacion.recomendarCompanieros(yo);

    assertThat(emailsDe(recomendados), empty());
  }

  @Test
  public void deberiaRecomendarAUnCompanieroSoloUnaVezAunqueComparteVariasMaterias() {
    agregarTarea(ALGEBRA, ana, COMPLETADA);
    agregarTarea(FISICA, ana, COMPLETADA);

    List<Recomendado> recomendados = servicioRecomendacion.recomendarCompanieros(yo);

    assertThat(emailsDe(recomendados), contains(ana.getEmail()));
  }

  @Test
  public void deberiaIndicarEnQueCompetenciasElCompanieroSuperaAlUsuario() {
    agregarTarea(ALGEBRA, yo, COMPLETADA);
    agregarTarea(ALGEBRA, yo, PENDIENTE);
    agregarTarea(ALGEBRA, ana, COMPLETADA);
    agregarTarea(FISICA, ana, COMPLETADA);

    Recomendado recomendadoAna = recomendacionDe(ana);

    assertThat(recomendadoAna.getAportaEn(), contains(ALGEBRA, FISICA));
  }

  @Test
  public void deberiaNoIncluirEnAportaEnLasCompetenciasDondeElUsuarioEsMejorOIgual() {
    agregarTarea(ALGEBRA, yo, COMPLETADA);
    agregarTarea(ALGEBRA, ana, COMPLETADA);
    agregarTarea(FISICA, ana, COMPLETADA);

    Recomendado recomendadoAna = recomendacionDe(ana);

    assertThat(recomendadoAna.getAportaEn(), contains(FISICA));
  }

  @Test
  public void deberiaCalcularLaCompatibilidadCombinandoComplementariedadYCumplimientoDelCompaniero() {
    agregarTarea(ALGEBRA, yo, COMPLETADA);
    agregarTarea(ALGEBRA, yo, PENDIENTE);
    agregarTarea(ALGEBRA, ana, COMPLETADA);
    agregarTarea(ALGEBRA, ana, COMPLETADA);
    agregarTarea(FISICA, ana, COMPLETADA);
    agregarTarea(FISICA, ana, PENDIENTE);

    Recomendado recomendadoAna = recomendacionDe(ana);

    assertThat(recomendadoAna.getCompatibilidad(), closeTo(43.75, DELTA));
    assertThat(recomendadoAna.getCumplimiento(), closeTo(75.0, DELTA));
    assertThat(recomendadoAna.getAporteEnGrupos(), closeTo(0.0, DELTA));
  }

  @Test
  public void deberiaSumarALaCompatibilidadElAporteIndividualDelCompanieroEnSusGrupos() {
    agregarTarea(ALGEBRA, yo, COMPLETADA);
    agregarTarea(ALGEBRA, yo, PENDIENTE);
    agregarTarea(ALGEBRA, ana, COMPLETADA);
    agregarTarea(ALGEBRA, ana, COMPLETADA);
    agregarTarea(FISICA, ana, COMPLETADA);
    agregarTarea(FISICA, ana, PENDIENTE);
    agregarTareaDeGrupo("Otra", ana, COMPLETADA);

    Recomendado recomendadoAna = recomendacionDe(ana);

    assertThat(recomendadoAna.getAporteEnGrupos(), closeTo(100.0, DELTA));
    assertThat(recomendadoAna.getCompatibilidad(), closeTo(68.75, DELTA));
  }

  @Test
  public void deberiaOrdenarLasRecomendacionesDeMayorAMenorCompatibilidad() {
    agregarTarea(ALGEBRA, yo, COMPLETADA);
    agregarTarea(ALGEBRA, yo, PENDIENTE);
    agregarTarea(ALGEBRA, ana, COMPLETADA);
    agregarTarea(ALGEBRA, ana, COMPLETADA);
    agregarTarea(FISICA, ana, COMPLETADA);
    agregarTarea(FISICA, ana, PENDIENTE);
    agregarTarea(FISICA, carla, COMPLETADA);

    List<Recomendado> recomendados = servicioRecomendacion.recomendarCompanieros(yo);

    assertThat(emailsDe(recomendados), contains(carla.getEmail(), ana.getEmail()));
    assertThat(recomendados.get(0).getCompatibilidad(), closeTo(50.0, DELTA));
    assertThat(recomendados.get(1).getCompatibilidad(), closeTo(43.75, DELTA));
  }

  @Test
  public void deberiaNoRecomendarACompanierosConMenorCumplimientoQueElUsuario() {
    agregarTarea(ALGEBRA, yo, COMPLETADA);
    agregarTarea(ALGEBRA, yo, PENDIENTE);
    agregarTarea(FISICA, yo, COMPLETADA);
    agregarTarea(ALGEBRA, ana, COMPLETADA);

    List<Recomendado> recomendados = servicioRecomendacion.recomendarCompanieros(yo);

    assertThat(emailsDe(recomendados), empty());
  }

  @Test
  public void deberiaRecomendarACompanierosConElMismoCumplimientoQueElUsuario() {
    agregarTarea(ALGEBRA, yo, PENDIENTE);
    agregarTarea(FISICA, yo, COMPLETADA);
    agregarTarea(FISICA, yo, PENDIENTE);
    agregarTarea(ALGEBRA, beto, COMPLETADA);
    agregarTarea(ALGEBRA, beto, PENDIENTE);
    agregarTarea(ALGEBRA, beto, PENDIENTE);
    agregarTarea(ALGEBRA, beto, PENDIENTE);

    List<Recomendado> recomendados = servicioRecomendacion.recomendarCompanieros(yo);

    assertThat(emailsDe(recomendados), contains(beto.getEmail()));
  }

  @Test
  public void deberiaExponerElCompaineroRecomendado() {
    agregarTarea(FISICA, carla, COMPLETADA);

    Recomendado recomendadoCarla = recomendacionDe(carla);

    assertThat(recomendadoCarla.getCompaniero(), equalTo(carla));
  }

  @Test
  public void deberiaLimitarLaCantidadDeRecomendacionesAlMaximoPermitido() {
    Materia redes = crearMateria(3, "Redes", yo);
    cursa(yo, redes);
    for (long id = 10; id < 17; id++) {
      Usuario companiero = crearUsuario(id, "companiero" + id + "@unlam.edu.ar");
      redes.getUsuarios().add(companiero);
      cursa(companiero, redes);
      agregarTarea("Redes", companiero, COMPLETADA);
    }

    List<Recomendado> recomendados = servicioRecomendacion.recomendarCompanieros(yo);

    assertThat(recomendados, hasSize(ServicioRecomendacionImpl.MAXIMO_RECOMENDACIONES));
  }

  @Test
  public void deberiaCalcularElAporteEnGruposComoPorcentajeDeSusTareasDeGrupoCompletadas() {
    agregarTareaDeGrupo(ALGEBRA, yo, COMPLETADA);
    agregarTareaDeGrupo(ALGEBRA, yo, PENDIENTE);
    agregarTareaDeGrupo(FISICA, yo, PENDIENTE);
    agregarTareaDeGrupo(FISICA, yo, PENDIENTE);

    assertThat(servicioRecomendacion.obtenerAporteEnGrupos(yo), closeTo(25.0, DELTA));
  }

  @Test
  public void deberiaIgnorarLasTareasSinGrupoAlCalcularElAporteEnGrupos() {
    agregarTarea(ALGEBRA, yo, PENDIENTE);
    agregarTareaDeGrupo(ALGEBRA, yo, COMPLETADA);

    assertThat(servicioRecomendacion.obtenerAporteEnGrupos(yo), closeTo(100.0, DELTA));
  }

  @Test
  public void deberiaIgnorarLasTareasDeGrupoDeOtrosResponsablesAlCalcularElAporteEnGrupos() {
    agregarTareaDeGrupo(ALGEBRA, ana, COMPLETADA);

    assertThat(servicioRecomendacion.obtenerAporteEnGrupos(yo), closeTo(0.0, DELTA));
  }

  @Test
  public void deberiaTenerAporteEnGruposCeroSiNoTieneTareasDeGrupo() {
    agregarTarea(ALGEBRA, yo, COMPLETADA);

    assertThat(servicioRecomendacion.obtenerAporteEnGrupos(yo), closeTo(0.0, DELTA));
  }

  private Recomendado recomendacionDe(Usuario companero) {
    return servicioRecomendacion
      .recomendarCompanieros(yo)
      .stream()
      .filter(recomendado -> companero.getEmail().equals(recomendado.getCompaniero().getEmail()))
      .findFirst()
      .orElseThrow();
  }

  private List<String> emailsDe(List<Recomendado> recomendados) {
    return recomendados
      .stream()
      .map(recomendado -> recomendado.getCompaniero().getEmail())
      .collect(Collectors.toList());
  }

  private void cursa(Usuario usuario, Materia... materias) {
    when(this.repositorioMateriaMock.obtenerPorUsuario(usuario.getId()))
      .thenReturn(Arrays.asList(materias));
  }

  private void agregarTarea(String materia, Usuario responsable, String estado) {
    Tarea tarea = new Tarea();
    tarea.setMateria(materia);
    tarea.setResponsable(responsable.getEmail());
    tarea.setEstado(estado);
    this.tareas.add(tarea);
  }

  private void agregarTareaDeGrupo(String materia, Usuario responsable, String estado) {
    agregarTarea(materia, responsable, estado);
    this.tareas.get(this.tareas.size() - 1).setGrupo(new Grupo());
  }

  private Usuario crearUsuario(Long id, String email) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setEmail(email);
    return usuario;
  }

  private Materia crearMateria(Integer id, String nombre, Usuario... usuarios) {
    Materia materia = new Materia();
    materia.setId(id);
    materia.setNombre(nombre);
    materia.setUsuarios(new ArrayList<>(Arrays.asList(usuarios)));
    return materia;
  }
}
