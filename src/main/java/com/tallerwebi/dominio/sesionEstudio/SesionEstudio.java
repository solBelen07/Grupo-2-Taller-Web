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

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getTema() {
    return tema;
  }

  public void setTema(String tema) {
    this.tema = tema;
  }

  public LocalDate getFecha() {
    return fecha;
  }

  public void setFecha(LocalDate fecha) {
    this.fecha = fecha;
  }

  public Integer getCantidadHoras() {
    return cantidadHoras;
  }

  public void setCantidadHoras(Integer cantidadHoras) {
    this.cantidadHoras = cantidadHoras;
  }

  public Boolean getCompletada() {
    return completada;
  }

  public void setCompletada(Boolean completada) {
    this.completada = completada;
  }

  public Parcial getParcial() {
    return parcial;
  }

  public void setParcial(Parcial parcial) {
    this.parcial = parcial;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }
}
