package com.tallerwebi.dominio.perfil;

public interface RepositorioFotoPerfil {
    FotoPerfil buscar(Long usuarioId);
    void guardar(FotoPerfil foto);
    void eliminar(Long usuarioId);
}