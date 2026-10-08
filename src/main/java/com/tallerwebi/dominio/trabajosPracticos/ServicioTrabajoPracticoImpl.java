package com.tallerwebi.dominio.trabajosPracticos;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioTrabajoPractico")
@Transactional
public class ServicioTrabajoPracticoImpl implements ServicioTrabajoPractico {

  private final RepositorioTrabajoPractico repositorioTrabajoPractico;

  @Autowired
  public ServicioTrabajoPracticoImpl(RepositorioTrabajoPractico repositorioTrabajoPractico) {
    this.repositorioTrabajoPractico = repositorioTrabajoPractico;
  }

  @Override
  public TrabajoPractico crearTrabajoPractico(TrabajoPractico trabajoPractico) {
    return this.repositorioTrabajoPractico.save(trabajoPractico);
  }

  @Override
  public TrabajoPractico cambiarEstado(Long id, EstadoTP nuevoEstado) {
    TrabajoPractico trabajoPractico = this.repositorioTrabajoPractico.buscarPorId(id);

    if (trabajoPractico == null) {
      throw new RuntimeException("Trabajo práctico no encontrado");
    }

    trabajoPractico.setEstado(nuevoEstado);
    return this.repositorioTrabajoPractico.save(trabajoPractico);
  }

  @Override
  public List<TrabajoPractico> obtenerTodos() {
    return this.repositorioTrabajoPractico.obtenerTodos();
  }

  @Override
  public List<TrabajoPractico> buscarPorMateria(String materia) {
    return repositorioTrabajoPractico.buscarPorMateria(materia);
  }

  @Override
  public TrabajoPractico buscarPorId(Long id) {
    return repositorioTrabajoPractico.buscarPorId(id);
  }

  @Override
  public void eliminar(Long id) {
    TrabajoPractico tp = repositorioTrabajoPractico.buscarPorId(id);
    if (tp == null) {
      throw new RuntimeException("Trabajo práctico no encontrado para eliminar");
    }
    repositorioTrabajoPractico.eliminar(tp);
  }
}
