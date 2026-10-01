package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.ServicioCalendario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.calendario.CalendarioMensual;
import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorCalendarioTest {

  private static final Long USUARIO_ID = 5L;
  private static final LocalDate HOY = LocalDate.of(2026, 9, 28);
  private static final Clock RELOJ = Clock.fixed(
    Instant.parse("2026-09-28T13:00:00Z"),
    ZoneId.of("America/Argentina/Buenos_Aires")
  );

  private ServicioCalendario servicioCalendarioMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private ControladorCalendario controladorCalendario;
  private CalendarioMensual mes;
  private CalendarioSemanal semana;

  @BeforeEach
  public void init() {
    servicioCalendarioMock = mock(ServicioCalendario.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    when(requestMock.getSession()).thenReturn(sessionMock);
    Usuario usuario = new Usuario();
    usuario.setId(USUARIO_ID);
    when(sessionMock.getAttribute("USUARIO")).thenReturn(usuario);
    mes = new CalendarioMensual("septiembre de 2026", HOY, HOY, HOY, List.of(), List.of(), 0);
    semana =
      new CalendarioSemanal(
        "28 sep – 4 oct 2026",
        HOY,
        HOY,
        HOY,
        List.of(),
        7,
        22,
        List.of(),
        List.of(),
        0
      );
    when(servicioCalendarioMock.obtenerMes(USUARIO_ID, HOY)).thenReturn(mes);
    when(servicioCalendarioMock.obtenerSemana(USUARIO_ID, HOY)).thenReturn(semana);
    controladorCalendario = new ControladorCalendario(servicioCalendarioMock, RELOJ);
  }

  @Test
  public void deberiaMostrarElMesActualPorDefecto() {
    // ejecucion
    ModelAndView modelAndView = controladorCalendario.verCalendario(null, null, requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), is("calendario"));
    assertThat(modelAndView.getModel().get("vista"), is("mes"));
    assertThat(modelAndView.getModel().get("fecha"), is(HOY));
    assertThat(modelAndView.getModel().get("calendario"), sameInstance(mes));
    assertThat(modelAndView.getModel().get("error"), is(nullValue()));
    verify(servicioCalendarioMock).obtenerMes(USUARIO_ID, HOY);
  }

  @Test
  public void deberiaMostrarLaVistaSemanalCuandoSeLaPide() {
    // preparacion
    LocalDate pedida = LocalDate.of(2026, 9, 30);
    CalendarioSemanal otra = new CalendarioSemanal(
      "otra",
      HOY,
      HOY,
      HOY,
      List.of(),
      7,
      22,
      List.of(),
      List.of(),
      0
    );
    when(servicioCalendarioMock.obtenerSemana(USUARIO_ID, pedida)).thenReturn(otra);

    // ejecucion
    ModelAndView modelAndView = controladorCalendario.verCalendario(
      "semana",
      "2026-09-30",
      requestMock
    );

    // validacion
    assertThat(modelAndView.getModel().get("vista"), is("semana"));
    assertThat(modelAndView.getModel().get("fecha"), is(pedida));
    assertThat(modelAndView.getModel().get("calendario"), sameInstance(otra));
    verify(servicioCalendarioMock, never()).obtenerMes(USUARIO_ID, pedida);
  }

  @Test
  public void deberiaAceptarLaVistaEnCualquierCapitalizacion() {
    // ejecucion
    ModelAndView modelAndView = controladorCalendario.verCalendario("SEMANA", null, requestMock);

    // validacion
    assertThat(modelAndView.getModel().get("vista"), is("semana"));
    assertThat(modelAndView.getModel().get("calendario"), sameInstance(semana));
  }

  @Test
  public void deberiaCaerEnLaVistaMensualSiLaVistaEsDesconocida() {
    // ejecucion
    ModelAndView modelAndView = controladorCalendario.verCalendario("anio", null, requestMock);

    // validacion
    assertThat(modelAndView.getModel().get("vista"), is("mes"));
    assertThat(modelAndView.getModel().get("calendario"), sameInstance(mes));
  }

  @Test
  public void deberiaAvisarYMostrarHoyCuandoLaFechaNoEsValida() {
    // ejecucion
    ModelAndView modelAndView = controladorCalendario.verCalendario(
      "mes",
      "no-es-una-fecha",
      requestMock
    );

    // validacion
    assertThat(modelAndView.getViewName(), is("calendario"));
    assertThat(modelAndView.getModel().get("error"), is(notNullValue()));
    assertThat(modelAndView.getModel().get("fecha"), is(HOY));
    assertThat(modelAndView.getModel().get("calendario"), sameInstance(mes));
  }

  @Test
  public void deberiaAvisarCuandoLaFechaEstaFueraDelRangoSoportado() {
    // ejecucion
    ModelAndView modelAndView = controladorCalendario.verCalendario(
      "mes",
      "1800-01-01",
      requestMock
    );

    // validacion
    assertThat(modelAndView.getModel().get("error"), is(notNullValue()));
    assertThat(modelAndView.getModel().get("fecha"), is(HOY));
  }

  @Test
  public void deberiaAvisarCuandoLaFechaSuperaElRangoSoportado() {
    // ejecucion
    ModelAndView modelAndView = controladorCalendario.verCalendario(
      "mes",
      "2200-01-01",
      requestMock
    );

    // validacion
    assertThat(modelAndView.getModel().get("error"), is(notNullValue()));
    assertThat(modelAndView.getModel().get("fecha"), is(HOY));
  }

  @Test
  public void deberiaIgnorarUnaFechaEnBlanco() {
    // ejecucion
    ModelAndView modelAndView = controladorCalendario.verCalendario("mes", "   ", requestMock);

    // validacion
    assertThat(modelAndView.getModel().get("error"), is(nullValue()));
    assertThat(modelAndView.getModel().get("fecha"), is(HOY));
  }

  @Test
  public void deberiaRedirigirALoginCuandoNoHayUsuarioEnSesion() {
    // preparacion
    when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

    // ejecucion
    ModelAndView modelAndView = controladorCalendario.verCalendario(null, null, requestMock);

    // validacion
    assertThat(modelAndView.getViewName(), is("redirect:/login"));
    verify(servicioCalendarioMock, never()).obtenerMes(USUARIO_ID, HOY);
  }
}
