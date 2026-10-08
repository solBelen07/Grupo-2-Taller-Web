package com.tallerwebi.dominio.logro;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.invitacion.Invitacion;
import com.tallerwebi.dominio.invitacion.RepositorioInvitacion;
import com.tallerwebi.dominio.tecnicasestudio.RepositorioPomodoro;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioLogroTest {

  private ServicioLogro servicioLogro;
  private RepositorioLogro repositorioLogroMock;
  private RepositorioInvitacion repositorioInvitacionMock;
  private RepositorioPomodoro repositorioPomodoroMock;

  @BeforeEach
  public void init() {
    repositorioLogroMock = mock(RepositorioLogro.class);
    repositorioInvitacionMock = mock(RepositorioInvitacion.class);
    repositorioPomodoroMock = mock(RepositorioPomodoro.class);
    servicioLogro =
      new ServicioLogroImpl(
        repositorioLogroMock,
        repositorioInvitacionMock,
        repositorioPomodoroMock
      );
  }

  @Test
  public void deberiaObtenerProgresosSiElUsuarioTieneLogros() {
    dadoQueExistenLogros();
    dadoQueElUsuarioTieneLogrosConseguidos();
    List<Logro> logros = cuandoSeObtienenLosProgresosDelUsuario();
    entoncesSeObtienenLosLogrosDelUsuario(logros);
  }

  @Test
  public void deberiaObtenerNullSiElUsuarioNoTieneLogros() {
    dadoQueExistenLogros();
    List<Logro> logros = cuandoSeObtienenLosProgresosDelUsuario();
    entoncesSeObtienenNull(logros);
  }

  private void entoncesSeObtienenNull(List<Logro> logros) {
    assertTrue(logros.isEmpty());
  }

  private void entoncesSeObtienenLosLogrosDelUsuario(List<Logro> logros) {
    assertFalse(logros.isEmpty());
    assertThat(logros.get(0), instanceOf(Logro.class));
  }

  private List<Logro> cuandoSeObtienenLosProgresosDelUsuario() {
    Usuario usuario = mock(Usuario.class);
    return servicioLogro.obtenerProgresos(usuario);
  }

  private void dadoQueElUsuarioTieneLogrosConseguidos() {
    when(repositorioInvitacionMock.buscarInvitacionesPorReceptor(anyString()))
      .thenReturn(List.of(mock(Invitacion.class)));
    when(repositorioPomodoroMock.calcularMinutosCompletados(anyLong())).thenReturn(4000L);
  }

  private void dadoQueExistenLogros() {
    when(repositorioLogroMock.buscar(anyString())).thenReturn(new Logro());
  }
}
