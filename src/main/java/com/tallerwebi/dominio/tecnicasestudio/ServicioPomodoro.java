package com.tallerwebi.dominio.tecnicasestudio;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import java.util.Optional;

public interface ServicioPomodoro {
  Pomodoro registrarSesionEstudio(
    String origen,
    String destino,
    int duracion,
    Materia materia,
    Usuario usuario,
    String objetivo
  );
  long obtenerSegundosRestantes(long pomodoroId);
  void modificarEstadoCompletada(long pomodoroId);
  void modificarEstadoCancelada(long pomodoroId);
  void pausarSesion(long pomodoroId);
  void reanudarSesion(long pomodoroId);
  Pomodoro buscarPorId(long id);
  Optional<Pomodoro> buscarSesionActiva(Usuario usuario);
}
