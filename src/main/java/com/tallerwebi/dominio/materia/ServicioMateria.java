package com.tallerwebi.dominio.materia;

import java.util.List;

import com.tallerwebi.dominio.excepcion.excepcionMateria.MateriaExistente;

public interface ServicioMateria {
  void crearMateria(Materia materia) throws MateriaExistente;
  List<Materia> obtenerMaterias();
  Materia buscarMateriaPorNombre(String nombre);
  Materia buscarMateriaPorId(Integer id);
  void editarMateria(Materia materia);
  void eliminarMateria(String nombre);
}
