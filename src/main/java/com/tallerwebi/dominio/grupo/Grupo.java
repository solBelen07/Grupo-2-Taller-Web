package com.tallerwebi.dominio.grupo;

import jakarta.persistence.*;

@Entity
public class Grupo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;

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
