package com.tallerwebi.dominio.materialBibliografico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.excepcionMateria.MateriaNoEncontrada;
import com.tallerwebi.dominio.excepcion.excepcionMaterial.*;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.infraestructura.r2.AlmacenamientoR2;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;

public class ServicioMaterialTest {

  private static final String NOMBRE_ARCHIVO_VALIDO = "nombre-test.pdf";
  private static final String NOMBRE_ARCHIVO_INVALIDO = "nombre-test.exe";
  private static final Long TAMANIO_ARCHIVO_EXCEDIDO = (50L * 1024 * 1024) + 1L;
  private static final Long TAMANIO_ARCHIVO_PERMITIDO = 1L;
  private static final String CLAVE_R2_VALIDA = "clave-ficticia";

  private ServicioMaterial servicioMaterial;
  private RepositorioMaterial repositorioMaterial;
  private ServicioMateria servicioMateria;
  private AlmacenamientoR2 almacenamientoR2;
  private Materia materiaMock;
  private MultipartFile archivoMock;
  private MaterialBibliografico materialBibliograficoMock;

  @BeforeEach
  public void inicializar() {
    repositorioMaterial = mock(RepositorioMaterial.class);
    servicioMateria = mock(ServicioMateria.class);
    almacenamientoR2 = mock(AlmacenamientoR2.class);
    materiaMock = mock(Materia.class);
    archivoMock = mock(MultipartFile.class);
    materialBibliograficoMock = mock(MaterialBibliografico.class);
    servicioMaterial =
      new ServicioMaterialImpl(repositorioMaterial, servicioMateria, almacenamientoR2);
  }

  @Test
  public void subirArchivoDeUnaMateriaInexistenteDeberiaDevolverException() {
    dadoQueNoExisteLaMateria();

    entoncesSubirMaterialDevuelveExceptionDeMateriaNoEncontrada();
  }

  @Test
  public void subirArchivoNuloDeberiaDevolverException() {
    dadoQueElArchivoEsNulo();
    dadoQueExisteUnaMateria();

    entoncesSubirMaterialDevuelveExceptionDeArchivoVacio();
  }

  @Test
  public void subirArchivoVacioDeberiaDevolverException() {
    dadoQueElArchivoEsVacio();
    dadoQueExisteUnaMateria();

    entoncesSubirMaterialDevuelveExceptionDeArchivoVacio();
  }

  @Test
  public void subirArchivoQueExcedeTamanioMaximoDeberiaDevolverException() {
    dadoQueExisteUnaMateria();
    dadoQueElArchivoTieneClaveR2NombreYExtensionValidoConTamanioExcedido();

    entoncesSubirMaterialDevuelveExceptionDePesoExcedido();
  }

  @Test
  public void subirArchivoSinNombreDeberiaDevolverException() {
    dadoQueExisteUnaMateria();
    dadoQueElArchivoTieneNombreNulo();

    entoncesSubirMaterialDevuelveExceptionDeArchivoSinNombre();
  }

  @Test
  public void subirArchivoConNombreVacioDeberiaDevolverException() {
    dadoQueExisteUnaMateria();
    dadoQueElArchivoTieneNombreVacio();

    entoncesSubirMaterialDevuelveExceptionDeArchivoSinNombre();
  }

  @Test
  public void subirArchivoConExtensionInvalidaDeberiaDevolverException() {
    dadoQueExisteUnaMateria();
    dadoQueElArchivoTIeneClaveR2PesoNombreValidoConExtensionInvalida();

    entoncesSubirMaterialDevuelveExceptionDeExtensionInvalida();
  }

  @Test
  public void deberiaGuardarEnRepositorioSiElArchivoEsValido() throws IOException {
    dadoQueExisteUnaMateria();
    dadoQueElArchivoSubidoTieneClaveR2PesoNombreYExtensionValidos();

    cuandoSeSubeMaterial();

    entoncesSeGuardaEnElRepositorioElMaterial();
  }

  @Test
  public void deberiaEliminarArchivoDeLaNubeSiFallaGuardadoEnBaseDeDatos() throws IOException {
    dadoQueExisteUnaMateria();
    dadoQueElArchivoSubidoTieneClaveR2PesoNombreYExtensionValidos();
    dadoQueSeAlmacenaEnR2ElArchivo();

    cuandoFallaElRepositorioAlGuardarMaterial();

    entoncesElServicioLanzaExcepcionYSeEliminaDelAlmacenamientoR2ElArchivo();
  }

