package com.tallerwebi.dominio.grupo;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioGrupo {
  Grupo buscar(String nombre);
  List<Grupo> listar();
}
