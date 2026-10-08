package com.tallerwebi.dominio.logro;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository("repositorioLogro")
public interface RepositorioLogro {
  Logro buscar(String nombre);
  List<Logro> listar();
}
