package com.tallerwebi.dominio.tecnicasestudio;

import com.tallerwebi.dominio.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "pomodoro")
public class Pomodoro {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Embedded
  private PlanDeVuelo plan;

  @Column(name = "duracion_minutos", nullable = false)
  private int duracionMinutos;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", nullable = false, length = 20)
  private Estado estado;

  @Column(name = "fecha_inicio")
  private LocalDateTime fechaInicio;

  @Column(name = "fecha_pausa")
  private LocalDateTime fechaPausa;

  @Column(name = "fecha_fin")
  private LocalDateTime fechaFin;

  @SuppressWarnings("all")
  public Long getId() {
    return id;
  }

  @SuppressWarnings("all")
  public void setId(Long id) {
    this.id = id;
  }

  @SuppressWarnings("all")
  public Usuario getUsuario() {
    return usuario;
  }

  @SuppressWarnings("all")
  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  @SuppressWarnings("all")
  public PlanDeVuelo getPlan() {
    return plan;
  }

  @SuppressWarnings("all")
  public void setPlan(PlanDeVuelo plan) {
    this.plan = plan;
  }

  @SuppressWarnings("all")
  public int getDuracionMinutos() {
    return duracionMinutos;
  }

  @SuppressWarnings("all")
  public void setDuracionMinutos(int duracionMinutos) {
    this.duracionMinutos = duracionMinutos;
  }

  @SuppressWarnings("all")
  public Estado getEstado() {
    return estado;
  }

  @SuppressWarnings("all")
  public void setEstado(Estado estado) {
    this.estado = estado;
  }

  @SuppressWarnings("all")
  public LocalDateTime getFechaInicio() {
    return fechaInicio;
  }

  @SuppressWarnings("all")
  public void setFechaInicio(LocalDateTime fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  @SuppressWarnings("all")
  public LocalDateTime getFechaPausa() {
    return fechaPausa;
  }

  @SuppressWarnings("all")
  public void setFechaPausa(LocalDateTime fechaPausa) {
    this.fechaPausa = fechaPausa;
  }

  @SuppressWarnings("all")
  public LocalDateTime getFechaFin() {
    return fechaFin;
  }

  @SuppressWarnings("all")
  public void setFechaFin(LocalDateTime fechaFin) {
    this.fechaFin = fechaFin;
  }
}
