package com.tallerwebi.dominio.parcial;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

import com.tallerwebi.dominio.sesionEstudio.ServicioSesionEstudio;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;

@Service
@Transactional
public class ServicioParcialImpl implements ServicioParcial {

  private final RepositorioParcial repositorioParcial;
    private final ServicioSesionEstudio servicioSesionEstudio;

    public ServicioParcialImpl(RepositorioParcial repositorioParcial, ServicioSesionEstudio servicioSesionEstudio) {
        this.repositorioParcial = repositorioParcial;
        this.servicioSesionEstudio = servicioSesionEstudio;
    }

  @Override
  public void crearParcial(Parcial parcial) {
    repositorioParcial.guardar(parcial);
  }

  @Override
  public Parcial buscarParcialPorId(Integer id) {
    return repositorioParcial.buscarPorId(id);
  }

  @Override
  public List<Parcial> obtenerParciales() {
    return repositorioParcial.obtenerTodos();
  }

  @Override
  public void editarParcial(Parcial parcial) {
    repositorioParcial.modificar(parcial);
  }

  @Override
  public void eliminarParcial(Integer id) {
    Parcial parcial = repositorioParcial.buscarPorId(id);

    if (parcial != null) {
      repositorioParcial.eliminar(parcial);
    }
  }

  @Override
    public void agregarTema(Integer parcialId, String tema, LocalDate fecha, Integer cantidadHoras) {

        Parcial parcial = repositorioParcial.buscarPorId(parcialId);

        if (parcial != null) {

            SesionEstudio sesion = new SesionEstudio();

            sesion.setTema(tema);
            sesion.setFecha(fecha);
            sesion.setCantidadHoras(cantidadHoras);
            sesion.setCompletada(false);

            parcial.agregarSesion(sesion);

            servicioSesionEstudio.crearSesion(sesion);
        }
    }
}
