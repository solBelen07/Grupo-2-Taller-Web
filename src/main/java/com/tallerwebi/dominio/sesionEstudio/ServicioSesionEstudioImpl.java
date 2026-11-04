package com.tallerwebi.dominio.sesionEstudio;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioSesionEstudioImpl implements ServicioSesionEstudio {

  private final RepositorioSesionEstudio repositorioSesionEstudio;

  public ServicioSesionEstudioImpl(RepositorioSesionEstudio repositorioSesionEstudio) {
    this.repositorioSesionEstudio = repositorioSesionEstudio;
  }

  @Override
  public void crearSesion(SesionEstudio sesionEstudio) {
    repositorioSesionEstudio.guardar(sesionEstudio);
  }

  @Override
  public void editarSesion(SesionEstudio sesionEstudio) {
    repositorioSesionEstudio.modificar(sesionEstudio);
  }

  @Override
  public void eliminarSesion(SesionEstudio sesionEstudio) {
    repositorioSesionEstudio.eliminar(sesionEstudio);
  }

  @Override
  public Boolean cambiarEstado(Integer id) {
    SesionEstudio sesion = repositorioSesionEstudio.buscarPorId(id);

    if (sesion == null) return null;

    boolean completada = !Boolean.TRUE.equals(sesion.getCompletada());

    sesion.setCompletada(completada);

    sesion.setFechaCompletado(completada ? LocalDate.now() : null);

    repositorioSesionEstudio.modificar(sesion);

    return completada;
  }
}
