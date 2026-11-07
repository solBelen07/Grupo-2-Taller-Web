package com.tallerwebi.dominio.resumen;

import java.util.List;

public interface RepositorioResumen {
  public void guardar(Resumen resumen);

  public void actualizar(Resumen resumen);

  public List<Resumen> buscarPorUsuario(Long usuarioId);

  public Resumen buscarPorIdYUsuario(Long id, Long usuarioId);

  public Resumen buscarPorUsuarioYMarca(Long usuarioId, String marca);
}
