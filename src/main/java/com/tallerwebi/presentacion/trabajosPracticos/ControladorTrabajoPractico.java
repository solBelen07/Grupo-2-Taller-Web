package com.tallerwebi.presentacion.trabajosPracticos;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.trabajosPracticos.EstadoTP;
import com.tallerwebi.dominio.trabajosPracticos.ServicioTrabajoPractico;
import com.tallerwebi.dominio.trabajosPracticos.TipoTrabajo;
import com.tallerwebi.dominio.trabajosPracticos.TrabajoPractico;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

  private static final String CLAVE_MATERIAS = "materias";
  private static final String CLAVE_TIPOS = "tipos";
  private static final String CLAVE_ESTADOS = "estados";
  private static final String CLAVE_TP = "trabajoPractico";
  private static final String VISTA_FORMULARIO = "paginas/nuevo-trabajo-practico";
  private static final String REDIRECT_LISTADO = "redirect:/trabajos-practicos";

  private final ServicioTrabajoPractico servicioTrabajoPractico;
  private final ServicioMateria servicioMateria;

  @Autowired
  public ControladorTrabajoPractico(
    ServicioTrabajoPractico servicioTrabajoPractico,
    ServicioMateria servicioMateria
  ) {
    this.servicioTrabajoPractico = servicioTrabajoPractico;
    this.servicioMateria = servicioMateria;
  }

  @RequestMapping(method = RequestMethod.GET)
  public ModelAndView listarTrabajosPracticos(
    @RequestParam(value = "materia", required = false) String materia,
    HttpServletRequest request
  ) {
    List<TrabajoPractico> lista = (materia != null && !materia.isEmpty())
      ? servicioTrabajoPractico.buscarPorMateria(materia)
      : servicioTrabajoPractico.obtenerTodos();

    Map<String, Object> modelo = new ModelMap();
    modelo.put("pendientes", filtrar(lista, EstadoTP.PENDIENTE));
    modelo.put("enCurso", filtrar(lista, EstadoTP.EN_CURSO));
    modelo.put("finalizados", filtrar(lista, EstadoTP.FINALIZADO));
    modelo.put("materiaBuscada", materia);
    modelo.put(CLAVE_MATERIAS, materiasDelUsuario(request));
    return new ModelAndView("paginas/trabajos-practicos", modelo);
  }

  @RequestMapping(path = "/nuevo", method = RequestMethod.GET)
  public ModelAndView irANuevoTrabajoPractico(HttpServletRequest request) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put(CLAVE_TP, new TrabajoPractico());
    cargarOpcionesDelFormulario(modelo, request);
    return new ModelAndView(VISTA_FORMULARIO, modelo);
  }

  @RequestMapping(path = "/guardar", method = RequestMethod.POST)
  public ModelAndView guardarTrabajoPractico(
    @ModelAttribute(CLAVE_TP) TrabajoPractico trabajoPractico
  ) {
    servicioTrabajoPractico.crearTrabajoPractico(trabajoPractico);
    return new ModelAndView(REDIRECT_LISTADO);
  }

  @RequestMapping(path = "/editar/{id}", method = RequestMethod.GET)
  public ModelAndView editarTrabajoPractico(
    @PathVariable("id") Long id,
    HttpServletRequest request
  ) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put(CLAVE_TP, servicioTrabajoPractico.buscarPorId(id));
    cargarOpcionesDelFormulario(modelo, request);
    return new ModelAndView(VISTA_FORMULARIO, modelo);
  }

  @RequestMapping(path = "/avanzar/{id}", method = RequestMethod.POST)
  public String avanzarEstado(@PathVariable("id") Long id) {
    servicioTrabajoPractico.avanzarEstado(id);
    return REDIRECT_LISTADO;
  }

  @RequestMapping(path = "/eliminar/{id}", method = RequestMethod.POST)
  public String eliminarTrabajoPractico(@PathVariable("id") Long id) {
    servicioTrabajoPractico.eliminar(id);
    return REDIRECT_LISTADO;
  }

  private void cargarOpcionesDelFormulario(Map<String, Object> modelo, HttpServletRequest request) {
    modelo.put(CLAVE_TIPOS, TipoTrabajo.values());
    modelo.put(CLAVE_ESTADOS, EstadoTP.values());
    modelo.put(CLAVE_MATERIAS, materiasDelUsuario(request));
  }

  private List<String> materiasDelUsuario(HttpServletRequest request) {
    HttpSession sesion = request.getSession(false);
    Object usuario = sesion == null ? null : sesion.getAttribute("USUARIO");
    if (!(usuario instanceof Usuario)) {
      return List.of();
    }
    return servicioMateria
      .obtenerMateriasPorUsuario(((Usuario) usuario).getId())
      .stream()
      .map(Materia::getNombre)
      .toList();
  }

  private List<TrabajoPractico> filtrar(List<TrabajoPractico> lista, EstadoTP estado) {
    return lista.stream().filter(tp -> tp.getEstado() == estado).toList();
  }
}
