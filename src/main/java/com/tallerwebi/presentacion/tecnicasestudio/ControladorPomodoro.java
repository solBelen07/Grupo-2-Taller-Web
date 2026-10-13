package com.tallerwebi.presentacion.tecnicasestudio;

import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.racha.ServicioRacha;
import com.tallerwebi.dominio.tecnicasestudio.Estado;
import com.tallerwebi.dominio.tecnicasestudio.Pomodoro;
import com.tallerwebi.dominio.tecnicasestudio.ServicioPomodoro;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPomodoro {

  private static final String VISTA_CONFIGURACION = "paginas/tecnicasestudio/pomodoro";
  private static final String VISTA_BOLETO = "paginas/tecnicasestudio/boleto";
  private static final String VISTA_TIMER = "paginas/tecnicasestudio/timer";
  private static final String REDIRECCION_INICIO = "redirect:/focusflight";
  private static final String REDIRECCION_TIMER = "redirect:/focusflight/timer/";
  private static final String ESTADO_INEXISTENTE = "INEXISTENTE";

  private final ServicioPomodoro servicioPomodoro;
  private final ServicioLogin servicioUsuario;
  private final ServicioMateria servicioMateria;
  private final ServicioRacha servicioRacha;

  @Autowired
  public ControladorPomodoro(
    ServicioPomodoro servicioPomodoro,
    ServicioLogin servicioUsuario,
    ServicioMateria servicioMateria,
    ServicioRacha servicioRacha
  ) {
    this.servicioPomodoro = servicioPomodoro;
    this.servicioUsuario = servicioUsuario;
    this.servicioMateria = servicioMateria;
    this.servicioRacha = servicioRacha;
  }

  @RequestMapping("/focusflight")
  public ModelAndView mostrarConfigPomodoro(HttpServletRequest request) {
    Usuario usuario = obtenerUsuarioLogueado(request);
    if (usuario == null) return new ModelAndView("redirect:/login");

    Optional<Pomodoro> sesionActiva = this.servicioPomodoro.buscarSesionActiva(usuario);
    if (sesionActiva.isPresent()) return new ModelAndView(
      REDIRECCION_TIMER + sesionActiva.get().getId()
    );

    Map<String, Object> modelo = new ModelMap();
    List<Materia> materias = this.servicioMateria.obtenerMaterias();

    modelo.put("datosPomodoro", new DatosPomodoro());
    modelo.put("materias", materias);

    return new ModelAndView(VISTA_CONFIGURACION, modelo);
  }

  @RequestMapping(value = "/focusflight/boleto", method = RequestMethod.POST)
  public ModelAndView mostrarBoletoPomodoro(
    HttpServletRequest request,
    @ModelAttribute("datosPomodoro") DatosPomodoro datosPomodoro
  ) {
    Map<String, Object> modelo = new ModelMap();

    Materia materia = this.servicioMateria.buscarMateriaPorId(datosPomodoro.getMateria());

    if (materia == null) throw new RuntimeException("La materia seleccionada no existe.");

    Usuario usuario = obtenerUsuarioLogueado(request);
    if (usuario == null) return new ModelAndView("redirect:/login");

    UsuarioDTO usuarioDTO = new UsuarioDTO(
      usuario.getId(),
      usuario.getNombre(),
      usuario.getApellido()
    );

    Pomodoro sesion =
      this.servicioPomodoro.registrarSesionEstudio(
          datosPomodoro.getOrigen(),
          datosPomodoro.getDestino(),
          datosPomodoro.getDuracion(),
          materia,
          usuario,
          datosPomodoro.getObjetivo()
        );

    datosPomodoro.setUsuario(usuarioDTO);

    modelo.put("datosPomodoro", datosPomodoro);
    modelo.put("materia", materia);
    modelo.put("pomodoroId", sesion.getId());

    return new ModelAndView(VISTA_BOLETO, modelo);
  }

  @RequestMapping(
    value = "/focusflight/timer/{id}",
    method = { RequestMethod.GET, RequestMethod.POST }
  )
  public ModelAndView mostrarTimer(HttpServletRequest request, @PathVariable("id") long id) {
    Usuario usuario = obtenerUsuarioLogueado(request);

    long segundosRestantes = this.servicioPomodoro.obtenerSegundosRestantes(id);
    Pomodoro pomodoro = this.servicioPomodoro.buscarPorId(id);

    if (pomodoro == null) return new ModelAndView(REDIRECCION_INICIO);

    if (segundosRestantes == 0) pomodoro.setEstado(Estado.COMPLETADO);

    if (segundosRestantes == 0 && usuario != null && pomodoro.getEstado() == Estado.COMPLETADO) {
      servicioRacha.registrarRacha(usuario);
      request.getSession().setAttribute("USUARIO", usuario);
    }

    Map<String, Object> modelo = new ModelMap();
    modelo.put("pomodoro", pomodoro);
    modelo.put("segundosRestantes", segundosRestantes);

    return new ModelAndView(VISTA_TIMER, modelo);
  }

  @ResponseBody
  @RequestMapping(value = "/focusflight/timer/estado/{id}", method = RequestMethod.GET)
  public Map<String, Object> obtenerEstadoTimer(@PathVariable("id") long id) {
    long segundos = this.servicioPomodoro.obtenerSegundosRestantes(id);
    Pomodoro pomodoro = this.servicioPomodoro.buscarPorId(id);

    String estado = ESTADO_INEXISTENTE;
    if (pomodoro != null && pomodoro.getEstado() != null) estado = pomodoro.getEstado().name();

    Map<String, Object> respuesta = new HashMap<>();
    respuesta.put("segundosRestantes", segundos);
    respuesta.put("estado", estado);
    return respuesta;
  }

  @RequestMapping(value = "/focusflight/timer/pausar/{id}", method = RequestMethod.POST)
  public ModelAndView pausarSesion(@PathVariable("id") long id) {
    this.servicioPomodoro.pausarSesion(id);
    return new ModelAndView(REDIRECCION_TIMER + id);
  }

  @RequestMapping(value = "/focusflight/timer/reanudar/{id}", method = RequestMethod.POST)
  public ModelAndView reanudarSesion(@PathVariable("id") long id) {
    this.servicioPomodoro.reanudarSesion(id);
    return new ModelAndView(REDIRECCION_TIMER + id);
  }

  @RequestMapping(value = "/focusflight/timer/cancelar/{id}", method = RequestMethod.POST)
  public ModelAndView cancelarSesion(@PathVariable("id") long id) {
    this.servicioPomodoro.modificarEstadoCancelada(id);
    return new ModelAndView(REDIRECCION_INICIO);
  }

  private Usuario obtenerUsuarioLogueado(HttpServletRequest request) {
    return (Usuario) request.getSession().getAttribute("USUARIO");
  }
}
