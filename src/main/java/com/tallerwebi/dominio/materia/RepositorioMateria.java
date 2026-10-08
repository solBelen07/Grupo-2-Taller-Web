package com.tallerwebi.dominio.materia;

import java.util.List;

public interface RepositorioMateria {
  void guardar(Materia materia);
  List<Materia> obtenerTodas();
  Materia buscarPorNombre(String nombre);
  Materia buscarPorId(Integer id);
  void modificar(Materia materia);
  void eliminar(Materia materia);
  Long obtenerCantidadMateriasPorUsuario(Long idUsuario);
}
