package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

public class VistaDashboard extends VistaWeb {

  private static final DateTimeFormatter FORMATO_PROXIMA_SESION = DateTimeFormatter.ofPattern(
    "dd/MM"
  );

  public VistaDashboard(Page page) {
    super(page);
  }

  public static VistaDashboard navegar(Page page) {
    page.navigate("localhost:8080/spring/dashboard");
    return new VistaDashboard(page);
  }

  /** Lee la tarjeta "Horas de Estudio Registradas" (por ejemplo "13h") y devuelve 13. */
  public long obtenerHorasDeEstudio() {
    String texto = this.obtenerTextoDelElemento("#kpi-horas-estudio").trim();
    return Long.parseLong(texto.replace("h", "").trim());
  }

  /** Texto tal cual se muestra en la tarjeta de próxima sesión ("dd/MM" o "Sin sesiones"). */
  public String obtenerTextoProximaSesion() {
    return this.obtenerTextoDelElemento("#kpi-proxima-sesion").trim();
  }

  /**
   * La tarjeta muestra la fecha sin año ("dd/MM"). Se arma la fecha completa con el año de
   * {@code referencia}; si el día/mes ya pasó, se asume el año siguiente. Devuelve null si no hay
   * sesiones.
   */
  public LocalDate obtenerFechaProximaSesion(LocalDate referencia) {
    String texto = this.obtenerTextoProximaSesion();
    if (texto.isEmpty() || texto.equalsIgnoreCase("Sin sesiones")) return null;

    LocalDate fecha = MonthDay.parse(texto, FORMATO_PROXIMA_SESION).atYear(referencia.getYear());
    return fecha.isBefore(referencia) ? fecha.plusYears(1) : fecha;
  }
}
