package com.tallerwebi.dominio.invitacion;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.InvitacionInvalida;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.RepositorioGrupo;
import com.tallerwebi.presentacion.DatosInvitacion;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioInvitacion")
@Transactional
public class ServicioInvitacionImpl implements ServicioInvitacion {

  private RepositorioInvitacion repositorioInvitacion;
  private RepositorioGrupo repositorioGrupo;
  private RepositorioUsuario repositorioUsuario;

  public ServicioInvitacionImpl(
    RepositorioInvitacion repositorioInvitacion,
    RepositorioUsuario repositorioUsuario,
    RepositorioGrupo repositorioGrupo
  ) {
    this.repositorioInvitacion = repositorioInvitacion;
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioGrupo = repositorioGrupo;
  }

  public Invitacion validarInvitacion(DatosInvitacion datosInvitacion) throws InvitacionInvalida {
    Usuario usuarioEmisor = repositorioUsuario.buscar(datosInvitacion.getEmisor());
    Usuario usuarioReceptor = repositorioUsuario.buscar(datosInvitacion.getReceptor());
    Grupo grupoInvitado = repositorioGrupo.buscar(datosInvitacion.getGrupo());

    if (
      usuarioEmisor == null ||
      usuarioReceptor == null ||
      grupoInvitado == null ||
      usuarioEmisor == usuarioReceptor
    ) {
      throw new InvitacionInvalida();
    }

    Invitacion invitacion = new Invitacion(usuarioEmisor, usuarioReceptor, grupoInvitado);
    return crearInvitacion(invitacion);
  }

  public Invitacion crearInvitacion(Invitacion invitacion) {
    return this.repositorioInvitacion.enviarInvitacion(invitacion);
  }

  public void aceptarInvitacion(DatosInvitacion datosInvitacion) throws InvitacionInvalida {
    Invitacion invitacion = repositorioInvitacion.buscar(
      datosInvitacion.getEmisor(),
      datosInvitacion.getReceptor(),
      datosInvitacion.getGrupo()
    );
    if (invitacion == null) {
      throw new InvitacionInvalida();
    }
    invitacion.setVigente(Boolean.FALSE);
    repositorioInvitacion.cambiarEstado(invitacion, Estado.ACEPTADA);
  }

  public void rechazarInvitacion(DatosInvitacion datosInvitacion) throws InvitacionInvalida {
    Invitacion invitacion = repositorioInvitacion.buscar(
      datosInvitacion.getEmisor(),
      datosInvitacion.getReceptor(),
      datosInvitacion.getGrupo()
    );
    if (invitacion == null) {
      throw new InvitacionInvalida();
    }
    invitacion.setVigente(Boolean.FALSE);
    repositorioInvitacion.cambiarEstado(invitacion, Estado.RECHAZADA);
  }

  public List<Invitacion> listarInvitaciones(String usuarioLogueado) {
    return this.repositorioInvitacion.buscarInvitacionesPorReceptor(usuarioLogueado);
  }
}
