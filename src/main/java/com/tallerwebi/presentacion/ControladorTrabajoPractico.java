package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.EstadoTP;
import com.tallerwebi.dominio.ServicioTrabajoPractico;
import com.tallerwebi.dominio.TipoTrabajo;
import com.tallerwebi.dominio.TrabajoPractico;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/trabajos-practicos")
public class ControladorTrabajoPractico {

  private final ServicioTrabajoPractico servicioTrabajoPractico;

  @Autowired
  public ControladorTrabajoPractico(ServicioTrabajoPractico servicioTrabajoPractico) {
    this.servicioTrabajoPractico = servicioTrabajoPractico;
  }

  @RequestMapping(method = RequestMethod.GET)
  public ModelAndView listarTrabajosPracticos(
    @RequestParam(value = "materia", required = false) String materia
  ) {
    List<TrabajoPractico> lista = (materia != null && !materia.isEmpty())
      ? servicioTrabajoPractico.buscarPorMateria(materia)
      : servicioTrabajoPractico.obtenerTodos();

    Map<String, Object> modelo = new ModelMap();
    modelo.put("pendientes", filtrar(lista, EstadoTP.PENDIENTE));
    modelo.put("enCurso", filtrar(lista, EstadoTP.EN_CURSO));
    modelo.put("finalizados", filtrar(lista, EstadoTP.FINALIZADO));
    modelo.put("materiaBuscada", materia);
    return new ModelAndView("paginas/trabajos-practicos", modelo);
  }

  private List<TrabajoPractico> filtrar(List<TrabajoPractico> lista, EstadoTP estado) {
    return lista.stream().filter(tp -> tp.getEstado() == estado).toList();
  }

  @RequestMapping(path = "/nuevo", method = RequestMethod.GET)
  public ModelAndView irANuevoTrabajoPractico() {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("trabajoPractico", new TrabajoPractico());
    modelo.put("tipos", TipoTrabajo.values());
    modelo.put("estados", EstadoTP.values());
    return new ModelAndView("paginas/nuevo-trabajo-practico", modelo);
  }

  @RequestMapping(path = "/guardar", method = RequestMethod.POST)
  public ModelAndView guardarTrabajoPractico(
    @ModelAttribute("trabajoPractico") TrabajoPractico trabajoPractico
  ) {
    servicioTrabajoPractico.crearTrabajoPractico(trabajoPractico);
    return new ModelAndView("redirect:/trabajos-practicos");
  }

  @RequestMapping(path = "/editar/{id}", method = RequestMethod.GET)
  public ModelAndView editarTrabajoPractico(@PathVariable("id") Long id) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("trabajoPractico", servicioTrabajoPractico.buscarPorId(id));
    modelo.put("tipos", TipoTrabajo.values());
    modelo.put("estados", EstadoTP.values());
    return new ModelAndView("paginas/nuevo-trabajo-practico", modelo);
  }

  @RequestMapping(path = "/eliminar/{id}", method = RequestMethod.POST)
  public String eliminarTrabajoPractico(@PathVariable("id") Long id) {
    servicioTrabajoPractico.eliminar(id);
    return "redirect:/trabajos-practicos";
  }
}
