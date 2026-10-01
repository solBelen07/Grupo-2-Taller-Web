package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaCalendario extends VistaWeb {

  public VistaCalendario(Page page) {
    super(page);
    page.navigate("localhost:8080/spring/calendario");
  }

  public String obtenerTituloDelPeriodo() {
    return this.obtenerTextoDelElemento("[data-testid=titulo-periodo]");
  }

  public boolean estaVisibleLaVistaMensual() {
    return page.locator("[data-testid=vista-mes]").count() > 0;
  }

  public boolean estaVisibleLaVistaSemanal() {
    return page.locator("[data-testid=vista-semana]").count() > 0;
  }

  public int contarMateriasEnLaLeyenda() {
    return page.locator("[data-testid=leyenda] li").count();
  }

  public int contarBloquesDeLaSemana() {
    return page.locator(".bloque").count();
  }

  public void darClickEnVerSemana() {
    this.darClickEnElElemento("[data-testid=ver-semana]");
  }

  public void darClickEnVerMes() {
    this.darClickEnElElemento("[data-testid=ver-mes]");
  }

  public void darClickEnSiguiente() {
    this.darClickEnElElemento("[data-testid=siguiente]");
  }

  public void darClickEnHoy() {
    this.darClickEnElElemento("[data-testid=hoy]");
  }
}
