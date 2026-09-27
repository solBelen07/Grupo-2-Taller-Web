package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioMateria {
  void guardar(Materia materia);
  List<Materia> obtenerTodas();
  Materia buscarPorNombre(String nombre);
  void modificar(Materia materia);
  void eliminar(Materia materia);
}
