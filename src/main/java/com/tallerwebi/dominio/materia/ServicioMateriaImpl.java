package com.tallerwebi.dominio.materia;

import com.tallerwebi.dominio.RepositorioEvento;
import com.tallerwebi.dominio.excepcion.excepcionMateria.MateriaExistente;
import com.tallerwebi.dominio.parcial.RepositorioParcial;
import com.tallerwebi.dominio.sesionEstudio.RepositorioSesionEstudio;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioMateriaImpl implements ServicioMateria {

  private RepositorioMateria repositorioMateria;
  private RepositorioEvento repositorioEvento;
  private RepositorioParcial repositorioParcial;
  private RepositorioSesionEstudio repositorioSesion;

  public ServicioMateriaImpl(
    RepositorioMateria repositorioMateria,
    RepositorioEvento repositorioEvento,
    RepositorioParcial repositorioParcial,
    RepositorioSesionEstudio repositorioSesion
  ) {
    this.repositorioMateria = repositorioMateria;
    this.repositorioEvento = repositorioEvento;
    this.repositorioParcial = repositorioParcial;
    this.repositorioSesion = repositorioSesion;
  }

  @Override
  public void crearMateria(Materia materia) throws MateriaExistente {
    Materia existente = repositorioMateria.buscarPorNombre(materia.getNombre());

    if (existente != null) {
      throw new MateriaExistente();
    }

    repositorioMateria.guardar(materia);
  }

  @Override
  public List<Materia> obtenerMaterias() {
    return repositorioMateria.obtenerTodas();
  }

  @Override
  public Materia buscarMateriaPorNombre(String nombre) {
    return repositorioMateria.buscarPorNombre(nombre);
  }

  @Override
  public Materia buscarMateriaPorId(Integer id) {
    return repositorioMateria.buscarPorId(id);
  }

  @Override
  public void editarMateria(Materia materia) {
    repositorioMateria.modificar(materia);
  }

  @Override
  public void eliminarMateria(String nombre) {
    Materia materia = repositorioMateria.buscarPorNombre(nombre);

    if (materia != null) {
      repositorioSesion.eliminarPorMateriaId(materia.getId());
      repositorioParcial.eliminarPorMateriaId(materia.getId());
      repositorioEvento.eliminarPorMateriaId(materia.getId());
      repositorioMateria.eliminar(materia);
    }
  }
}
