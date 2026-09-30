package com.tallerwebi.dominio;

import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

import com.tallerwebi.dominio.calendario.BloqueHorario;
import com.tallerwebi.dominio.calendario.CalendarioMensual;
import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.calendario.DiaMensual;
import com.tallerwebi.dominio.calendario.DiaSemanal;
import com.tallerwebi.dominio.calendario.DisposicionDeBloques;
import com.tallerwebi.dominio.calendario.ResumenMateria;
import com.tallerwebi.dominio.calendario.SegmentoDia;
import com.tallerwebi.dominio.calendario.SemanaMensual;
import com.tallerwebi.dominio.calendario.TextosCalendario;
import com.tallerwebi.dominio.materia.Materia;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación del calendario unificado: consulta los eventos y arma las grillas. */
@Service("servicioCalendario")
@Transactional
public class ServicioCalendarioImpl implements ServicioCalendario {

  static final int HORA_INICIO_MINIMA = 7;
  static final int HORA_FIN_MINIMA = 22;

  private static final int DIAS_POR_SEMANA = 7;
  private static final int MINUTOS_POR_HORA = 60;
  private static final int HORAS_POR_DIA = 24;

  private RepositorioEvento repositorioEvento;
  private Clock reloj;

  @Autowired
  public ServicioCalendarioImpl(RepositorioEvento repositorioEvento, Clock reloj) {
    this.repositorioEvento = repositorioEvento;
    this.reloj = reloj;
  }

