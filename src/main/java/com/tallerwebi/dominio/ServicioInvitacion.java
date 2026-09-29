package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.InvitacionInvalida;
import com.tallerwebi.presentacion.DatosInvitacion;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("servicioInvitacion")
public interface ServicioInvitacion {
  Invitacion validarInvitacion(DatosInvitacion datosInvitacion) throws InvitacionInvalida;
  Invitacion crearInvitacion(Invitacion invitacion);
  List<Invitacion> listarInvitaciones(String usuarioLogueado);
  void aceptarInvitacion(DatosInvitacion datosInvitacion) throws InvitacionInvalida;
}
