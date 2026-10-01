package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
public class ControladorTrabajoPracticoTest {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void debeRetornarLaVistaDeTrabajosPracticosCuandoSeNavegaALaRutaCorrespondiente()
    throws Exception {
    MvcResult result =
      this.mockMvc.perform(get("/trabajos-practicos")).andExpect(status().isOk()).andReturn();

    ModelAndView modelAndView = result.getModelAndView();
    assert modelAndView != null;
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("paginas/trabajos-practicos"));
  }

  @Test
  public void debeRetornarVistaDeTpsYContenerAtributosEnElModelo() throws Exception {
    this.mockMvc.perform(get("/trabajos-practicos"))
      .andExpect(status().isOk())
      .andExpect(view().name("paginas/trabajos-practicos"))
      .andExpect(model().attributeExists("pendientes", "enCurso", "finalizados"));
  }

  @Test
  public void debeFiltrarTrabajosPracticosPorMateria() throws Exception {
    this.mockMvc.perform(get("/trabajos-practicos").param("materia", "Taller Web"))
      .andExpect(status().isOk())
      .andExpect(view().name("paginas/trabajos-practicos"))
      .andExpect(model().attributeExists("pendientes", "enCurso", "finalizados"));
  }

  @Test
  public void debeGuardarTrabajoPracticoYRedirigir() throws Exception {
    this.mockMvc.perform(
        post("/trabajos-practicos/guardar")
          .param("nombre", "TP Integrador")
          .param("materia", "Taller Web")
          .param("tipo", "GRUPAL")
          .param("estado", "PENDIENTE")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/trabajos-practicos"));
  }
}
