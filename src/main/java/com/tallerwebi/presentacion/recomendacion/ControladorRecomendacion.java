package com.tallerwebi.presentacion.recomendacion;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.recomendacion.MatrizCompetencias;
import com.tallerwebi.dominio.recomendacion.Recomendado;
import com.tallerwebi.dominio.recomendacion.ServicioRecomendacion;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRecomendacion {

  private final ServicioRecomendacion servicioRecomendacion;

  @Autowired
  public ControladorRecomendacion(ServicioRecomendacion servicioRecomendacion) {
    this.servicioRecomendacion = servicioRecomendacion;
  }

  @GetMapping("/recomendaciones")
  public ModelAndView verRecomendaciones(HttpServletRequest request) {
    Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
    if (usuario == null) return new ModelAndView("redirect:/login");

    MatrizCompetencias matriz = servicioRecomendacion.obtenerMatrizDeCompetencias(usuario);
    RecomendacionDTO datos = new RecomendacionDTO(
      matriz.cumplimientoGeneral(),
      servicioRecomendacion.obtenerAporteEnGrupos(usuario),
      matriz.getNiveles(),
      armarCompanieros(servicioRecomendacion.recomendarCompanieros(usuario))
    );

    Map<String, Object> modelo = new ModelMap();
    modelo.put("RecomendacionDTO", datos);
    return new ModelAndView("paginas/grupo/recomendaciones", modelo);
  }

  private List<CompanieroDTO> armarCompanieros(List<Recomendado> recomendados) {
    return recomendados
      .stream()
      .map(recomendado ->
        new CompanieroDTO(
          recomendado.getCompaniero().getNombre(),
          recomendado.getCompaniero().getApellido(),
          recomendado.getCompaniero().getEmail(),
          recomendado.getCompatibilidad(),
          recomendado.getCumplimiento(),
          recomendado.getAporteEnGrupos(),
          recomendado.getAportaEn()
        )
      )
      .collect(Collectors.toList());
  }
}
