package com.tallerwebi.dominio.sesionEstudio;

public interface ServicioSesionEstudio {
  void crearSesion(SesionEstudio sesionEstudio);
  void editarSesion(SesionEstudio sesionEstudio);
  void eliminarSesion(SesionEstudio sesionEstudio);
  void cambiarEstado(Integer id);
}
