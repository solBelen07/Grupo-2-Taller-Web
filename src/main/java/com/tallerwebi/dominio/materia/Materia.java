package com.tallerwebi.dominio.materia;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materialBibliografico.MaterialBibliografico;
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

  @ManyToMany
  @JoinTable(
    name = "materia_usuario",
    joinColumns = @JoinColumn(name = "materia_id"),
    inverseJoinColumns = @JoinColumn(name = "usuario_id")
  )
  private List<Usuario> usuarios = new ArrayList<>();

  @OneToMany(mappedBy = "materia")
  private List<MaterialBibliografico> materiales = new ArrayList<>();

  @SuppressWarnings("all")
  public Integer getId() {
    return id;
  }

  @SuppressWarnings("all")
  public String getNombre() {
    return nombre;
  }

  @SuppressWarnings("all")
  public String getDescripcion() {
    return descripcion;
  }

  @SuppressWarnings("all")
  public String getDocente() {
    return docente;
  }

  @SuppressWarnings("all")
  public String getColor() {
    return color;
  }

  @SuppressWarnings("all")
  public String getDias() {
    return dias;
  }

  @SuppressWarnings("all")
  public String getHorario() {
    return horario;
  }

  @SuppressWarnings("all")
  public String getMaterialBibliografico() {
    return materialBibliografico;
  }

  // SETTERS

  @SuppressWarnings("all")
  public void setId(Integer id) {
    this.id = id;
  }

  @SuppressWarnings("all")
  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  @SuppressWarnings("all")
  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  @SuppressWarnings("all")
  public void setDocente(String docente) {
    this.docente = docente;
  }

  @SuppressWarnings("all")
  public void setColor(String color) {
    this.color = color;
  }

  @SuppressWarnings("all")
  public void setDias(String dias) {
    this.dias = dias;
  }

  @SuppressWarnings("all")
  public void setHorario(String horario) {
    this.horario = horario;
  }

  @SuppressWarnings("all")
  public void setMaterialBibliografico(String materialBibliografico) {
    this.materialBibliografico = materialBibliografico;
  }

  @SuppressWarnings("all")
  public List<Usuario> getUsuarios() {
    return usuarios;
  }

  @SuppressWarnings("all")
  public void setUsuarios(List<Usuario> usuarios) {
    this.usuarios = usuarios;
  }

  @SuppressWarnings("all")
  public List<MaterialBibliografico> getMateriales() {
    return materiales;
  }

  @SuppressWarnings("all")
  public void setMateriales(List<MaterialBibliografico> materiales) {
    this.materiales = materiales;
  }
}
