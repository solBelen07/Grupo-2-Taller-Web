package com.tallerwebi.presentacion.logro;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.logro.Logro;
import com.tallerwebi.dominio.logro.LogroObtenido;
import com.tallerwebi.dominio.logro.ServicioLogro;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLogro {

  private ServicioLogro servicioLogro;

  @Autowired
  public ControladorLogro(ServicioLogro servicioLogro) {
    this.servicioLogro = servicioLogro;
  }

  public ControladorLogro() {}

  @GetMapping("/logros")
  public ModelAndView mostrarLogros(HttpSession session) {
    Usuario usuario = (Usuario) session.getAttribute("USUARIO");

    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    // primero se registran los logros que el usuario haya alcanzado,
    // y después se leen los que tiene guardados (con su fecha).
    servicioLogro.otorgarLogrosAlcanzados(usuario);
    Map<String, LocalDate> fechaPorLogro = fechasDeLogrosObtenidos(usuario);

    List<DatosLogro> logrosDTO = new ArrayList<>();
    for (Logro logro : servicioLogro.obtenerLogros()) {
      logrosDTO.add(
        new DatosLogro(
          logro.getNombre(),
          logro.getDescripcion(),
          iconoDe(logro.getNombre()),
          fechaPorLogro.get(logro.getNombre())
        )
      );
    }
    return new ModelAndView("paginas/logro/logros", Map.of("logros", logrosDTO));
  }

  private Map<String, LocalDate> fechasDeLogrosObtenidos(Usuario usuario) {
    Map<String, LocalDate> fechas = new HashMap<>();
    List<LogroObtenido> obtenidos = servicioLogro.obtenerLogrosObtenidos(usuario);
    if (obtenidos != null) {
      for (LogroObtenido obtenido : obtenidos) {
        fechas.put(obtenido.getLogro().getNombre(), obtenido.getFechaObtencion());
      }
    }
    return fechas;
  }

  // Ícono de cada logro (Font Awesome). Si un logro no está en la lista, se muestra un trofeo.
  private static final Map<String, String> ICONOS = Map.of(
    "Maratonista",
    "fa-person-running",
    "Popular",
    "fa-user-plus",
    "Primeros pasos",
    "fa-seedling",
    "Primer vuelo",
    "fa-plane-departure",
    "Piloto frecuente",
    "fa-plane",
    "Constante",
    "fa-fire",
    "Imparable",
    "fa-fire-flame-curved",
    "Plan cumplido",
    "fa-clipboard-check",
    "Líder de grupo",
    "fa-crown"
  );

  private String iconoDe(String nombreLogro) {
    return ICONOS.getOrDefault(nombreLogro, "fa-trophy");
  }
}
