package com.tallerwebi.presentacion.logro;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.logro.Logro;
import com.tallerwebi.dominio.logro.ServicioLogro;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
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

    List<Logro> todosLosLogros = servicioLogro.obtenerLogros();
    List<Logro> logrosDelUsuario = servicioLogro.obtenerProgresos(usuario);

    List<DatosLogro> logrosDTO = new ArrayList<>();

    for (Logro logro : todosLosLogros) {
      boolean estaObtenido = logrosDelUsuario
        .stream()
        .anyMatch(obtenido -> obtenido.getNombre().equals(logro.getNombre()));
      String claseIcono;

      switch (logro.getNombre()) {
        case "Maratonista":
          claseIcono = "fa-person-running";
          break;
        case "Invitador":
          claseIcono = "fa-user-plus";
          break;
        default:
          claseIcono = "fa-trophy";
          break;
      }

      logrosDTO.add(
        new DatosLogro(logro.getNombre(), logro.getDescripcion(), estaObtenido, claseIcono)
      );
    }
    return new ModelAndView("paginas/logro/logros", Map.of("logros", logrosDTO));
  }
}
