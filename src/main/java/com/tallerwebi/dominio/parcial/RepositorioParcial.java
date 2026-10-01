package com.tallerwebi.dominio.parcial;

import java.util.List;

public interface RepositorioParcial {
  void guardar(Parcial parcial);

  Parcial buscarPorId(Integer id);

  List<Parcial> obtenerTodos();

  void modificar(Parcial parcial);

  void eliminar(Parcial parcial);

  void eliminarPorMateriaId(Integer materiaId);
}
