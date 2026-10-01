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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public PlanDeVuelo getPlan() {
    return plan;
  }

  public void setPlan(PlanDeVuelo plan) {
    this.plan = plan;
  }

  public int getDuracionMinutos() {
    return duracionMinutos;
  }

  public void setDuracionMinutos(int duracionMinutos) {
    this.duracionMinutos = duracionMinutos;
  }

  public Estado getEstado() {
    return estado;
  }

  public void setEstado(Estado estado) {
    this.estado = estado;
  }

  public LocalDateTime getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(LocalDateTime fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public LocalDateTime getFechaPausa() {
    return fechaPausa;
  }

  public void setFechaPausa(LocalDateTime fechaPausa) {
    this.fechaPausa = fechaPausa;
  }

  public LocalDateTime getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(LocalDateTime fechaFin) {
    this.fechaFin = fechaFin;
  }
}
