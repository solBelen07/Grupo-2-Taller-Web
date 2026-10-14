package com.tallerwebi.dominio.materialBibliografico;

import java.io.IOException;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ServicioMaterial {
  void subirMaterial(Integer materiaId, MultipartFile archivo) throws IOException;
  List<MaterialBibliografico> obtenerPorMateria(Integer materiaId);
  MaterialBibliografico buscarPorId(Integer id);
  byte[] descargarMaterial(Integer id);
  void eliminarMaterial(Integer id);
}
