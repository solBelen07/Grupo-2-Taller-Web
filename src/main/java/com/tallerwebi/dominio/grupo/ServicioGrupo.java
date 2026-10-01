package com.tallerwebi.dominio.grupo;

import com.tallerwebi.dominio.excepcion.GrupoNoEncontrado;
import java.util.List;

public interface ServicioGrupo {
  Grupo buscarPorNombre(String nombre) throws GrupoNoEncontrado;
  List<Grupo> listarGrupos();
}
