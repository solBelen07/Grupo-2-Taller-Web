package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.MateriaExistente;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ServicioMateriaImpl implements ServicioMateria {

  private RepositorioMateria repositorioMateria;

  public ServicioMateriaImpl(RepositorioMateria repositorioMateria) {
    this.repositorioMateria = repositorioMateria;
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
  public void editarMateria(Materia materia) {
    repositorioMateria.modificar(materia);
  }

  @Override
  public void eliminarMateria(String nombre) {
    Materia materia = repositorioMateria.buscarPorNombre(nombre);

    if (materia != null) {
      repositorioMateria.eliminar(materia);
    }
  }
}
