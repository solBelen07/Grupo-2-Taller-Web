package com.tallerwebi.presentacion.tecnicasestudio;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.tecnicasestudio.Estado;
import com.tallerwebi.dominio.tecnicasestudio.PlanDeVuelo;
import com.tallerwebi.dominio.tecnicasestudio.Pomodoro;
import com.tallerwebi.dominio.tecnicasestudio.ServicioPomodoro;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ControladorPomodoroTest {

  private static final long ID_SESION = 1L;
  private static final String VISTA_TIMER = "paginas/tecnicasestudio/timer";

  private ServicioPomodoro servicioPomodoroMock;
  private ServicioLogin servicioLoginMock;
  private ServicioMateria servicioMateriaMock;
  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    this.servicioPomodoroMock = mock(ServicioPomodoro.class);
    this.servicioLoginMock = mock(ServicioLogin.class);
    this.servicioMateriaMock = mock(ServicioMateria.class);

    ControladorPomodoro controlador = new ControladorPomodoro(
      this.servicioPomodoroMock,
      this.servicioLoginMock,
      this.servicioMateriaMock
    );
    this.mockMvc = MockMvcBuilders.standaloneSetup(controlador).build();
  }

  @Test
  public void deberiaMostrarLaVistaTimerConLaSesionYLosSegundosRestantesEnElModelo()
    throws Exception {
    Pomodoro pomodoro = this.crearPomodoro(Estado.EN_CURSO);
    when(this.servicioPomodoroMock.obtenerSegundosRestantes(ID_SESION)).thenReturn(900L);
    when(this.servicioPomodoroMock.buscarPorId(ID_SESION)).thenReturn(pomodoro);

    this.mockMvc.perform(get("/focusflight/timer/1"))
      .andExpect(status().isOk())
      .andExpect(view().name(VISTA_TIMER))
      .andExpect(model().attribute("pomodoro", sameInstance(pomodoro)))
      .andExpect(model().attribute("segundosRestantes", 900L));
  }

  @Test
  public void deberiaMostrarLaVistaTimerAlCortarElBoleto() throws Exception {
    Pomodoro pomodoro = this.crearPomodoro(Estado.EN_CURSO);
    when(this.servicioPomodoroMock.obtenerSegundosRestantes(ID_SESION)).thenReturn(1500L);
    when(this.servicioPomodoroMock.buscarPorId(ID_SESION)).thenReturn(pomodoro);

    this.mockMvc.perform(post("/focusflight/timer/1"))
      .andExpect(status().isOk())
      .andExpect(view().name(VISTA_TIMER))
      .andExpect(model().attribute("segundosRestantes", 1500L));
  }

  @Test
  public void deberiaRedirigirAlInicioSiLaSesionDelTimerNoExiste() throws Exception {
    when(this.servicioPomodoroMock.buscarPorId(ID_SESION)).thenReturn(null);

    this.mockMvc.perform(get("/focusflight/timer/1"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/focusflight"));
  }

  @Test
  public void deberiaResponderJsonConLosSegundosRestantesYElEstadoAlConsultarPorAjax()
    throws Exception {
    when(this.servicioPomodoroMock.obtenerSegundosRestantes(ID_SESION)).thenReturn(900L);
    when(this.servicioPomodoroMock.buscarPorId(ID_SESION))
      .thenReturn(this.crearPomodoro(Estado.PAUSADO));

    this.mockMvc.perform(get("/focusflight/timer/estado/1"))
      .andExpect(status().isOk())
      .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
      .andExpect(content().string(containsString("\"segundosRestantes\":900")))
      .andExpect(content().string(containsString("\"estado\":\"PAUSADO\"")));
  }

  @Test
  public void deberiaInformarEstadoInexistenteAlConsultarPorAjaxUnaSesionQueNoExiste()
    throws Exception {
    when(this.servicioPomodoroMock.obtenerSegundosRestantes(ID_SESION)).thenReturn(0L);
    when(this.servicioPomodoroMock.buscarPorId(ID_SESION)).thenReturn(null);

    this.mockMvc.perform(get("/focusflight/timer/estado/1"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("\"segundosRestantes\":0")))
      .andExpect(content().string(containsString("\"estado\":\"INEXISTENTE\"")));
  }

  @Test
  public void noDeberiaPermitirConsultarElEstadoPorAjaxConPost() throws Exception {
    this.mockMvc.perform(post("/focusflight/timer/estado/1"))
      .andExpect(status().isMethodNotAllowed());
  }

  @Test
  public void deberiaPausarLaSesionYRedirigirAlTimer() throws Exception {
    this.mockMvc.perform(post("/focusflight/timer/pausar/1"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/focusflight/timer/1"));

    verify(this.servicioPomodoroMock, times(1)).pausarSesion(ID_SESION);
  }

  @Test
  public void deberiaReanudarLaSesionYRedirigirAlTimer() throws Exception {
    this.mockMvc.perform(post("/focusflight/timer/reanudar/1"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/focusflight/Ttmer/1"));

    verify(this.servicioPomodoroMock, times(1)).reanudarSesion(ID_SESION);
  }

  @Test
  public void deberiaCancelarLaSesionYRedirigirAlInicioDeFocusFlight() throws Exception {
    this.mockMvc.perform(post("/focusflight/timer/cancelar/1"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/FocusFlight"));

    verify(this.servicioPomodoroMock, times(1)).modificarEstadoCancelada(ID_SESION);
  }

  @Test
  public void noDeberiaPermitirPausarNiCancelarConGet() throws Exception {
    this.mockMvc.perform(get("/focusflight/timer/pausar/1"))
      .andExpect(status().isMethodNotAllowed());
    this.mockMvc.perform(get("/focusflight/timer/cancelar/1"))
      .andExpect(status().isMethodNotAllowed());

    verify(this.servicioPomodoroMock, never()).pausarSesion(ID_SESION);
    verify(this.servicioPomodoroMock, never()).modificarEstadoCancelada(ID_SESION);
  }

  @Test
  public void deberiaMostrarElFormularioDeConfiguracionConLasMateriasSiNoHaySesionActiva()
    throws Exception {
    Usuario usuario = this.crearUsuario();
    List<Materia> materias = List.of(new Materia());
    when(this.servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(usuario);
    when(this.servicioPomodoroMock.buscarSesionActiva(usuario)).thenReturn(Optional.empty());
    when(this.servicioMateriaMock.obtenerMaterias()).thenReturn(materias);

    this.mockMvc.perform(get("/focusflight"))
      .andExpect(status().isOk())
      .andExpect(view().name("paginas/tecnicasestudio/pomodoro"))
      .andExpect(model().attribute("materias", materias))
      .andExpect(model().attributeExists("datosPomodoro"));
  }

  @Test
  public void deberiaRedirigirAlTimerSiElUsuarioYaTieneUnaSesionActiva() throws Exception {
    Usuario usuario = this.crearUsuario();
    Pomodoro sesionActiva = this.crearPomodoro(Estado.PAUSADO);
    when(this.servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(usuario);
    when(this.servicioPomodoroMock.buscarSesionActiva(usuario))
      .thenReturn(Optional.of(sesionActiva));

    this.mockMvc.perform(get("/focusflight"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/focusflight/timer/1"));

    verify(this.servicioMateriaMock, never()).obtenerMaterias();
  }

  @Test
  public void deberiaRegistrarLaSesionYMostrarElBoleto() throws Exception {
    Usuario usuario = this.crearUsuario();
    Materia materia = new Materia();
    materia.setNombre("Taller Web I");
    Pomodoro registrada = this.crearPomodoro(Estado.EN_CURSO);
    when(this.servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(usuario);
    when(this.servicioMateriaMock.buscarMateriaPorId(3)).thenReturn(materia);
    when(
      this.servicioPomodoroMock.registrarSesionEstudio(
          eq("ARG"),
          eq("ESP"),
          eq(25),
          eq(materia),
          eq(usuario),
          eq("Repasar patrones")
        )
    )
      .thenReturn(registrada);

    this.mockMvc.perform(
        post("/focusflight/boleto")
          .param("origen", "ARG")
          .param("destino", "ESP")
          .param("duracion", "25")
          .param("materia", "3")
          .param("objetivo", "Repasar patrones")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("paginas/tecnicasestudio/boleto"))
      .andExpect(model().attribute("materia", sameInstance(materia)))
      .andExpect(model().attribute("pomodoroId", ID_SESION));

    verify(this.servicioPomodoroMock, times(1))
      .registrarSesionEstudio(any(), any(), eq(25), any(), any(), any());
  }

  private Usuario crearUsuario() {
    Usuario usuario = new Usuario();
    usuario.setId(10L);
    usuario.setEmail("test@unlam.edu.ar");
    return usuario;
  }

  private Pomodoro crearPomodoro(Estado estado) {
    Pomodoro pomodoro = new Pomodoro();
    pomodoro.setId(ID_SESION);
    pomodoro.setEstado(estado);
    pomodoro.setDuracionMinutos(25);
    pomodoro.setPlan(new PlanDeVuelo("Taller Web I", "ARG", "ESP", "Repasar patrones"));
    return pomodoro;
  }
}
