package com.tallerwebi.integracion;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tallerwebi.dominio.Usuario;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class ControladorCalendarioTest {

  private Usuario usuarioLogueado;

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.usuarioLogueado = new Usuario();
    this.usuarioLogueado.setId(1L);
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
  }

  @Test
  public void debeRedirigirALoginCuandoNoHayUsuarioEnSesion() throws Exception {
    this.mockMvc.perform(get("/calendario"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));
  }

  @Test
  public void debeMostrarElCalendarioMensualPorDefecto() throws Exception {
    this.mockMvc.perform(get("/calendario").sessionAttr("USUARIO", this.usuarioLogueado))
      .andExpect(status().isOk())
      .andExpect(view().name("calendario"))
      .andExpect(model().attribute("vista", "mes"));
  }

  @Test
  public void debeAsociarLosParametrosVistaYFecha() throws Exception {
    this.mockMvc.perform(
        get("/calendario")
          .sessionAttr("USUARIO", this.usuarioLogueado)
          .param("vista", "semana")
          .param("fecha", "2026-09-30")
      )
      .andExpect(status().isOk())
      .andExpect(model().attribute("vista", "semana"));
  }

  @Test
  public void debeResponderOkConUnaFechaInvalidaYMostrarElAviso() throws Exception {
    this.mockMvc.perform(
        get("/calendario").sessionAttr("USUARIO", this.usuarioLogueado).param("fecha", "abc")
      )
      .andExpect(status().isOk())
      .andExpect(model().attributeExists("error"));
  }
}
