package com.tallerwebi.presentacion.parcial;

import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.parcial.Parcial;
import com.tallerwebi.dominio.parcial.ServicioParcial;
import com.tallerwebi.dominio.sesionEstudio.ServicioSesionEstudio;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/parciales")
public class ControladorParcial {

  private final ServicioMateria servicioMateria;
  private final ServicioParcial servicioParcial;
  private final ServicioSesionEstudio servicioSesionEstudio;

  @Autowired
  public ControladorParcial(
    ServicioMateria servicioMateria,
    ServicioParcial servicioParcial,
    ServicioSesionEstudio servicioSesionEstudio
  ) {
    this.servicioMateria = servicioMateria;
    this.servicioParcial = servicioParcial;
    this.servicioSesionEstudio = servicioSesionEstudio;
  }

  @RequestMapping(path = "/nuevo", method = RequestMethod.GET)
  public ModelAndView nuevoParcial() {
    Map<String, Object> modelo = new ModelMap();

    modelo.put("datosParcial", new DatosParcial());
    modelo.put("materias", servicioMateria.obtenerMaterias());

    return new ModelAndView("paginas/materia/parcial-formulario", modelo);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ModelAndView listarParciales() {
    Map<String, Object> modelo = new ModelMap();

    modelo.put("parciales", servicioParcial.obtenerParciales());

    return new ModelAndView("parciales", modelo);
  }

  @RequestMapping(path = "/guardar", method = RequestMethod.POST)
  public ModelAndView guardarParcial(@ModelAttribute("datosParcial") DatosParcial datosParcial) {
    Materia materia = servicioMateria.buscarMateriaPorId(datosParcial.getMateriaId().intValue());
    Parcial parcial = new Parcial();

    parcial.setMateria(materia);
    parcial.setFecha(datosParcial.getFecha());
    parcial.setHorario(datosParcial.getHorario());
    parcial.setCantidadDiasEstudio(datosParcial.getCantidadDiasEstudio());
    parcial.setHorasPorDia(datosParcial.getHorasPorDia());
    parcial.setNombre(datosParcial.getNombre());
    servicioParcial.crearParcial(parcial);

    LocalDate fechaInicioEstudio = datosParcial
      .getFecha()
      .minusDays(datosParcial.getCantidadDiasEstudio());

    List<String> temas = datosParcial.getTemas();

    for (int i = 0; i < temas.size(); i++) {
      String tema = temas.get(i);

      if (tema != null && !tema.trim().isEmpty()) {
        LocalDate fechaSesion = fechaInicioEstudio.plusDays(i);
        servicioParcial.agregarTema(
          parcial.getId(),
          tema,
          fechaSesion,
          datosParcial.getHorasPorDia()
        );
      }
    }

    return new ModelAndView("redirect:/parciales");
  }

  @RequestMapping(path = "/sesion/completar", method = RequestMethod.POST)
  public ModelAndView cambiarEstadoSesion(@RequestParam("id") Integer id) {
    servicioSesionEstudio.cambiarEstado(id);
    return new ModelAndView("redirect:/parciales");
  }
}
