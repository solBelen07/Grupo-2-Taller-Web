package com.tallerwebi.presentacion.dashboard;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.calendario.CalendarioSemanal;
import com.tallerwebi.dominio.dashboard.ServicioDashboard;
import com.tallerwebi.dominio.racha.ServicioRacha;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorDashboard {

  private static final LocalTime INICIO_MANIANA = LocalTime.of(6, 0);
  private static final LocalTime INICIO_TARDE = LocalTime.of(12, 0);
  private static final LocalTime INICIO_NOCHE = LocalTime.of(20, 0);

  private final ServicioDashboard servicioDashboard;
  private final ServicioRacha servicioRacha;

  @Autowired
  public ControladorDashboard(ServicioDashboard servicioDashboard, ServicioRacha servicioRacha) {
    this.servicioDashboard = servicioDashboard;
    this.servicioRacha = servicioRacha;
  }

  @RequestMapping("/dashboard")
  public ModelAndView verDashboard(
    @RequestParam(name = "vista", required = false) String vista,
    @RequestParam(name = "fecha", required = false) String fecha,
    HttpServletRequest request
  ) {
    Map<String, Object> modelo = new ModelMap();

    Usuario usuario = obtenerUsuarioLogueado(request);
    if (usuario == null) return new ModelAndView("redirect:/login");

    servicioRacha.modificarRacha(usuario);
    request.getSession().setAttribute("USUARIO", usuario);

    Long idUsuario = usuario.getId();

    Long horasEstudio = servicioDashboard.obtenerHorasEstudio(idUsuario);
    Double porcentaje = servicioDashboard.porcentajeCumplimiento(idUsuario);
    Long cantidadMaterias = servicioDashboard.obtenerCantidadMaterias(idUsuario);
    CalendarioSemanal calendario = servicioDashboard.obtenerCalendarioSemanal(
      idUsuario,
      LocalDate.now()
    );
    SesionEstudio proximaSesion = servicioDashboard.obtenerProximaSesion(idUsuario);
    Integer racha = servicioDashboard.obtenerRacha(idUsuario);
    Integer recordRacha = servicioDashboard.obtenerRecordRacha(idUsuario);

    DashboardDTO dashboardDTO = new DashboardDTO(
      horasEstudio,
      porcentaje,
      cantidadMaterias,
      calendario,
      proximaSesion,
      racha,
      recordRacha
    );

    modelo.put("dashboardData", dashboardDTO);
    modelo.put("usuario", usuario);
    modelo.put("saludo", saludoSegunHora(LocalTime.now()));
    modelo.put("racha", racha != null ? racha : 0);

    return new ModelAndView("paginas/dashboard/dashboard", modelo);
  }

  private static Usuario obtenerUsuarioLogueado(HttpServletRequest request) {
    return (Usuario) request.getSession().getAttribute("USUARIO");
  }

  // Saludo segun horario correspondiente
  static String saludoSegunHora(LocalTime hora) {
    if (hora.isBefore(INICIO_MANIANA) || !hora.isBefore(INICIO_NOCHE)) {
      return "Buenas noches";
    }
    return hora.isBefore(INICIO_TARDE) ? "Buenos días" : "Buenas tardes";
  }
}
