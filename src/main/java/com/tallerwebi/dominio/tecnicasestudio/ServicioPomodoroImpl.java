package com.tallerwebi.dominio.tecnicasestudio;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service("ServicioPomodoro")
@Transactional
public class ServicioPomodoroImpl implements ServicioPomodoro {

  private RepositorioPomodoro repositorioPomodoro;
  private RepositorioUsuario repositorioUsuario;

  public ServicioPomodoroImpl(
    RepositorioPomodoro repositorioPomodoro,
    RepositorioUsuario repositorioUsuario
  ) {
    this.repositorioPomodoro = repositorioPomodoro;
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public void registrarSesionEstudio(
    String origen,
    String destino,
    int duracion,
    Materia materia,
    Usuario usuario,
    String objetivo
  ) {
    Pomodoro sesion = this.repositorioPomodoro.buscarSesionesEnCurso(usuario);

    if (sesion != null) throw new RuntimeException("Ya tenes una sesion en curso");

    sesion = new Pomodoro(origen, destino, duracion, Estado.EN_CURSO, materia, usuario, objetivo);
    this.repositorioPomodoro.guardar(sesion);
  }

  @Override
  public void modificarEstadoCompletada(long pomodoroId) {
    Pomodoro sesion = this.repositorioPomodoro.buscarPorId(pomodoroId);
    if (sesion == null) throw new RuntimeException("La sesion no exite");

    sesion.setEstado(Estado.COMPLETADA);
    this.repositorioPomodoro.modificar(sesion);

    Usuario usuario = sesion.getUsuario();
    usuario.setPuntos(usuario.getPuntos() + 3);
    this.repositorioUsuario.modificar(usuario);
  }

  @Override
  public void modificarEstadoCancelada(long pomodoroId) {
    Pomodoro sesion = this.repositorioPomodoro.buscarPorId(pomodoroId);
    int puntaje;

    if (sesion == null) throw new RuntimeException("La sesion no exite");

    sesion.setEstado(Estado.CANCELADA);
    this.repositorioPomodoro.modificar(sesion);

    Usuario usuario = sesion.getUsuario();
    puntaje = (usuario.getPuntos() - 2) < 0 ? 0 : usuario.getPuntos() - 2;
    usuario.setPuntos(puntaje);
    this.repositorioUsuario.modificar(usuario);
  }
}
