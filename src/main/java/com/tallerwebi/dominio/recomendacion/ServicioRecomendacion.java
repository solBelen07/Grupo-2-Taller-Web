package com.tallerwebi.dominio.recomendacion;

import com.tallerwebi.dominio.Usuario;
import java.util.List;

public interface ServicioRecomendacion {
  MatrizCompetencias obtenerMatrizDeCompetencias(Usuario usuario);
  double obtenerAporteEnGrupos(Usuario usuario);
  List<Recomendado> recomendarCompanieros(Usuario usuario);
}
