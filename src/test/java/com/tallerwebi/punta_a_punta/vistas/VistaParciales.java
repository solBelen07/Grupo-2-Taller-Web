package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Listado de parciales con su plan de estudio (/parciales). */
public class VistaParciales extends VistaWeb {

  public VistaParciales(Page page) {
    super(page);
  }

  public static VistaParciales navegar(Page page) {
    page.navigate("localhost:8080/spring/parciales");
    return new VistaParciales(page);
  }

  // Marca (o desmarca, si ya estaba marcada) la primera sesion del plan
  public void darClickEnLaPrimeraSesion(String nombreParcial) {
    page
      .locator(".parcial-plan-card")
      .filter(new Locator.FilterOptions().setHasText(nombreParcial))
      .locator("label.checkbox-personalizado")
      .first()
      .click();
    page.waitForURL("**/parciales**");
  }

  // Lee el cartel del encabezado "Racha de N días" y devuelve N
  public int obtenerDiasDeRacha() {
    String texto = this.obtenerTextoDelElemento(".streak-badge span").trim();
    return Integer.parseInt(texto.replaceAll("\\D+", ""));
  }
}
