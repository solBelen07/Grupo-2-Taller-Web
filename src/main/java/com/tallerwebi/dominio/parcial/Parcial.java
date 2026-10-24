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
  private String nombre;

  @ManyToOne
  @JoinColumn(name = "materia_id", nullable = false)
  private Materia materia;

  @OneToMany(mappedBy = "parcial", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SesionEstudio> sesiones = new ArrayList<>();

  public Parcial() {}

  @SuppressWarnings("all")
  public Integer getId() {
    return id;
  }

  @SuppressWarnings("all")
  public void setId(Integer id) {
    this.id = id;
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
  public LocalTime getHorario() {
    return horario;
  }

  @SuppressWarnings("all")
  public void setHorario(LocalTime horario) {
    this.horario = horario;
  }

  @SuppressWarnings("all")
  public Integer getCantidadDiasEstudio() {
    return cantidadDiasEstudio;
  }

  @SuppressWarnings("all")
  public void setCantidadDiasEstudio(Integer cantidadDiasEstudio) {
    this.cantidadDiasEstudio = cantidadDiasEstudio;
  }

  @SuppressWarnings("all")
  public Integer getHorasPorDia() {
    return horasPorDia;
  }

  @SuppressWarnings("all")
  public void setHorasPorDia(Integer horasPorDia) {
    this.horasPorDia = horasPorDia;
  }

  @SuppressWarnings("all")
  public Materia getMateria() {
    return materia;
  }

  @SuppressWarnings("all")
  public void setMateria(Materia materia) {
    this.materia = materia;
  }

  @SuppressWarnings("all")
  public List<SesionEstudio> getSesiones() {
    return sesiones;
  }

  @SuppressWarnings("all")
  public void setSesiones(List<SesionEstudio> sesiones) {
    this.sesiones = sesiones;
  }

  public void agregarSesion(SesionEstudio sesion) {
    sesiones.add(sesion);
    sesion.setParcial(this);
  }

  @SuppressWarnings("all")
  public String getNombre() {
    return nombre;
  }

  @SuppressWarnings("all")
  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Integer getPorcentajeCompletado() {
    if (sesiones == null || sesiones.isEmpty()) {
      return 0;
    }

    long sesionesCompletadas = sesiones
      .stream()
      .filter(sesion -> Boolean.TRUE.equals(sesion.getCompletada()))
      .count();

    return (int) ((sesionesCompletadas * 100) / sesiones.size());
  }

  public Integer getCantidadSesiones() {
    if (sesiones == null) {
      return 0;
    }

    return sesiones.size();
  }

  public Long getCantidadSesionesCompletadas() {
    if (sesiones == null) {
      return 0L;
    }

    return sesiones.stream().filter(sesion -> Boolean.TRUE.equals(sesion.getCompletada())).count();
  }
}
