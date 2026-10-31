package com.tallerwebi.dominio.racha;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.sesionEstudio.RepositorioSesionEstudio;
import com.tallerwebi.dominio.tecnicasestudio.RepositorioPomodoro;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioRachaImpl implements ServicioRacha {

  private RepositorioUsuario repositorioUsuario;
  private RepositorioSesionEstudio repositorioSesionEstudio;
  private RepositorioPomodoro repositorioPomodoro;

  public ServicioRachaImpl(
    RepositorioUsuario repositorioUsuario,
    RepositorioSesionEstudio repositorioSesionEstudio,
    RepositorioPomodoro repositorioPomodoro
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioSesionEstudio = repositorioSesionEstudio;
    this.repositorioPomodoro = repositorioPomodoro;
  }

  @Override
  public void registrarRacha(Usuario usuario) {
    if (usuario == null) return;

    Usuario persistido = obtenerPersistido(usuario);
    Racha racha = persistido.getRacha();

    LocalDate hoy = LocalDate.now();
    LocalDate ultimaFecha = racha.getUltimaFechaEstudio();
    int actual = racha.getRachaActual();

    if (ultimaFecha != null && ultimaFecha.equals(hoy) && actual > 0) {
      sincronizarSesion(usuario, racha);
      return;
    }

    if (ultimaFecha == null || actual == 0) racha.setRachaActual(1); else if (
      ultimaFecha.equals(hoy.minusDays(1))
    ) racha.setRachaActual(actual + 1); else racha.setRachaActual(1);

    if (racha.getRachaActual() > racha.getRecordRacha()) {
      racha.setRecordRacha(racha.getRachaActual());
    }

    racha.setUltimaFechaEstudio(hoy);
    persistido.setRacha(racha);
    this.repositorioUsuario.modificar(persistido);
    sincronizarSesion(usuario, racha);
  }

  @Override
  public void modificarRacha(Usuario usuario) {
    if (usuario == null || usuario.getId() == null) return;

    Usuario persistido = obtenerPersistido(usuario);
    Racha racha = persistido.getRacha();

    LocalDate hoy = LocalDate.now();
    LocalDate ultimaFecha = racha.getUltimaFechaEstudio();

    if (
      ultimaFecha != null && ultimaFecha.isBefore(hoy.minusDays(1)) && racha.getRachaActual() != 0
    ) {
      racha.setRachaActual(0);
      persistido.setRacha(racha);
      this.repositorioUsuario.modificar(persistido);
    }
    sincronizarSesion(usuario, racha);
  }

  @Override
  public Integer obtenerRacha(Usuario usuario) {
    return usuario == null ? 0 : usuario.getRacha().getRachaActual();
  }

  @Override
  public Integer obtenerRecordRacha(Usuario usuario) {
    return usuario == null ? 0 : usuario.getRacha().getRecordRacha();
  }

  @Override
  public void recalcularRacha(Usuario usuario) {
    if (usuario == null || usuario.getId() == null) {
      return;
    }

    LocalDate hoy = LocalDate.now();

    boolean sesionesHoy = repositorioSesionEstudio.existeSesionCompletadaEnFecha(
      usuario.getId(),
      hoy
    );

    boolean pomodorosHoy = repositorioPomodoro.existePomodoroCompletadoEnFecha(
      usuario.getId(),
      hoy
    );

    if (sesionesHoy || pomodorosHoy) {
      registrarRacha(usuario);
      return;
    }

    Usuario persistido = obtenerPersistido(usuario);
    Racha racha = persistido.getRacha();

    if (hoy.equals(racha.getUltimaFechaEstudio())) {
      int nuevaRacha = Math.max(0, racha.getRachaActual() - 1);

      racha.setRachaActual(nuevaRacha);

      racha.setUltimaFechaEstudio(nuevaRacha > 0 ? hoy.minusDays(1) : null);

      persistido.setRacha(racha);
      repositorioUsuario.modificar(persistido);
      sincronizarSesion(usuario, racha);
    }
  }

  private Usuario obtenerPersistido(Usuario usuario) {
    Usuario persistido = this.repositorioUsuario.buscarPorId(usuario.getId());
    if (persistido == null) throw new UsuarioNoEncontrado();
    return persistido;
  }

  private void sincronizarSesion(Usuario usuarioSesion, Racha racha) {
    usuarioSesion.setRacha(racha.copiar());
  }
}
