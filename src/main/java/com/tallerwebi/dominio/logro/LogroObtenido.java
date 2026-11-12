package com.tallerwebi.dominio.logro;

import com.tallerwebi.dominio.Usuario;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * - indica QUIEN obtuvo QUÉ logro y CUANDO.
 *
 * - Relaciona Usuario con Logro (un usuario puede tener muchos logros y un logro
 *
 * - puede ser obtenido por muchos usuarios). Un usuario no puede obtener dos veces el mismo logro.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "usuario_id", "logro_id" }))
@Getter
@Setter
@NoArgsConstructor
public class LogroObtenido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @ManyToOne(optional = false)
  @JoinColumn(name = "logro_id")
  private Logro logro;

  private LocalDate fechaObtencion;

  public LogroObtenido(Usuario usuario, Logro logro, LocalDate fechaObtencion) {
    this.usuario = usuario;
    this.logro = logro;
    this.fechaObtencion = fechaObtencion;
  }
}