  @Override
  public CalendarioMensual obtenerMes(Long usuarioId, LocalDate referencia) {
    YearMonth mes = YearMonth.from(referencia);
    LocalDate primerDia = mes.atDay(1).with(previousOrSame(DayOfWeek.MONDAY));
    LocalDate ultimoDia = mes.atEndOfMonth().with(nextOrSame(DayOfWeek.SUNDAY));
    List<Evento> eventos = repositorioEvento.buscarEnRango(
      usuarioId,
      primerDia.atStartOfDay(),
      ultimoDia.plusDays(1).atStartOfDay()
    );
    LocalDate hoy = LocalDate.now(reloj);

    List<SemanaMensual> semanas = new ArrayList<>();
    for (LocalDate lunes = primerDia; !lunes.isAfter(ultimoDia); lunes = lunes.plusWeeks(1)) {
      List<DiaMensual> dias = new ArrayList<>();
      for (int i = 0; i < DIAS_POR_SEMANA; i++) {
        LocalDate fecha = lunes.plusDays(i);
        dias.add(
          new DiaMensual(
            fecha,
            YearMonth.from(fecha).equals(mes),
            fecha.equals(hoy),
            segmentosDelDia(eventos, fecha)
          )
        );
      }
      semanas.add(
        new SemanaMensual(lunes.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), lunes, List.copyOf(dias))
      );
    }
    return new CalendarioMensual(
      TextosCalendario.tituloMes(mes),
      mes.minusMonths(1).atDay(1),
      mes.plusMonths(1).atDay(1),
      hoy,
      List.copyOf(semanas),
      resumirMaterias(eventos),
      eventos.size()
    );
  }

  @Override
  public CalendarioSemanal obtenerSemana(Long usuarioId, LocalDate referencia) {
    LocalDate lunes = referencia.with(previousOrSame(DayOfWeek.MONDAY));
    List<Evento> eventos = repositorioEvento.buscarEnRango(
      usuarioId,
      lunes.atStartOfDay(),
      lunes.plusDays(DIAS_POR_SEMANA).atStartOfDay()
    );
    LocalDate hoy = LocalDate.now(reloj);

    List<List<SegmentoDia>> segmentosPorDia = new ArrayList<>();
    for (int i = 0; i < DIAS_POR_SEMANA; i++) {
      segmentosPorDia.add(segmentosDelDia(eventos, lunes.plusDays(i)));
    }
    int[] rangoDeHoras = calcularRangoDeHoras(segmentosPorDia);
    int horaInicio = rangoDeHoras[0];
    int horaFin = rangoDeHoras[1];

    return new CalendarioSemanal(
      TextosCalendario.tituloSemana(lunes),
      lunes.minusWeeks(1),
      lunes.plusWeeks(1),
      hoy,
      armarDiasDeLaSemana(lunes, segmentosPorDia, horaInicio, hoy),
      horaInicio,
      horaFin,
      armarEtiquetasDeHoras(horaInicio, horaFin),
      resumirMaterias(eventos),
      eventos.size()
    );
  }

  /** Menor y mayor hora que ocupa algún segmento, acotadas entre el mínimo y las 24 horas. */
  private static int[] calcularRangoDeHoras(List<List<SegmentoDia>> segmentosPorDia) {
    int horaInicio = HORA_INICIO_MINIMA;
    int horaFin = HORA_FIN_MINIMA;
    for (List<SegmentoDia> segmentos : segmentosPorDia) {
      for (SegmentoDia segmento : segmentos) {
        horaInicio = Math.min(horaInicio, segmento.minutoInicio() / MINUTOS_POR_HORA);
        horaFin =
          Math.min(
            HORAS_POR_DIA,
            Math.max(horaFin, (segmento.minutoFin() + MINUTOS_POR_HORA - 1) / MINUTOS_POR_HORA)
          );
      }
    }
    return new int[] { horaInicio, horaFin };
  }

  private static List<DiaSemanal> armarDiasDeLaSemana(
    LocalDate lunes,
    List<List<SegmentoDia>> segmentosPorDia,
    int horaInicio,
    LocalDate hoy
  ) {
    List<DiaSemanal> dias = new ArrayList<>();
    for (int i = 0; i < DIAS_POR_SEMANA; i++) {
      LocalDate fecha = lunes.plusDays(i);
      List<BloqueHorario> bloques = DisposicionDeBloques.disponer(
        segmentosPorDia.get(i),
        horaInicio * MINUTOS_POR_HORA
      );
      dias.add(
        new DiaSemanal(
          fecha,
          TextosCalendario.nombreDia(fecha.getDayOfWeek()),
          TextosCalendario.abreviaturaDia(fecha.getDayOfWeek()),
          fecha.equals(hoy),
          bloques
        )
      );
    }
    return List.copyOf(dias);
  }

  private static List<String> armarEtiquetasDeHoras(int horaInicio, int horaFin) {
    List<String> etiquetas = new ArrayList<>();
    for (int hora = horaInicio; hora < horaFin; hora++) {
      etiquetas.add("%02d:00".formatted(hora));
    }
    return List.copyOf(etiquetas);
  }

  private static List<SegmentoDia> segmentosDelDia(List<Evento> eventos, LocalDate dia) {
    return eventos
      .stream()
      .flatMap(evento -> SegmentoDia.recortar(evento, dia).stream())
      .sorted(
        Comparator
          .comparingInt(SegmentoDia::minutoInicio)
          .thenComparingInt(SegmentoDia::minutoFin)
          .thenComparing(SegmentoDia::titulo)
      )
      .toList();
  }

  /** Materias que efectivamente tienen algún evento en el período mostrado, con su cantidad. */
  private static List<ResumenMateria> resumirMaterias(List<Evento> eventos) {
    Map<Materia, Long> cantidadPorMateria = eventos
      .stream()
      .collect(
        Collectors.groupingBy(Evento::getMateria, IdentityHashMap::new, Collectors.counting())
      );
    return cantidadPorMateria
      .entrySet()
      .stream()
      .sorted(
        Comparator.comparing(entrada -> entrada.getKey().getNombre(), String.CASE_INSENSITIVE_ORDER)
      )
      .map(entrada ->
        new ResumenMateria(
          entrada.getKey().getNombre(),
          entrada.getKey().getColor(),
          entrada.getValue().intValue()
        )
      )
      .toList();
  }
}
