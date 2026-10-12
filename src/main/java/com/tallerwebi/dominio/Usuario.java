package com.tallerwebi.dominio;

import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Interes;
import com.tallerwebi.dominio.catalogo.Universidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Usuario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private String apellido;

  @Column(unique = true)
  private String email;

  private String password;
  private String rol;
  private boolean activo = false;
  private int puntos;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "universidad_id")
  private Universidad universidad;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "carrera_id")
  private Carrera carrera;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
          name = "usuario_interes",
          joinColumns = @JoinColumn(name = "usuario_id"),
          inverseJoinColumns = @JoinColumn(name = "interes_id")
  )
  private Set<Interes> intereses = new HashSet<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRol() {
    return rol;
  }

  public void setRol(String rol) {
    this.rol = rol;
  }

  public boolean getActivo() {
    return activo;
  }

  public void setActivo(Boolean activo) {
    this.activo = activo;
  }

  public void activar() {
    activo = true;
  }

  public int getPuntos() {
    return puntos;
  }

  public void setPuntos(int puntos) {
    this.puntos = puntos;
  }

  public Universidad getUniversidad() {
    return universidad;
  }

  public void setUniversidad(Universidad universidad) {
    this.universidad = universidad;
  }

  public Carrera getCarrera() {
    return carrera;
  }

  public void setCarrera(Carrera carrera) {
    this.carrera = carrera;
  }

  public Set<Interes> getIntereses() {
    return intereses;
  }

  public void setIntereses(Set<Interes> intereses) {
    this.intereses = intereses;
  }
}