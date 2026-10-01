package com.tallerwebi.presentacion.tecnicasestudio;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.tecnicasestudio.Estado;

@SuppressWarnings("CPD-START")
public class DatosPomodoro {

  private String origen;
  private String destino;
  private Integer duracion;
  private Estado estado;
  private Integer materia;
  private Usuario usuario;
  private String objetivo;

  public DatosPomodoro(
    String origen,
    String destino,
    Integer duracion,
    Estado estado,
    Integer materia,
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

  public DatosPomodoro() {}

  public String getOrigen() {
    return origen;
  }

  public void setOrigen(String origen) {
    this.origen = origen;
  }

  public String getDestino() {
    return destino;
  }

  public void setDestino(String destino) {
    this.destino = destino;
  }

  public Integer getDuracion() {
    return duracion;
  }

  public void setDuracion(Integer duracion) {
    this.duracion = duracion;
  }

  public Integer getMateria() {
    return materia;
  }

  public void setMateria(Integer materia) {
    this.materia = materia;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public Estado getEstado() {
    return estado;
  }

  public void setEstado(Estado estado) {
    this.estado = estado;
  }

  public String getObjetivo() {
    return objetivo;
  }

  public void setObjetivo(String objetivo) {
    this.objetivo = objetivo;
  }
}
