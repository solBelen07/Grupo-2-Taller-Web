package com.tallerwebi.dominio.catalogo;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioCatalogo")
@Transactional
public class ServicioCatalogoImpl implements ServicioCatalogo {

  private final RepositorioCatalogo repositorioCatalogo;

  @Autowired
  public ServicioCatalogoImpl(RepositorioCatalogo repositorioCatalogo) {
    this.repositorioCatalogo = repositorioCatalogo;
  }

  @Override
  public List<Universidad> obtenerUniversidades() {
    return repositorioCatalogo.obtenerUniversidades();
  }

  @Override
  public List<Carrera> obtenerCarreras() {
    return repositorioCatalogo.obtenerCarreras();
  }

  @Override
  public List<Interes> obtenerIntereses() {
    return repositorioCatalogo.obtenerIntereses();
  }
}
