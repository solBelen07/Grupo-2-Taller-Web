package com.tallerwebi.dominio.materialBibliografico;

import java.util.List;

public interface RepositorioMaterial {
  void guardar(MaterialBibliografico material);
  MaterialBibliografico buscarPorId(Integer id);
  List<MaterialBibliografico> buscarPorMateria(Integer materiaId);
  void eliminar(MaterialBibliografico material);
}
