package com.tallerwebi.dominio;

import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Interes;
import com.tallerwebi.dominio.catalogo.Universidad;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;

@Embeddable
public class PerfilAcademico {

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
