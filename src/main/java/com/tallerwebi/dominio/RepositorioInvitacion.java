package com.tallerwebi.dominio;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioInvitacion {
  Invitacion enviarInvitacion(Invitacion invitacion);
  List<Invitacion> buscarInvitacionesPorReceptor(String emailReceptor);
}
