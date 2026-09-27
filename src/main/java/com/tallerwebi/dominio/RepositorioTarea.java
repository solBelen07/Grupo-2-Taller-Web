package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioTarea {
  void guardar(Tarea tarea);

  List<Tarea> buscarTodas();
}
