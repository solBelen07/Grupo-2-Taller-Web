package com.tallerwebi.dominio.sesionEstudio;

import java.time.LocalDate;

public interface RepositorioSesionEstudio {
  void guardar(SesionEstudio sesionEstudio);
  void modificar(SesionEstudio sesionEstudio);
  void eliminar(SesionEstudio sesionEstudio);
  SesionEstudio buscarPorId(Integer id);
  void eliminarPorMateriaId(Integer materiaId);
  SesionEstudio buscarProximaSesionEstudio(Long idUsuario);
  long calcularHorasCompletadas(Long idUsuario);
  boolean existeSesionCompletadaEnFecha(Long idUsuario, LocalDate fecha);
}
