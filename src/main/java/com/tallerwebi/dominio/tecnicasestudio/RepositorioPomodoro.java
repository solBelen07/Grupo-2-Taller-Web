package com.tallerwebi.dominio.tecnicasestudio;

import com.tallerwebi.dominio.Usuario;

public interface RepositorioPomodoro {
  void guardar(Pomodoro sesion);
  void modificar(Pomodoro sesion);

  Pomodoro buscarPorId(long id);
  Pomodoro buscarSesionesEnCurso(Usuario usuario);

  Integer obtenerTotalMinutosCompletados(Usuario usuario);
}
