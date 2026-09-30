package com.tallerwebi.dominio.parcial;

import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Parcial {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private LocalDate fecha;

  private LocalTime horario;

  private Integer cantidadDiasEstudio;

  private Integer horasPorDia;

  @ManyToOne
  @JoinColumn(name = "materia_id", nullable = false)
  private Materia materia;

  @OneToMany(mappedBy = "parcial", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SesionEstudio> sesiones = new ArrayList<>();

  public Parcial() {}

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public LocalDate getFecha() {
    return fecha;
  }

  public void setFecha(LocalDate fecha) {
    this.fecha = fecha;
  }

  public LocalTime getHorario() {
    return horario;
  }

  public void setHorario(LocalTime horario) {
    this.horario = horario;
  }

  public Integer getCantidadDiasEstudio() {
    return cantidadDiasEstudio;
  }

  public void setCantidadDiasEstudio(Integer cantidadDiasEstudio) {
    this.cantidadDiasEstudio = cantidadDiasEstudio;
  }

  public Integer getHorasPorDia() {
    return horasPorDia;
  }

  public void setHorasPorDia(Integer horasPorDia) {
    this.horasPorDia = horasPorDia;
  }

  public Materia getMateria() {
    return materia;
  }

  public void setMateria(Materia materia) {
    this.materia = materia;
  }

  public List<SesionEstudio> getSesiones() {
    return sesiones;
  }

  public void setSesiones(List<SesionEstudio> sesiones) {
    this.sesiones = sesiones;
  }

  public void agregarSesion(SesionEstudio sesion) {
    sesiones.add(sesion);
    sesion.setParcial(this);
  }
}
