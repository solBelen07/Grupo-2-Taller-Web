package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.GrupoNoEncontrado;
import java.util.List;

public interface ServicioGrupo {
  Grupo buscarPorNombre(String nombre) throws GrupoNoEncontrado;
  List<Grupo> listarGrupos();
}
