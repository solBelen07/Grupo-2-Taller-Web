package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioTarea")
@Transactional
public class ServicioTareaImpl implements ServicioTarea {

  private RepositorioTarea repositorioTarea;

  private static final String ESTADO_COMPLETADA = "COMPLETADA";
  private static final String TIPO_PARCIAL = "PARCIAL";

  // Constantes utilizadas para evitar números escritos directamente
  // dentro de las condiciones y facilitar la lectura del código.
  private static final int HORAS_MINIMAS = 0;
  private static final int PREPARACION_ADECUADA = 80;
  private static final int PREPARACION_REGULAR = 50;
  private static final int DIAS_PROXIMO_VENCIMIENTO = 3;

  @Autowired
  public ServicioTareaImpl(RepositorioTarea repositorioTarea) {
    this.repositorioTarea = repositorioTarea;
  }

  @Override
  public void crearTarea(Tarea tarea) {
    repositorioTarea.guardar(tarea);
  }

  @Override
  public List<Tarea> obtenerTodas() {
    return repositorioTarea.buscarTodas();
  }

  @Override
  public double calcularProgreso() {
    List<Tarea> tareas = repositorioTarea.buscarTodas();

    if (tareas.isEmpty()) {
      return 0;
    }

    long tareasCompletadas = tareas
      .stream()
      .filter(tarea -> ESTADO_COMPLETADA.equals(tarea.getEstado()))
      .count();

    return (tareasCompletadas * 100.0) / tareas.size();
  }

  @Override
  public long contarTareasCompletadas() {
    return repositorioTarea
      .buscarTodas()
      .stream()
      .filter(tarea -> ESTADO_COMPLETADA.equals(tarea.getEstado()))
      .count();
  }

  @Override
  public long contarTareasPendientes() {
    return repositorioTarea
      .buscarTodas()
      .stream()
      .filter(tarea -> !ESTADO_COMPLETADA.equals(tarea.getEstado()))
      .count();
  }

  @Override
  public long contarTareasTotales() {
    return repositorioTarea.buscarTodas().size();
  }

  @Override
  public Map<String, Long> calcularAporteIndividual() {
    //busca todas las tareas y filtra las completadas, luego cuenta cuantas tiene cada responsable
    List<Tarea> tareas = repositorioTarea.buscarTodas();

    Map<String, Long> aporte = new HashMap<>();

    tareas
      .stream()
      //se queda solo con las completadas
      .filter(tarea -> "COMPLETADA".equals(tarea.getEstado()))
      .forEach(tarea -> {
        //mira quien es el responsable y le va acumulando las tareas
        String responsable = tarea.getResponsable();

        if (responsable != null && !responsable.isBlank()) {
          aporte.put(responsable, aporte.getOrDefault(responsable, 0L) + 1);
        }
      });

    return aporte;
  }

  // Calcula qué porcentaje de las horas planificadas ya fueron realizadas.
  // Ej : 10 horas planificadas y 8 realizadas = 80%.
  //lo usamos para determinar el nivel de preparación de los parciales.
  @Override
  public double calcularPorcentajeHoras(Tarea tarea) {
    if (tarea.getHorasPlanificadas() == null || tarea.getHorasPlanificadas() <= HORAS_MINIMAS) {
      return 0;
    }

    if (tarea.getHorasRealizadas() == null || tarea.getHorasRealizadas() < HORAS_MINIMAS) {
      return 0;
    }

    return (tarea.getHorasRealizadas() * 100.0) / tarea.getHorasPlanificadas();
  }

  @Override
  public String calcularIndicador(Tarea tarea) {
    if (TIPO_PARCIAL.equalsIgnoreCase(tarea.getTipo())) {
      double porcentaje = calcularPorcentajeHoras(tarea);

      if (porcentaje >= PREPARACION_ADECUADA) {
        return "PREPARACIÓN ADECUADA";
      }

      if (porcentaje >= PREPARACION_REGULAR) {
        return "REGULAR";
      }

      return "INSUFICIENTE";
    }

    if (ESTADO_COMPLETADA.equals(tarea.getEstado())) {
      return "COMPLETADO";
    }

    // evitarmos q no tenga fecha de vencimiento.
    if (tarea.getFechaVencimiento() == null) {
      return "EN TIEMPO";
    }

    LocalDate hoy = LocalDate.now();

    // Calculamos cuántos días faltan para la fecha de vencimiento.
    long diasRestantes = ChronoUnit.DAYS.between(hoy, tarea.getFechaVencimiento());

    if (diasRestantes < HORAS_MINIMAS) {
      return "ATRASADO";
    }

    if (diasRestantes <= DIAS_PROXIMO_VENCIMIENTO) {
      return "PRÓXIMO A VENCER";
    }

    return "EN TIEMPO";
  }
}
// INDICADORES QUE USE :
// Para PARCIALES :
// - 80% o más: PREPARACIÓN ADECUADA
// - 50% a 79%: REGULAR
// - Menos de 50%: INSUFICIENTE
//
// Para TPs se analiza la fecha de vencimiento:
// - COMPLETADA: la tarea ya fue terminada.
// - ATRASADO: la fecha de vencimiento ya pasó.
// - PRÓXIMO A VENCER: faltan 3 días o menos.
// - EN TIEMPO: faltan más de 3 días.
