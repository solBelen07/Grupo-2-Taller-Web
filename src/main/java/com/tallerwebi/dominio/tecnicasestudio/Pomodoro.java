package com.tallerwebi.dominio.tecnicasestudio;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import jakarta.persistence.*;

@Entity
public class Pomodoro {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String origen;
  private String destino;
  private int duracion;

  @Enumerated(EnumType.STRING)
  private Estado estado;

  @ManyToOne
  @JoinColumn(name = "materia_id")
  private Materia materia;

  @ManyToOne
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  private String objetivo;

  public Pomodoro(
    String origen,
    String destino,
    int duracion,
    Estado estado,
    Materia materia,
    Usuario usuario,
    String objetivo
  ) {
    this.origen = origen;
    this.destino = destino;
    this.duracion = duracion;
    this.estado = estado;
    this.materia = materia;
    this.usuario = usuario;
    this.objetivo = objetivo;
  }

  public Pomodoro() {}

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public void setEstado(Estado estado) {
    this.estado = estado;
  }

  public Estado getEstado() {
    return estado;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public String getObjetivo() {
    return objetivo;
  }

  public void setObjetivo(String objetivo) {
    this.objetivo = objetivo;
  }
}
