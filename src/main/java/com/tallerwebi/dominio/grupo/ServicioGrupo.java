package com.tallerwebi.dominio.grupo;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoExistente;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NoEsAdministradorDelGrupo;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NoEsIntegranteDelGrupo;
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

  // Cualquier integrante (administrador o miembro) puede cambiar el nombre.
  void modificarGrupo(String nombreActual, String nombreNuevo, Usuario usuario)
    throws GrupoNoEncontrado, NoEsIntegranteDelGrupo, NombreDeGrupoInvalido, GrupoExistente;

  Grupo buscarGrupoDeIntegrante(String nombre, Usuario usuario)
    throws GrupoNoEncontrado, NoEsIntegranteDelGrupo;

  boolean esIntegrante(Grupo grupo, Usuario usuario);

  // Si sale el administrador, la administracion pasa al integrante mas antiguo.
  // Si no queda nadie, el grupo se elimina.
  SalidaDeGrupo salirDelGrupo(String nombre, Usuario usuario)
    throws GrupoNoEncontrado, NoEsIntegranteDelGrupo;

  void eliminarGrupo(String nombre, Usuario usuario)
    throws GrupoNoEncontrado, NoEsAdministradorDelGrupo;
  List<Usuario> listarIntegrantes(String nombre) throws GrupoNoEncontrado;
  Integer cantidadParticipantes(String nombre) throws GrupoNoEncontrado;
}
