package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.InvitacionInvalida;
import com.tallerwebi.presentacion.DatosInvitacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioInvitacionTest {

  private ServicioInvitacion servicioInvitacion;
  private RepositorioInvitacion repositorioInvitacionMock;
  private RepositorioGrupo repositorioGrupoMock;
  private RepositorioUsuario repositorioUsuarioMock;

  @BeforeEach
  public void init() {
    repositorioInvitacionMock = mock(RepositorioInvitacion.class);
    repositorioGrupoMock = mock(RepositorioGrupo.class);
    repositorioUsuarioMock = mock(RepositorioUsuario.class);

    servicioInvitacion =
      new ServicioInvitacionImpl(
        repositorioInvitacionMock,
        repositorioUsuarioMock,
        repositorioGrupoMock
      );
  }

  @Test
  public void deberiaCrearLaInvitacionCuandoTodosLosDatosSonValidos() throws InvitacionInvalida {
    String emailEmisor = "emisor@test.com";
    String emailReceptor = "receptor@test.com";
    String nombreGrupo = "grupo-test";
    DatosInvitacion datosInvitacion = new DatosInvitacion();
    datosInvitacion.setEmisor(emailEmisor);
    datosInvitacion.setReceptor(emailReceptor);
    datosInvitacion.setGrupo(nombreGrupo);

    Usuario emisor = dadoQueExisteUnUsuario(emailEmisor);
    Usuario receptor = dadoQueExisteUnUsuario(emailReceptor);
    Grupo grupo = dadoQueExisteUnGrupo(nombreGrupo);

    cuandoLosDatosDeInvitacionSonValidos();
    Invitacion resultado = servicioInvitacion.validarInvitacion(datosInvitacion);

    entoncesSeObtieneUnaInvitacionValida(resultado, emisor, receptor, grupo);
    entoncesSeLlamaAlRepositorio();
  }

  @Test
  public void deberiaLanzarExcepcionCuandoElEmisorNoExiste() {
    String emailEmisor = "emisor@noexiste.com";
    String emailReceptor = "receptor@test.com";
    String nombreGrupo = "grupo-test";

    dadoQueNoExisteUnUsuario(emailEmisor);
    dadoQueExisteUnUsuario(emailReceptor);
    dadoQueExisteUnGrupo(nombreGrupo);

    entoncesValidarInvitacionLanzaExcepcionInvitacionInvalida(
      emailEmisor,
      emailReceptor,
      nombreGrupo
    );
    verify(repositorioInvitacionMock, never()).enviarInvitacion(any());
  }

  @Test
  public void deberiaLanzarExcepcionCuandoElReceptorNoExiste() {
    when(repositorioUsuarioMock.buscar("emisor@test.com")).thenReturn(new Usuario());
    when(repositorioUsuarioMock.buscar("fantasma@test.com")).thenReturn(null);
    when(repositorioGrupoMock.buscar("Matematicas")).thenReturn(new Grupo());

    DatosInvitacion datosInvitacion = new DatosInvitacion();
    datosInvitacion.setEmisor("emisor@test.com");
    datosInvitacion.setReceptor("fantasma@test.com");
    datosInvitacion.setGrupo("Matematicas");

    assertThrows(
      InvitacionInvalida.class,
      () -> {
        servicioInvitacion.validarInvitacion(datosInvitacion);
      }
    );

    verify(repositorioInvitacionMock, never()).enviarInvitacion(any());
  }

  @Test
  public void deberiaLanzarExcepcionCuandoElGrupoNoExiste() {
    when(repositorioUsuarioMock.buscar("emisor@test.com")).thenReturn(new Usuario());
    when(repositorioUsuarioMock.buscar("receptor@test.com")).thenReturn(new Usuario());
    when(repositorioGrupoMock.buscar("GrupoFantasma")).thenReturn(null);

    DatosInvitacion datosInvitacion = new DatosInvitacion();
    datosInvitacion.setEmisor("emisor@test.com");
    datosInvitacion.setReceptor("receptor@test.com");
    datosInvitacion.setGrupo("GrupoFantasma");

    assertThrows(
      InvitacionInvalida.class,
      () -> {
        servicioInvitacion.validarInvitacion(datosInvitacion);
      }
    );

    verify(repositorioInvitacionMock, never()).enviarInvitacion(any());
  }

  @Test
  public void deberiaLanzarExcepcionCuandoEmisorYReceptorSonElMismo() {
    String emailUsuario = "usuario@test.com";
    String nombreGrupo = "grupo-test";
    when(repositorioUsuarioMock.buscar(emailUsuario)).thenReturn(new Usuario());
    when(repositorioGrupoMock.buscar(nombreGrupo)).thenReturn(new Grupo());
    DatosInvitacion datosInvitacion = new DatosInvitacion();
    datosInvitacion.setEmisor(emailUsuario);
    datosInvitacion.setReceptor(emailUsuario);
    datosInvitacion.setGrupo(nombreGrupo);
    assertThrows(
      InvitacionInvalida.class,
      () -> {
        servicioInvitacion.validarInvitacion(datosInvitacion);
      }
    );

    verify(repositorioInvitacionMock, never()).enviarInvitacion(any());
  }

  private Usuario dadoQueExisteUnUsuario(String email) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    when(repositorioUsuarioMock.buscar(email)).thenReturn(usuario);
    return usuario;
  }

  private void dadoQueNoExisteUnUsuario(String email) {
    when(repositorioUsuarioMock.buscar(email)).thenReturn(null);
  }

  private Grupo dadoQueExisteUnGrupo(String nombreGrupo) {
    Grupo grupo = new Grupo();
    grupo.setNombre(nombreGrupo);
    when(repositorioGrupoMock.buscar(nombreGrupo)).thenReturn(grupo);
    return grupo;
  }

  private void cuandoLosDatosDeInvitacionSonValidos() {
    when(repositorioInvitacionMock.enviarInvitacion(any(Invitacion.class)))
      .thenAnswer(invocation -> invocation.getArgument(0));
  }

  private void entoncesSeObtieneUnaInvitacionValida(
    Invitacion invitacion,
    Usuario emisor,
    Usuario receptor,
    Grupo grupo
  ) {
    assertNotNull(invitacion);
    assertInstanceOf(Invitacion.class, invitacion);
    assertEquals(emisor, invitacion.getEmisor());
    assertEquals(receptor, invitacion.getReceptor());
    assertEquals(grupo, invitacion.getGrupo());
  }

  private void entoncesSeLlamaAlRepositorio() {
    verify(repositorioInvitacionMock, times(1)).enviarInvitacion(any(Invitacion.class));
  }

  private void entoncesValidarInvitacionLanzaExcepcionInvitacionInvalida(
    String emailEmisor,
    String emailReceptor,
    String nombreGrupo
  ) {
    DatosInvitacion datosInvitacion = new DatosInvitacion();
    datosInvitacion.setEmisor(emailEmisor);
    datosInvitacion.setReceptor(emailReceptor);
    datosInvitacion.setGrupo(nombreGrupo);
    assertThrows(
      InvitacionInvalida.class,
      () -> {
        servicioInvitacion.validarInvitacion(datosInvitacion);
      }
    );
  }
}
