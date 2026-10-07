package com.tallerwebi.dominio.calendario;

import com.tallerwebi.dominio.Evento;
import com.tallerwebi.dominio.TipoEvento;
import com.tallerwebi.dominio.parcial.Parcial;
import com.tallerwebi.dominio.parcial.RepositorioParcial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Aporta al calendario los paricales. Duran  (#DURACION_PARCIAL_HORAS) horas (la entidad solo guarda la hora de inicio).
 *  Parcial no tiene dueño todavía, así que se muestra a cualquier usuario.
 */
@Component
public class FuenteDeParciales implements FuenteDeEventos {

  static final int DURACION_PARCIAL_HORAS = 2;
  static final int DIAS_PROXIMO_VENCIMIENTO = 3;
  static final int PORCENTAJE_COMPLETO = 100;
  static final int UMBRAL_PARCIALMENTE_COMPLETADA = 50;

  private final RepositorioParcial repositorioParcial;

  @Autowired
  public FuenteDeParciales(RepositorioParcial repositorioParcial) {
    this.repositorioParcial = repositorioParcial;
  }

  @Override
  public List<EventoConEstado> eventosEntre(
    Long usuarioId,
    LocalDateTime desde,
    LocalDateTime hasta,
    LocalDate hoy
  ) {
    List<EventoConEstado> eventos = new ArrayList<>();
    for (Parcial parcial : repositorioParcial.obtenerTodos()) {
      if (parcial.getFecha() == null || parcial.getHorario() == null) {
        continue;
      }
      LocalDateTime inicio = LocalDateTime.of(parcial.getFecha(), parcial.getHorario());
      LocalDateTime fin = inicio.plusHours(DURACION_PARCIAL_HORAS);
      if (!inicio.isBefore(hasta) || !fin.isAfter(desde)) {
        continue;
      }
      Evento evento = new Evento(
        usuarioId,
        parcial.getMateria(),
        TipoEvento.PARCIAL,
        titulo(parcial),
        inicio,
        fin
      );
      eventos.add(new EventoConEstado(evento, estadoDe(parcial, hoy)));
    }
    return eventos;
  }

  private static String titulo(Parcial parcial) {
    String nombre = parcial.getNombre();
    if (nombre != null && !nombre.isBlank()) {
      return nombre;
    }
    return "Parcial de " + parcial.getMateria().getNombre();
  }

  /**
   * Estado de un Parcial con la misma regla única que usa el calendario para las Tareas: la
   * urgencia por fecha pesa más que el progreso, salvo que el progreso ya esté al 100%.
   */
  static EstadoActividad estadoDe(Parcial parcial, LocalDate hoy) {
    Integer porcentajeGuardado = parcial.getPorcentajeCompletado();
    int porcentaje = porcentajeGuardado == null ? 0 : porcentajeGuardado;
    if (porcentaje >= PORCENTAJE_COMPLETO) {
      return EstadoActividad.COMPLETADA;
    }
    LocalDate fecha = parcial.getFecha();
    if (fecha != null && fecha.isBefore(hoy)) {
      return EstadoActividad.VENCIDA;
    }
    if (fecha != null && !fecha.isAfter(hoy.plusDays(DIAS_PROXIMO_VENCIMIENTO))) {
      return EstadoActividad.PROXIMA_A_VENCER;
    }
    if (porcentaje >= UMBRAL_PARCIALMENTE_COMPLETADA) {
      return EstadoActividad.PARCIALMENTE_COMPLETADA;
    }
    return EstadoActividad.EN_TIEMPO;
  }
}
