package com.tallerwebi.dominio.materia;

import com.tallerwebi.dominio.excepcion.excepcionMateria.MateriaExistente;
import java.util.List;

public interface ServicioMateria {
  void crearMateria(Materia materia) throws MateriaExistente;
  List<Materia> obtenerMaterias();
  List<Materia> obtenerMateriasPorUsuario(Long idUsuario);
  Materia buscarMateriaPorNombre(String nombre);
  Materia buscarMateriaPorNombreYUsuario(String nombre, Long idUsuario);
  Materia buscarMateriaPorId(Integer id);
  Materia buscarMateriaPorIdYUsuario(Integer id, Long idUsuario);
  void editarMateria(Materia materia);
  void eliminarMateria(String nombre);
}
