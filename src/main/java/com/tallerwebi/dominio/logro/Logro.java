package com.tallerwebi.dominio.logro;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * - Logro solo describe el logro (nombre y descripción).
 *
 * - No sabe quién lo obtuvo (eso lo indica LogroObtenido) ni cuándo se gana
 *
 * - (eso lo define ServicioLogroImpl).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Logro {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String descripcion;
}
