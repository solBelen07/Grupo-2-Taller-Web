package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Materia;
import com.tallerwebi.dominio.ServicioMateria;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

public class ControladorMateria {

  private final ServicioMateria servicioMateria;

  public ControladorMateria(ServicioMateria servicioMateria) {
    this.servicioMateria = servicioMateria;
  }

  @RequestMapping("/materias")
  public ModelAndView verMaterias() {
    ModelAndView modelAndView = new ModelAndView("materias");
    List<Materia> materias = servicioMateria.listarMaterias();
    modelAndView.addObject("materias", materias);
    return modelAndView;
  }
}
