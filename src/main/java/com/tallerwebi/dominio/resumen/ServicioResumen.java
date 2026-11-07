package com.tallerwebi.dominio.resumen;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionResumen.ResumenInvalido;
import com.tallerwebi.dominio.excepcion.excepcionResumen.ResumenNoGenerado;
import java.util.List;

public interface ServicioResumen {
  ResultadoResumen resumir(Usuario usuario, String texto) throws ResumenInvalido, ResumenNoGenerado;
  List<Resumen> obtenerHistorial(Long usuarioId);
  Resumen buscarPorIdYUsuario(Long id, Long usuarioId);
}
