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
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
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
}
