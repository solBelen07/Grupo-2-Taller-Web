package com.tallerwebi.integracion;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Grupo;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
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

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;
  private MvcResult mvcResult;

  @BeforeEach
  public void init() {
    grupoMock = mock(Grupo.class);
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void debeRetornarLaPaginaDeUnGrupoCuandoSeNavegaAGrupo() throws Exception {
    String nombreDeGrupo = "grupo-test";
    dadoQueExisteUnGrupoConNombre(nombreDeGrupo);
    cuandoNavegoALaPaginaDeUnGrupo(nombreDeGrupo);
    entoncesSeMuestraLaPaginaDeUnGrupo();
  }

  @Test
  public void deberiaRetornarLaPaginaDeGruposCuandoSeNavegaAGrupos() throws Exception {
    cuandoNavegoALaPaginaDeGrupos();
    entoncesSeMuestraLaPaginaDeGrupos();
  }

  private void dadoQueExisteUnGrupoConNombre(String nombre) {
    when(grupoMock.getNombre()).thenReturn(nombre);
  }

  private void cuandoNavegoALaPaginaDeUnGrupo(String nombre) throws Exception {
    this.mvcResult =
      this.mockMvc.perform(get("/grupos/" + nombre)).andExpect(status().isOk()).andReturn();
  }

  private void cuandoNavegoALaPaginaDeGrupos() throws Exception {
    this.mvcResult = this.mockMvc.perform(get("/grupos")).andExpect(status().isOk()).andReturn();
  }

  private void entoncesSeMuestraLaPaginaDeUnGrupo() {
    ModelAndView modelAndView = this.mvcResult.getModelAndView();
    assert modelAndView != null;
    assert modelAndView.getViewName().equals("grupo-detalle");
  }

  private void entoncesSeMuestraLaPaginaDeGrupos() {
    ModelAndView modelAndView = this.mvcResult.getModelAndView();
    assert modelAndView != null;
    assert modelAndView.getViewName().equals("grupos");
  }
}
