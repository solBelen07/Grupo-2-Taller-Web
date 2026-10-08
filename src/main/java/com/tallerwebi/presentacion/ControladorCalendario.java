package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioCalendario;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/** CAL-01: pantalla del calendario unificado (vista mensual y vista semanal). */
@Controller
public class ControladorCalendario {

  static final String VISTA_MES = "mes";
  static final String VISTA_SEMANA = "semana";

  private static final int ANIO_MINIMO = 1970;
  private static final int ANIO_MAXIMO = 2100;

  private ServicioCalendario servicioCalendario;
  private Clock reloj;

  @Autowired
  public ControladorCalendario(ServicioCalendario servicioCalendario, Clock reloj) {
    this.servicioCalendario = servicioCalendario;
    this.reloj = reloj;
  }

  /**
   * Muestra el calendario del usuario logueado; si no hay sesión, lo manda a loguearse primero.
   */
  @RequestMapping(path = "/calendario", method = RequestMethod.GET)
  public ModelAndView verCalendario(
    @RequestParam(name = "vista", required = false) String vista,
    @RequestParam(name = "fecha", required = false) String fecha,
    HttpServletRequest request
  ) {
    Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    Map<String, Object> modelo = new ModelMap();
    LocalDate referencia = LocalDate.now(reloj);
    if (fecha != null && !fecha.isBlank()) {
      LocalDate pedida = parsear(fecha);
      if (pedida == null) {
        modelo.put("error", "La fecha ingresada no es válida. Se muestra el período actual.");
      } else {
        referencia = pedida;
      }
    }

    boolean semanal = VISTA_SEMANA.equalsIgnoreCase(vista);
    if (semanal) {
      modelo.put("calendario", servicioCalendario.obtenerSemana(usuario.getId(), referencia));
    } else {
      modelo.put("calendario", servicioCalendario.obtenerMes(usuario.getId(), referencia));
    }
    modelo.put("vista", semanal ? VISTA_SEMANA : VISTA_MES);
    modelo.put("fecha", referencia);
    return new ModelAndView("calendario", modelo);
  }

  private static LocalDate parsear(String texto) {
    try {
      LocalDate fecha = LocalDate.parse(texto.strip());
      boolean soportada = fecha.getYear() >= ANIO_MINIMO && fecha.getYear() <= ANIO_MAXIMO;
      return soportada ? fecha : null;
    } catch (DateTimeParseException e) {
      return null;
    }
  }
}
