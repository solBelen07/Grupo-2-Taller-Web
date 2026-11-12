package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;

public class VistaGrupos extends VistaWeb {

  public VistaGrupos(Page page) {
    super(page);
  }

  public static VistaGrupos navegar(Page page) {
    page.navigate("localhost:8080/spring/grupos");
    return new VistaGrupos(page);
  }

  public String obtenerTituloPrincipal() {
    return this.obtenerTextoDelElemento("h1");
  }

  public String obtenerMensajeDeError() {
    return this.obtenerTextoDelElemento("div.alert-danger");
  }

  public String obtenerMensajeDeExito() {
    return this.obtenerTextoDelElemento("div.alert-success");
  }

  public List<String> obtenerNombresDeLosGrupos() {
    return page.locator("div.tp-form-card h5").allTextContents();
  }

  public VistaFormularioGrupo darClickEnCrearGrupo() {
    this.darClickEnElElemento("a.btn-crear-grupo");
    return new VistaFormularioGrupo(page);
  }

  public VistaInvitarGrupo darClickEnInvitarAlGrupo(String nombreGrupo) {
    tarjetaDelGrupo(nombreGrupo).locator("a.btn-invitar-grupo").click();
    return new VistaInvitarGrupo(page);
  }

  public void darClickEnVerGrupo(String nombreGrupo) {
    tarjetaDelGrupo(nombreGrupo).locator("a.btn-ver-grupo").click();
  }

  private Locator tarjetaDelGrupo(String nombreGrupo) {
    Locator titulo = page.getByRole(
      AriaRole.HEADING,
      new Page.GetByRoleOptions().setName(nombreGrupo).setExact(true)
    );
    return page.locator("div.tp-form-card", new Page.LocatorOptions().setHas(titulo));
  }
}
