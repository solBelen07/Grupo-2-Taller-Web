package com.tallerwebi.dominio.perfil;

import com.tallerwebi.dominio.Usuario;

public interface ServicioPerfil {
  Usuario obtenerUsuario(String email);
  Usuario actualizarDatos(String email, DatosPerfil datos);
  void cambiarPassword(String email, DatosCambioPassword datos);
  void guardarFoto(String email, String tipoContenido, byte[] contenido);
  void eliminarFoto(String email);
  FotoPerfil obtenerFoto(String email);
  boolean tieneFoto(Long usuarioId);
}
