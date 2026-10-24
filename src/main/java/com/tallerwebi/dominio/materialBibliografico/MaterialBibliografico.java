package com.tallerwebi.dominio.materialBibliografico;

import com.tallerwebi.dominio.materia.Materia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class MaterialBibliografico {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false)
  private String nombreArchivo;

  @Column(nullable = false)
  private String tipoArchivo;

  @Column(nullable = false)
  private Long tamanio;

  @Column(nullable = false, unique = true)
  private String claveR2;

  @Column(nullable = false)
  private LocalDateTime fechaSubida;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "materia_id", nullable = false)
  private Materia materia;

  public MaterialBibliografico() {}

  public MaterialBibliografico(
    String nombreArchivo,
    String tipoArchivo,
    Long tamanio,
    String claveR2,
    Materia materia
  ) {
    this.nombreArchivo = nombreArchivo;
    this.tipoArchivo = tipoArchivo;
    this.tamanio = tamanio;
    this.claveR2 = claveR2;
    this.materia = materia;
    this.fechaSubida = LocalDateTime.now();
  }

  @SuppressWarnings("all")
  public String getClaveR2() {
    return claveR2;
  }

  @SuppressWarnings("all")
  public void setId(Integer id) {
    this.id = id;
  }

  @SuppressWarnings("all")
  public String getNombreArchivo() {
    return nombreArchivo;
  }

  @SuppressWarnings("all")
  public void setNombreArchivo(String nombreArchivo) {
    this.nombreArchivo = nombreArchivo;
  }

  @SuppressWarnings("all")
  public void setTipoArchivo(String tipoArchivo) {
    this.tipoArchivo = tipoArchivo;
  }

  @SuppressWarnings("all")
  public Materia getMateria() {
    return materia;
  }

  @SuppressWarnings("all")
  public void setMateria(Materia materia) {
    this.materia = materia;
  }
}
