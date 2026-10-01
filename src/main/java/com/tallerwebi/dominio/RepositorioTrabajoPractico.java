package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioTrabajoPractico {
  TrabajoPractico save(TrabajoPractico any);
  TrabajoPractico buscarPorId(Long id);
  List<TrabajoPractico> obtenerTodos();
  List<TrabajoPractico> buscarPorMateria(String materia);
  List<TrabajoPractico> buscarPorNombre(String nombre);
  List<TrabajoPractico> buscarPorTipo(TipoTrabajo tipo);
  void eliminar(TrabajoPractico trabajoPractico);
}
