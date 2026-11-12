package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import java.time.LocalDate;
import java.util.List;

public class VistaFormularioParcial extends VistaWeb {

  public VistaFormularioParcial(Page page) {
    super(page);
  }

  public static VistaFormularioParcial navegar(Page page) {
    page.navigate("localhost:8080/spring/parciales/nuevo");
    return new VistaFormularioParcial(page);
  }

  public void escribirNombre(String nombre) {
    page.locator("#nombre").fill(nombre);
  }

  /** Elige la primera materia real (la opción 0 es el placeholder "Seleccionar materia"). */
  public void seleccionarPrimeraMateria() {
    page.locator("#materia").selectOption(new SelectOption().setIndex(1));
  }

  public void escribirFecha(LocalDate fecha) {
    // los input type="date" se completan en formato ISO (yyyy-MM-dd)
    page.locator("#fecha").fill(fecha.toString());
  }

  public void escribirHorario(String horario) {
    page.locator("#horario").fill(horario);
  }

  public void escribirCantidadDeDias(int dias) {
    page.locator("#dias").fill(String.valueOf(dias));
  }

  public void escribirHorasPorDia(int horas) {
    page.locator("#horas").fill(String.valueOf(horas));
  }

  /** Completa el primer tema y agrega un campo nuevo por cada tema siguiente. */
  public void escribirTemas(List<String> temas) {
    Locator campos = page.locator("#lista-temas input[name^='temas']");
    for (int i = 0; i < temas.size(); i++) {
      if (i > 0) this.darClickEnElElemento("button[onclick='agregarTema()']");
      campos.nth(i).fill(temas.get(i));
    }
  }

  public void darClickEnCrearPlanificacion() {
    this.darClickEnElElemento("form[action*='/parciales/guardar'] button[type='submit']");
    page.waitForURL("**/parciales**");
  }
}
