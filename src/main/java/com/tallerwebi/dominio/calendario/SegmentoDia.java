package com.tallerwebi.dominio.calendario;

import com.tallerwebi.dominio.Evento;
import com.tallerwebi.dominio.TipoEvento;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Porción de un evento que cae dentro de un día. Un evento que cruza la medianoche genera un
 * segmento por cada día que ocupa. Los minutos se cuentan desde las 00:00 del día (0 a 1440).
 *
 * @param eventoId id del evento original
 * @param titulo título del evento
 * @param tipo tipo del evento
 * @param materia nombre de la materia del evento
 * @param color color de la materia, para pintar el segmento
 * @param minutoInicio minuto del día en que empieza el segmento
 * @param minutoFin minuto del día en que termina el segmento
 * @param continuaDesdeAntes si el evento ya venía ocupando el día anterior
 * @param continuaDespues si el evento sigue ocupando el día siguiente
 * @param estado estado de seguimiento (CAL-02) de la Tarea vinculada; null si no tiene ninguna
 */
public record SegmentoDia(
  Long eventoId,
  String titulo,
  TipoEvento tipo,
  String materia,
  String color,
  int minutoInicio,
  int minutoFin,
  boolean continuaDesdeAntes,
  boolean continuaDespues,
  EstadoActividad estado
) {
  private static final int MINUTOS_POR_HORA = 60;
  private static final int HORAS_POR_DIA = 24;
  private static final String SEPARADOR = " · ";

  /**
   * Recorta el evento al día indicado; vacío si el evento no ocupa ese día.
   *
   * @param estado estado de seguimiento ya calculado para este evento, o null si el
   *     evento no tiene una Tarea vinculada
   */
  public static Optional<SegmentoDia> recortar(
    Evento evento,
    LocalDate dia,
    EstadoActividad estado
  ) {
    LocalDateTime inicioDia = dia.atStartOfDay();
    LocalDateTime finDia = dia.plusDays(1).atStartOfDay();
    if (!evento.getInicio().isBefore(finDia) || !evento.getFin().isAfter(inicioDia)) {
      return Optional.empty();
    }
    boolean empiezaAntes = evento.getInicio().isBefore(inicioDia);
    boolean terminaDespues = evento.getFin().isAfter(finDia);
    LocalDateTime desde = empiezaAntes ? inicioDia : evento.getInicio();
    LocalDateTime hasta = terminaDespues ? finDia : evento.getFin();
    return Optional.of(
      new SegmentoDia(
        evento.getId(),
        evento.getTitulo(),
        evento.getTipo(),
        evento.getMateria().getNombre(),
        evento.getMateria().getColor(),
        (int) Duration.between(inicioDia, desde).toMinutes(),
        (int) Duration.between(inicioDia, hasta).toMinutes(),
        empiezaAntes,
        terminaDespues,
        estado
      )
    );
  }

  /** Duración del segmento en minutos. */
  public int duracion() {
    return minutoFin - minutoInicio;
  }

  /** Hora de inicio con formato HH:mm */
  public String horaInicio() {
    return formatear(minutoInicio);
  }

  /** Hora de fin con formato HH:mm (la medianoche final se muestra como 00:00). */
  public String horaFin() {
    return formatear(minutoFin);
  }

  /** Rango legible, por ejemplo  18:00 – 20:00 */
  public String horario() {
    return horaInicio() + " – " + horaFin();
  }

  public String descripcion() {
    String base =
      tipo.getEtiqueta() + SEPARADOR + materia + SEPARADOR + titulo + SEPARADOR + horario();
    return estado == null ? base : base + SEPARADOR + estado.getEtiqueta();
  }

  private static String formatear(int minutos) {
    return "%02d:%02d".formatted(
        (minutos / MINUTOS_POR_HORA) % HORAS_POR_DIA,
        minutos % MINUTOS_POR_HORA
      );
  }
}
