package com.tallerwebi.dominio.logro;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Logro solo describe el logro (nombre y descripción).
 * No sabe quién lo obtuvo (eso lo indica LogroObtenido) ni cuándo se gana
 * (eso lo define ServicioLogroImpl).
 */
@Entity
public class Logro {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String descripcion;

  @SuppressWarnings("all")
  public String getDescripcion() {
    return descripcion;
  }

  @SuppressWarnings("all")
  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  @SuppressWarnings("all")
  public Long getId() {
    return id;
  }

  @SuppressWarnings("all")
  public void setId(Long id) {
    this.id = id;
  }

  @SuppressWarnings("all")
  public String getNombre() {
    return nombre;
  }

  @SuppressWarnings("all")
  public void setNombre(String nombre) {
    this.nombre = nombre;
  }
}
