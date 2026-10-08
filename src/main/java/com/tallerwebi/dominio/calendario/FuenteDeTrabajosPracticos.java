package com.tallerwebi.dominio.calendario;

import com.tallerwebi.dominio.Evento;
import com.tallerwebi.dominio.TipoEvento;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import com.tallerwebi.dominio.trabajosPracticos.EstadoTP;
import com.tallerwebi.dominio.trabajosPracticos.RepositorioTrabajoPractico;
import com.tallerwebi.dominio.trabajosPracticos.TrabajoPractico;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Aporta al calendario los Trabajos Prácticos del módulo /trabajos-practicos
 */
@Component
public class FuenteDeTrabajosPracticos implements FuenteDeEventos {

  static final LocalTime HORA_DE_ENTREGA = LocalTime.of(20, 0);
  static final int DURACION_HORAS = 1;
  static final String TITULO_GENERICO = "Entrega de TP";
  static final String COLOR_SIN_MATERIA = "#9CA3AF";
  static final String MATERIA_SIN_NOMBRE = "Sin materia";
  static final int DIAS_PROXIMO_VENCIMIENTO = 3;

  private final RepositorioTrabajoPractico repositorioTrabajoPractico;
  private final RepositorioMateria repositorioMateria;

  @Autowired
  public FuenteDeTrabajosPracticos(
    RepositorioTrabajoPractico repositorioTrabajoPractico,
    RepositorioMateria repositorioMateria
  ) {
    this.repositorioTrabajoPractico = repositorioTrabajoPractico;
    this.repositorioMateria = repositorioMateria;
  }

  @Override
  public List<EventoConEstado> eventosEntre(
    Long usuarioId,
    LocalDateTime desde,
    LocalDateTime hasta,
    LocalDate hoy
  ) {
    List<Materia> materias = repositorioMateria.obtenerTodas();
    Map<String, Materia> sinCatalogo = new HashMap<>();
    List<EventoConEstado> eventos = new ArrayList<>();
    for (TrabajoPractico trabajo : repositorioTrabajoPractico.obtenerTodos()) {
      if (trabajo.getFechaEntrega() == null) {
        continue;
      }
      LocalDateTime inicio = trabajo.getFechaEntrega().atTime(HORA_DE_ENTREGA);
      LocalDateTime fin = inicio.plusHours(DURACION_HORAS);
      if (!inicio.isBefore(hasta) || !fin.isAfter(desde)) {
        continue;
      }
      Evento evento = new Evento(
        usuarioId,
        materiaDe(trabajo, materias, sinCatalogo),
        TipoEvento.TRABAJO_PRACTICO,
        titulo(trabajo),
        inicio,
        fin
      );
      eventos.add(new EventoConEstado(evento, estadoDe(trabajo, hoy)));
    }
    return eventos;
  }

  private static String titulo(TrabajoPractico trabajo) {
    String nombre = trabajo.getNombre();
    return nombre == null || nombre.isBlank() ? TITULO_GENERICO : nombre;
  }

  /**
   * Busca la Materia real por nombre (sin importar mayúsculas ni espacios).
   */
  private static Materia materiaDe(
    TrabajoPractico trabajo,
    List<Materia> materias,
    Map<String, Materia> sinCatalogo
  ) {
    String nombre = trabajo.getMateria() == null ? "" : trabajo.getMateria().trim();
    for (Materia materia : materias) {
      if (materia.getNombre() != null && materia.getNombre().equalsIgnoreCase(nombre)) {
        return materia;
      }
    }
    return sinCatalogo.computeIfAbsent(
      nombre.toLowerCase(Locale.ROOT),
      clave -> {
        Materia gris = new Materia();
        gris.setNombre(nombre.isEmpty() ? MATERIA_SIN_NOMBRE : nombre);
        gris.setColor(COLOR_SIN_MATERIA);
        return gris;
      }
    );
  }

  /**
   * Estado de un TP: un TP no guarda horas ni avance, así que solo cuentan su estado y su fecha.
   * Finalizado, Vencido, Próximo a vencer, En tiempo.
   */
  static EstadoActividad estadoDe(TrabajoPractico trabajo, LocalDate hoy) {
    if (trabajo.getEstado() == EstadoTP.FINALIZADO) {
      return EstadoActividad.COMPLETADA;
    }
    LocalDate entrega = trabajo.getFechaEntrega();
    if (entrega != null && entrega.isBefore(hoy)) {
      return EstadoActividad.VENCIDA;
    }
    if (entrega != null && !entrega.isAfter(hoy.plusDays(DIAS_PROXIMO_VENCIMIENTO))) {
      return EstadoActividad.PROXIMA_A_VENCER;
    }
    return EstadoActividad.EN_TIEMPO;
  }
}
