package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ServicioLoginImplTest {

  private ServicioLoginImpl servicioLogin;
  private RepositorioUsuario repositorioUsuarioMock;

  @BeforeEach
  public void init() {
    repositorioUsuarioMock = mock(RepositorioUsuario.class);
    servicioLogin = new ServicioLoginImpl(repositorioUsuarioMock);
  }

  @Test
  public void consultarUsuarioConPasswordBCryptCorrectaDeberiaDevolverElUsuario() {
    // Preparación
    Usuario usuario = usuarioConPassword(new BCryptPasswordEncoder().encode("clave12345"));
    when(repositorioUsuarioMock.buscar("ana@test.com")).thenReturn(usuario);

    // Ejecución
    Usuario resultado = servicioLogin.consultarUsuario("ana@test.com", "clave12345");

    // Validación
    assertThat(resultado, sameInstance(usuario));
  }

  @Test
  public void consultarUsuarioConPasswordBCryptIncorrectaDeberiaDevolverNull() {
    Usuario usuario = usuarioConPassword(new BCryptPasswordEncoder().encode("clave12345"));
    when(repositorioUsuarioMock.buscar("ana@test.com")).thenReturn(usuario);

    assertThat(servicioLogin.consultarUsuario("ana@test.com", "otraClave"), nullValue());
  }

  @Test
  public void consultarUsuarioConPasswordEnTextoPlanoCorrectaDeberiaDevolverElUsuario() {
    Usuario usuario = usuarioConPassword("test");
    when(repositorioUsuarioMock.buscar("test@unlam.edu.ar")).thenReturn(usuario);

    assertThat(servicioLogin.consultarUsuario("test@unlam.edu.ar", "test"), sameInstance(usuario));
  }

  @Test
  public void consultarUsuarioConPasswordEnTextoPlanoIncorrectaDeberiaDevolverNull() {
    Usuario usuario = usuarioConPassword("test");
    when(repositorioUsuarioMock.buscar("test@unlam.edu.ar")).thenReturn(usuario);

    assertThat(servicioLogin.consultarUsuario("test@unlam.edu.ar", "mala"), nullValue());
  }

  @Test
  public void consultarUsuarioInexistenteDeberiaDevolverNull() {
    when(repositorioUsuarioMock.buscar("nadie@test.com")).thenReturn(null);

    assertThat(servicioLogin.consultarUsuario("nadie@test.com", "clave12345"), nullValue());
    verify(repositorioUsuarioMock, times(1)).buscar("nadie@test.com");
  }

  @Test
  public void consultarUsuarioConEmailNullDeberiaDevolverNullSinConsultarLaBase() {
    assertThat(servicioLogin.consultarUsuario(null, "clave12345"), nullValue());
    verifyNoInteractions(repositorioUsuarioMock);
  }

  @Test
  public void consultarUsuarioConPasswordNullDeberiaDevolverNullSinConsultarLaBase() {
    assertThat(servicioLogin.consultarUsuario("ana@test.com", null), nullValue());
    verifyNoInteractions(repositorioUsuarioMock);
  }

  @Test
  public void consultarUsuarioConPasswordGuardadaNullDeberiaDevolverNull() {
    Usuario usuario = usuarioConPassword(null);
    when(repositorioUsuarioMock.buscar("ana@test.com")).thenReturn(usuario);

    assertThat(servicioLogin.consultarUsuario("ana@test.com", "clave12345"), nullValue());
  }

  @Test
  public void consultarUsuarioDeberiaNormalizarElEmailAntesDeBuscar() {
    Usuario usuario = usuarioConPassword("clave12345");
    when(repositorioUsuarioMock.buscar("ana@test.com")).thenReturn(usuario);

    Usuario resultado = servicioLogin.consultarUsuario("  Ana@Test.COM ", "clave12345");

    assertThat(resultado, sameInstance(usuario));
    verify(repositorioUsuarioMock, times(1)).buscar("ana@test.com");
  }

  @Test
  public void consultarUsuarioDeberiaReintentarConElEmailTalCualSiLaBusquedaNormalizadaFalla() {
    Usuario usuario = usuarioConPassword("test");
    when(repositorioUsuarioMock.buscar("test@unlam.edu.ar")).thenReturn(null);
    when(repositorioUsuarioMock.buscar("Test@Unlam.edu.ar")).thenReturn(usuario);

    Usuario resultado = servicioLogin.consultarUsuario("Test@Unlam.edu.ar", "test");

    assertThat(resultado, sameInstance(usuario));
    verify(repositorioUsuarioMock).buscar("test@unlam.edu.ar");
    verify(repositorioUsuarioMock).buscar("Test@Unlam.edu.ar");
  }

  @Test
  public void registrarConEmailNuevoDeberiaGuardarElUsuario() throws UsuarioExistente {
    Usuario usuario = new Usuario();
    usuario.setEmail("nuevo@test.com");
    when(repositorioUsuarioMock.buscar("nuevo@test.com")).thenReturn(null);

    servicioLogin.registrar(usuario);

    verify(repositorioUsuarioMock, times(1)).guardar(usuario);
  }

  @Test
  public void registrarConEmailExistenteDeberiaLanzarUsuarioExistenteYNoGuardar() {
    Usuario usuario = new Usuario();
    usuario.setEmail("ana@test.com");
    when(repositorioUsuarioMock.buscar("ana@test.com")).thenReturn(new Usuario());

    assertThrows(UsuarioExistente.class, () -> servicioLogin.registrar(usuario));

    verify(repositorioUsuarioMock, never()).guardar(any(Usuario.class));
  }

  private Usuario usuarioConPassword(String password) {
    Usuario usuario = new Usuario();
    usuario.setEmail("ana@test.com");
    usuario.setPassword(password);
    return usuario;
  }
}