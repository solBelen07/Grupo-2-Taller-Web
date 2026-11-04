package com.tallerwebi.presentacion.parcial;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.parcial.Parcial;
import com.tallerwebi.dominio.parcial.ServicioParcial;
import com.tallerwebi.dominio.racha.ServicioRacha;
import com.tallerwebi.dominio.sesionEstudio.ServicioSesionEstudio;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/parciales")
public class ControladorParcial {

  private final ServicioMateria servicioMateria;
  private final ServicioParcial servicioParcial;
  private final ServicioSesionEstudio servicioSesionEstudio;
  private final ServicioRacha servicioRacha;

  @Autowired
  public ControladorParcial(
    ServicioMateria servicioMateria,
    ServicioParcial servicioParcial,
    ServicioSesionEstudio servicioSesionEstudio,
    ServicioRacha servicioRacha
  ) {
    this.servicioMateria = servicioMateria;
    this.servicioParcial = servicioParcial;
    this.servicioSesionEstudio = servicioSesionEstudio;
    this.servicioRacha = servicioRacha;
  }

  @RequestMapping(path = "/nuevo", method = RequestMethod.GET)
  public ModelAndView nuevoParcial() {
    Map<String, Object> modelo = new ModelMap();

    modelo.put("datosParcial", new DatosParcial());
    modelo.put("materias", servicioMateria.obtenerMaterias());

    return new ModelAndView("paginas/materia/parcial-formulario", modelo);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ModelAndView listarParciales() {
    Map<String, Object> modelo = new ModelMap();

    modelo.put("parciales", servicioParcial.obtenerParciales());

    return new ModelAndView("parciales", modelo);
  }

  @RequestMapping(path = "/guardar", method = RequestMethod.POST)
  public ModelAndView guardarParcial(
    @ModelAttribute("datosParcial") DatosParcial datosParcial,
    HttpServletRequest request
  ) {
    Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

    if (usuario == null) return new ModelAndView("redirect:/login");

    List<String> temas = datosParcial.getTemas() == null
      ? List.of()
      : datosParcial
        .getTemas()
        .stream()
        .filter(t -> t != null && !t.trim().isEmpty())
        .map(String::trim)
        .toList();

    Integer diasEstudio = datosParcial.getCantidadDiasEstudio();
    Integer horasPorDia = datosParcial.getHorasPorDia();

    boolean datosInvalidos =
      diasEstudio == null ||
      diasEstudio <= 0 ||
      horasPorDia == null ||
      horasPorDia <= 0 ||
      temas.isEmpty();

    boolean demasiadosTemas = !datosInvalidos && temas.size() > (long) diasEstudio * horasPorDia;

    if (datosInvalidos || demasiadosTemas) {
      Map<String, Object> modelo = new ModelMap();
      modelo.put("datosParcial", datosParcial);
      modelo.put("materias", servicioMateria.obtenerMaterias());
      modelo.put(
        "error",
        "Ingresá datos válidos. Con horas enteras, " +
        "cada tema necesita al menos una hora. " +
        "Reducí los temas o aumentá los días u horas."
      );

      return new ModelAndView("paginas/materia/parcial-formulario", modelo);
    }

    Materia materia = servicioMateria.buscarMateriaPorId(datosParcial.getMateriaId().intValue());

    Parcial parcial = new Parcial();
    parcial.setMateria(materia);
    parcial.setFecha(datosParcial.getFecha());
    parcial.setHorario(datosParcial.getHorario());
    parcial.setCantidadDiasEstudio(diasEstudio);
    parcial.setHorasPorDia(horasPorDia);
    parcial.setNombre(datosParcial.getNombre());

    servicioParcial.crearParcial(parcial);

    LocalDate fechaInicioEstudio = datosParcial.getFecha().minusDays(diasEstudio);

    int totalTemas = temas.size();
    int diasConTemas = Math.min(diasEstudio, totalTemas);

    int temasBasePorDia = totalTemas / diasConTemas;
    int temasSobrantes = totalTemas % diasConTemas;

    int indiceTema = 0;

    for (int dia = 0; dia < diasConTemas; dia++) {
      int temasDelDia = temasBasePorDia;

      if (dia < temasSobrantes) {
        temasDelDia++;
      }

      // 6. Dividir las horas del día entre los temas
      int horasBasePorTema = horasPorDia / temasDelDia;
      int horasSobrantes = horasPorDia % temasDelDia;

      LocalDate fechaSesion = fechaInicioEstudio.plusDays(dia);

      // 7. Crear las sesiones del día
      for (int j = 0; j < temasDelDia; j++) {
        String tema = temas.get(indiceTema);

        int horasSesion = horasBasePorTema;

        if (j < horasSobrantes) {
          horasSesion++;
        }

        servicioParcial.agregarTema(parcial.getId(), tema, fechaSesion, horasSesion, usuario);

        indiceTema++;
      }
    }

    return new ModelAndView("redirect:/parciales");
  }

  @RequestMapping(path = "/sesion/completar", method = RequestMethod.POST)
  public ModelAndView cambiarEstadoSesion(
    @RequestParam("id") Integer id,
    HttpServletRequest request
  ) {
    Boolean quedoCompletada = servicioSesionEstudio.cambiarEstado(id);

    Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

    if (usuario != null && quedoCompletada != null) {
      servicioRacha.recalcularRacha(usuario);

      request.getSession().setAttribute("USUARIO", usuario);
    }

    return new ModelAndView("redirect:/parciales");
  }
}
