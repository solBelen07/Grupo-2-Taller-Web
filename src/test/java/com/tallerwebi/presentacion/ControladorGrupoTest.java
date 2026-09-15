package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Grupo;
import com.tallerwebi.dominio.ServicioGrupo;
import com.tallerwebi.dominio.excepcion.GrupoNoEncontrado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorGrupoTest {

  private ControladorGrupo controladorGrupo;
  private ServicioGrupo servicioGrupoMock;
  private Grupo grupoMock;

  @BeforeEach
  public void init() {
    servicioGrupoMock = mock(ServicioGrupo.class);
    controladorGrupo = new ControladorGrupo(servicioGrupoMock);
    grupoMock = mock(Grupo.class);
    when(grupoMock.getNombre()).thenReturn("grupo1");
  }

  @Test
  public void irAGrupoDeberiaRetornarVistaGrupo() {
    ModelAndView modelAndView = controladorGrupo.verGrupo("grupo1");

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("grupo-detalle"));
  }

  @Test
  public void irAGruposDeberiaRetornarVistaGruposYDatosGrupo() {
    ModelAndView modelAndView = controladorGrupo.verGrupos();

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("grupos"));
    assertThat(modelAndView.getModel().get("datosGrupo"), instanceOf(DatosGrupo.class));
  }

  @Test
  public void irAGruposDeberiaRetornarVistaGrupos() {
    ModelAndView modelAndView = controladorGrupo.verGrupos();

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("grupos"));
  }

  @Test
  public void irAGrupoQueNoExisteDeberiaRetornarVistaGrupoDetalleConError()
    throws GrupoNoEncontrado {
    doThrow(GrupoNoEncontrado.class).when(servicioGrupoMock).buscarPorNombre("grupo2");

    ModelAndView modelAndView = controladorGrupo.verGrupo("grupo2");

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("grupo-detalle"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("El grupo no existe")
    );
  }
}
