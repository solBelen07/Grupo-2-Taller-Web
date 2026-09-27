package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Invitacion;
import com.tallerwebi.dominio.ServicioInvitacion;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.InvitacionInvalida;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorInvitacion {

  private ServicioInvitacion servicioInvitacion;

  @RequestMapping(path = "/grupos/{nombreGrupo}/invitar", method = RequestMethod.POST)
  public ModelAndView validarInvitacion(
    @PathVariable("nombreGrupo") String nombreGrupo,
    @ModelAttribute("datosInvitacion") DatosInvitacion datosInvitacion,
    HttpServletRequest request,
    RedirectAttributes redirectAttributes
  ) {
    Usuario emisor = (Usuario) request.getSession().getAttribute("USUARIO");
    if (emisor == null) {
      return new ModelAndView("redirect:/login");
    }
    datosInvitacion.setEmisor(emisor.getEmail());
    try {
      servicioInvitacion.validarInvitacion(datosInvitacion);
      redirectAttributes.addFlashAttribute("exito", "Invitación enviada");
      return new ModelAndView("redirect:/grupos");
    } catch (InvitacionInvalida e) {
      redirectAttributes.addFlashAttribute("error", "Invitación inválida");
      return new ModelAndView("redirect:/grupos/" + nombreGrupo + "/invitar");
    }
  }

  @RequestMapping(path = "/invitaciones", method = RequestMethod.GET)
  public ModelAndView verInvitaciones(HttpServletRequest request) {
    Map<String, Object> modelo = new ModelMap();
    try {
      Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");
      modelo.put(
        "datosInvitacion",
        servicioInvitacion.listarInvitaciones(usuarioLogueado.getEmail())
      );
      return new ModelAndView("invitaciones", modelo);
    } catch (Exception e) {
      modelo.put("error", "No se pudo mostrar invitaciones");
      return new ModelAndView("invitaciones", modelo);
    }
  }

  public ControladorInvitacion(ServicioInvitacion servicioInvitacion) {
    this.servicioInvitacion = servicioInvitacion;
  }
}
