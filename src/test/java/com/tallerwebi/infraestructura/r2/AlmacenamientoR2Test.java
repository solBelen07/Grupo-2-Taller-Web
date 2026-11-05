package com.tallerwebi.infraestructura.r2;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tallerwebi.infraestructura.r2.AlmacenamientoR2;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class AlmacenamientoR2Test {

  private static final String BUCKET = "bucket-prueba";
  private static final Integer MATERIA_ID = 1234;
  private static final String CLAVE = "materias/1234/archivo-prueba";
  private static final String TIPO_ARCHIVO = "application/pdf";

  private S3Client s3Client;
  private MultipartFile archivo;
  private AlmacenamientoR2 almacenamientoR2;

  @BeforeEach
  public void inicializar() {
    s3Client = mock(S3Client.class);
    archivo = mock(MultipartFile.class);
    almacenamientoR2 = new AlmacenamientoR2(s3Client);
    // Al instanciar sin Spring, @Value no se inyecta automaticamente.
    ReflectionTestUtils.setField(almacenamientoR2, "bucketName", BUCKET);
  }

  @Test
  public void deberiaSubirArchivoConBucketClaveTipoYContenidoCorrectos() throws IOException {
    byte[] contenido = new byte[] { 1, 2, 3, 4 };
    dadoQueTengoUnArchivo(contenido);

    String claveObtenida = almacenamientoR2.subirArchivo(archivo, MATERIA_ID);

    ArgumentCaptor<PutObjectRequest> solicitud = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> cuerpo = ArgumentCaptor.forClass(RequestBody.class);
    verify(s3Client).putObject(solicitud.capture(), cuerpo.capture());

    assertEquals(BUCKET, solicitud.getValue().bucket());
    assertEquals(claveObtenida, solicitud.getValue().key());
    assertEquals(TIPO_ARCHIVO, solicitud.getValue().contentType());
    assertEquals((long) contenido.length, cuerpo.getValue().contentLength());
    try (InputStream datos = cuerpo.getValue().contentStreamProvider().newStream()) {
      assertArrayEquals(contenido, datos.readAllBytes());
    }
    entoncesLaClaveTieneElPrefijoYUnUuidValido(claveObtenida);
  }

  @Test
  public void deberiaGenerarClavesDiferentesParaDosSubidas() throws IOException {
    dadoQueTengoUnArchivo(new byte[] { 1, 2 });

    String primeraClave = almacenamientoR2.subirArchivo(archivo, MATERIA_ID);
    String segundaClave = almacenamientoR2.subirArchivo(archivo, MATERIA_ID);

    assertNotEquals(primeraClave, segundaClave);
    entoncesLaClaveTieneElPrefijoYUnUuidValido(primeraClave);
    entoncesLaClaveTieneElPrefijoYUnUuidValido(segundaClave);
  }

  @Test
  public void noDeberiaLlamarAS3SiFallaLaLecturaDelArchivo() throws IOException {
    IOException errorEsperado = new IOException("No se pudo leer el archivo");
    when(archivo.getContentType()).thenReturn(TIPO_ARCHIVO);
    when(archivo.getBytes()).thenThrow(errorEsperado);

    IOException errorObtenido = assertThrows(
      IOException.class,
      () -> almacenamientoR2.subirArchivo(archivo, MATERIA_ID)
    );

    assertSame(errorEsperado, errorObtenido);
    verifyNoInteractions(s3Client);
  }

  @Test
  public void deberiaPropagarElErrorDeS3AlSubirArchivo() throws IOException {
    dadoQueTengoUnArchivo(new byte[] { 1 });
    S3Exception errorEsperado = dadoUnErrorDeS3();
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
      .thenThrow(errorEsperado);

    S3Exception errorObtenido = assertThrows(
      S3Exception.class,
      () -> almacenamientoR2.subirArchivo(archivo, MATERIA_ID)
    );

    assertSame(errorEsperado, errorObtenido);
  }

  @Test
  public void deberiaDescargarArchivoUsandoBucketYClaveCorrectos() {
    byte[] contenidoEsperado = new byte[] { 5, 6, 7 };
    dadoQueS3DevuelveUnArchivo(contenidoEsperado);

    byte[] contenidoObtenido = almacenamientoR2.descargarArchivo(CLAVE);

    ArgumentCaptor<GetObjectRequest> solicitud = ArgumentCaptor.forClass(GetObjectRequest.class);
    verify(s3Client).getObjectAsBytes(solicitud.capture());
    assertEquals(BUCKET, solicitud.getValue().bucket());
    assertEquals(CLAVE, solicitud.getValue().key());
    assertArrayEquals(contenidoEsperado, contenidoObtenido);
  }

  @Test
  public void deberiaDevolverUnArregloVacioSiElArchivoDescargadoEstaVacio() {
    dadoQueS3DevuelveUnArchivo(new byte[0]);

    byte[] contenidoObtenido = almacenamientoR2.descargarArchivo(CLAVE);

    assertArrayEquals(new byte[0], contenidoObtenido);
  }

  @Test
  public void deberiaPropagarElErrorDeS3AlDescargarArchivo() {
    S3Exception errorEsperado = dadoUnErrorDeS3();
    when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(errorEsperado);

    S3Exception errorObtenido = assertThrows(
      S3Exception.class,
      () -> almacenamientoR2.descargarArchivo(CLAVE)
    );

    assertSame(errorEsperado, errorObtenido);
  }

  @Test
  public void deberiaEliminarArchivoUsandoBucketYClaveCorrectos() {
    almacenamientoR2.eliminarArchivo(CLAVE);

    ArgumentCaptor<DeleteObjectRequest> solicitud = ArgumentCaptor.forClass(
      DeleteObjectRequest.class
    );
    verify(s3Client).deleteObject(solicitud.capture());
    assertEquals(BUCKET, solicitud.getValue().bucket());
    assertEquals(CLAVE, solicitud.getValue().key());
  }

  @Test
  public void deberiaPropagarElErrorDeS3AlEliminarArchivo() {
    S3Exception errorEsperado = dadoUnErrorDeS3();
    when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(errorEsperado);

    S3Exception errorObtenido = assertThrows(
      S3Exception.class,
      () -> almacenamientoR2.eliminarArchivo(CLAVE)
    );

    assertSame(errorEsperado, errorObtenido);
  }

  // DADO QUE
  private void dadoQueTengoUnArchivo(byte[] contenido) throws IOException {
    when(archivo.getContentType()).thenReturn(TIPO_ARCHIVO);
    when(archivo.getBytes()).thenReturn(contenido);
  }

  private void dadoQueS3DevuelveUnArchivo(byte[] contenido) {
    ResponseBytes<GetObjectResponse> respuesta = ResponseBytes.fromByteArray(
      GetObjectResponse.builder().build(),
      contenido
    );
    when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(respuesta);
  }

  private S3Exception dadoUnErrorDeS3() {
    return (S3Exception) S3Exception.builder().message("Fallo en S3").statusCode(500).build();
  }

  // ENTONCES
  private void entoncesLaClaveTieneElPrefijoYUnUuidValido(String clave) {
    String prefijo = "materias/" + MATERIA_ID + "/";
    assertTrue(clave.startsWith(prefijo));
    String identificador = clave.substring(prefijo.length());
    assertEquals(identificador, UUID.fromString(identificador).toString());
  }
}