  @Test
  public void deberiaAgregarExcepcionSuprimidaSiFallaBorradoEnLaNube() throws IOException {
    dadoQueExisteUnaMateria();
    dadoQueElArchivoSubidoTieneClaveR2PesoNombreYExtensionValidos();

    cuandoFallaElRepositorioAlGuardarMaterial();
    cuandoFallaElAlmacenamientoAlEliminarArchivo();

    entoncesElServicioLanzaExcepcionDeRepositorioAlSubirMaterial();
  }

  @Test
  public void eliminarArchivoNuloDeberiaDevolverExceptionArchivoNoEncontrado() {
    dadoQueNoSeEncuentraElArchivoBuscado();

    entoncesSeLanzaExcepcionArchivoNoEncontradoAlEliminarMaterial();
  }

  @Test
  public void eliminarArchivoDeberiaLlamarAlAlmacenamiento() {
    dadoQueSeEncuentraElArchivoBuscado();

    entoncesSeLlamaAAlmacenamientoParaEliminarArchivo();
  }

  @Test
  public void buscarPorIdDeberiaLlamarAlRepositorio() {
    dadoQueSeEncuentraElArchivoBuscado();

    MaterialBibliografico materialBuscado = servicioMaterial.buscarPorId(1234);

    assertEquals(materialBibliograficoMock, materialBuscado);
  }

  //DADO QUE
  private void dadoQueExisteUnaMateria() {
    when(materiaMock.getId()).thenReturn(1234);
    when(servicioMateria.buscarMateriaPorId(materiaMock.getId())).thenReturn(materiaMock);
  }

  private void dadoQueNoExisteLaMateria() {
    when(materiaMock.getId()).thenReturn(1234);
    when(servicioMateria.buscarMateriaPorId(any())).thenReturn(null);
  }

  private void dadoQueElArchivoSubidoTieneClaveR2PesoNombreYExtensionValidos() throws IOException {
    when(archivoMock.getSize()).thenReturn(TAMANIO_ARCHIVO_PERMITIDO);
    when(archivoMock.isEmpty()).thenReturn(false);
    when(archivoMock.getOriginalFilename()).thenReturn(NOMBRE_ARCHIVO_VALIDO);
    when(archivoMock.getContentType()).thenReturn("pdf");
    when(almacenamientoR2.subirArchivo(any(), anyInt())).thenReturn(CLAVE_R2_VALIDA);
  }

  private void dadoQueElArchivoTIeneClaveR2PesoNombreValidoConExtensionInvalida() {
    when(archivoMock.getSize()).thenReturn(TAMANIO_ARCHIVO_PERMITIDO);
    when(archivoMock.isEmpty()).thenReturn(false);
    when(archivoMock.getOriginalFilename()).thenReturn(NOMBRE_ARCHIVO_INVALIDO);
  }

  private void dadoQueElArchivoTieneClaveR2NombreYExtensionValidoConTamanioExcedido() {
    when(archivoMock.getSize()).thenReturn(TAMANIO_ARCHIVO_EXCEDIDO);
    when(archivoMock.isEmpty()).thenReturn(false);
    when(archivoMock.getContentType()).thenReturn("pdf");
    when(archivoMock.getOriginalFilename()).thenReturn(NOMBRE_ARCHIVO_VALIDO);
  }

  private void dadoQueElArchivoTieneNombreNulo() {
    when(archivoMock.getSize()).thenReturn(TAMANIO_ARCHIVO_PERMITIDO);
    when(archivoMock.isEmpty()).thenReturn(false);
    when(archivoMock.getContentType()).thenReturn("pdf");
    when(archivoMock.getOriginalFilename()).thenReturn(null);
  }

  private void dadoQueElArchivoTieneNombreVacio() {
    when(archivoMock.getSize()).thenReturn(TAMANIO_ARCHIVO_PERMITIDO);
    when(archivoMock.isEmpty()).thenReturn(false);
    when(archivoMock.getContentType()).thenReturn("pdf");
    when(archivoMock.getOriginalFilename()).thenReturn("");
  }

  private void dadoQueElArchivoEsNulo() {
    archivoMock = null;
  }

  private void dadoQueElArchivoEsVacio() {
    when(archivoMock.isEmpty()).thenReturn(true);
  }

  private void dadoQueNoSeEncuentraElArchivoBuscado() {
    when(repositorioMaterial.buscarPorId(anyInt())).thenReturn(null);
  }

  private void dadoQueSeEncuentraElArchivoBuscado() {
    when(materialBibliograficoMock.getId()).thenReturn(1234);
    when(materialBibliograficoMock.getClaveR2()).thenReturn(CLAVE_R2_VALIDA);
    when(materialBibliograficoMock.getNombreArchivo()).thenReturn(NOMBRE_ARCHIVO_VALIDO);
    when(materialBibliograficoMock.getTamanio()).thenReturn(TAMANIO_ARCHIVO_PERMITIDO);
    when(repositorioMaterial.buscarPorId(anyInt())).thenReturn(materialBibliograficoMock);
  }

