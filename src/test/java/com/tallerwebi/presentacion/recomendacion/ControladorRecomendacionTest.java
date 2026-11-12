package com.tallerwebi.presentacion.recomendacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.recomendacion.MatrizCompetencias;
import com.tallerwebi.dominio.recomendacion.Recomendado;
import com.tallerwebi.dominio.recomendacion.ServicioRecomendacion;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ControladorRecomendacionTest {

  private static final String VISTA = "paginas/grupo/recomendaciones";
  private static final String URL = "/recomendaciones";
  private static final double DELTA = 0.001;

  private ServicioRecomendacion servicioRecomendacionMock;
  private MockMvc mockMvc;
  private Usuario usuario;
  private MockHttpSession session;

  @BeforeEach
  public void init() {
    this.servicioRecomendacionMock = mock(ServicioRecomendacion.class);
    ControladorRecomendacion controlador = new ControladorRecomendacion(
      this.servicioRecomendacionMock
    );
    this.mockMvc = MockMvcBuilders.standaloneSetup(controlador).build();

    this.usuario = new Usuario();
    this.usuario.setId(1L);
    this.session = new MockHttpSession();
    this.session.setAttribute("USUARIO", usuario);
    when(this.servicioRecomendacionMock.obtenerMatrizDeCompetencias(usuario))
      .thenReturn(new MatrizCompetencias(Map.of()));
  }

  @Test
  public void deberiaRedirigirAlLoginSiNoHayUsuarioEnSesion() throws Exception {
    this.mockMvc.perform(get(URL))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));

    verify(this.servicioRecomendacionMock, never()).recomendarCompanieros(any());
    verify(this.servicioRecomendacionMock, never()).obtenerMatrizDeCompetencias(any());
  }

  @Test
  public void deberiaMostrarLaVistaDeRecomendaciones() throws Exception {
    this.mockMvc.perform(get(URL).session(session))
      .andExpect(status().isOk())
      .andExpect(view().name(VISTA));
  }

  @Test
  public void deberiaPasarALaVistaUnDtoSinLosObjetosDelDominio() throws Exception {
    Object datos = pedirDatos();

    assertThat(datos instanceof RecomendacionDTO, equalTo(true));
  }

  @Test
  public void deberiaPasarALaVistaLosNivelesYElCumplimientoDeLaMatrizDelUsuario() throws Exception {
    when(this.servicioRecomendacionMock.obtenerMatrizDeCompetencias(usuario))
      .thenReturn(new MatrizCompetencias(Map.of("Algebra", 50.0, "Fisica", 100.0)));

    RecomendacionDTO datos = (RecomendacionDTO) pedirDatos();

    assertThat(datos.getNiveles().get("Algebra"), closeTo(50.0, DELTA));
    assertThat(datos.getNiveles().get("Fisica"), closeTo(100.0, DELTA));
    assertThat(datos.getCumplimiento(), closeTo(75.0, DELTA));
  }

  @Test
  public void deberiaPasarALaVistaElAporteEnGruposDelUsuario() throws Exception {
    when(this.servicioRecomendacionMock.obtenerAporteEnGrupos(usuario)).thenReturn(60.0);

    RecomendacionDTO datos = (RecomendacionDTO) pedirDatos();

    assertThat(datos.getAporteEnGrupos(), closeTo(60.0, DELTA));
  }

  @Test
  public void deberiaPasarALaVistaLosDatosDeCadaCompaneroRecomendado() throws Exception {
    Usuario ana = new Usuario();
    ana.setNombre("Ana");
    ana.setApellido("Perez");
    ana.setEmail("ana@unlam.edu.ar");
    when(this.servicioRecomendacionMock.recomendarCompanieros(usuario))
      .thenReturn(List.of(new Recomendado(ana, 80.0, 90.0, 70.0, List.of("Algebra"))));

    RecomendacionDTO datos = (RecomendacionDTO) pedirDatos();

    CompanieroDTO companero = datos.getCompaneros().get(0);
    assertThat(datos.getCompaneros().size(), equalTo(1));
    assertThat(companero.getNombre(), equalTo("Ana"));
    assertThat(companero.getApellido(), equalTo("Perez"));
    assertThat(companero.getEmail(), equalTo("ana@unlam.edu.ar"));
    assertThat(companero.getCompatibilidad(), closeTo(80.0, DELTA));
    assertThat(companero.getCumplimiento(), closeTo(90.0, DELTA));
    assertThat(companero.getAporteEnGrupos(), closeTo(70.0, DELTA));
    assertThat(companero.getAportaEn(), contains("Algebra"));
  }

  @Test
  public void deberiaPasarALaVistaUnaListaVaciaSiNoHayCompanerosRecomendados() throws Exception {
    RecomendacionDTO datos = (RecomendacionDTO) pedirDatos();

    assertThat(datos.getCompaneros(), empty());
  }

  private Object pedirDatos() throws Exception {
    return this.mockMvc.perform(get(URL).session(session))
      .andReturn()
      .getModelAndView()
      .getModel()
      .get("RecomendacionDTO");
  }
}
