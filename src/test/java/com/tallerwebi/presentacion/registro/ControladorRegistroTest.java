package com.tallerwebi.presentacion.registro;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Interes;
import com.tallerwebi.dominio.catalogo.ServicioCatalogo;
import com.tallerwebi.dominio.catalogo.Universidad;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.registro.RegistroDTO;
import com.tallerwebi.dominio.registro.ServicioRegistro;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorRegistroTest {

    private ControladorRegistro controladorRegistro;
    private ServicioRegistro servicioRegistroMock;
    private ServicioCatalogo servicioCatalogoMock;

    @BeforeEach
    public void init() {
        servicioRegistroMock = mock(ServicioRegistro.class);
        servicioCatalogoMock = mock(ServicioCatalogo.class);

        when(servicioCatalogoMock.obtenerUniversidades())
                .thenReturn(List.of(new Universidad("UNLaM")));
        when(servicioCatalogoMock.obtenerCarreras())
                .thenReturn(List.of(new Carrera("Ingeniería en Sistemas")));
        when(servicioCatalogoMock.obtenerIntereses()).thenReturn(List.of(new Interes("Java")));

        controladorRegistro = new ControladorRegistro(servicioRegistroMock, servicioCatalogoMock);
    }

    @Test
    public void mostrarFormularioDeberiaRetornarVistaRegistroConCatalogosYDtoVacio() {
        // Ejecución
        ModelAndView modelAndView = controladorRegistro.mostrarFormulario();

        // Validación
        Map<String, Object> modelo = modelAndView.getModel();
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("registro"));
        assertThat(modelo.get("registro"), instanceOf(RegistroDTO.class));
        assertThat((List<?>) modelo.get("universidades"), hasSize(1));
        assertThat((List<?>) modelo.get("carreras"), hasSize(1));
        assertThat((List<?>) modelo.get("intereses"), hasSize(1));
        assertThat(modelo.get("error"), nullValue());
        verifyNoInteractions(servicioRegistroMock);
    }

    @Test
    public void registrarConDatosValidosDeberiaLlamarAlServicioYRedirigirALogin()
            throws UsuarioExistente {
        // Preparación
        RegistroDTO registro = new RegistroDTO();

        // Ejecución
        ModelAndView modelAndView = controladorRegistro.registrar(registro);

        // Validación
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(servicioRegistroMock, times(1)).registrar(registro);
    }

    @Test
    public void registrarConEmailExistenteDeberiaVolverAlFormularioConMensajeDeError()
            throws UsuarioExistente {
        // Preparación
        RegistroDTO registro = new RegistroDTO();
        doThrow(new UsuarioExistente()).when(servicioRegistroMock).registrar(registro);

        // Ejecución
        ModelAndView modelAndView = controladorRegistro.registrar(registro);

        // Validación
        Map<String, Object> modelo = modelAndView.getModel();
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("registro"));
        assertThat(modelo.get("error"), equalTo("Ese email ya está registrado"));
        assertThat(modelo.get("registro"), sameInstance(registro));
        assertThat((List<?>) modelo.get("universidades"), hasSize(1));
    }

    @Test
    public void registrarConDatosInvalidosDeberiaVolverAlFormularioConElMensajeDelServicio()
            throws UsuarioExistente {
        // Preparación
        RegistroDTO registro = new RegistroDTO();
        doThrow(new IllegalArgumentException("Ingresá un email válido"))
                .when(servicioRegistroMock)
                .registrar(registro);

        // Ejecución
        ModelAndView modelAndView = controladorRegistro.registrar(registro);

        // Validación
        Map<String, Object> modelo = modelAndView.getModel();
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("registro"));
        assertThat(modelo.get("error"), equalTo("Ingresá un email válido"));
        assertThat((List<?>) modelo.get("carreras"), hasSize(1));
        assertThat((List<?>) modelo.get("intereses"), hasSize(1));
    }
}