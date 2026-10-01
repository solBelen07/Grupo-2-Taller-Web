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

  @RequestMapping(value = "/grupos", method = RequestMethod.GET)
  public ModelAndView verGrupos() {
    Map<String, Object> modelo = new ModelMap();
    try {
      modelo.put("datosGrupo", servicioGrupo.listarGrupos());
    } catch (Exception e) {
      modelo.put("error", "No se pudieron cargar los grupos");
    }
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

  @GetMapping("/grupos/{nombre}/invitar")
  public ModelAndView irAInvitar(@PathVariable("nombre") String grupoAInvitar) {
    Map<String, Object> modelo = new ModelMap();
    try {
      Grupo grupo = servicioGrupo.buscarPorNombre(grupoAInvitar);
      modelo.put("datosGrupo", grupo);
      return new ModelAndView("grupo-invitar", modelo);
    } catch (GrupoNoEncontrado e) {
      modelo.put("error", "El grupo no existe");
      return new ModelAndView("grupo-invitar", modelo);
    }
  }

  @Autowired
  public ControladorGrupo(ServicioGrupo servicioGrupo) {
    this.servicioGrupo = servicioGrupo;
  }
}
