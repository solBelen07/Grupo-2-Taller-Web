package com.tallerwebi.presentacion.trabajosPracticos;

import com.tallerwebi.dominio.trabajosPracticos.DisponibilidadHoraria;
import com.tallerwebi.dominio.trabajosPracticos.EstadoTP;
import com.tallerwebi.dominio.trabajosPracticos.ServicioTrabajoPractico;
import com.tallerwebi.dominio.trabajosPracticos.TipoTrabajo;
import com.tallerwebi.dominio.trabajosPracticos.TrabajoPractico;
import java.util.ArrayList;
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

  private static final List<String> DIAS = List.of(
    "Lunes",
    "Martes",
    "Miércoles",
    "Jueves",
    "Viernes",
    "Sábado",
    "Domingo"
  );

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
    TrabajoPractico tp = new TrabajoPractico();
    completarDisponibilidades(tp);

    Map<String, Object> modelo = new ModelMap();
    modelo.put("trabajoPractico", tp);
    modelo.put("tipos", TipoTrabajo.values());
    modelo.put("estados", EstadoTP.values());
    return new ModelAndView("paginas/nuevo-trabajo-practico", modelo);
  }

  @RequestMapping(path = "/guardar", method = RequestMethod.POST)
  public ModelAndView guardarTrabajoPractico(
    @ModelAttribute("trabajoPractico") TrabajoPractico trabajoPractico
  ) {
    if (trabajoPractico.getDisponibilidades() != null) {
      trabajoPractico.getDisponibilidades().removeIf(d -> d.getHoras() == 0);
    }
    servicioTrabajoPractico.crearTrabajoPractico(trabajoPractico);
    return new ModelAndView("redirect:/trabajos-practicos");
  }

  @RequestMapping(path = "/editar/{id}", method = RequestMethod.GET)
  public ModelAndView editarTrabajoPractico(@PathVariable("id") Long id) {
    TrabajoPractico tp = servicioTrabajoPractico.buscarPorId(id);
    completarDisponibilidades(tp);

    Map<String, Object> modelo = new ModelMap();
    modelo.put("trabajoPractico", tp);
    modelo.put("tipos", TipoTrabajo.values());
    modelo.put("estados", EstadoTP.values());
    return new ModelAndView("paginas/nuevo-trabajo-practico", modelo);
  }

  @RequestMapping(path = "/eliminar/{id}", method = RequestMethod.POST)
  public String eliminarTrabajoPractico(@PathVariable("id") Long id) {
    servicioTrabajoPractico.eliminar(id);
    return "redirect:/trabajos-practicos";
  }

  private void completarDisponibilidades(TrabajoPractico tp) {
    List<DisponibilidadHoraria> completas = new ArrayList<>();
    for (String dia : DIAS) {
      DisponibilidadHoraria existente = null;
      if (tp.getDisponibilidades() != null) {
        existente =
          tp
            .getDisponibilidades()
            .stream()
            .filter(d -> dia.equals(d.getDia()))
            .findFirst()
            .orElse(null);
      }
      completas.add(existente != null ? existente : new DisponibilidadHoraria(dia, 0));
    }
    tp.setDisponibilidades(completas);
  }
}
