package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaTareas extends VistaWeb {

  public VistaTareas(Page page) {
    super(page);
  }

  public String obtenerTitulo() {
    return this.obtenerTextoDelElemento("h1.titulo-principal");
  }

  public void darClickEnNuevaTarea() {
    this.darClickEnElElemento("a.btn-nueva-tarea");
  }

  public void escribirTitulo(String titulo) {
    this.escribirEnElElemento("#titulo", titulo);
  }

  public void seleccionarGrupo(String grupo) {
    page.locator("#grupo").selectOption(grupo);
  }

  public void seleccionarMateria(String materia) {
    page.locator("#materia").selectOption(materia);
  }

  public void seleccionarTipo(String tipo) {
    page.locator("#tipo").selectOption(tipo);
  }

  public void escribirResponsable(String responsable) {
    this.escribirEnElElemento("#responsable", responsable);
  }

  public void seleccionarEstado(String estado) {
    page.locator("#estado").selectOption(estado);
  }

  public void escribirFechaVencimiento(String fecha) {
    page.locator("#fechaVencimiento").fill(fecha);
  }

  public void escribirHorasPlanificadas(String horas) {
    this.escribirEnElElemento("#horasPlanificadas", horas);
  }

  public void escribirHorasRealizadas(String horas) {
    this.escribirEnElElemento("#horasRealizadas", horas);
  }

  public void darClickEnGuardar() {
    this.darClickEnElElemento("button.btn-guardar");
  }

  public String obtenerContenido() {
    return this.obtenerTextoDelElemento("body");
  }
}
