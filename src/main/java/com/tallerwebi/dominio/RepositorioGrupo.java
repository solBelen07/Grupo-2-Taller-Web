package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioGrupo {
  Grupo buscar(String nombre);
  List<Grupo> listar();
}
