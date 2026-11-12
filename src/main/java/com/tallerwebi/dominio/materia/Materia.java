package com.tallerwebi.dominio.materia;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materialBibliografico.MaterialBibliografico;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Materia {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String nombre;
  private String descripcion;
  private String docente;
  private String dias;
  private String horario;
  private String color;
  private String materialBibliografico;

  @ManyToMany
  @JoinTable(
    name = "materia_usuario",
    joinColumns = @JoinColumn(name = "materia_id"),
    inverseJoinColumns = @JoinColumn(name = "usuario_id")
  )
  private List<Usuario> usuarios = new ArrayList<>();

  @OneToMany(mappedBy = "materia")
  private List<MaterialBibliografico> materiales = new ArrayList<>();
}
