package com.tallerwebi.dominio.logro;

import com.tallerwebi.dominio.Usuario;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("servicioLogro")
public interface ServicioLogro {
  List<Logro> obtenerProgresos(Usuario usuario);
  List<Logro> obtenerLogros();
}
