package com.tallerwebi.dominio.sesionEstudio;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.parcial.Parcial;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class SesionEstudio {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String tema;

  private LocalDate fecha;

  private Integer cantidadHoras;

  private Boolean completada = false;

  @ManyToOne
  @JoinColumn(name = "parcial_id", nullable = false)
  private Parcial parcial;

  @ManyToOne
  @JoinColumn(name = "usuario")
  private Usuario usuario;

  public SesionEstudio() {}

  @SuppressWarnings("all")
  public Integer getId() {
    return id;
  }

  @SuppressWarnings("all")
  public void setId(Integer id) {
    this.id = id;
  }

  @SuppressWarnings("all")
  public String getTema() {
    return tema;
  }

  @SuppressWarnings("all")
  public void setTema(String tema) {
    this.tema = tema;
  }

  @SuppressWarnings("all")
  public LocalDate getFecha() {
    return fecha;
  }

  @SuppressWarnings("all")
  public void setFecha(LocalDate fecha) {
    this.fecha = fecha;
  }

  @SuppressWarnings("all")
  public Integer getCantidadHoras() {
    return cantidadHoras;
  }

  @SuppressWarnings("all")
  public void setCantidadHoras(Integer cantidadHoras) {
    this.cantidadHoras = cantidadHoras;
  }

  @SuppressWarnings("all")
  public Boolean getCompletada() {
    return completada;
  }

  @SuppressWarnings("all")
  public void setCompletada(Boolean completada) {
    this.completada = completada;
  }

  @SuppressWarnings("all")
  public Parcial getParcial() {
    return parcial;
  }

  @SuppressWarnings("all")
  public void setParcial(Parcial parcial) {
    this.parcial = parcial;
  }

  @SuppressWarnings("all")
  public Usuario getUsuario() {
    return usuario;
  }

  @SuppressWarnings("all")
  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }
}
