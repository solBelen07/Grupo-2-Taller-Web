package com.tallerwebi.presentacion.parcial;

import com.tallerwebi.dominio.materia.ServicioMateria;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/parciales")
public class ControladorParcial {

  private final ServicioMateria servicioMateria;

  @Autowired
  public ControladorParcial(ServicioMateria servicioMateria) {
    this.servicioMateria = servicioMateria;
  }

  @RequestMapping(path = "/nuevo", method = RequestMethod.GET)
  public ModelAndView nuevoParcial() {
    Map<String, Object> modelo = new ModelMap();

    modelo.put("datosParcial", new DatosParcial());

    modelo.put("materias", servicioMateria.obtenerMaterias());

    return new ModelAndView("paginas/materia/parcial-formulario", modelo);
  }
}
