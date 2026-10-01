package com.tallerwebi.presentacion.materia;

import com.tallerwebi.dominio.excepcion.excepcionMateria.MateriaExistente;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorMateria {

  private static final String DATOS_MATERIA = "datosMateria";
  private final ServicioMateria servicioMateria;

  public ControladorMateria(ServicioMateria servicioMateria) {
    this.servicioMateria = servicioMateria;
  }

  @RequestMapping(path = "/materias/nueva", method = RequestMethod.GET)
  public ModelAndView nuevaMateria() {
    Map<String, Object> model = new ModelMap();

    model.put(DATOS_MATERIA, new DatosMateria());

    return new ModelAndView("paginas/materia/materia-formulario", model);
  }

  @RequestMapping(path = "/materias/guardar", method = RequestMethod.POST)
  public ModelAndView guardarMateria(@ModelAttribute(DATOS_MATERIA) DatosMateria datosMateria) {
    Materia materia = new Materia();

    materia.setNombre(datosMateria.getNombre());
    materia.setDescripcion(datosMateria.getDescripcion());
    materia.setDocente(datosMateria.getDocente());
    materia.setColor(datosMateria.getColor());
    materia.setDias(datosMateria.getDias());
    materia.setHorario(datosMateria.getHorario());
    materia.setMaterialBibliografico(datosMateria.getMaterialBibliografico());

    try {
      servicioMateria.crearMateria(materia);
    } catch (MateriaExistente e) {
      Map<String, Object> model = new ModelMap();

      model.put("error", "Ya existe una materia con ese nombre");
      model.put(DATOS_MATERIA, datosMateria);

      return new ModelAndView("paginas/materia/materia-formulario", model);
    }

    return new ModelAndView("redirect:/materias");
  }

  @RequestMapping(path = "/materias", method = RequestMethod.GET)
  public ModelAndView listarMaterias() {
    Map<String, Object> model = new ModelMap();

    model.put("materias", servicioMateria.obtenerMaterias());

    return new ModelAndView("paginas/materia/materias", model);
  }

  @RequestMapping(path = "/materias/detalle", method = RequestMethod.GET)
  public ModelAndView verMateria(@RequestParam("id") Integer id) {
    Materia materia = servicioMateria.buscarMateriaPorId(id);

    Map<String, Object> model = new ModelMap();

    model.put("materia", materia);

    return new ModelAndView("paginas/materia/materia-detalle", model);
  }

  @RequestMapping(path = "/materias/editar", method = RequestMethod.GET)
  public ModelAndView editarMateria(@RequestParam("id") Integer id) {
    Materia materia = servicioMateria.buscarMateriaPorId(id);

    DatosMateria datosMateria = new DatosMateria();

    datosMateria.setId(materia.getId());
    datosMateria.setNombre(materia.getNombre());
    datosMateria.setDescripcion(materia.getDescripcion());
    datosMateria.setDocente(materia.getDocente());
    datosMateria.setColor(materia.getColor());
    datosMateria.setDias(materia.getDias());
    datosMateria.setHorario(materia.getHorario());
    datosMateria.setMaterialBibliografico(materia.getMaterialBibliografico());

    Map<String, Object> model = new ModelMap();
    model.put(DATOS_MATERIA, datosMateria);

    return new ModelAndView("paginas/materia/materia-formulario", model);
  }

  @RequestMapping(path = "/materias/modificar", method = RequestMethod.POST)
  public ModelAndView guardarMateriaModificada(
    @ModelAttribute(DATOS_MATERIA) DatosMateria datosMateria
  ) {
    Materia materia = new Materia();

    materia.setId(datosMateria.getId());

    materia.setNombre(datosMateria.getNombre());
    materia.setDescripcion(datosMateria.getDescripcion());
    materia.setDocente(datosMateria.getDocente());
    materia.setColor(datosMateria.getColor());
    materia.setDias(datosMateria.getDias());
    materia.setHorario(datosMateria.getHorario());
    materia.setMaterialBibliografico(datosMateria.getMaterialBibliografico());

    servicioMateria.editarMateria(materia);

    return new ModelAndView("redirect:/materias");
  }

  @RequestMapping(path = "/materias/eliminar", method = RequestMethod.POST)
  public ModelAndView eliminarMateria(@RequestParam("nombre") String nombre) {
    servicioMateria.eliminarMateria(nombre);

    return new ModelAndView("redirect:/materias");
  }

  @RequestMapping(path = "/materias/buscar", method = RequestMethod.GET)
  public ModelAndView buscarMateria(@RequestParam("nombre") String nombre) {
    Materia materia = servicioMateria.buscarMateriaPorNombre(nombre);

    Map<String, Object> model = new ModelMap();

    model.put("materiaBuscada", materia);
    model.put("nombreBuscado", nombre);

    return new ModelAndView("paginas/materia/materias", model);
  }
}
