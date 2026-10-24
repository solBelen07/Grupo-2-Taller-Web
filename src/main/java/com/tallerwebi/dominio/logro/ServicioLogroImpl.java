package com.tallerwebi.dominio.logro;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.invitacion.RepositorioInvitacion;
import com.tallerwebi.dominio.tecnicasestudio.RepositorioPomodoro;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("servicioLogro")
@Transactional
public class ServicioLogroImpl implements ServicioLogro {

  static final Long MINUTOS_PARA_LOGRO_MARATONISTA = 3000L;
  static final int CANTIDAD_INVITACIONES_PARA_LOGRO_POPULAR = 5;

  private RepositorioLogro repositorioLogro;
  private RepositorioInvitacion repositorioInvitacion;
  private RepositorioPomodoro repositorioPomodoro;

  public ServicioLogroImpl(
    RepositorioLogro repositorioLogro,
    RepositorioInvitacion repositorioInvitacion,
    RepositorioPomodoro repositorioPomodoro
  ) {
    this.repositorioLogro = repositorioLogro;
    this.repositorioInvitacion = repositorioInvitacion;
    this.repositorioPomodoro = repositorioPomodoro;
  }

  @Override
  public List<Logro> obtenerProgresos(Usuario usuario) {
    List<Logro> logros = new ArrayList<>();
    Integer cantInvitacionesRecibidas =
      repositorioInvitacion.cantidadDeInvitacionesHistoricoPorReceptor(usuario.getEmail());
    if (cantInvitacionesRecibidas > CANTIDAD_INVITACIONES_PARA_LOGRO_POPULAR) {
      logros.add(repositorioLogro.buscar("Popular"));
    }

    Long minutosPomodoro = repositorioPomodoro.calcularMinutosCompletados(usuario.getId());
    if (minutosPomodoro > MINUTOS_PARA_LOGRO_MARATONISTA) {
      logros.add(repositorioLogro.buscar("Maratonista"));
    }

    return logros;
  }

  @Override
  public List<Logro> obtenerLogros() {
    return repositorioLogro.listar();
  }
}
