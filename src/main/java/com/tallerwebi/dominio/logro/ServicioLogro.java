package com.tallerwebi.dominio.logro;

import com.tallerwebi.dominio.Usuario;
import java.util.List;

public interface ServicioLogro {
  // Todos los logros que existen (el catálogo).
  List<Logro> obtenerLogros();

  // revisa las condiciones de cada logro y registra los que el usuario acaba de ganar.
  void otorgarLogrosAlcanzados(Usuario usuario);

  // los logros que el usuario ya obtuvo, con su fecha.
  List<LogroObtenido> obtenerLogrosObtenidos(Usuario usuario);
}
