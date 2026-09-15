package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Grupo;
import com.tallerwebi.dominio.ServicioGrupo;
import com.tallerwebi.dominio.excepcion.GrupoNoEncontrado;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorGrupo {

  private ServicioGrupo servicioGrupo;

  @RequestMapping("/grupos")
  public ModelAndView verGrupos() {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("datosGrupo", new DatosGrupo());

    return new ModelAndView("grupos", modelo);
  }

  @GetMapping("/grupos/{nombre}")
  public ModelAndView verGrupo(@PathVariable("nombre") String nombre) {
    Map<String, Object> modelo = new ModelMap();
    try {
      Grupo grupo = servicioGrupo.buscarPorNombre(nombre);
      modelo.put("datosGrupo", grupo);
      return new ModelAndView("grupo-detalle", modelo);
    } catch (GrupoNoEncontrado e) {
      modelo.put("error", "El grupo no existe");
      return new ModelAndView("grupo-detalle", modelo);
    }
  }

  @Autowired
  public ControladorGrupo(ServicioGrupo servicioGrupo) {
    this.servicioGrupo = servicioGrupo;
  }
}
