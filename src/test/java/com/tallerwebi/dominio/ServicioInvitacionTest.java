package com.tallerwebi.dominio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.InvitacionInvalida;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.RepositorioGrupo;
import com.tallerwebi.dominio.invitacion.*;
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
  public void rechazarDeberiaHacerInvitacionInvalida() throws InvitacionInvalida {
    Usuario emisor = dadoQueExisteUnUsuario("usuario@test.com");
    Usuario receptor = dadoQueExisteUnUsuario("receptor@test.com");
    Grupo grupo = dadoQueExisteUnGrupo("grupo-test");
    DatosInvitacion datosInvitacion = new DatosInvitacion();
    datosInvitacion.setEmisor(emisor.getEmail());
    datosInvitacion.setReceptor(receptor.getEmail());
    datosInvitacion.setGrupo(grupo.getNombre());

    Invitacion invitacion = new Invitacion(emisor, receptor, grupo);

    when(
      repositorioInvitacionMock.buscar(
        datosInvitacion.getEmisor(),
        datosInvitacion.getReceptor(),
        datosInvitacion.getGrupo()
      )
    )
      .thenReturn(invitacion);

    cuandoSeRechazaLaInvitacion(datosInvitacion);

    entoncesDejaDeEstarVigenteLaInvitacion(invitacion);
    entoncesSeLlamaAlRepositorioParaCambiarDeEstado();
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
    entoncesSeLlamaAlRepositorioParaCrearInvitacion();
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

    entoncesNoDeberiaCrearseLaInvitacionEnElRepositorio();
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

  @Test
  public void deberiaAceptarLaInvitacionExitosamente() throws InvitacionInvalida {
    DatosInvitacion datos = new DatosInvitacion();
    datos.setEmisor("emisor@test.com");
    datos.setReceptor("receptor@test.com");
    datos.setGrupo("grupo-test");

    Invitacion invitacionReal = new Invitacion();
    invitacionReal.setVigente(true);

    when(repositorioInvitacionMock.buscar(anyString(), anyString(), anyString()))
      .thenReturn(invitacionReal);

    cuandoSeAceptaLaInvitacion(datos);

    entoncesDejaDeEstarVigenteLaInvitacion(invitacionReal);
  }

  private Invitacion dadoQueExisteLaInvitacion(DatosInvitacion datosInvitacion) {
    Invitacion invitacion = new Invitacion(new Usuario(), new Usuario(), new Grupo());
    invitacion.getEmisor().setEmail(datosInvitacion.getEmisor());
    invitacion.getReceptor().setEmail(datosInvitacion.getReceptor());
    invitacion.getGrupo().setNombre(datosInvitacion.getGrupo());
    when(
      repositorioInvitacionMock.buscar(
        datosInvitacion.getEmisor(),
        datosInvitacion.getReceptor(),
        datosInvitacion.getGrupo()
      )
    )
      .thenReturn(invitacion);
    return invitacion;
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

  private void cuandoSeAceptaLaInvitacion(DatosInvitacion datosInvitacion)
    throws InvitacionInvalida {
    servicioInvitacion.aceptarInvitacion(datosInvitacion);
  }

  private void cuandoSeRechazaLaInvitacion(DatosInvitacion datosInvitacion)
    throws InvitacionInvalida {
    servicioInvitacion.rechazarInvitacion(datosInvitacion);
  }

  private void entoncesDejaDeEstarVigenteLaInvitacion(Invitacion invitacion) {
    assertFalse(invitacion.getVigente());
    verify(repositorioInvitacionMock, times(1)).cambiarEstado(any(), any());
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

  private void entoncesSeLlamaAlRepositorioParaCrearInvitacion() {
    verify(repositorioInvitacionMock, times(1)).enviarInvitacion(any(Invitacion.class));
  }

  private void entoncesSeLlamaAlRepositorioParaCambiarDeEstado() {
    verify(repositorioInvitacionMock, times(1)).cambiarEstado(any(), any());
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

  private void entoncesNoDeberiaCrearseLaInvitacionEnElRepositorio() {
    verify(repositorioInvitacionMock, never()).enviarInvitacion(any());
  }
}
