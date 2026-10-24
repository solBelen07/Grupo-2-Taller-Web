package com.tallerwebi.dominio.grupo;

import com.tallerwebi.dominio.Usuario;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioGrupo {
  Grupo buscar(String nombre);
  List<Grupo> listar();
  List<Grupo> listarGruposPorUsuario(String nombreUsuario);
  Integer cantidadParticipantes(String nombre);
  List<Usuario> listarIntegrantes(String nombre);
}
