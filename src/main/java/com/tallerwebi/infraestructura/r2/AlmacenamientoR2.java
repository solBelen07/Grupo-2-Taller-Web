package com.tallerwebi.infraestructura.r2;

import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class AlmacenamientoR2 {

  private final S3Client s3Client;
  private final String bucketName;

  public AlmacenamientoR2(S3Client s3Client) {
    this.s3Client = s3Client;

    String bucket = System.getenv("R2_BUCKET_NAME");

    if (bucket == null || bucket.isBlank()) {
      bucket = System.getProperty("R2_BUCKET_NAME");
    }

    if (bucket == null || bucket.isBlank()) {
      throw new IllegalStateException("Falta configurar R2_BUCKET_NAME");
    }

    this.bucketName = bucket;
  }

  public String subirArchivo(MultipartFile archivo, Integer materiaId) throws IOException {
    String clave = "materias/" + materiaId + "/" + UUID.randomUUID();

    PutObjectRequest request = PutObjectRequest
      .builder()
      .bucket(bucketName)
      .key(clave)
      .contentType(archivo.getContentType())
      .build();

    s3Client.putObject(request, RequestBody.fromBytes(archivo.getBytes()));

    return clave;
  }

  public byte[] descargarArchivo(String clave) {
    GetObjectRequest request = GetObjectRequest.builder().bucket(bucketName).key(clave).build();

    return s3Client.getObjectAsBytes(request).asByteArray();
  }

  public void eliminarArchivo(String clave) {
    DeleteObjectRequest request = DeleteObjectRequest
      .builder()
      .bucket(bucketName)
      .key(clave)
      .build();

    s3Client.deleteObject(request);
  }
}
