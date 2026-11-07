package com.tallerwebi.dominio.resumen;

import com.tallerwebi.dominio.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Resumen {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(nullable = false, length = 5000)
  private String textoOriginal;

  @Column(nullable = false, length = 64)
  private String marca;

  @Column(nullable = false, length = 5000)
  private String respuestaHtml;

  @Column(nullable = false)
  private LocalDateTime fechaCreacion;

  public Resumen() {}

  public Resumen(
    Usuario usuario,
    String textoOriginal,
    String marca,
    String respuestaHtml,
    LocalDateTime fechaCreacion
  ) {
    this.usuario = usuario;
    this.textoOriginal = textoOriginal;
    this.marca = marca;
    this.respuestaHtml = respuestaHtml;
    this.fechaCreacion = fechaCreacion;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public String getTextoOriginal() {
    return textoOriginal;
  }

  public String getMarca() {
    return marca;
  }

  public String getRespuestaHtml() {
    return respuestaHtml;
  }

  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }
}
