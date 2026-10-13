package com.tallerwebi.dominio.racha;

import com.tallerwebi.dominio.Usuario;

public interface ServicioRacha {
  void registrarRacha(Usuario usuario);
  void modificarRacha(Usuario usuario);
  Integer obtenerRacha(Usuario usuario);
  Integer obtenerRecordRacha(Usuario usuario);
}
