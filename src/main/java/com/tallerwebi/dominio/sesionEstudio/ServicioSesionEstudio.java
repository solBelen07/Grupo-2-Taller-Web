package com.tallerwebi.dominio.sesionEstudio;

public interface ServicioSesionEstudio {
  void crearSesion(SesionEstudio sesionEstudio);
  void editarSesion(SesionEstudio sesionEstudio);
  void eliminarSesion(SesionEstudio sesionEstudio);
  /** Marca o desmarca la sesión. Devuelve true si quedó completada, false si quedó sin completar y null si no existe. */
  Boolean cambiarEstado(Integer id);
}
