package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioMateria {
  Materia consultarMateria(String nombre);
  List<Materia> listarMaterias();
}
