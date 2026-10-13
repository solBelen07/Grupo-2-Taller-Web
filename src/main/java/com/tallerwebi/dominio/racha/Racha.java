package com.tallerwebi.dominio.racha;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;

@Embeddable
public class Racha {

  @Column(name = "racha")
  private Integer rachaActual = 0;

  @Column(name = "record_racha")
  private Integer recordRacha = 0;

  @Column(name = "ultima_fecha_estudio")
  private LocalDate ultimaFechaEstudio;

  public Racha() {}

  public Racha(Integer racha, Integer recordRacha, LocalDate ultimaFechaEstudio) {
    this.rachaActual = racha == null ? 0 : racha;
    this.recordRacha = recordRacha == null ? 0 : recordRacha;
    this.ultimaFechaEstudio = ultimaFechaEstudio;
  }

  public Integer getRachaActual() {
    return rachaActual == null ? 0 : rachaActual;
  }

  public void setRachaActual(Integer rachaActual) {
    this.rachaActual = rachaActual == null ? 0 : rachaActual;
  }

  public Integer getRecordRacha() {
    return recordRacha == null ? 0 : recordRacha;
  }

  public void setRecordRacha(Integer recordRacha) {
    this.recordRacha = recordRacha == null ? 0 : recordRacha;
  }

  public LocalDate getUltimaFechaEstudio() {
    return ultimaFechaEstudio;
  }

  public void setUltimaFechaEstudio(LocalDate ultimaFechaEstudio) {
    this.ultimaFechaEstudio = ultimaFechaEstudio;
  }

  public Racha copiar() {
    return new Racha(getRachaActual(), getRecordRacha(), ultimaFechaEstudio);
  }
}
