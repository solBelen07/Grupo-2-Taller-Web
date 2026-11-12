package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaBoleto extends VistaWeb {

  public VistaBoleto(Page page) {
    super(page);
  }

  public VistaTimer darClickEnEmbarcar() {
    this.darClickEnElElemento("button.btn-cut");
    page.waitForURL("**/focusflight/timer/**");
    return new VistaTimer(page);
  }
}
