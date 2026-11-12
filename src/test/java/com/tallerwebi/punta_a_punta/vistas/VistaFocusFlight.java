package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

public class VistaFocusFlight extends VistaWeb {

  public VistaFocusFlight(Page page) {
    super(page);
  }

  public static VistaFocusFlight navegar(Page page) {
    page.navigate("localhost:8080/spring/focusflight");
    return new VistaFocusFlight(page);
  }

  public void escribirDestino(String destino) {
    page.locator("input[name='destino']").fill(destino);
  }

  public void seleccionarDuracion(int minutos) {
    page.locator("select[name='duracion']").selectOption(String.valueOf(minutos));
  }

  /** Elige la primera materia real (la opción 0 es el placeholder "Seleccionar materia"). */
  public void seleccionarPrimeraMateria() {
    page.locator("select[name='materia']").selectOption(new SelectOption().setIndex(1));
  }

  public void escribirObjetivo(String objetivo) {
    page.locator("textarea[name='objetivo']").fill(objetivo);
  }

  public VistaBoleto darClickEnIniciarVuelo() {
    this.darClickEnElElemento("#form-vuelo button[type='submit']");
    page.waitForURL("**/focusflight/boleto**");
    return new VistaBoleto(page);
  }
}
