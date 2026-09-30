package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.tecnicasestudio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioPomodoroTest {

  private ServicioPomodoro servicioPomodoro;
  private RepositorioPomodoro repositorioPomodoroMock;
  private RepositorioUsuario repositorioUsuarioMock;

  @BeforeEach
  public void init() {
    this.repositorioPomodoroMock = mock(RepositorioPomodoro.class);
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.servicioPomodoro =
      new ServicioPomodoroImpl(this.repositorioPomodoroMock, this.repositorioUsuarioMock);
  }

  @Test
  public void registrarSesionEstudioExitosa() {
    this.servicioPomodoro.registrarSesionEstudio(
        "ARG",
        "ESP",
        25,
        new Materia(),
        usuarioNuevoMockeado(),
        "Estudiar para Taller Web"
      );
    verify(this.repositorioPomodoroMock, times(1)).guardar(any(Pomodoro.class));
  }

  @Test
  public void registrarSesionEstudioDeberiaLanzarExcepcionSiYaExisteUnaSesionEnCurso() {
    Usuario usuario = usuarioNuevoMockeado();
    Materia materia = new Materia();
    Pomodoro sesionEnCurso = new Pomodoro();

    when(this.repositorioPomodoroMock.buscarSesionesEnCurso(usuario)).thenReturn(sesionEnCurso);

    RuntimeException excepcion = assertThrows(
      RuntimeException.class,
      () -> {
        this.servicioPomodoro.registrarSesionEstudio(
            "ARG",
            "ESP",
            25,
            materia,
            usuario,
            "Estudiar para Taller Web"
          );
      }
    );

    assertThat(excepcion.getMessage(), equalTo("Ya tenes una sesion en curso"));
    verify(this.repositorioPomodoroMock, never()).guardar(any(Pomodoro.class));
  }

  @Test
  public void modificarEstadoCompletadaExitosamenteYSumarPuntos() {
    Usuario usuario = usuarioNuevoMockeado();
    Pomodoro sesion = crearPomodoroMockeado(usuario, Estado.EN_CURSO);

    when(this.repositorioPomodoroMock.buscarPorId(1L)).thenReturn(sesion);

    this.servicioPomodoro.modificarEstadoCompletada(1L);

    assertThat(sesion.getEstado(), equalTo(Estado.COMPLETADA));
    assertThat(usuario.getPuntos(), equalTo(3));

    verify(this.repositorioPomodoroMock, times(1)).modificar(sesion);
    verify(this.repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void modificarEstadoCompletadaDeberiaLanzarExcepcionSiLaSesionNoExiste() {
    when(this.repositorioPomodoroMock.buscarPorId(1L)).thenReturn(null);

    RuntimeException excepcion = assertThrows(
      RuntimeException.class,
      () -> {
        this.servicioPomodoro.modificarEstadoCompletada(1L);
      }
    );

    assertThat(excepcion.getMessage(), equalTo("La sesion no exite"));

    verify(this.repositorioPomodoroMock, never()).modificar(any());
    verify(this.repositorioUsuarioMock, never()).modificar(any());
  }

  @Test
  public void modificarEstadoCanceladaExitosamenteYRestarPuntos() {
    Usuario usuario = usuarioNuevoMockeado();
    Pomodoro sesion = crearPomodoroMockeado(usuario, Estado.EN_CURSO);
    usuario.setPuntos(10);

    when(this.repositorioPomodoroMock.buscarPorId(1L)).thenReturn(sesion);

    this.servicioPomodoro.modificarEstadoCancelada(1L);

    assertThat(sesion.getEstado(), equalTo(Estado.CANCELADA));
    assertThat(usuario.getPuntos(), equalTo(8));

    verify(this.repositorioPomodoroMock, times(1)).modificar(sesion);
    verify(this.repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void modificarEstadoCanceladaNoDeberiaDejarPuntosNegativos() {
    Usuario usuario = usuarioNuevoMockeado();
    Pomodoro sesion = crearPomodoroMockeado(usuario, Estado.EN_CURSO);

    usuario.setPuntos(1);

    when(this.repositorioPomodoroMock.buscarPorId(1L)).thenReturn(sesion);

    this.servicioPomodoro.modificarEstadoCancelada(1L);

    assertThat(usuario.getPuntos(), equalTo(0));

    verify(this.repositorioPomodoroMock, times(1)).modificar(sesion);
    verify(this.repositorioUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void modificarEstadoCanceladaDeberiaLanzarExcepcionSiLaSesionNoExiste() {
    when(this.repositorioPomodoroMock.buscarPorId(1L)).thenReturn(null);

    RuntimeException excepcion = assertThrows(
      RuntimeException.class,
      () -> {
        this.servicioPomodoro.modificarEstadoCancelada(1L);
      }
    );

    assertThat(excepcion.getMessage(), equalTo("La sesion no exite"));

    verify(this.repositorioPomodoroMock, never()).modificar(any());
    verify(this.repositorioUsuarioMock, never()).modificar(any());
  }

  private Usuario usuarioNuevoMockeado() {
    Usuario usuario = new Usuario();
    usuario.setEmail("nuevo@test.com");
    usuario.setPassword("1234");
    when(this.repositorioUsuarioMock.buscarUsuario(usuario.getEmail(), usuario.getPassword()))
      .thenReturn(usuario);
    return usuario;
  }

  private Pomodoro crearPomodoroMockeado(Usuario usuario, Estado estado) {
    Pomodoro pomodoro = new Pomodoro();
    pomodoro.setUsuario(usuario);
    pomodoro.setEstado(estado);
    return pomodoro;
  }
}
