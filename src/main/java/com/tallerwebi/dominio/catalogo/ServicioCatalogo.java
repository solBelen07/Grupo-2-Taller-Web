package com.tallerwebi.dominio.catalogo;

import java.util.List;

public interface ServicioCatalogo {
  List<Universidad> obtenerUniversidades();
  List<Carrera> obtenerCarreras();
  List<Interes> obtenerIntereses();
}
