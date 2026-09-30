package com.tallerwebi.dominio.tecnicasestudio;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;

public interface ServicioPomodoro {
  void registrarSesionEstudio(
    String origen,
    String destino,
    int duracion,
    Materia materia,
    Usuario usuario,
    String objetivo
  );
  void modificarEstadoCompletada(long pomodoroId);
  void modificarEstadoCancelada(long pomodoroId);
}
