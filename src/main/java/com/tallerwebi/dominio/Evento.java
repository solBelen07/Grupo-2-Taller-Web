package com.tallerwebi.dominio;

import com.tallerwebi.dominio.materia.Materia;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

/**
 * Evento del calendario académico de un alumno: clase, parcial, trabajo práctico, estudio o
 * reunión grupal. La materia es el catálogo compartido de la app (no tiene dueño), así que el
 * evento guarda su propio usuarioId para saber de quién es.
 */
@Entity
public class Evento {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long usuarioId;
  private String titulo;

  @Enumerated(EnumType.STRING)
  private TipoEvento tipo;

  private LocalDateTime inicio;
  private LocalDateTime fin;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Materia materia;

  /** Id de la Tarea de /tareas vinculada a este evento; null si no tiene ninguna. */
  private Long tareaId;

  /** Requerido por Hibernate. */
  protected Evento() {}

  /**
   * Crea un evento validando sus datos.
   *
   * @throws IllegalArgumentException si falta algún dato o el fin no es posterior al inicio
   */
  public Evento(
    Long usuarioId,
    Materia materia,
    TipoEvento tipo,
    String titulo,
    LocalDateTime inicio,
    LocalDateTime fin
  ) {
    validar(usuarioId, materia, tipo, titulo, inicio, fin);
    this.usuarioId = usuarioId;
    this.materia = materia;
    this.tipo = tipo;
    this.titulo = titulo.strip();
    this.inicio = inicio;
    this.fin = fin;
  }

  private static void validar(
    Long usuarioId,
    Materia materia,
    TipoEvento tipo,
    String titulo,
    LocalDateTime inicio,
    LocalDateTime fin
  ) {
    exigir(usuarioId != null, "El evento debe pertenecer a un usuario");
    exigir(materia != null && tipo != null, "La materia y el tipo son obligatorios");
    exigir(inicio != null && fin != null, "El inicio y el fin son obligatorios");
    exigir(fin.isAfter(inicio), "El fin del evento debe ser posterior al inicio");
    exigir(titulo != null && !titulo.isBlank(), "El título del evento es obligatorio");
  }

  private static void exigir(boolean condicion, String mensaje) {
    if (!condicion) {
      throw new IllegalArgumentException(mensaje);
    }
  }

  public Long getId() {
    return id;
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public String getTitulo() {
    return titulo;
  }

  public TipoEvento getTipo() {
    return tipo;
  }

  public LocalDateTime getInicio() {
    return inicio;
  }

  public LocalDateTime getFin() {
    return fin;
  }

  public Materia getMateria() {
    return materia;
  }

  public Long getTareaId() {
    return tareaId;
  }

  /** Vincula este evento a una Tarea de /tareas (CAL-02), para mostrar su estado. */
  public void vincularTarea(Long idDeLaTarea) {
    this.tareaId = idDeLaTarea;
  }
}