  private void dadoQueSeAlmacenaEnR2ElArchivo() throws IOException {
    when(almacenamientoR2.subirArchivo(any(), anyInt())).thenReturn(CLAVE_R2_VALIDA);
  }

  //CUANDO
  private void cuandoFallaElRepositorioAlGuardarMaterial() {
    doThrow(new RuntimeException("Fallo en BD"))
      .when(repositorioMaterial)
      .guardar(any(MaterialBibliografico.class));
  }

  private void cuandoFallaElAlmacenamientoAlEliminarArchivo() {
    doThrow(new RuntimeException("Fallo al borrar en R2"))
      .when(almacenamientoR2)
      .eliminarArchivo(CLAVE_R2_VALIDA);
  }

  private void cuandoSeSubeMaterial() throws IOException {
    servicioMaterial.subirMaterial(materiaMock.getId(), archivoMock);
  }

  //ENTONCES
  private void entoncesElServicioLanzaExcepcionYSeEliminaDelAlmacenamientoR2ElArchivo() {
    assertThrows(
      RuntimeException.class,
      () -> {
        servicioMaterial.subirMaterial(materiaMock.getId(), archivoMock);
      }
    );
    verify(almacenamientoR2, times(1)).eliminarArchivo(CLAVE_R2_VALIDA);
  }

  private void entoncesElServicioLanzaExcepcionDeRepositorioAlSubirMaterial() {
    RuntimeException excepcionLanzada = assertThrows(
      RuntimeException.class,
      () -> {
        servicioMaterial.subirMaterial(materiaMock.getId(), archivoMock);
      }
    );
    assertEquals("Fallo en BD", excepcionLanzada.getMessage());
    Throwable[] suprimidos = excepcionLanzada.getSuppressed();
    assertEquals(1, suprimidos.length);
    assertEquals("Fallo al borrar en R2", suprimidos[0].getMessage());
  }

  private void entoncesSeLanzaExcepcionArchivoNoEncontradoAlEliminarMaterial() {
    assertThrows(
      ArchivoNoEncontrado.class,
      () -> {
        servicioMaterial.eliminarMaterial(1234);
      }
    );
  }

  private void entoncesSubirMaterialDevuelveExceptionDeArchivoVacio() {
    assertThrows(
      ArchivoVacio.class,
      () -> {
        servicioMaterial.subirMaterial(materiaMock.getId(), archivoMock);
      }
    );
  }

  private void entoncesSubirMaterialDevuelveExceptionDeExtensionInvalida() {
    assertThrows(
      ExtensionInvalida.class,
      () -> {
        servicioMaterial.subirMaterial(materiaMock.getId(), archivoMock);
      }
    );
  }

  private void entoncesSubirMaterialDevuelveExceptionDeArchivoSinNombre() {
    assertThrows(
      ArchivoSinNombre.class,
      () -> {
        servicioMaterial.subirMaterial(materiaMock.getId(), archivoMock);
      }
    );
  }

  private void entoncesSubirMaterialDevuelveExceptionDePesoExcedido() {
    assertThrows(
      PesoExcedido.class,
      () -> {
        servicioMaterial.subirMaterial(materiaMock.getId(), archivoMock);
      }
    );
  }

  private void entoncesSubirMaterialDevuelveExceptionDeMateriaNoEncontrada() {
    assertThrows(
      MateriaNoEncontrada.class,
      () -> {
        servicioMaterial.subirMaterial(materiaMock.getId(), archivoMock);
      }
    );
  }

  private void entoncesSeGuardaEnElRepositorioElMaterial() {
    ArgumentCaptor<MaterialBibliografico> captor = ArgumentCaptor.forClass(
      MaterialBibliografico.class
    );
    verify(repositorioMaterial, times(1)).guardar(captor.capture());

    MaterialBibliografico materialGuardado = captor.getValue();
    assertEquals(NOMBRE_ARCHIVO_VALIDO, materialGuardado.getNombreArchivo());
    assertEquals(CLAVE_R2_VALIDA, materialGuardado.getClaveR2());
  }

  private void entoncesSeLlamaAAlmacenamientoParaEliminarArchivo() {
    servicioMaterial.eliminarMaterial(materialBibliograficoMock.getId());
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

    verify(almacenamientoR2, times(1)).eliminarArchivo(captor.capture());
    materialBibliograficoMock.setClaveR2(captor.getValue());

    assertEquals(CLAVE_R2_VALIDA, materialBibliograficoMock.getClaveR2());
  }
}
