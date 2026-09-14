package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.mockito.Mockito.mock;

import com.tallerwebi.dominio.ServicioMateria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorMateriaTest {

  private ControladorMateria controladorMateria;
  private ServicioMateria servicioMateriaMock;

  @BeforeEach
  public void init() {
    servicioMateriaMock = mock(ServicioMateria.class);
    controladorMateria = new ControladorMateria(servicioMateriaMock);
  }

  @Test
  public void consultarMateriasDeberiaRetornarVistaMaterias() {
    ModelAndView modelAndView = controladorMateria.verMaterias();
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("materias"));
  }
}
