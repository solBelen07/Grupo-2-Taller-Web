package com.tallerwebi.presentacion.dashboard;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.dashboard.ServicioDashboard;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import com.tallerwebi.presentacion.dashboard.DashboardDTO;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ControladorDashboardTest {

  private static final Long ID_USUARIO = 1L;
  private static final String VISTA_DASHBOARD = "paginas/dashboard/dashboard";

  private ServicioDashboard servicioDashboardMock;
  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.servicioDashboardMock = mock(ServicioDashboard.class);

    ControladorDashboard controlador = new ControladorDashboard(this.servicioDashboardMock);
    this.mockMvc = MockMvcBuilders.standaloneSetup(controlador).build();
  }

  @Test
  public void deberiaRedirigirAlLoginSiNoHayUsuarioEnSesion() throws Exception {
    this.mockMvc.perform(get("/dashboard"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));

    verify(this.servicioDashboardMock, never()).obtenerHorasEstudio(any());
  }

  @Test
  public void deberiaMostrarLaVistaDashboardConSusDatosYElUsuarioEnElModelo() throws Exception {
    Usuario usuario = crearUsuario();
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("USUARIO", usuario);

    CalendarioSemanal calendarioMock = mock(CalendarioSemanal.class);
    SesionEstudio sesionMock = new SesionEstudio();

    when(this.servicioDashboardMock.obtenerHorasEstudio(ID_USUARIO)).thenReturn(2L);
    when(this.servicioDashboardMock.porcentajeCumplimiento(ID_USUARIO)).thenReturn(85.0);
    when(this.servicioDashboardMock.obtenerCantidadMaterias(ID_USUARIO)).thenReturn(6L);
    when(this.servicioDashboardMock.obtenerCalendarioSemanal(eq(ID_USUARIO), any(LocalDate.class)))
      .thenReturn(calendarioMock);
    when(this.servicioDashboardMock.obtenerProximaSesion(ID_USUARIO)).thenReturn(sesionMock);

    this.mockMvc.perform(get("/dashboard").session(session))
      .andExpect(status().isOk())
      .andExpect(view().name(VISTA_DASHBOARD))
      .andExpect(model().attribute("usuario", usuario))
      .andExpect(model().attributeExists("dashboardData"))
      .andExpect(result -> {
        DashboardDTO dto = (DashboardDTO) result.getModelAndView().getModel().get("dashboardData");
        assertThat(dto, is(notNullValue()));
        assertThat(dto.getHorasEstudio(), equalTo(2L));
        assertThat(dto.getPorcentajeCumplimiento(), equalTo(85.0));
        assertThat(dto.getCantidadMaterias(), equalTo(6L));
        assertThat(dto.getCalendarioSemanal(), equalTo(calendarioMock));
        assertThat(dto.getProximaSesion(), equalTo(sesionMock));
      });

    verify(this.servicioDashboardMock, times(1)).obtenerHorasEstudio(ID_USUARIO);
    verify(this.servicioDashboardMock, times(1)).porcentajeCumplimiento(ID_USUARIO);
    verify(this.servicioDashboardMock, times(1)).obtenerCantidadMaterias(ID_USUARIO);
    verify(this.servicioDashboardMock, times(1))
      .obtenerCalendarioSemanal(eq(ID_USUARIO), any(LocalDate.class));
    verify(this.servicioDashboardMock, times(1)).obtenerProximaSesion(ID_USUARIO);
  }

  private Usuario crearUsuario() {
    Usuario usuario = new Usuario();
    usuario.setId(ID_USUARIO);
    usuario.setNombre("Agustina");
    usuario.setEmail("test@unlam.edu.ar");
    return usuario;
  }
}
