package com.tallerwebi.presentacion.tarea;

import com.tallerwebi.dominio.tarea.ServicioTarea;
import com.tallerwebi.dominio.tarea.Tarea;
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

  @Autowired
  public ControladorTarea(ServicioTarea servicioTarea) {
    this.servicioTarea = servicioTarea;
  }

  @RequestMapping("/tareas")
  public ModelAndView verTareas() {
    List<Tarea> tareas = servicioTarea.obtenerTodas();
    double progreso = servicioTarea.calcularProgreso();
    // Calculamos el indicador de seguimiento para cada tarea, el resultado depende de si es un parcial o un TP.
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

    return new ModelAndView("tareas", modelo);
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
    @RequestParam(name = "fechaVencimiento") LocalDate fechaVencimiento
  ) {
    Tarea tarea = new Tarea();
    tarea.setTitulo(titulo);
    tarea.setMateria(materia);
    tarea.setEstado(estado);
    tarea.setResponsable(responsable);
    tarea.setHorasPlanificadas(horasPlanificadas);
    tarea.setHorasRealizadas(horasRealizadas);
    tarea.setTipo(tipo);
    tarea.setFechaVencimiento(fechaVencimiento);

    servicioTarea.crearTarea(tarea);

    return new ModelAndView("redirect:/tareas");
  }
}
