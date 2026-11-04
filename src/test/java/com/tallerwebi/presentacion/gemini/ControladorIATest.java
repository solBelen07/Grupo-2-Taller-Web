package com.tallerwebi.presentacion.gemini;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public class ControladorIATest {

  private ServicioMateria servicioMateriaMock;
  private ControladorIA controladorIA;
  private Usuario usuarioMock;
  private MockHttpServletRequest request;

  @BeforeEach
  public void inicializar() {
    servicioMateriaMock = mock(ServicioMateria.class);
    controladorIA = new ControladorIA(servicioMateriaMock);
    usuarioMock = mock(Usuario.class);
    when(usuarioMock.getId()).thenReturn(1L);
    request = new MockHttpServletRequest();
  }

  @Test
  public void deberiaRedirigirAlLoginSiElUsuarioNoEstaAutenticado() {
    ModelAndView modelo = controladorIA.mostrarChatIA(request);

    assertEquals("redirect:/login", modelo.getViewName());
    verify(servicioMateriaMock, never()).obtenerMateriasPorUsuario(any());
  }

  @Test
  public void deberiaMostrarElChatConMaterias() {
    request.getSession().setAttribute("USUARIO", usuarioMock);
    List<Materia> materias = List.of(new Materia(), new Materia());
    when(servicioMateriaMock.obtenerMateriasPorUsuario(1L)).thenReturn(materias);

    ModelAndView modelo = controladorIA.mostrarChatIA(request);

    assertEquals("paginas/ia/ia", modelo.getViewName());
    assertEquals(materias, modelo.getModel().get("materias"));
    verify(servicioMateriaMock).obtenerMateriasPorUsuario(1L);
  }
}
