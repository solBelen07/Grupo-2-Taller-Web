package com.tallerwebi.dominio.tecnicasestudio;

import java.time.LocalDate;
import java.util.*;

public interface RepositorioPomodoro {
  void guardar(Pomodoro pomodoro);
  void actualizar(Pomodoro pomodoro);
  Optional<Pomodoro> buscarPorId(Long id);
  Optional<Pomodoro> buscarPorUsuarioYEstado(Long idUsuario, Estado estado);
  long calcularMinutosCompletados(Long idUsuario);
  boolean existePomodoroCompletadoEnFecha(Long idUsuario, LocalDate fecha);
}
