package com.tallerwebi.presentacion.grupo;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.InvitacionInvalida;
import com.tallerwebi.dominio.invitacion.ServicioInvitacion;
import com.tallerwebi.presentacion.grupo.DatosInvitacion;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorInvitacion {

  private ServicioInvitacion servicioInvitacion;

  @RequestMapping(path = "/grupos/{nombreGrupo}/invitar", method = RequestMethod.POST)
  public ModelAndView validarInvitacion(
    @PathVariable("nombreGrupo") String nombreGrupo,
    @ModelAttribute(modeloDeDatos) DatosInvitacion datosInvitacion,
    HttpServletRequest request,
    RedirectAttributes redirectAttributes
  ) {
    if (obtenerUsuarioLogueado(request) == null) {
      return new ModelAndView("redirect:/login");
    }
    datosInvitacion.setEmisor(obtenerUsuarioLogueado(request).getEmail());
    try {
      servicioInvitacion.validarInvitacion(datosInvitacion);
      redirectAttributes.addFlashAttribute("exito", "Invitación enviada");
      return new ModelAndView("redirect:/grupos");
    } catch (InvitacionInvalida e) {
      redirigirMensajeDeError(redirectAttributes, "Invitación inválida");
      return new ModelAndView("redirect:/grupos/" + nombreGrupo + "/invitar");
    }
  }

  @RequestMapping(path = "/invitaciones", method = RequestMethod.GET)
  public ModelAndView verInvitaciones(HttpServletRequest request) {
    Map<String, Object> modelo = new ModelMap();
    try {
      obtenerUsuarioLogueado(request).getEmail();
      modelo.put(
        "datosInvitacion",
        servicioInvitacion.listarInvitaciones(obtenerUsuarioLogueado(request).getEmail())
      );
      return new ModelAndView("paginas/grupo/invitaciones", modelo);
    } catch (Exception e) {
      modelo.put("error", "No se pudo mostrar invitaciones");
      return new ModelAndView("paginas/grupo/invitaciones", modelo);
    }
  }

  @RequestMapping(path = "/invitaciones/aceptar", method = RequestMethod.POST)
  public ModelAndView aceptar(
    @ModelAttribute(modeloDeDatos) DatosInvitacion datosInvitacion,
    HttpServletRequest request,
    RedirectAttributes redirectAttrs
  ) {
    try {
      if (obtenerUsuarioLogueado(request) == null) {
        return new ModelAndView("redirect:/login");
      }
      datosInvitacion.setReceptor(obtenerUsuarioLogueado(request).getEmail());
      servicioInvitacion.aceptarInvitacion(datosInvitacion);
      redirectAttrs.addFlashAttribute("exito", "Invitación aceptada");
    } catch (Exception e) {
      redirigirMensajeDeError(redirectAttrs, "No se pudo aceptar la invitación");
    }
    return new ModelAndView("redirect:/invitaciones");
  }

  @RequestMapping(path = "/invitaciones/rechazar", method = RequestMethod.POST)
  public ModelAndView rechazar(
    @ModelAttribute(modeloDeDatos) DatosInvitacion datosInvitacion,
    HttpServletRequest request,
    RedirectAttributes redirectAttributes
  ) {
    try {
      if (obtenerUsuarioLogueado(request) == null) {
        return new ModelAndView("redirect:/login");
      }
      datosInvitacion.setReceptor(obtenerUsuarioLogueado(request).getEmail());
      servicioInvitacion.rechazarInvitacion(datosInvitacion);

      redirectAttributes.addFlashAttribute("exito", "Invitación rechazada");
    } catch (Exception e) {
      redirigirMensajeDeError(redirectAttributes, "No se pudo rechazar la invitación");
    }
    return new ModelAndView("redirect:/invitaciones");
  }

  private Usuario obtenerUsuarioLogueado(HttpServletRequest request) {
    return (Usuario) request.getSession().getAttribute("USUARIO");
  }

  private void redirigirMensajeDeError(RedirectAttributes redirectAttributes, String mensaje) {
    redirectAttributes.addFlashAttribute("error", mensaje);
  }

  public static final String modeloDeDatos = "datosInvitacion";

  public ControladorInvitacion(ServicioInvitacion servicioInvitacion) {
    this.servicioInvitacion = servicioInvitacion;
  }
}
