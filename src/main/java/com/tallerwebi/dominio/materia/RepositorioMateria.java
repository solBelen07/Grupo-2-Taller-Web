package com.tallerwebi.dominio.materia;

import java.util.List;

public interface RepositorioMateria {
  void guardar(Materia materia);
  List<Materia> obtenerTodas();
  List<Materia> obtenerPorUsuario(Long idUsuario);
  Materia buscarPorNombre(String nombre);
  Materia buscarPorNombreYUsuario(String nombre, Long idUsuario);
  Materia buscarPorId(Integer id);
  Materia buscarPorIdYUsuario(Integer id, Long idUsuario);
  void modificar(Materia materia);
  void eliminar(Materia materia);
  Long obtenerCantidadMateriasPorUsuario(Long idUsuario);
}
