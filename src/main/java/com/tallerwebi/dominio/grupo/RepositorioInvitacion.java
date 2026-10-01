package com.tallerwebi.dominio.grupo;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioInvitacion {
  Invitacion enviarInvitacion(Invitacion invitacion);
  List<Invitacion> buscarInvitacionesPorReceptor(String emailReceptor);
  Invitacion buscar(String emailEmisor, String emailReceptor, String nombreGrupo);
  void aceptar(Invitacion invitacion);
}
