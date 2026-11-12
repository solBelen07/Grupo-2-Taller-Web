package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VistaTimer extends VistaWeb {

  private static final Pattern ID_EN_URL = Pattern.compile("/focusflight/timer/(\\d+)");

  public VistaTimer(Page page) {
    super(page);
  }

  public static VistaTimer navegar(Page page, long idVuelo) {
    page.navigate("localhost:8080/spring/focusflight/timer/" + idVuelo);
    return new VistaTimer(page);
  }

  public long obtenerIdDelVuelo() {
    Matcher matcher = ID_EN_URL.matcher(page.url());
    if (!matcher.find()) {
      throw new IllegalStateException("La URL no es la del timer: " + page.url());
    }
    return Long.parseLong(matcher.group(1));
  }

  public String obtenerTiempoRestante() {
    return this.obtenerTextoDelElemento("#timer-display").trim();
  }
}
