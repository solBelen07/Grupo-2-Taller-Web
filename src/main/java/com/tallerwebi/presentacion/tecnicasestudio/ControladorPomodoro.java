package com.tallerwebi.presentacion.tecnicasestudio;

import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.tecnicasestudio.Pomodoro;
import com.tallerwebi.dominio.tecnicasestudio.ServicioPomodoro;
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
  private static final String REDIRECCION_INICIO = "redirect:/FocusFlight";
  private static final String REDIRECCION_TIMER = "redirect:/FocusFlight/Timer/";
  private static final String ESTADO_INEXISTENTE = "INEXISTENTE";

  private final ServicioPomodoro servicioPomodoro;
  private final ServicioLogin servicioUsuario;
  private final ServicioMateria servicioMateria;

  @Autowired
  public ControladorPomodoro(
    ServicioPomodoro servicioPomodoro,
    ServicioLogin servicioUsuario,
    ServicioMateria servicioMateria
  ) {
    this.servicioPomodoro = servicioPomodoro;
    this.servicioUsuario = servicioUsuario;
    this.servicioMateria = servicioMateria;
  }

  @RequestMapping("/FocusFlight")
  public ModelAndView mostrarConfigPomodoro() {
    Usuario usuario = this.obtenerUsuarioActual();

    if (usuario != null) {
      Optional<Pomodoro> sesionActiva = this.servicioPomodoro.buscarSesionActiva(usuario);
      if (sesionActiva.isPresent()) {
        return new ModelAndView(REDIRECCION_TIMER + sesionActiva.get().getId());
      }
    }

    Map<String, Object> modelo = new ModelMap();
    List<Materia> materias = this.servicioMateria.obtenerMaterias();

    modelo.put("datosPomodoro", new DatosPomodoro());
    modelo.put("materias", materias);

    return new ModelAndView(VISTA_CONFIGURACION, modelo);
  }

  @RequestMapping(value = "/FocusFlight/Boleto", method = RequestMethod.POST)
  public ModelAndView mostrarBoletoPomodoro(
    @ModelAttribute("datosPomodoro") DatosPomodoro datosPomodoro
  ) {
    Map<String, Object> modelo = new ModelMap();

    Materia materia = this.servicioMateria.buscarMateriaPorId(datosPomodoro.getMateria());
    if (materia == null) {
      throw new RuntimeException("La materia seleccionada no existe.");
    }
    Usuario usuario = this.obtenerUsuarioActual();

    Pomodoro sesion =
      this.servicioPomodoro.registrarSesionEstudio(
          datosPomodoro.getOrigen(),
          datosPomodoro.getDestino(),
          datosPomodoro.getDuracion(),
          materia,
          usuario,
          datosPomodoro.getObjetivo()
        );

    modelo.put("datosPomodoro", datosPomodoro);
    modelo.put("materia", materia);
    modelo.put("pomodoroId", sesion.getId());

    return new ModelAndView(VISTA_BOLETO, modelo);
  }

  @RequestMapping(
    value = "/FocusFlight/Timer/{id}",
    method = { RequestMethod.GET, RequestMethod.POST }
  )
  public ModelAndView mostrarTimer(@PathVariable("id") long id) {
    long segundosRestantes = this.servicioPomodoro.obtenerSegundosRestantes(id);
    Pomodoro pomodoro = this.servicioPomodoro.buscarPorId(id);

    if (pomodoro == null) return new ModelAndView(REDIRECCION_INICIO);

    Map<String, Object> modelo = new ModelMap();
    modelo.put("pomodoro", pomodoro);
    modelo.put("segundosRestantes", segundosRestantes);

    return new ModelAndView(VISTA_TIMER, modelo);
  }

  @ResponseBody
  @RequestMapping(value = "/FocusFlight/Timer/estado/{id}", method = RequestMethod.GET)
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

  @RequestMapping(value = "/FocusFlight/Timer/pausar/{id}", method = RequestMethod.POST)
  public ModelAndView pausarSesion(@PathVariable("id") long id) {
    this.servicioPomodoro.pausarSesion(id);
    return new ModelAndView(REDIRECCION_TIMER + id);
  }

  @RequestMapping(value = "/FocusFlight/Timer/reanudar/{id}", method = RequestMethod.POST)
  public ModelAndView reanudarSesion(@PathVariable("id") long id) {
    this.servicioPomodoro.reanudarSesion(id);
    return new ModelAndView(REDIRECCION_TIMER + id);
  }

  @RequestMapping(value = "/FocusFlight/Timer/cancelar/{id}", method = RequestMethod.POST)
  public ModelAndView cancelarSesion(@PathVariable("id") long id) {
    this.servicioPomodoro.modificarEstadoCancelada(id);
    return new ModelAndView(REDIRECCION_INICIO);
  }

  private Usuario obtenerUsuarioActual() {
    return this.servicioUsuario.consultarUsuario("test@unlam.edu.ar", "test");
  }
}
