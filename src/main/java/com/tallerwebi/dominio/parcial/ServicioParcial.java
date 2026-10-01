package com.tallerwebi.dominio.parcial;

import java.time.LocalDate;
import java.util.List;

public interface ServicioParcial {
  void crearParcial(Parcial parcial);
  Parcial buscarParcialPorId(Integer id);
  List<Parcial> obtenerParciales();
  void editarParcial(Parcial parcial);
  void eliminarParcial(Integer id);
  void agregarTema(Integer parcialId, String tema, LocalDate fecha, Integer cantidadHoras);
}
