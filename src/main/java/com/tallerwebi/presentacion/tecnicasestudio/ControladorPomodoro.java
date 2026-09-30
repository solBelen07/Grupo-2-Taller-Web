package com.tallerwebi.presentacion.tecnicasestudio;

import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.tecnicasestudio.ServicioPomodoro;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorPomodoro {

  private ServicioPomodoro servicioPomodoro;
  private ServicioLogin servicioUsuario;
  private ServicioMateria servicioMateria;

  @Autowired
  public ControladorPomodoro(ServicioPomodoro servicioPomodoro, ServicioLogin servicioUsuario) {
    this.servicioPomodoro = servicioPomodoro;
    this.servicioUsuario = servicioUsuario;
  }

  @RequestMapping("/FocusFlight")
  public ModelAndView mostrarConfigPomodoro() {
    Map<String, Object> modelo = new ModelMap();
    List<Materia> materias = this.servicioMateria.obtenerMaterias();

    modelo.put("datosPomodoro", new DatosPomodoro());
    modelo.put("materias", materias);

    return new ModelAndView("paginas/tecnicasestudio/pomodoro", modelo);
  }

  @RequestMapping(value = "/FocusFlight/Boleto", method = RequestMethod.POST)
  public ModelAndView mostrarBoletoPomodoro(
    @ModelAttribute("datosPomodoro") DatosPomodoro datosPomodoro
  ) {
    Map<String, Object> modelo = new ModelMap();

    Usuario usuario = this.servicioUsuario.consultarUsuario("test@unlam.edu.ar", "test");

    this.servicioPomodoro.registrarSesionEstudio(
        datosPomodoro.getOrigen(),
        datosPomodoro.getDestino(),
        datosPomodoro.getDuracion(),
        datosPomodoro.getMateria(),
        usuario,
        datosPomodoro.getObjetivo()
      );

    modelo.put("datosPomodoro", datosPomodoro);

    return new ModelAndView("paginas/tecnicasestudio/boleto", modelo);
  }
}
