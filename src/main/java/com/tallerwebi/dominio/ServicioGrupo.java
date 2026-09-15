package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.GrupoNoEncontrado;

@FunctionalInterface
public interface ServicioGrupo {
  Grupo buscarPorNombre(String nombre) throws GrupoNoEncontrado;
}
