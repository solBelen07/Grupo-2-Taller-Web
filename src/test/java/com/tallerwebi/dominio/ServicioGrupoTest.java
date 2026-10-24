package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoExistente;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NoEsAdministradorDelGrupo;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NombreDeGrupoInvalido;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.RepositorioGrupo;
import com.tallerwebi.dominio.grupo.ServicioGrupo;
import com.tallerwebi.dominio.grupo.ServicioGrupoImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioGrupoTest {

  private RepositorioGrupo repositorioGrupoMock;
  private ServicioGrupo servicioGrupo;

  @BeforeEach
  public void init() {
    repositorioGrupoMock = mock(RepositorioGrupo.class);
    servicioGrupo = new ServicioGrupoImpl(repositorioGrupoMock);
  }

  @Test
  public void buscarGrupoDeberiaLlamarAlRepositorio() throws GrupoNoEncontrado {
    String nombreGrupo = "grupo-test";
    Grupo grupoEsperado = new Grupo();
    when(repositorioGrupoMock.buscar(nombreGrupo)).thenReturn(grupoEsperado);

    Grupo grupoObtenido = servicioGrupo.buscarPorNombre(nombreGrupo);

    assertThat(grupoObtenido, equalTo(grupoEsperado));
    verify(repositorioGrupoMock, times(2)).buscar(nombreGrupo);
  }

  @Test
  public void buscarGrupoNoExistenteDeberiaLanzarExcepcion() {
    String nombreGrupo = "grupo-no-existente";
    when(repositorioGrupoMock.buscar(nombreGrupo)).thenReturn(null);

    assertThrows(
      GrupoNoEncontrado.class,
      () -> {
        servicioGrupo.buscarPorNombre(nombreGrupo);
      }
    );

    verify(repositorioGrupoMock, times(1)).buscar(nombreGrupo);
  }

  @Test
  public void buscarUnGrupoExistenteDeberiaRetornarElGrupo() throws GrupoNoEncontrado {
    String nombreGrupo = "grupo-existente";
    Grupo grupoEsperado = new Grupo();
    when(repositorioGrupoMock.buscar(nombreGrupo)).thenReturn(grupoEsperado);

    Grupo grupoObtenido = servicioGrupo.buscarPorNombre(nombreGrupo);

    assertThat(grupoObtenido, equalTo(grupoEsperado));
    verify(repositorioGrupoMock, times(2)).buscar(nombreGrupo);
  }

  @Test
  public void listarGruposDelUsuarioDeberiaPedirleAlRepositorioLosGruposDeEseUsuario() {
    Grupo grupo = new Grupo();
    grupo.setNombre("grupo-del-usuario");
    when(repositorioGrupoMock.listarGruposPorUsuario("test@unlam.edu.ar"))
      .thenReturn(List.of(grupo));

    List<Grupo> grupos = servicioGrupo.listarGruposDelUsuario("test@unlam.edu.ar");

    assertThat(grupos, equalTo(List.of(grupo)));
    verify(repositorioGrupoMock).listarGruposPorUsuario("test@unlam.edu.ar");
    verify(repositorioGrupoMock, never()).listar();
  }

  @Test
  public void cantidadParticipantesDeberiaDevolverLoQueCuentaElRepositorio()
    throws GrupoNoEncontrado {
    when(repositorioGrupoMock.buscar("grupo1")).thenReturn(new Grupo());
    when(repositorioGrupoMock.cantidadParticipantes("grupo1")).thenReturn(3);

    Integer cantidad = servicioGrupo.cantidadParticipantes("grupo1");

    assertThat(cantidad, equalTo(3));
  }

  @Test
  public void cantidadParticipantesDeUnGrupoQueNoExisteDeberiaLanzarExcepcion() {
    when(repositorioGrupoMock.buscar("no-existe")).thenReturn(null);

    assertThrows(GrupoNoEncontrado.class, () -> servicioGrupo.cantidadParticipantes("no-existe"));
    verify(repositorioGrupoMock, never()).cantidadParticipantes("no-existe");
  }

  @Test
  public void listarIntegrantesDeberiaDevolverLosDelRepositorio() throws GrupoNoEncontrado {
    Usuario integrante = new Usuario();
    integrante.setEmail("test@unlam.edu.ar");
    when(repositorioGrupoMock.buscar("grupo1")).thenReturn(new Grupo());
    when(repositorioGrupoMock.listarIntegrantes("grupo1")).thenReturn(List.of(integrante));

    assertThat(servicioGrupo.listarIntegrantes("grupo1"), equalTo(List.of(integrante)));
  }

  @Test
  public void listarIntegrantesDeUnGrupoQueNoExisteDeberiaLanzarExcepcion() {
    when(repositorioGrupoMock.buscar("no-existe")).thenReturn(null);

    assertThrows(GrupoNoEncontrado.class, () -> servicioGrupo.listarIntegrantes("no-existe"));
  }

  // ABM de grupo

  @Test
  public void crearGrupoDeberiaGuardarloConElCreadorComoAdministrador() throws Exception {
    Usuario creador = usuarioConId(1L);
    when(repositorioGrupoMock.buscar("Equipo Web")).thenReturn(null);

    Grupo grupo = servicioGrupo.crearGrupo("  Equipo Web  ", creador);

    assertThat(grupo.getNombre(), equalTo("Equipo Web"));
    assertThat(grupo.getCreador(), sameInstance(creador));
    assertThat(grupo.esAdministradoPor(creador), equalTo(true));
    verify(repositorioGrupoMock).guardar(grupo);
  }

  @Test
  public void crearGrupoConNombreVacioDeberiaLanzarExcepcionYNoGuardar() {
    NombreDeGrupoInvalido error = assertThrows(
      NombreDeGrupoInvalido.class,
      () -> servicioGrupo.crearGrupo("   ", usuarioConId(1L))
    );

    assertThat(error.getMessage(), equalTo("El nombre del grupo es obligatorio"));
    verify(repositorioGrupoMock, never()).guardar(any(Grupo.class));
  }

  @Test
  public void crearGrupoConNombreMuyLargoDeberiaLanzarExcepcion() {
    String nombreLargo = "a".repeat(51);

    assertThrows(
      NombreDeGrupoInvalido.class,
      () -> servicioGrupo.crearGrupo(nombreLargo, usuarioConId(1L))
    );
  }

  @Test
  public void crearGrupoConBarraEnElNombreDeberiaLanzarExcepcion() {
    assertThrows(
      NombreDeGrupoInvalido.class,
      () -> servicioGrupo.crearGrupo("grupo/1", usuarioConId(1L))
    );
  }

  @Test
  public void crearGrupoConNombreRepetidoDeberiaLanzarExcepcionYNoGuardar() {
    when(repositorioGrupoMock.buscar("grupo1")).thenReturn(new Grupo());

    assertThrows(GrupoExistente.class, () -> servicioGrupo.crearGrupo("grupo1", usuarioConId(1L)));
    verify(repositorioGrupoMock, never()).guardar(any(Grupo.class));
  }

  @Test
  public void elAdministradorPuedeCambiarleElNombreAlGrupo() throws Exception {
    Usuario admin = usuarioConId(1L);
    Grupo grupo = grupoCreadoPor("viejo", admin);
    when(repositorioGrupoMock.buscar("viejo")).thenReturn(grupo);
    when(repositorioGrupoMock.buscar("nuevo")).thenReturn(null);

    servicioGrupo.modificarGrupo("viejo", "nuevo", admin);

    assertThat(grupo.getNombre(), equalTo("nuevo"));
    verify(repositorioGrupoMock).modificar(grupo);
  }

  @Test
  public void elAdministradorPuedeGuardarElGrupoSinCambiarleElNombre() throws Exception {
    Usuario admin = usuarioConId(1L);
    Grupo grupo = grupoCreadoPor("mismo", admin);
    when(repositorioGrupoMock.buscar("mismo")).thenReturn(grupo);

    servicioGrupo.modificarGrupo("mismo", "mismo", admin);

    verify(repositorioGrupoMock).modificar(grupo);
  }

  @Test
  public void modificarConUnNombreQueYaUsaOtroGrupoDeberiaLanzarExcepcion() {
    Usuario admin = usuarioConId(1L);
    when(repositorioGrupoMock.buscar("viejo")).thenReturn(grupoCreadoPor("viejo", admin));
    when(repositorioGrupoMock.buscar("ocupado")).thenReturn(new Grupo());

    assertThrows(
      GrupoExistente.class,
      () -> servicioGrupo.modificarGrupo("viejo", "ocupado", admin)
    );
    verify(repositorioGrupoMock, never()).modificar(any(Grupo.class));
  }

  @Test
  public void unUsuarioQueNoEsAdministradorNoPuedeModificarElGrupo() {
    when(repositorioGrupoMock.buscar("grupo1"))
      .thenReturn(grupoCreadoPor("grupo1", usuarioConId(1L)));

    assertThrows(
      NoEsAdministradorDelGrupo.class,
      () -> servicioGrupo.modificarGrupo("grupo1", "otro", usuarioConId(2L))
    );
    verify(repositorioGrupoMock, never()).modificar(any(Grupo.class));
  }

  @Test
  public void elAdministradorPuedeEliminarElGrupo() throws Exception {
    Usuario admin = usuarioConId(1L);
    Grupo grupo = grupoCreadoPor("grupo1", admin);
    when(repositorioGrupoMock.buscar("grupo1")).thenReturn(grupo);

    servicioGrupo.eliminarGrupo("grupo1", admin);

    verify(repositorioGrupoMock).eliminar(grupo);
  }

  @Test
  public void unUsuarioQueNoEsAdministradorNoPuedeEliminarElGrupo() {
    when(repositorioGrupoMock.buscar("grupo1"))
      .thenReturn(grupoCreadoPor("grupo1", usuarioConId(1L)));

    assertThrows(
      NoEsAdministradorDelGrupo.class,
      () -> servicioGrupo.eliminarGrupo("grupo1", usuarioConId(2L))
    );
    verify(repositorioGrupoMock, never()).eliminar(any(Grupo.class));
  }

  @Test
  public void unGrupoSinCreadorNoTieneAdministrador() {
    when(repositorioGrupoMock.buscar("viejo")).thenReturn(new Grupo());

    assertThrows(
      NoEsAdministradorDelGrupo.class,
      () -> servicioGrupo.buscarGrupoAdministrado("viejo", usuarioConId(1L))
    );
  }

  @Test
  public void eliminarUnGrupoQueNoExisteDeberiaLanzarExcepcion() {
    when(repositorioGrupoMock.buscar("no-existe")).thenReturn(null);

    assertThrows(
      GrupoNoEncontrado.class,
      () -> servicioGrupo.eliminarGrupo("no-existe", usuarioConId(1L))
    );
  }

  private Usuario usuarioConId(Long id) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    return usuario;
  }

  private Grupo grupoCreadoPor(String nombre, Usuario creador) {
    Grupo grupo = new Grupo();
    grupo.setNombre(nombre);
    grupo.setCreador(creador);
    return grupo;
  }
}
