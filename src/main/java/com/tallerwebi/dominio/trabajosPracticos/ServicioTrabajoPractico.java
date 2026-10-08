package com.tallerwebi.dominio.trabajosPracticos;

import java.util.List;

public interface ServicioTrabajoPractico {
  TrabajoPractico crearTrabajoPractico(TrabajoPractico trabajoPractico);
  TrabajoPractico cambiarEstado(Long id, EstadoTP nuevoEstado);
  List<TrabajoPractico> obtenerTodos();
  List<TrabajoPractico> buscarPorMateria(String materiaFiltro);
  TrabajoPractico buscarPorId(Long id);
  void eliminar(Long id);
}
