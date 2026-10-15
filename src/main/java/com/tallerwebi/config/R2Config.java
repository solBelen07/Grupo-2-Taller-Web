package com.tallerwebi.config;

import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class R2Config {

  private String obtenerConfiguracion(String nombre) {
    String valor = System.getenv(nombre);

    if (valor == null || valor.isBlank()) {
      valor = System.getProperty(nombre);
    }

    if (valor == null || valor.isBlank()) {
      throw new IllegalStateException("Falta configurar: " + nombre);
    }

    return valor;
  }

  @Bean
  public S3Client s3Client() {
    String accessKey = obtenerConfiguracion("R2_ACCESS_KEY");
    String secretKey = obtenerConfiguracion("R2_SECRET_KEY");
    String endpoint = obtenerConfiguracion("R2_ENDPOINT");

    AwsBasicCredentials credenciales = AwsBasicCredentials.create(accessKey, secretKey);

    return S3Client
      .builder()
      .endpointOverride(URI.create(endpoint))
      .credentialsProvider(StaticCredentialsProvider.create(credenciales))
      .region(Region.of("auto"))
      .serviceConfiguration(
        S3Configuration.builder().pathStyleAccessEnabled(true).chunkedEncodingEnabled(false).build()
      )
      .build();
  }
}
