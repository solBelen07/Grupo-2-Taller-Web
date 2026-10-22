package com.tallerwebi.presentacion.tarea;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.ServicioGrupo;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.tarea.ServicioTarea;
import com.tallerwebi.dominio.tarea.Tarea;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorTarea {

  private ServicioTarea servicioTarea;
  private ServicioMateria servicioMateria;
  private ServicioGrupo servicioGrupo;

  @Autowired
  public ControladorTarea(
    ServicioTarea servicioTarea,
    ServicioMateria servicioMateria,
    ServicioGrupo servicioGrupo
  ) {
    this.servicioTarea = servicioTarea;
    this.servicioMateria = servicioMateria;
    this.servicioGrupo = servicioGrupo;
  }

  @RequestMapping("/tareas")
  public ModelAndView verTareas(HttpServletRequest request) {
    if (!tieneUsuarioAutenticado(request)) {
      return new ModelAndView("redirect:/login");
    }

    List<Tarea> tareas = servicioTarea.obtenerTodas();
    double progreso = servicioTarea.calcularProgreso();
    // Calculamos el indicador de seguimiento para cada tarea, el resultado depende
    // de si es un parcial o un TP.
    Map<Long, String> indicadores = new HashMap<>();

    for (Tarea tarea : tareas) {
      indicadores.put(tarea.getId(), servicioTarea.calcularIndicador(tarea));
    }

    long completadas = servicioTarea.contarTareasCompletadas();
    long pendientes = servicioTarea.contarTareasPendientes();
    long total = servicioTarea.contarTareasTotales();

    Map<String, Long> aporteIndividual = servicioTarea.calcularAporteIndividual();

    Map<String, Object> modelo = new ModelMap();

    modelo.put("tareas", tareas);
    modelo.put("progreso", progreso);
    modelo.put("completadas", completadas);
    modelo.put("pendientes", pendientes);
    modelo.put("total", total);
    modelo.put("aporteIndividual", aporteIndividual);
    modelo.put("indicadores", indicadores);

    // GRP-02: las tareas se muestran agrupadas por grupo, cada una con su propio
    // progreso.
    modelo.put("tareasPorGrupo", servicioTarea.agruparPorGrupo());
    modelo.put("progresoPorGrupo", servicioTarea.calcularProgresoPorGrupo());

    return new ModelAndView("paginas/materia/tareas", modelo);
  }

  @RequestMapping("/tareas/nueva")
  public ModelAndView verNuevaTarea(HttpServletRequest request) {
    if (!tieneUsuarioAutenticado(request)) {
      return new ModelAndView("redirect:/login");
    }

    return armarFormularioNuevaTarea(new HashMap<>());
  }

  @RequestMapping("/tareas/crear")
  public ModelAndView crearTarea(
    @RequestParam(name = "titulo") String titulo,
    @RequestParam(name = "materia") String materia,
    @RequestParam(name = "estado") String estado,
    @RequestParam(name = "responsable") String responsable,
    @RequestParam(name = "horasPlanificadas") Integer horasPlanificadas,
    @RequestParam(name = "horasRealizadas") Integer horasRealizadas,
    @RequestParam(name = "tipo") String tipo,
    @RequestParam(name = "fechaVencimiento") LocalDate fechaVencimiento,
    @RequestParam(name = "grupo") String nombreGrupo,
    HttpServletRequest request
  ) {
    if (!tieneUsuarioAutenticado(request)) {
      return new ModelAndView("redirect:/login");
    }

    // GRP-02: toda tarea nueva tiene que quedar asociada a un grupo existente.
    Grupo grupo;
    try {
      grupo = servicioGrupo.buscarPorNombre(nombreGrupo);
    } catch (GrupoNoEncontrado e) {
      Map<String, Object> modelo = new HashMap<>();
      modelo.put("error", "El grupo seleccionado no existe. Elegí uno de la lista.");
      return armarFormularioNuevaTarea(modelo);
    }

    Tarea tarea = new Tarea();
    tarea.setTitulo(titulo);
    tarea.setMateria(materia);
    tarea.setEstado(estado);
    tarea.setResponsable(responsable);
    tarea.setHorasPlanificadas(horasPlanificadas);
    tarea.setHorasRealizadas(horasRealizadas);
    tarea.setTipo(tipo);
    tarea.setFechaVencimiento(fechaVencimiento);
    tarea.setGrupo(grupo);

    servicioTarea.crearTarea(tarea);

    return new ModelAndView("redirect:/tareas");
  }

  private ModelAndView armarFormularioNuevaTarea(Map<String, Object> modelo) {
    List<Materia> materias = this.servicioMateria.obtenerMaterias();
    List<Grupo> grupos = this.servicioGrupo.listarGrupos();
    modelo.put("materias", materias);
    modelo.put("grupos", grupos);

    return new ModelAndView("paginas/materia/nueva-tarea", modelo);
  }

  private boolean tieneUsuarioAutenticado(HttpServletRequest request) {
    HttpSession session = request.getSession(false);

    return session != null && session.getAttribute("USUARIO") instanceof Usuario;
  }
}
