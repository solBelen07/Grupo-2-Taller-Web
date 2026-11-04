
package com.tallerwebi.dominio.materialBibliografico;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.infraestructura.r2.AlmacenamientoR2;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

public class ServicioMaterialTest {

  private ServicioMaterial servicioMaterial;
  private RepositorioMaterial repositorioMaterialMock;
  private ServicioMateria servicioMateriaMock;
  private AlmacenamientoR2 almacenamientoR2Mock;

  private Materia materia;

  @BeforeEach
  public void init() {
    repositorioMaterialMock = mock(RepositorioMaterial.class);
    servicioMateriaMock = mock(ServicioMateria.class);
    almacenamientoR2Mock = mock(AlmacenamientoR2.class);

    servicioMaterial = new ServicioMaterialImpl(
      repositorioMaterialMock,
      servicioMateriaMock,
      almacenamientoR2Mock
    );

    materia = new Materia();
  }

  @Test
  public void queSePuedaSubirUnArchivoValido() throws IOException {
    MultipartFile archivo = mock(MultipartFile.class);

    when(servicioMateriaMock.buscarMateriaPorId(1)).thenReturn(materia);
    when(archivo.isEmpty()).thenReturn(false);
    when(archivo.getSize()).thenReturn(1024L);
    when(archivo.getOriginalFilename()).thenReturn("apunte.pdf");
    when(archivo.getContentType()).thenReturn("application/pdf");
    when(almacenamientoR2Mock.subirArchivo(archivo, 1)).thenReturn("clave-r2");

    servicioMaterial.subirMaterial(1, archivo);

    verify(almacenamientoR2Mock, times(1)).subirArchivo(archivo, 1);
    verify(repositorioMaterialMock, times(1)).guardar(any(MaterialBibliografico.class));
  }

  @Test
  public void queNoSePuedaSubirUnArchivoSiLaMateriaNoExiste() {
    when(servicioMateriaMock.buscarMateriaPorId(1)).thenReturn(null);

    assertThrows(
      IllegalArgumentException.class,
      () -> servicioMaterial.subirMaterial(1, null)
    );

    verifyNoInteractions(almacenamientoR2Mock);
    verifyNoInteractions(repositorioMaterialMock);
  }

  @Test
  public void queNoSePuedaSubirUnArchivoNulo() {
    when(servicioMateriaMock.buscarMateriaPorId(1)).thenReturn(materia);

    assertThrows(
      IllegalArgumentException.class,
      () -> servicioMaterial.subirMaterial(1, null)
    );

    verifyNoInteractions(almacenamientoR2Mock);
  }

  @Test
  public void queNoSePuedaSubirUnArchivoVacio() {
    MultipartFile archivo = mock(MultipartFile.class);

    when(servicioMateriaMock.buscarMateriaPorId(1)).thenReturn(materia);
    when(archivo.isEmpty()).thenReturn(true);

    assertThrows(
      IllegalArgumentException.class,
      () -> servicioMaterial.subirMaterial(1, archivo)
    );

    verifyNoInteractions(almacenamientoR2Mock);
  }

  @Test
  public void queNoSePuedaSubirUnArchivoQueSupereLos50MB() {
    MultipartFile archivo = mock(MultipartFile.class);

    when(servicioMateriaMock.buscarMateriaPorId(1)).thenReturn(materia);
    when(archivo.isEmpty()).thenReturn(false);
    when(archivo.getSize()).thenReturn(51L * 1024 * 1024);

    assertThrows(
      IllegalArgumentException.class,
      () -> servicioMaterial.subirMaterial(1, archivo)
    );

    verifyNoInteractions(almacenamientoR2Mock);
  }

  @Test
  public void queNoSePuedaSubirUnArchivoSinNombre() {
    MultipartFile archivo = mock(MultipartFile.class);

    when(servicioMateriaMock.buscarMateriaPorId(1)).thenReturn(materia);
    when(archivo.isEmpty()).thenReturn(false);
    when(archivo.getSize()).thenReturn(1024L);
    when(archivo.getOriginalFilename()).thenReturn(null);

    assertThrows(
      IllegalArgumentException.class,
      () -> servicioMaterial.subirMaterial(1, archivo)
    );

    verifyNoInteractions(almacenamientoR2Mock);
  }

