package com.tallerwebi.dominio.logro;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.RepositorioGrupo;
import com.tallerwebi.dominio.invitacion.RepositorioInvitacion;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import com.tallerwebi.dominio.parcial.Parcial;
import com.tallerwebi.dominio.parcial.RepositorioParcial;
import com.tallerwebi.dominio.racha.Racha;
import com.tallerwebi.dominio.sesionEstudio.SesionEstudio;
import com.tallerwebi.dominio.tecnicasestudio.RepositorioPomodoro;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioLogroTest {

  private static final Long ID_USUARIO = 1L;
  private static final String EMAIL_USUARIO = "test@unlam.edu.ar";

  private ServicioLogro servicioLogro;
  private RepositorioLogro repositorioLogroMock;
  private RepositorioLogroObtenido repositorioLogroObtenidoMock;
  private RepositorioInvitacion repositorioInvitacionMock;
  private RepositorioPomodoro repositorioPomodoroMock;
  private RepositorioMateria repositorioMateriaMock;
  private RepositorioParcial repositorioParcialMock;
  private RepositorioUsuario repositorioUsuarioMock;
  private RepositorioGrupo repositorioGrupoMock;
  private Usuario usuario;
  private Logro maratonista;
  private Logro popular;

  @BeforeEach
  public void init() {
    repositorioLogroMock = mock(RepositorioLogro.class);
    repositorioLogroObtenidoMock = mock(RepositorioLogroObtenido.class);
    repositorioInvitacionMock = mock(RepositorioInvitacion.class);
    repositorioPomodoroMock = mock(RepositorioPomodoro.class);
    repositorioMateriaMock = mock(RepositorioMateria.class);
    repositorioParcialMock = mock(RepositorioParcial.class);
    repositorioUsuarioMock = mock(RepositorioUsuario.class);
    repositorioGrupoMock = mock(RepositorioGrupo.class);
    servicioLogro =
      new ServicioLogroImpl(
        repositorioLogroMock,
        repositorioLogroObtenidoMock,
        repositorioInvitacionMock,
        repositorioPomodoroMock,
        repositorioMateriaMock,
        repositorioParcialMock,
        repositorioUsuarioMock,
        repositorioGrupoMock
      );

    usuario = new Usuario();
    usuario.setId(ID_USUARIO);
    usuario.setEmail(EMAIL_USUARIO);

    maratonista = logro("Maratonista");
    popular = logro("Popular");
    when(repositorioLogroMock.buscar("Maratonista")).thenReturn(maratonista);
    when(repositorioLogroMock.buscar("Popular")).thenReturn(popular);
    when(repositorioPomodoroMock.calcularMinutosCompletados(anyLong())).thenReturn(0L);
    when(repositorioInvitacionMock.cantidadDeInvitacionesHistoricoPorReceptor(anyString()))
      .thenReturn(0);
  }

  @Test
  public void deberiaObtenerElListadoDeLogrosDelRepositorio() {
    when(repositorioLogroMock.listar()).thenReturn(List.of(new Logro()));

    List<Logro> logros = servicioLogro.obtenerLogros();

    assertThat(logros.get(0), instanceOf(Logro.class));
  }

  @Test
  public void siEstudioMasDe50HorasDeberiaRegistrarElLogroMaratonistaConLaFechaDeHoy() {
    when(repositorioPomodoroMock.calcularMinutosCompletados(ID_USUARIO)).thenReturn(4000L);

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    LogroObtenido guardado = logroGuardado();
    assertThat(guardado.getUsuario(), sameInstance(usuario));
    assertThat(guardado.getLogro(), sameInstance(maratonista));
    assertThat(guardado.getFechaObtencion(), equalTo(LocalDate.now()));
  }

  @Test
  public void siFueInvitadoMasDe5VecesDeberiaRegistrarElLogroPopular() {
    when(repositorioInvitacionMock.cantidadDeInvitacionesHistoricoPorReceptor(EMAIL_USUARIO))
      .thenReturn(6);

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    assertThat(logroGuardado().getLogro(), sameInstance(popular));
  }

  @Test
  public void siCumpleLasDosCondicionesDeberiaRegistrarLosDosLogros() {
    when(repositorioPomodoroMock.calcularMinutosCompletados(ID_USUARIO)).thenReturn(4000L);
    when(repositorioInvitacionMock.cantidadDeInvitacionesHistoricoPorReceptor(EMAIL_USUARIO))
      .thenReturn(6);

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    verify(repositorioLogroObtenidoMock, times(2)).guardar(any(LogroObtenido.class));
  }

  @Test
  public void siNoCumpleNingunaCondicionNoDeberiaRegistrarNada() {
    servicioLogro.otorgarLogrosAlcanzados(usuario);

    verify(repositorioLogroObtenidoMock, never()).guardar(any(LogroObtenido.class));
  }

  @Test
  public void conExactamente50HorasTodaviaNoGanaElLogro() {
    when(repositorioPomodoroMock.calcularMinutosCompletados(ID_USUARIO)).thenReturn(3000L);

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    verify(repositorioLogroObtenidoMock, never()).guardar(any(LogroObtenido.class));
  }

  @Test
  public void siYaTieneElLogroNoDeberiaRegistrarloDeNuevo() {
    when(repositorioPomodoroMock.calcularMinutosCompletados(ID_USUARIO)).thenReturn(4000L);
    when(repositorioLogroObtenidoMock.existe(ID_USUARIO, "Maratonista")).thenReturn(true);

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    verify(repositorioLogroObtenidoMock, never()).guardar(any(LogroObtenido.class));
  }

  @Test
  public void siElLogroNoEstaCargadoEnLaBaseNoDeberiaRegistrarNada() {
    when(repositorioPomodoroMock.calcularMinutosCompletados(ID_USUARIO)).thenReturn(4000L);
    when(repositorioLogroMock.buscar("Maratonista")).thenReturn(null);

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    verify(repositorioLogroObtenidoMock, never()).guardar(any(LogroObtenido.class));
  }

  @Test
  public void siNoHayDatosDeInvitacionesNoDeberiaRegistrarNada() {
    when(repositorioInvitacionMock.cantidadDeInvitacionesHistoricoPorReceptor(EMAIL_USUARIO))
      .thenReturn(null);

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    verify(repositorioLogroObtenidoMock, never()).guardar(any(LogroObtenido.class));
  }

  @Test
  public void deberiaDevolverLosLogrosObtenidosDelUsuario() {
    LogroObtenido obtenido = new LogroObtenido(usuario, popular, LocalDate.now());
    when(repositorioLogroObtenidoMock.buscarPorUsuario(ID_USUARIO)).thenReturn(List.of(obtenido));

    List<LogroObtenido> obtenidos = servicioLogro.obtenerLogrosObtenidos(usuario);

    assertThat(obtenidos, equalTo(List.of(obtenido)));
  }

  private LogroObtenido logroGuardado() {
    ArgumentCaptor<LogroObtenido> captor = ArgumentCaptor.forClass(LogroObtenido.class);
    verify(repositorioLogroObtenidoMock).guardar(captor.capture());
    return captor.getValue();
  }

  private Logro logro(String nombre) {
    Logro logro = new Logro();
    logro.setNombre(nombre);
    return logro;
  }

  // Más logros

  private void dadoQueExisteElLogro(String nombre) {
    when(repositorioLogroMock.buscar(nombre)).thenReturn(logro(nombre));
  }

  private String nombreDelLogroGuardado() {
    ArgumentCaptor<LogroObtenido> captor = ArgumentCaptor.forClass(LogroObtenido.class);
    verify(repositorioLogroObtenidoMock).guardar(captor.capture());
    return captor.getValue().getLogro().getNombre();
  }

  private Usuario conRecordDeRacha(Integer dias) {
    Usuario persistido = new Usuario();
    persistido.setRacha(new Racha());
    persistido.getRacha().setRecordRacha(dias);
    return persistido;
  }

  @Test
  public void conSuPrimeraMateriaGanaPrimerosPasos() {
    dadoQueExisteElLogro("Primeros pasos");
    when(repositorioMateriaMock.obtenerCantidadMateriasPorUsuario(ID_USUARIO)).thenReturn(1L);

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    assertThat(nombreDelLogroGuardado(), equalTo("Primeros pasos"));
  }

  @Test
  public void sinMateriasNoGanaPrimerosPasos() {
    dadoQueExisteElLogro("Primeros pasos");
    when(repositorioMateriaMock.obtenerCantidadMateriasPorUsuario(ID_USUARIO)).thenReturn(null);

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    verify(repositorioLogroObtenidoMock, never()).guardar(any(LogroObtenido.class));
  }

  @Test
  public void conSuPrimerVueloGanaPrimerVueloPeroNoPilotoFrecuente() {
    dadoQueExisteElLogro("Primer vuelo");
    dadoQueExisteElLogro("Piloto frecuente");
    when(repositorioPomodoroMock.calcularMinutosCompletados(anyLong())).thenReturn(25L);

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    assertThat(nombreDelLogroGuardado(), equalTo("Primer vuelo"));
  }

  @Test
  public void conCincoHorasDeVueloGanaPilotoFrecuente() {
    dadoQueExisteElLogro("Piloto frecuente");
    when(repositorioPomodoroMock.calcularMinutosCompletados(anyLong())).thenReturn(300L);

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    assertThat(nombreDelLogroGuardado(), equalTo("Piloto frecuente"));
  }

  @Test
  public void conRecordDe3DiasGanaConstantePeroNoImparable() {
    dadoQueExisteElLogro("Constante");
    dadoQueExisteElLogro("Imparable");
    when(repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(conRecordDeRacha(3));

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    assertThat(nombreDelLogroGuardado(), equalTo("Constante"));
  }

  @Test
  public void conRecordDe7DiasGanaImparable() {
    dadoQueExisteElLogro("Imparable");
    when(repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(conRecordDeRacha(7));

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    assertThat(nombreDelLogroGuardado(), equalTo("Imparable"));
  }

  @Test
  public void sinRachaOSinUsuarioNoGanaLogrosDeRacha() {
    dadoQueExisteElLogro("Constante");
    when(repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(conRecordDeRacha(null));
    servicioLogro.otorgarLogrosAlcanzados(usuario);

    Usuario sinRacha = new Usuario();
    sinRacha.setRacha(null);
    when(repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(sinRacha);
    servicioLogro.otorgarLogrosAlcanzados(usuario);

    when(repositorioUsuarioMock.buscarPorId(ID_USUARIO)).thenReturn(null);
    servicioLogro.otorgarLogrosAlcanzados(usuario);

    verify(repositorioLogroObtenidoMock, never()).guardar(any(LogroObtenido.class));
  }

  private Parcial parcialDeLaMateriaDe(Usuario duenio, boolean sesionCompletada) {
    Materia materia = new Materia();
    materia.getUsuarios().add(duenio);
    Parcial parcial = new Parcial();
    parcial.setMateria(materia);
    SesionEstudio sesion = new SesionEstudio();
    sesion.setCompletada(sesionCompletada);
    parcial.agregarSesion(sesion);
    return parcial;
  }

  @Test
  public void conUnPlanDeSuMateriaCompletoGanaPlanCumplido() {
    dadoQueExisteElLogro("Plan cumplido");
    when(repositorioParcialMock.obtenerTodos())
      .thenReturn(
        List.of(parcialDeLaMateriaDe(usuario, false), parcialDeLaMateriaDe(usuario, true))
      );

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    assertThat(nombreDelLogroGuardado(), equalTo("Plan cumplido"));
  }

  @Test
  public void unPlanCompletoDeOtroUsuarioOSinMateriaNoCuenta() {
    dadoQueExisteElLogro("Plan cumplido");
    Usuario otro = new Usuario();
    otro.setId(99L);
    Parcial sinMateria = parcialDeLaMateriaDe(usuario, true);
    sinMateria.setMateria(null);
    when(repositorioParcialMock.obtenerTodos())
      .thenReturn(List.of(parcialDeLaMateriaDe(otro, true), sinMateria));

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    verify(repositorioLogroObtenidoMock, never()).guardar(any(LogroObtenido.class));
  }

  @Test
  public void siCreoUnGrupoGanaLiderDeGrupo() {
    dadoQueExisteElLogro("Líder de grupo");
    Grupo propio = new Grupo();
    propio.setCreador(usuario);
    Grupo ajeno = new Grupo();
    when(repositorioGrupoMock.listarGruposPorUsuario(EMAIL_USUARIO))
      .thenReturn(List.of(ajeno, propio));

    servicioLogro.otorgarLogrosAlcanzados(usuario);

    assertThat(nombreDelLogroGuardado(), equalTo("Líder de grupo"));
  }

  @Test
  public void siSoloEsMiembroNoGanaLiderDeGrupo() {
    dadoQueExisteElLogro("Líder de grupo");
    when(repositorioGrupoMock.listarGruposPorUsuario(EMAIL_USUARIO))
      .thenReturn(List.of(new Grupo()));
    servicioLogro.otorgarLogrosAlcanzados(usuario);

    when(repositorioGrupoMock.listarGruposPorUsuario(EMAIL_USUARIO)).thenReturn(null);
    servicioLogro.otorgarLogrosAlcanzados(usuario);

    verify(repositorioLogroObtenidoMock, never()).guardar(any(LogroObtenido.class));
  }
}
