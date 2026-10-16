package com.tallerwebi.dominio.registro;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Interes;
import com.tallerwebi.dominio.catalogo.Universidad;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ServicioRegistroImplTest {

    private ServicioRegistroImpl servicioRegistro;
    private RepositorioRegistro repositorioRegistroMock;
    private RepositorioUsuario repositorioUsuarioMock;

    private Universidad universidad;
    private Carrera carrera;
    private Interes interes;

    @BeforeEach
    public void init() {
        repositorioRegistroMock = mock(RepositorioRegistro.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        servicioRegistro = new ServicioRegistroImpl(repositorioRegistroMock, repositorioUsuarioMock);

        universidad = new Universidad("UNLaM");
        carrera = new Carrera("Ingeniería en Sistemas");
        interes = new Interes("Java");

        when(repositorioRegistroMock.buscarUniversidad(1L)).thenReturn(universidad);
        when(repositorioRegistroMock.buscarCarrera(2L)).thenReturn(carrera);
        when(repositorioRegistroMock.buscarIntereses(List.of(3L))).thenReturn(List.of(interes));
    }

    @Test
    public void registrarConDatosValidosDeberiaGuardarUsuarioCompleto() throws UsuarioExistente {
        // Preparación
        RegistroDTO registro = registroValido();

        // Ejecución
        servicioRegistro.registrar(registro);

        // Validación
        Usuario guardado = capturarUsuarioGuardado();
        assertThat(guardado.getNombre(), equalTo("Ana"));
        assertThat(guardado.getApellido(), equalTo("Pérez"));
        assertThat(guardado.getEmail(), equalTo("ana@test.com"));
        assertThat(guardado.getRol(), equalTo("USER"));
        assertThat(guardado.getActivo(), is(true));
        assertThat(guardado.getUniversidad(), sameInstance(universidad));
        assertThat(guardado.getCarrera(), sameInstance(carrera));
        assertThat(guardado.getIntereses(), contains(interes));
    }

    @Test
    public void registrarDeberiaGuardarLaContrasenaConHashBCrypt() throws UsuarioExistente {
        servicioRegistro.registrar(registroValido());

        Usuario guardado = capturarUsuarioGuardado();
        assertThat(guardado.getPassword(), not(equalTo("clave12345")));
        assertThat(guardado.getPassword(), startsWith("$2"));
        assertThat(new BCryptPasswordEncoder().matches("clave12345", guardado.getPassword()), is(true));
    }

    @Test
    public void registrarDeberiaNormalizarElEmailYRecortarLosTextos() throws UsuarioExistente {
        RegistroDTO registro = registroValido();
        registro.setNombre("  Ana ");
        registro.setApellido(" Pérez  ");
        registro.setEmail("  Ana@Test.COM ");

        servicioRegistro.registrar(registro);

        Usuario guardado = capturarUsuarioGuardado();
        assertThat(guardado.getEmail(), equalTo("ana@test.com"));
        assertThat(guardado.getNombre(), equalTo("Ana"));
        assertThat(guardado.getApellido(), equalTo("Pérez"));
        verify(repositorioUsuarioMock).buscar("ana@test.com");
    }

    @Test
    public void registrarConEmailExistenteDeberiaLanzarUsuarioExistenteYNoGuardar() {
        when(repositorioUsuarioMock.buscar("ana@test.com")).thenReturn(new Usuario());

        assertThrows(UsuarioExistente.class, () -> servicioRegistro.registrar(registroValido()));

        verify(repositorioUsuarioMock, never()).guardar(any(Usuario.class));
    }

    @Test
    public void registrarConUniversidadInexistenteDeberiaLanzarIllegalArgument() {
        RegistroDTO registro = registroValido();
        registro.setUniversidadId(999L);

        assertThrows(IllegalArgumentException.class, () -> servicioRegistro.registrar(registro));

        verify(repositorioUsuarioMock, never()).guardar(any(Usuario.class));
    }

    @Test
    public void registrarConCarreraInexistenteDeberiaLanzarIllegalArgument() {
        RegistroDTO registro = registroValido();
        registro.setCarreraId(999L);

        assertThrows(IllegalArgumentException.class, () -> servicioRegistro.registrar(registro));

        verify(repositorioUsuarioMock, never()).guardar(any(Usuario.class));
    }

    @Test
    public void registrarSinNombreDeberiaLanzarIllegalArgument() {
        RegistroDTO registro = registroValido();
        registro.setNombre("  ");

        assertMensaje(registro, "Completá nombre y apellido");
    }

    @Test
    public void registrarSinApellidoDeberiaLanzarIllegalArgument() {
        RegistroDTO registro = registroValido();
        registro.setApellido(null);

        assertMensaje(registro, "Completá nombre y apellido");
    }

    @Test
    public void registrarConEmailVacioDeberiaLanzarIllegalArgument() {
        RegistroDTO registro = registroValido();
        registro.setEmail("");

        assertMensaje(registro, "Ingresá un email válido");
    }

    @Test
    public void registrarConEmailSinFormatoValidoDeberiaLanzarIllegalArgument() {
        RegistroDTO registro = registroValido();
        registro.setEmail("no-es-un-email");

        assertMensaje(registro, "Ingresá un email válido");
    }

    @Test
    public void registrarConContrasenaNulaDeberiaLanzarIllegalArgument() {
        RegistroDTO registro = registroValido();
        registro.setPassword(null);

        assertMensaje(registro, "al menos 8 caracteres");
    }

    @Test
    public void registrarConContrasenaCortaDeberiaLanzarIllegalArgument() {
        RegistroDTO registro = registroValido();
        registro.setPassword("corta");

        assertMensaje(registro, "al menos 8 caracteres");
    }

    @Test
    public void registrarSinUniversidadDeberiaLanzarIllegalArgument() {
        RegistroDTO registro = registroValido();
        registro.setUniversidadId(null);

        assertMensaje(registro, "Seleccioná universidad y carrera");
    }

    @Test
    public void registrarSinCarreraDeberiaLanzarIllegalArgument() {
        RegistroDTO registro = registroValido();
        registro.setCarreraId(null);

        assertMensaje(registro, "Seleccioná universidad y carrera");
    }

    @Test
    public void registrarSinInteresesDeberiaGuardarUsuarioSinIntereses() throws UsuarioExistente {
        RegistroDTO registro = registroValido();
        registro.setInteresesIds(List.of());
        when(repositorioRegistroMock.buscarIntereses(List.of())).thenReturn(List.of());

        servicioRegistro.registrar(registro);

        assertThat(capturarUsuarioGuardado().getIntereses(), empty());
    }

    private RegistroDTO registroValido() {
        RegistroDTO registro = new RegistroDTO();
        registro.setNombre("Ana");
        registro.setApellido("Pérez");
        registro.setEmail("ana@test.com");
        registro.setPassword("clave12345");
        registro.setUniversidadId(1L);
        registro.setCarreraId(2L);
        registro.setInteresesIds(List.of(3L));
        return registro;
    }

    private Usuario capturarUsuarioGuardado() {
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorioUsuarioMock).guardar(captor.capture());
        return captor.getValue();
    }

    private void assertMensaje(RegistroDTO registro, String fragmento) {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicioRegistro.registrar(registro)
        );
        assertThat(error.getMessage(), containsString(fragmento));
        verify(repositorioUsuarioMock, never()).guardar(any(Usuario.class));
    }
}