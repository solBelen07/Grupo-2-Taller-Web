package com.tallerwebi.presentacion.gemini;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.ServicioMateria;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorIA {

  private final ServicioMateria servicioMateria;

  public ControladorIA(ServicioMateria servicioMateria) {
    this.servicioMateria = servicioMateria;
  }

  @GetMapping("/ia")
  public ModelAndView mostrarChatIA(HttpServletRequest request) {
    Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    Map<String, Object> modelo = new HashMap<>();
    modelo.put("materias", servicioMateria.obtenerMateriasPorUsuario(usuario.getId()));

    return new ModelAndView("paginas/ia/ia", modelo);
  }
}