  @Test
  public void queNoSePuedaSubirUnArchivoConExtensionInvalida() {
    MultipartFile archivo = mock(MultipartFile.class);

    when(servicioMateriaMock.buscarMateriaPorId(1)).thenReturn(materia);
    when(archivo.isEmpty()).thenReturn(false);
    when(archivo.getSize()).thenReturn(1024L);
    when(archivo.getOriginalFilename()).thenReturn("imagen.png");

    assertThrows(
      IllegalArgumentException.class,
      () -> servicioMaterial.subirMaterial(1, archivo)
    );

    verifyNoInteractions(almacenamientoR2Mock);
  }

  @Test
  public void queSeElimineElArchivoDeR2SiFallaElGuardadoEnRepositorio() throws IOException {
    MultipartFile archivo = mock(MultipartFile.class);

    when(servicioMateriaMock.buscarMateriaPorId(1)).thenReturn(materia);
    when(archivo.isEmpty()).thenReturn(false);
    when(archivo.getSize()).thenReturn(1024L);
    when(archivo.getOriginalFilename()).thenReturn("apunte.pdf");
    when(archivo.getContentType()).thenReturn("application/pdf");
    when(almacenamientoR2Mock.subirArchivo(archivo, 1)).thenReturn("clave-r2");

    doThrow(new RuntimeException("Error al guardar"))
      .when(repositorioMaterialMock)
      .guardar(any(MaterialBibliografico.class));

    assertThrows(
      RuntimeException.class,
      () -> servicioMaterial.subirMaterial(1, archivo)
    );

    verify(almacenamientoR2Mock, times(1)).eliminarArchivo("clave-r2");
  }

  @Test
  public void queSePuedanObtenerLosMaterialesDeUnaMateria() {
    List<MaterialBibliografico> materiales = List.of(
      new MaterialBibliografico("apunte.pdf", "application/pdf", 1024L, "clave", materia)
    );

    when(repositorioMaterialMock.buscarPorMateria(1)).thenReturn(materiales);

    List<MaterialBibliografico> resultado = servicioMaterial.obtenerPorMateria(1);

    assertEquals(materiales, resultado);
    verify(repositorioMaterialMock, times(1)).buscarPorMateria(1);
  }

  @Test
  public void queSePuedaBuscarUnMaterialPorId() {
    MaterialBibliografico material = new MaterialBibliografico(
      "apunte.pdf", "application/pdf", 1024L, "clave", materia
    );

    when(repositorioMaterialMock.buscarPorId(1)).thenReturn(material);

    MaterialBibliografico resultado = servicioMaterial.buscarPorId(1);

    assertEquals(material, resultado);
    verify(repositorioMaterialMock, times(1)).buscarPorId(1);
  }

  @Test
  public void queNoSePuedaDescargarUnMaterialInexistente() {
    when(repositorioMaterialMock.buscarPorId(1)).thenReturn(null);

    assertThrows(
      IllegalArgumentException.class,
      () -> servicioMaterial.descargarMaterial(1)
    );

    verifyNoInteractions(almacenamientoR2Mock);
  }

  @Test
  public void queSePuedaDescargarUnMaterialExistente() {
    MaterialBibliografico material = new MaterialBibliografico(
      "apunte.pdf", "application/pdf", 1024L, "clave", materia
    );

    byte[] contenido = { 1, 2, 3 };

    when(repositorioMaterialMock.buscarPorId(1)).thenReturn(material);
    when(almacenamientoR2Mock.descargarArchivo("clave")).thenReturn(contenido);

    byte[] resultado = servicioMaterial.descargarMaterial(1);

    assertArrayEquals(contenido, resultado);
    verify(almacenamientoR2Mock, times(1)).descargarArchivo("clave");
  }

  @Test
  public void queNoSePuedaEliminarUnMaterialInexistente() {
    when(repositorioMaterialMock.buscarPorId(1)).thenReturn(null);

    assertThrows(
      IllegalArgumentException.class,
      () -> servicioMaterial.eliminarMaterial(1)
    );

    verifyNoInteractions(almacenamientoR2Mock);
  }

  @Test
  public void queSePuedaEliminarUnMaterialExistente() {
    MaterialBibliografico material = new MaterialBibliografico(
      "apunte.pdf", "application/pdf", 1024L, "clave", materia
    );

    when(repositorioMaterialMock.buscarPorId(1)).thenReturn(material);

    servicioMaterial.eliminarMaterial(1);

    verify(almacenamientoR2Mock, times(1)).eliminarArchivo("clave");
    verify(repositorioMaterialMock, times(1)).eliminar(material);
  }
}

