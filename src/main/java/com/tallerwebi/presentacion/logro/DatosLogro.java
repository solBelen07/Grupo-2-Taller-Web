package com.tallerwebi.presentacion.logro;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatosLogro {

  private String nombre;
  private String descripcion;
  private boolean obtenido;
  private String icono;
  private LocalDate fechaObtencion;

  public DatosLogro(String nombre, String descripcion, boolean obtenido, String icono) {
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.obtenido = obtenido;
    this.icono = icono;
  }

  public DatosLogro(String nombre, String descripcion, String icono, LocalDate fechaObtencion) {
    this(nombre, descripcion, fechaObtencion != null, icono);
    this.fechaObtencion = fechaObtencion;
  }
}
