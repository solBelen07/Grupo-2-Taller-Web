package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.GrupoNoEncontrado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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

        assertThrows(GrupoNoEncontrado.class, () -> {
            servicioGrupo.buscarPorNombre(nombreGrupo);
        });

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
}
