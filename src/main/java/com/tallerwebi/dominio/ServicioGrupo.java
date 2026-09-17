package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.GrupoNoEncontrado;

public interface ServicioGrupo {
  Grupo buscarPorNombre(String nombre) throws GrupoNoEncontrado;
  Object listarGrupos();
}
