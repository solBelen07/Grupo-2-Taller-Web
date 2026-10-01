package com.tallerwebi.dominio.materia;

import com.tallerwebi.dominio.tecnicasestudio.Pomodoro;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Materia {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String nombre;
  private String descripcion;
  private String docente;
  private String dias;
  private String horario;
  private String color;
  private String materialBibliografico;

  public Integer getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public String getDocente() {
    return docente;
  }

  public String getColor() {
    return color;
  }

  public String getDias() {
    return dias;
  }

  public String getHorario() {
    return horario;
  }

  public String getMaterialBibliografico() {
    return materialBibliografico;
  }

  // SETTERS

  public void setId(Integer id) {
    this.id = id;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public void setDocente(String docente) {
    this.docente = docente;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public void setDias(String dias) {
    this.dias = dias;
  }

  public void setHorario(String horario) {
    this.horario = horario;
  }

  public void setMaterialBibliografico(String materialBibliografico) {
    this.materialBibliografico = materialBibliografico;
  }
}
