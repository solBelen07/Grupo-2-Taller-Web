package com.tallerwebi.dominio;

import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Interes;
import com.tallerwebi.dominio.catalogo.Universidad;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Set;
import com.tallerwebi.dominio.racha.Racha;
import jakarta.persistence.*;

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

  @Embedded
  private Racha racha;

  private boolean activo = false;
  private int puntos;

  @Embedded
  private PerfilAcademico perfilAcademico = new PerfilAcademico();

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
    return perfil().getUniversidad();
  }

  public void setUniversidad(Universidad universidad) {
    perfil().setUniversidad(universidad);
  }

  public Carrera getCarrera() {
    return perfil().getCarrera();
  }

  public void setCarrera(Carrera carrera) {
    perfil().setCarrera(carrera);
  }

  public Set<Interes> getIntereses() {
    return perfil().getIntereses();
  }

  public void setIntereses(Set<Interes> intereses) {
    perfil().setIntereses(intereses);
  }

  private PerfilAcademico perfil() {
    if (perfilAcademico == null) {
      perfilAcademico = new PerfilAcademico();
    }
    return perfilAcademico;
  }


  public boolean isActivo() {
    return activo;
  }

  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  public Racha getRacha() {
    if (this.racha == null) {
      this.racha = new Racha(0, 0, null);
    }
    return this.racha;
  }

  public void setRacha(Racha racha) {
    this.racha = racha;
  }
}
