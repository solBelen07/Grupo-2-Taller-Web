package com.tallerwebi.dominio.logro;

import java.util.List;

public interface RepositorioLogroObtenido {
  void guardar(LogroObtenido logroObtenido);
  List<LogroObtenido> buscarPorUsuario(Long idUsuario);
  boolean existe(Long idUsuario, String nombreLogro);
}
