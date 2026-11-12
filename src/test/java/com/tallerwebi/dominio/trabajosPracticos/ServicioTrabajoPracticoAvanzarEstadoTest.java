package com.tallerwebi.dominio.trabajosPracticos;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioTrabajoPracticoAvanzarEstadoTest {

    private ServicioTrabajoPracticoImpl servicio;
    private RepositorioTrabajoPractico repositorioMock;

    @BeforeEach
    public void init() {
        repositorioMock = mock(RepositorioTrabajoPractico.class);
        servicio = new ServicioTrabajoPracticoImpl(repositorioMock);
    }

    @Test
    public void avanzarUnPendienteDeberiaPasarloAEnCurso() {
        TrabajoPractico tp = tpConEstado(EstadoTP.PENDIENTE);
        when(repositorioMock.buscarPorId(1L)).thenReturn(tp);

        TrabajoPractico resultado = servicio.avanzarEstado(1L);

        assertThat(resultado.getEstado(), equalTo(EstadoTP.EN_CURSO));
    }

    @Test
    public void avanzarUnEnCursoDeberiaPasarloAFinalizado() {
        TrabajoPractico tp = tpConEstado(EstadoTP.EN_CURSO);
        when(repositorioMock.buscarPorId(1L)).thenReturn(tp);

        TrabajoPractico resultado = servicio.avanzarEstado(1L);

        assertThat(resultado.getEstado(), equalTo(EstadoTP.FINALIZADO));
    }

    @Test
    public void avanzarUnFinalizadoNoDeberiaCambiarlo() {
        TrabajoPractico tp = tpConEstado(EstadoTP.FINALIZADO);
        when(repositorioMock.buscarPorId(1L)).thenReturn(tp);

        TrabajoPractico resultado = servicio.avanzarEstado(1L);

        assertThat(resultado.getEstado(), equalTo(EstadoTP.FINALIZADO));
    }

    @Test
    public void avanzarDosVecesDesdePendienteDeberiaLlegarAFinalizado() {
        TrabajoPractico tp = tpConEstado(EstadoTP.PENDIENTE);
        when(repositorioMock.buscarPorId(1L)).thenReturn(tp);

        servicio.avanzarEstado(1L);
        servicio.avanzarEstado(1L);

        assertThat(tp.getEstado(), equalTo(EstadoTP.FINALIZADO));
    }

    @Test
    public void avanzarUnIdInexistenteDeberiaLanzarExcepcion() {
        when(repositorioMock.buscarPorId(99L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> servicio.avanzarEstado(99L));
    }

    private TrabajoPractico tpConEstado(EstadoTP estado) {
        TrabajoPractico tp = new TrabajoPractico();
        tp.setNombre("TP de prueba");
        tp.setEstado(estado);
        return tp;
    }
}