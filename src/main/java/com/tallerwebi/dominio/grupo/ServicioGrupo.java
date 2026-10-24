package com.tallerwebi.dominio.grupo;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
import java.util.List;

public interface ServicioGrupo {
  Grupo buscarPorNombre(String nombre) throws GrupoNoEncontrado;
  List<Grupo> listarGrupos();
  List<Grupo> listarGruposDelUsuario(String email);
  List<Usuario> listarIntegrantes(String nombre) throws GrupoNoEncontrado;
  Integer cantidadParticipantes(String nombre) throws GrupoNoEncontrado;
}
