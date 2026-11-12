package com.tallerwebi.dominio;

import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Interes;
import com.tallerwebi.dominio.catalogo.Universidad;
import com.tallerwebi.dominio.racha.Racha;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
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

  public void activar() {
    activo = true;
  }

  public boolean getActivo() {
    return activo;
  }

  public void setActivo(Boolean activo) {
    this.activo = activo;
  }

  // --- Métodos con lógica personalizada (Lombok no los sobrescribe) ---

  public Racha getRacha() {
    if (this.racha == null) {
      this.racha = new Racha(0, 0, null);
    }
    return this.racha;
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
}
