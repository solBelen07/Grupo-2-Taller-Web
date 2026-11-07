package com.tallerwebi.infraestructura.resumen;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.resumen.RepositorioResumen;
import com.tallerwebi.dominio.resumen.Resumen;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { HibernateTestConfig.class })
@Transactional
public class RepositorioResumenTest {

  private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 4, 12, 0);

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioResumen repositorio;
  private Usuario usuario;
  private Usuario otroUsuario;

  @BeforeEach
  public void init() {
    repositorio = new RepositorioResumenImpl(sessionFactory);
    usuario = guardarUsuario("resumenes-uno@test.com");
    otroUsuario = guardarUsuario("resumenes-dos@test.com");
  }

  @Test
  public void guardarDeberiaAsignarIdYConservarTodosLosCampos() {
    Resumen resumen = new Resumen(usuario, "texto largo", "h1", "<p>resumen</p>", AHORA);

    repositorio.guardar(resumen);
    Resumen encontrado = recargar(resumen.getId(), usuario.getId());

    assertThat(resumen.getId(), notNullValue());
    assertThat(encontrado.getTextoOriginal(), equalTo("texto largo"));
    assertThat(encontrado.getMarca(), equalTo("h1"));
    assertThat(encontrado.getRespuestaHtml(), equalTo("<p>resumen</p>"));
    assertThat(encontrado.getFechaCreacion(), equalTo(AHORA));
  }

  @Test
  public void deberiaGuardarTextosLargos() {
    String largo = "palabra ".repeat(500);

    Resumen resumen = new Resumen(usuario, largo, "h-largo", "<p>" + largo + "</p>", AHORA);
    repositorio.guardar(resumen);
    Resumen encontrado = recargar(resumen.getId(), usuario.getId());

    assertThat(encontrado.getTextoOriginal(), equalTo(largo));
    assertThat(encontrado.getRespuestaHtml(), equalTo("<p>" + largo + "</p>"));
  }

  @Test
  public void buscarPorUsuarioDeberiaDevolverSoloLosSuyosDelMasNuevoAlMasViejo() {
    Resumen viejo = guardar(usuario, "viejo", "h-viejo", AHORA.minusDays(2));
    Resumen nuevo = guardar(usuario, "nuevo", "h-nuevo", AHORA);
    Resumen medio = guardar(usuario, "medio", "h-medio", AHORA.minusDays(1));
    guardar(otroUsuario, "ajeno", "h-ajeno", AHORA);

    List<Resumen> historial = repositorio.buscarPorUsuario(usuario.getId());

    assertThat(historial, hasSize(3));
    assertThat(historial.get(0).getId(), equalTo(nuevo.getId()));
    assertThat(historial.get(1).getId(), equalTo(medio.getId()));
    assertThat(historial.get(2).getId(), equalTo(viejo.getId()));
  }

  @Test
  public void buscarPorUsuarioConMismaFechaDesempataPorElMasReciente() {
    Resumen primero = guardar(usuario, "primero", "h-a", AHORA);
    Resumen segundo = guardar(usuario, "segundo", "h-b", AHORA);

    List<Resumen> historial = repositorio.buscarPorUsuario(usuario.getId());

    assertThat(historial.get(0).getId(), equalTo(segundo.getId()));
    assertThat(historial.get(1).getId(), equalTo(primero.getId()));
  }

  @Test
  public void buscarPorUsuarioSinResumenesDeberiaDevolverListaVacia() {
    assertThat(repositorio.buscarPorUsuario(usuario.getId()), empty());
  }

  @Test
  public void buscarPorIdYUsuarioDeberiaDevolverElResumenDelDuenio() {
    Resumen resumen = guardar(usuario, "texto", "h1", AHORA);

    Resumen encontrado = repositorio.buscarPorIdYUsuario(resumen.getId(), usuario.getId());

    assertThat(encontrado, notNullValue());
    assertThat(encontrado.getId(), equalTo(resumen.getId()));
  }

  @Test
  public void buscarPorIdYUsuarioNoDeberiaDevolverElResumenDeOtroUsuario() {
    Resumen resumen = guardar(usuario, "texto", "h1", AHORA);

    assertThat(repositorio.buscarPorIdYUsuario(resumen.getId(), otroUsuario.getId()), nullValue());
  }

  @Test
  public void buscarPorIdYUsuarioInexistenteDeberiaDevolverNull() {
    assertThat(repositorio.buscarPorIdYUsuario(999999L, usuario.getId()), nullValue());
  }

  @Test
  public void buscarPorUsuarioYMarcaDeberiaEncontrarElResumenPrevio() {
    Resumen resumen = guardar(usuario, "texto", "h-buscada", AHORA);

    Resumen encontrado = repositorio.buscarPorUsuarioYMarca(usuario.getId(), "h-buscada");

    assertThat(encontrado, notNullValue());
    assertThat(encontrado.getId(), equalTo(resumen.getId()));
  }

  @Test
  public void buscarPorUsuarioYMarcaNoDeberiaEncontrarOtraHuella() {
    guardar(usuario, "texto", "h-buscada", AHORA);

    assertThat(repositorio.buscarPorUsuarioYMarca(usuario.getId(), "otra"), nullValue());
  }

  @Test
  public void buscarPorUsuarioYMarcaNoDeberiaMezclarUsuarios() {
    guardar(otroUsuario, "texto", "h-compartida", AHORA);

    assertThat(repositorio.buscarPorUsuarioYMarca(usuario.getId(), "h-compartida"), nullValue());
  }

  @Test
  public void buscarPorUsuarioYMarcaConDuplicadosNoDeberiaFallar() {
    guardar(usuario, "texto", "h-repetida", AHORA);
    guardar(usuario, "texto", "h-repetida", AHORA.plusMinutes(1));

    assertThat(repositorio.buscarPorUsuarioYMarca(usuario.getId(), "h-repetida"), notNullValue());
  }

  private Usuario guardarUsuario(String email) {
    Usuario nuevo = new Usuario();
    nuevo.setEmail(email);
    sessionFactory.getCurrentSession().persist(nuevo);
    return nuevo;
  }

  private Resumen guardar(Usuario duenio, String texto, String huella, LocalDateTime fecha) {
    Resumen resumen = new Resumen(duenio, texto, huella, "<p>" + texto + "</p>", fecha);
    repositorio.guardar(resumen);
    return resumen;
  }

  private Resumen recargar(Long id, Long usuarioId) {
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();
    return repositorio.buscarPorIdYUsuario(id, usuarioId);
  }
}
