package com.tallerwebi.integracion;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.ServicioGrupo;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.presentacion.grupo.DatosGrupo;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorGrupoTest {

  private Grupo grupoMock;

  @MockitoBean
  private ServicioGrupo servicioGrupoMock;

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;
  private MvcResult mvcResult;

  @BeforeEach
  public void init() {
    grupoMock = new Grupo();
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void debeRetornarLaPaginaDeUnGrupoCuandoSeNavegaAGrupo() throws Exception {
    String nombreDeGrupo = "grupo-test";
    dadoQueExisteUnGrupoConNombre(nombreDeGrupo);
    cuandoNavegoALaPaginaDeUnGrupo(nombreDeGrupo);
    entoncesSeMuestraLaPaginaDeUnGrupo(nombreDeGrupo);
  }

  @Test
  public void deberiaRetornarLaPaginaDeGruposCuandoSeNavegaAGrupos() throws Exception {
    cuandoNavegoALaPaginaDeGrupos();
    entoncesSeMuestraLaPaginaDeGrupos();
  }

  private void dadoQueExisteUnGrupoConNombre(String nombre) throws GrupoNoEncontrado {
    grupoMock.setNombre(nombre);
    when(servicioGrupoMock.buscarPorNombre(nombre)).thenReturn(grupoMock);
  }

  private void cuandoNavegoALaPaginaDeUnGrupo(String nombre) throws Exception {
    DatosGrupo datosGrupo = new DatosGrupo();
    datosGrupo.setNombre(nombre);
    this.mvcResult =
      this.mockMvc.perform(get("/grupos/" + nombre)).andExpect(status().isOk()).andReturn();
  }

  private void cuandoNavegoALaPaginaDeGrupos() throws Exception {
    when(servicioGrupoMock.listarGrupos()).thenReturn(Arrays.asList(grupoMock));
    this.mvcResult = this.mockMvc.perform(get("/grupos")).andExpect(status().isOk()).andReturn();
    DatosGrupo datosGrupo = new DatosGrupo();
    datosGrupo.setNombre(grupoMock.getNombre());
    mvcResult.getModelAndView().getModel().put("datosGrupo", datosGrupo);
  }

  private void entoncesSeMuestraLaPaginaDeUnGrupo(String nombreGrupo) {
    ModelAndView modelAndView = this.mvcResult.getModelAndView();
    assert modelAndView != null;
    assert modelAndView.getViewName().equals("paginas/grupo/grupo-detalle");
    assert modelAndView.getModel().get("datosGrupo") != null;
    assert ((DatosGrupo) modelAndView.getModel().get("datosGrupo")).getNombre().equals(nombreGrupo);
  }

  private void entoncesSeMuestraLaPaginaDeGrupos() {
    ModelAndView modelAndView = this.mvcResult.getModelAndView();
    assert modelAndView != null;
    assert modelAndView.getViewName().equals("paginas/grupo/grupos");
  }
}
