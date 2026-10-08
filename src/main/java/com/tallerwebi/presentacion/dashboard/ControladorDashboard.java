package com.tallerwebi.presentacion.dashboard;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.dashboard.ServicioDashboard;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorDashboard {

  private final ServicioDashboard servicioDashboard;

  @Autowired
  public ControladorDashboard(ServicioDashboard servicioDashboard) {
    this.servicioDashboard = servicioDashboard;
  }

  @GetMapping("/dashboard")
  public ModelAndView verDashboard(HttpServletRequest request) {
    Map<String, Object> modelo = new ModelMap();

    Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    Long idUsuario = usuario.getId();

    Long horasEstudio = servicioDashboard.obtenerHorasEstudio(idUsuario);
    Double porcentaje = servicioDashboard.porcentajeCumplimiento(idUsuario);
    Long cantidadMaterias = servicioDashboard.obtenerCantidadMaterias(idUsuario);
    CalendarioSemanal calendario = servicioDashboard.obtenerCalendarioSemanal(
      idUsuario,
      LocalDate.now()
    );
    SesionEstudio proximaSesion = servicioDashboard.obtenerProximaSesion(idUsuario);

    DashboardDTO dashboardDTO = new DashboardDTO(
      horasEstudio,
      porcentaje,
      cantidadMaterias,
      calendario,
      proximaSesion
    );

    modelo.put("dashboardData", dashboardDTO);
    modelo.put("usuario", usuario);

    return new ModelAndView("paginas/dashboard/dashboard", modelo);
  }
}
