package com.tallerwebi.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class R2Config {

  @Value("${R2_ACCESS_KEY}")
  private String accessKey;

  @Value("${R2_SECRET_KEY}")
  private String secretKey;

  @Value("${R2_ENDPOINT}")
  private String endpoint;

  @Bean
  public S3Client s3Client() {
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
