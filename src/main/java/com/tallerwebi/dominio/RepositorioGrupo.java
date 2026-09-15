package com.tallerwebi.dominio;

@FunctionalInterface
public interface RepositorioGrupo {
  Grupo buscar(String nombre);
}
