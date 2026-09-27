package com.tallerwebi.dominio;

import java.util.List;
import java.util.Map;

public interface ServicioTarea {
  void crearTarea(Tarea tarea);

  List<Tarea> obtenerTodas();

  double calcularProgreso();

  long contarTareasCompletadas();

  long contarTareasPendientes();

  long contarTareasTotales();

  //map guarda el nombre del responsable y la cantidad de tareas completadas
  Map<String, Long> calcularAporteIndividual();

  // Calcula el porcentaje de horas realizadas sobre las horas planificadas, para medir el nivel de preparación de un parcial.
  double calcularPorcentajeHoras(Tarea tarea);

  // Determina el indicador de seguimiento según el tipo de tarea.
  // Para parciales: PREPARACIÓN ADECUADA, REGULAR o INSUFICIENTE.
  // Para TPs: EN TIEMPO, PRÓXIMO A VENCER, ATRASADO o COMPLETADO.
  String calcularIndicador(Tarea tarea);
}
