package com.tallerwebi.dominio.grupo;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoExistente;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NoEsAdministradorDelGrupo;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NombreDeGrupoInvalido;
import java.util.List;

public interface ServicioGrupo {
  Grupo buscarPorNombre(String nombre) throws GrupoNoEncontrado;
  List<Grupo> listarGrupos();
  List<Grupo> listarGruposDelUsuario(String email);

  // ABM de grupo
  Grupo crearGrupo(String nombre, Usuario creador) throws NombreDeGrupoInvalido, GrupoExistente;

  Grupo buscarGrupoAdministrado(String nombre, Usuario usuario)
    throws GrupoNoEncontrado, NoEsAdministradorDelGrupo;

  void modificarGrupo(String nombreActual, String nombreNuevo, Usuario usuario)
    throws GrupoNoEncontrado, NoEsAdministradorDelGrupo, NombreDeGrupoInvalido, GrupoExistente;

  void eliminarGrupo(String nombre, Usuario usuario)
    throws GrupoNoEncontrado, NoEsAdministradorDelGrupo;
  List<Usuario> listarIntegrantes(String nombre) throws GrupoNoEncontrado;
  Integer cantidadParticipantes(String nombre) throws GrupoNoEncontrado;
}
