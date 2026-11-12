package com.tallerwebi.dominio.racha;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.sesionEstudio.RepositorioSesionEstudio;
import com.tallerwebi.dominio.tecnicasestudio.RepositorioPomodoro;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioRachaTest {

  private static final Long ID_USUARIO = 1L;

  private RepositorioSesionEstudio repositorioSesionEstudioMock;
  private RepositorioPomodoro repositorioPomodoroMock;
  private RepositorioUsuario repositorioUsuarioMock;
  private ServicioRachaImpl servicioRacha;
  private LocalDate hoy;

  @BeforeEach
  public void init() {
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);

    this.repositorioSesionEstudioMock = mock(RepositorioSesionEstudio.class);

    this.repositorioPomodoroMock = mock(RepositorioPomodoro.class);

    this.servicioRacha =
      new ServicioRachaImpl(
        this.repositorioUsuarioMock,
        this.repositorioSesionEstudioMock,
        this.repositorioPomodoroMock
      );

    this.hoy = LocalDate.now();
  }

  @Test
  public void deberiaIniciarLaRachaEnUnoCuandoNuncaEstudioAntes() {
    Usuario persistido = crearUsuario(new Racha(0, 0, null));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.registrarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(1));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(1));
    assertThat(persistido.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy));
    verify(this.repositorioUsuarioMock, times(1)).modificar(persistido);
  }

  @Test
  public void deberiaIniciarLaRachaEnUnoCuandoElUsuarioPersistidoNoTieneRachaCargada() {
    Usuario persistido = crearUsuario(null);
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.registrarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(1));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(1));
    assertThat(persistido.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy));
    verify(this.repositorioUsuarioMock, times(1)).modificar(persistido);
  }

  @Test
  public void deberiaIniciarLaRachaEnUnoCuandoLosValoresPersistidosSonNulos() {
    Usuario persistido = crearUsuario(new Racha(null, null, null));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.registrarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(1));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(1));
  }

  @Test
  public void deberiaIniciarLaRachaEnUnoCuandoLaRachaActualEstaEnCeroAunqueHayaFechaPrevia() {
    Usuario persistido = crearUsuario(new Racha(0, 4, this.hoy.minusDays(1)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.registrarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(1));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(4));
    assertThat(persistido.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy));
  }

  @Test
  public void deberiaSincronizarLaRachaEnLaSesionConUnaCopiaIndependiente() {
    Usuario persistido = crearUsuario(new Racha(0, 0, null));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.registrarRacha(sesion);

    assertThat(sesion.getRacha().getRachaActual(), equalTo(1));
    assertThat(sesion.getRacha().getRecordRacha(), equalTo(1));
    assertThat(sesion.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy));
    assertThat(sesion.getRacha(), not(sameInstance(persistido.getRacha())));
  }

  @Test
  public void noDeberiaModificarLaRachaSiYaEstudioHoy() {
    Usuario persistido = crearUsuario(new Racha(3, 5, this.hoy));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.registrarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(3));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(5));
    assertThat(persistido.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy));
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void deberiaSincronizarLaSesionSinPersistirCuandoYaEstudioHoy() {
    Usuario persistido = crearUsuario(new Racha(3, 5, this.hoy));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.registrarRacha(sesion);

    assertThat(sesion.getRacha().getRachaActual(), equalTo(3));
    assertThat(sesion.getRacha().getRecordRacha(), equalTo(5));
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void deberiaIncrementarLaRachaActualSiEstudioAyer() {
    Usuario persistido = crearUsuario(new Racha(3, 10, this.hoy.minusDays(1)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.registrarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(4));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(10));
    assertThat(persistido.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy));
    verify(this.repositorioUsuarioMock, times(1)).modificar(persistido);
  }

  @Test
  public void deberiaActualizarElRecordSiLaRachaActualLoSupera() {
    Usuario persistido = crearUsuario(new Racha(5, 5, this.hoy.minusDays(1)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.registrarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(6));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(6));
    verify(this.repositorioUsuarioMock, times(1)).modificar(persistido);
  }

  @Test
  public void noDeberiaActualizarElRecordSiLaRachaActualNoLoSupera() {
    Usuario persistido = crearUsuario(new Racha(2, 8, this.hoy.minusDays(1)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.registrarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(3));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(8));
  }

  @Test
  public void deberiaSincronizarLaSesionConLaRachaIncrementada() {
    Usuario persistido = crearUsuario(new Racha(3, 10, this.hoy.minusDays(1)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.registrarRacha(sesion);

    assertThat(sesion.getRacha().getRachaActual(), equalTo(4));
    assertThat(sesion.getRacha().getRecordRacha(), equalTo(10));
    assertThat(sesion.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy));
  }

  @Test
  public void deberiaReiniciarLaRachaEnUnoSiPasaronDosDiasSinEstudiar() {
    Usuario persistido = crearUsuario(new Racha(7, 10, this.hoy.minusDays(2)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.registrarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(1));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(10));
    assertThat(persistido.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy));
    verify(this.repositorioUsuarioMock, times(1)).modificar(persistido);
  }

  @Test
  public void deberiaReiniciarLaRachaEnUnoSiPasaronMuchosDiasSinEstudiarYConservarElRecord() {
    Usuario persistido = crearUsuario(new Racha(12, 12, this.hoy.minusDays(30)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.registrarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(1));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(12));
    assertThat(sesion.getRacha().getRachaActual(), equalTo(1));
    assertThat(sesion.getRacha().getRecordRacha(), equalTo(12));
  }

  @Test
  public void noDeberiaHacerNadaAlRegistrarRachaConUsuarioNulo() {
    this.servicioRacha.registrarRacha(null);

    verifyNoInteractions(this.repositorioUsuarioMock);
  }

  @Test
  public void deberiaLanzarUsuarioNoEncontradoAlRegistrarRachaSiNoExisteEnElRepositorio() {
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(null);

    assertThrows(UsuarioNoEncontrado.class, () -> this.servicioRacha.registrarRacha(sesion));

    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void deberiaReiniciarLaRachaActualACeroSiPasoMasDeUnDiaSinEstudiar() {
    Usuario persistido = crearUsuario(new Racha(4, 9, this.hoy.minusDays(2)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.modificarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(0));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(9));
    assertThat(sesion.getRacha().getRachaActual(), equalTo(0));
    assertThat(sesion.getRacha().getRecordRacha(), equalTo(9));
    verify(this.repositorioUsuarioMock, times(1)).modificar(persistido);
  }

  @Test
  public void noDeberiaReiniciarLaRachaSiEstudioAyer() {
    Usuario persistido = crearUsuario(new Racha(4, 9, this.hoy.minusDays(1)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.modificarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(4));
    assertThat(sesion.getRacha().getRachaActual(), equalTo(4));
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void noDeberiaReiniciarLaRachaSiEstudioHoy() {
    Usuario persistido = crearUsuario(new Racha(4, 9, this.hoy));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.modificarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(4));
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void noDeberiaPersistirSiLaRachaYaEstabaEnCeroAlModificarRacha() {
    Usuario persistido = crearUsuario(new Racha(0, 9, this.hoy.minusDays(5)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.modificarRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(0));
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void noDeberiaPersistirSiNuncaEstudioAlModificarRacha() {
    Usuario persistido = crearUsuario(new Racha(0, 0, null));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.modificarRacha(sesion);

    assertThat(sesion.getRacha().getRachaActual(), equalTo(0));
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void noDeberiaHacerNadaAlModificarRachaConUsuarioNuloOSinId() {
    this.servicioRacha.modificarRacha(null);
    this.servicioRacha.modificarRacha(new Usuario());

    verifyNoInteractions(this.repositorioUsuarioMock);
  }

  @Test
  public void deberiaLanzarUsuarioNoEncontradoAlModificarRachaSiNoExisteEnElRepositorio() {
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(null);

    assertThrows(UsuarioNoEncontrado.class, () -> this.servicioRacha.modificarRacha(sesion));
  }

  @Test
  public void deberiaObtenerLaRachaActualDelUsuario() {
    Usuario usuario = crearUsuario(new Racha(4, 9, this.hoy));

    assertThat(this.servicioRacha.obtenerRacha(usuario), equalTo(4));
  }

  @Test
  public void deberiaObtenerCeroComoRachaSiElUsuarioEsNulo() {
    assertThat(this.servicioRacha.obtenerRacha(null), equalTo(0));
  }

  @Test
  public void deberiaObtenerCeroComoRachaSiElUsuarioNoTieneRachaCargada() {
    Usuario usuario = crearUsuario(null);

    assertThat(this.servicioRacha.obtenerRacha(usuario), equalTo(0));
  }

  @Test
  public void deberiaObtenerCeroComoRachaSiLosValoresDeLaRachaSonNulos() {
    Usuario usuario = crearUsuario(new Racha(null, null, null));

    assertThat(this.servicioRacha.obtenerRacha(usuario), equalTo(0));
  }

  @Test
  public void deberiaObtenerElRecordDeRachaDelUsuario() {
    Usuario usuario = crearUsuario(new Racha(4, 9, this.hoy));

    assertThat(this.servicioRacha.obtenerRecordRacha(usuario), equalTo(9));
  }

  @Test
  public void deberiaObtenerCeroComoRecordSiElUsuarioEsNulo() {
    assertThat(this.servicioRacha.obtenerRecordRacha(null), equalTo(0));
  }

  @Test
  public void deberiaObtenerCeroComoRecordSiElUsuarioNoTieneRachaCargada() {
    Usuario usuario = crearUsuario(null);

    assertThat(this.servicioRacha.obtenerRecordRacha(usuario), equalTo(0));
  }

  @Test
  public void deberiaObtenerCeroComoRecordSiLosValoresDeLaRachaSonNulos() {
    Usuario usuario = crearUsuario(new Racha(null, null, null));

    assertThat(this.servicioRacha.obtenerRecordRacha(usuario), equalTo(0));
  }

  // Pruebas de recalcularRacha(): actividad del día, decremento y casos límite.

  @Test
  public void noDeberiaConsultarRepositoriosAlRecalcularConUsuarioNuloOSinId() {
    this.servicioRacha.recalcularRacha(null);
    this.servicioRacha.recalcularRacha(new Usuario());

    verifyNoInteractions(
      this.repositorioUsuarioMock,
      this.repositorioSesionEstudioMock,
      this.repositorioPomodoroMock
    );
  }

  @Test
  public void deberiaRegistrarRachaSiCompletoUnaSesionDeEstudioHoy() {
    Usuario persistido = crearUsuario(new Racha(2, 5, this.hoy.minusDays(1)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioSesionEstudioMock.existeSesionCompletadaEnFecha(ID_USUARIO, this.hoy))
      .thenReturn(true);
    when(this.repositorioPomodoroMock.existePomodoroCompletadoEnFecha(ID_USUARIO, this.hoy))
      .thenReturn(false);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.recalcularRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(3));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(5));
    assertThat(persistido.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy));
    assertThat(sesion.getRacha().getRachaActual(), equalTo(3));
    assertThat(sesion.getRacha(), not(sameInstance(persistido.getRacha())));
    verify(this.repositorioSesionEstudioMock).existeSesionCompletadaEnFecha(ID_USUARIO, this.hoy);
    verify(this.repositorioPomodoroMock).existePomodoroCompletadoEnFecha(ID_USUARIO, this.hoy);
    verify(this.repositorioUsuarioMock, times(1)).modificar(persistido);
  }

  @Test
  public void deberiaRegistrarRachaSiCompletoUnPomodoroHoy() {
    Usuario persistido = crearUsuario(new Racha(2, 5, this.hoy.minusDays(1)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioSesionEstudioMock.existeSesionCompletadaEnFecha(ID_USUARIO, this.hoy))
      .thenReturn(false);
    when(this.repositorioPomodoroMock.existePomodoroCompletadoEnFecha(ID_USUARIO, this.hoy))
      .thenReturn(true);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.recalcularRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(3));
    assertThat(persistido.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy));
    assertThat(sesion.getRacha().getRachaActual(), equalTo(3));
    verify(this.repositorioUsuarioMock, times(1)).modificar(persistido);
  }

  @Test
  public void noDeberiaSumarDosDiasSiCompletoSesionYPomodoroElMismoDia() {
    Usuario persistido = crearUsuario(new Racha(2, 2, this.hoy.minusDays(1)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioSesionEstudioMock.existeSesionCompletadaEnFecha(ID_USUARIO, this.hoy))
      .thenReturn(true);
    when(this.repositorioPomodoroMock.existePomodoroCompletadoEnFecha(ID_USUARIO, this.hoy))
      .thenReturn(true);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.recalcularRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(3));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(3));
    assertThat(sesion.getRacha().getRachaActual(), equalTo(3));
    verify(this.repositorioUsuarioMock, times(1)).modificar(persistido);
  }

  @Test
  public void noDeberiaIncrementarSiYaRegistroActividadHoy() {
    Usuario persistido = crearUsuario(new Racha(3, 6, this.hoy));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioSesionEstudioMock.existeSesionCompletadaEnFecha(ID_USUARIO, this.hoy))
      .thenReturn(true);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.recalcularRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(3));
    assertThat(persistido.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy));
    assertThat(sesion.getRacha().getRachaActual(), equalTo(3));
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void deberiaDescontarUnDiaSiNoQuedanActividadesCompletadasHoy() {
    Usuario persistido = crearUsuario(new Racha(4, 9, this.hoy));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.recalcularRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(3));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(9));
    assertThat(persistido.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy.minusDays(1)));
    assertThat(sesion.getRacha().getRachaActual(), equalTo(3));
    assertThat(sesion.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy.minusDays(1)));
    assertThat(sesion.getRacha(), not(sameInstance(persistido.getRacha())));
    verify(this.repositorioSesionEstudioMock).existeSesionCompletadaEnFecha(ID_USUARIO, this.hoy);
    verify(this.repositorioPomodoroMock).existePomodoroCompletadoEnFecha(ID_USUARIO, this.hoy);
    verify(this.repositorioUsuarioMock, times(1)).modificar(persistido);
  }

  @Test
  public void deberiaDejarLaRachaEnCeroYFechaNulaSiSoloTeniaUnDia() {
    Usuario persistido = crearUsuario(new Racha(1, 8, this.hoy));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.recalcularRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(0));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(8));
    assertNull(persistido.getRacha().getUltimaFechaEstudio());
    assertThat(sesion.getRacha().getRachaActual(), equalTo(0));
    assertNull(sesion.getRacha().getUltimaFechaEstudio());
    verify(this.repositorioUsuarioMock, times(1)).modificar(persistido);
  }

  @Test
  public void noDeberiaDejarUnaRachaNegativaAlRecalcular() {
    Usuario persistido = crearUsuario(new Racha(0, 8, this.hoy));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.recalcularRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(0));
    assertNull(persistido.getRacha().getUltimaFechaEstudio());
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(8));
    verify(this.repositorioUsuarioMock, times(1)).modificar(persistido);
  }

  @Test
  public void noDeberiaModificarRachaSiNoHuboActividadHoyPeroLaUltimaFueAyer() {
    Usuario persistido = crearUsuario(new Racha(3, 7, this.hoy.minusDays(1)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.recalcularRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(3));
    assertThat(persistido.getRacha().getRecordRacha(), equalTo(7));
    assertThat(persistido.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy.minusDays(1)));
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void noDeberiaModificarRachaSiNuncaRegistroUnaFechaDeEstudio() {
    Usuario persistido = crearUsuario(new Racha(0, 0, null));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.recalcularRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(0));
    assertNull(persistido.getRacha().getUltimaFechaEstudio());
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void noDeberiaModificarRachaSiLaUltimaFechaFueHaceVariosDias() {
    Usuario persistido = crearUsuario(new Racha(0, 9, this.hoy.minusDays(4)));
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(persistido);

    this.servicioRacha.recalcularRacha(sesion);

    assertThat(persistido.getRacha().getRachaActual(), equalTo(0));
    assertThat(persistido.getRacha().getUltimaFechaEstudio(), equalTo(this.hoy.minusDays(4)));
    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void deberiaLanzarUsuarioNoEncontradoAlRecalcularSinActividadSiNoEstaPersistido() {
    Usuario sesion = crearUsuario(null);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(null);

    assertThrows(UsuarioNoEncontrado.class, () -> this.servicioRacha.recalcularRacha(sesion));

    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void deberiaLanzarUsuarioNoEncontradoAlRecalcularConActividadSiNoEstaPersistido() {
    Usuario sesion = crearUsuario(null);
    when(this.repositorioSesionEstudioMock.existeSesionCompletadaEnFecha(ID_USUARIO, this.hoy))
      .thenReturn(true);
    when(this.repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(null);

    assertThrows(UsuarioNoEncontrado.class, () -> this.servicioRacha.recalcularRacha(sesion));

    verify(this.repositorioUsuarioMock, never()).modificar(any(Usuario.class));
  }

  private Usuario crearUsuario(Racha racha) {
    Usuario usuario = new Usuario();
    usuario.setId(ID_USUARIO);
    usuario.setRacha(racha);
    return usuario;
  }
}
