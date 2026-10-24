package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
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
}
