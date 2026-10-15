package com.tallerwebi.dominio.materialBibliografico;

import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.infraestructura.r2.AlmacenamientoR2;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ServicioMaterialImpl implements ServicioMaterial {

  private static final long TAMANIO_MAXIMO = 50L * 1024 * 1024;

  private static final Set<String> EXTENSIONES = Set.of(".pdf", ".doc", ".docx");

  private final RepositorioMaterial repositorioMaterial;
  private final ServicioMateria servicioMateria;
  private final AlmacenamientoR2 almacenamientoR2;

  public ServicioMaterialImpl(
    RepositorioMaterial repositorioMaterial,
    ServicioMateria servicioMateria,
    AlmacenamientoR2 almacenamientoR2
  ) {
    this.repositorioMaterial = repositorioMaterial;
    this.servicioMateria = servicioMateria;
    this.almacenamientoR2 = almacenamientoR2;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void subirMaterial(Integer materiaId, MultipartFile archivo) throws IOException {
    Materia materia = servicioMateria.buscarMateriaPorId(materiaId);

    if (materia == null) {
      throw new IllegalArgumentException("La materia no existe");
    }

    validarArchivo(archivo);

    String claveR2 = almacenamientoR2.subirArchivo(archivo, materiaId);

    try {
      MaterialBibliografico material = new MaterialBibliografico(
        archivo.getOriginalFilename(),
        archivo.getContentType(),
        archivo.getSize(),
        claveR2,
        materia
      );

      repositorioMaterial.guardar(material);
    } catch (RuntimeException e) {
      try {
        almacenamientoR2.eliminarArchivo(claveR2);
      } catch (RuntimeException errorR2) {
        e.addSuppressed(errorR2);
      }
      throw e;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<MaterialBibliografico> obtenerPorMateria(Integer materiaId) {
    return repositorioMaterial.buscarPorMateria(materiaId);
  }

  @Override
  @Transactional(readOnly = true)
  public MaterialBibliografico buscarPorId(Integer id) {
    return repositorioMaterial.buscarPorId(id);
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] descargarMaterial(Integer id) {
    MaterialBibliografico material = repositorioMaterial.buscarPorId(id);

    if (material == null) {
      throw new IllegalArgumentException("El material no existe");
    }

    return almacenamientoR2.descargarArchivo(material.getClaveR2());
  }

  @Override
  @Transactional
  public void eliminarMaterial(Integer id) {
    MaterialBibliografico material = repositorioMaterial.buscarPorId(id);

    if (material == null) {
      throw new IllegalArgumentException("El material no existe");
    }

    almacenamientoR2.eliminarArchivo(material.getClaveR2());

    repositorioMaterial.eliminar(material);
  }

  private void validarArchivo(MultipartFile archivo) {
    validarArchivoNoVacio(archivo);
    validarTamanio(archivo);
    validarExtension(archivo);
  }

  private void validarArchivoNoVacio(MultipartFile archivo) {
    if (archivo == null || archivo.isEmpty()) {
      throw new IllegalArgumentException("No se selecciono ningun archivo");
    }
  }

  private void validarTamanio(MultipartFile archivo) {
    if (archivo.getSize() > TAMANIO_MAXIMO) {
      throw new IllegalArgumentException("El archivo supera los 50 MB");
    }
  }

  private void validarExtension(MultipartFile archivo) {
    String nombre = archivo.getOriginalFilename();

    if (nombre == null || nombre.isBlank()) {
      throw new IllegalArgumentException("El archivo debe tener un nombre");
    }

    String nombreNormalizado = nombre.toLowerCase(Locale.ROOT);

    boolean extensionValida = EXTENSIONES.stream().anyMatch(nombreNormalizado::endsWith);

    if (!extensionValida) {
      throw new IllegalArgumentException("Solo se permiten archivos PDF, DOC y DOCX");
    }
  }
}
