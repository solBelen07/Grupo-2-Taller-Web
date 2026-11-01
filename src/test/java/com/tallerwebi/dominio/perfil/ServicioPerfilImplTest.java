package com.tallerwebi.dominio.perfil;

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
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.registro.RepositorioRegistro;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ServicioPerfilImplTest {

    private static final String EMAIL = "ana@test.com";

    private ServicioPerfilImpl servicioPerfil;
    private RepositorioUsuario repositorioUsuarioMock;
    private RepositorioRegistro repositorioRegistroMock;
    private RepositorioFotoPerfil repositorioFotoMock;
    private Usuario usuario;

    @BeforeEach
    public void init() {
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        repositorioRegistroMock = mock(RepositorioRegistro.class);
        repositorioFotoMock = mock(RepositorioFotoPerfil.class);
        servicioPerfil =
                new ServicioPerfilImpl(repositorioUsuarioMock, repositorioRegistroMock, repositorioFotoMock);

        usuario = new Usuario();
        usuario.setId(7L);
        usuario.setEmail(EMAIL);
        usuario.setNombre("Ana");
        usuario.setApellido("Pérez");
        usuario.setPassword(new BCryptPasswordEncoder().encode("clave12345"));
        when(repositorioUsuarioMock.buscar(EMAIL)).thenReturn(usuario);
    }

    // ---------- obtenerUsuario ----------

    @Test
    public void obtenerUsuarioExistenteDeberiaDevolverlo() {
        assertThat(servicioPerfil.obtenerUsuario(EMAIL), sameInstance(usuario));
    }

    @Test
    public void obtenerUsuarioInexistenteDeberiaLanzarUsuarioNoEncontrado() {
        assertThrows(UsuarioNoEncontrado.class, () -> servicioPerfil.obtenerUsuario("nadie@test.com"));
    }

    // ---------- actualizarDatos ----------

    @Test
    public void actualizarDatosValidosDeberiaModificarSoloLosCamposPermitidos() {
        Universidad universidad = new Universidad("UNLaM");
        Carrera carrera = new Carrera("Sistemas");
        Interes java = new Interes("Java");
        when(repositorioRegistroMock.buscarUniversidad(1L)).thenReturn(universidad);
        when(repositorioRegistroMock.buscarCarrera(2L)).thenReturn(carrera);
        when(repositorioRegistroMock.buscarIntereses(List.of(3L))).thenReturn(List.of(java));

        DatosPerfil datos = datosValidos();
        datos.setNombre("  Laura ");
        datos.setApellido(" Gómez  ");

        Usuario resultado = servicioPerfil.actualizarDatos(EMAIL, datos);

        assertThat(resultado.getNombre(), equalTo("Laura"));
        assertThat(resultado.getApellido(), equalTo("Gómez"));
        assertThat(resultado.getUniversidad(), sameInstance(universidad));
        assertThat(resultado.getCarrera(), sameInstance(carrera));
        assertThat(resultado.getIntereses(), contains(java));
        assertThat(resultado.getEmail(), equalTo(EMAIL));
    }

    @Test
    public void actualizarDatosSinNombreDeberiaLanzarIllegalArgument() {
        DatosPerfil datos = datosValidos();
        datos.setNombre(" ");

        assertMensajeDatos(datos, "Completá nombre y apellido");
    }

    @Test
    public void actualizarDatosSinApellidoDeberiaLanzarIllegalArgument() {
        DatosPerfil datos = datosValidos();
        datos.setApellido(null);

        assertMensajeDatos(datos, "Completá nombre y apellido");
    }

    @Test
    public void actualizarDatosSinUniversidadDeberiaLanzarIllegalArgument() {
        DatosPerfil datos = datosValidos();
        datos.setUniversidadId(null);

        assertMensajeDatos(datos, "Seleccioná universidad y carrera");
    }

    @Test
    public void actualizarDatosSinCarreraDeberiaLanzarIllegalArgument() {
        DatosPerfil datos = datosValidos();
        datos.setCarreraId(null);

        assertMensajeDatos(datos, "Seleccioná universidad y carrera");
    }

    @Test
    public void actualizarDatosConUniversidadInexistenteDeberiaLanzarIllegalArgument() {
        when(repositorioRegistroMock.buscarUniversidad(1L)).thenReturn(null);
        when(repositorioRegistroMock.buscarCarrera(2L)).thenReturn(new Carrera("Sistemas"));

        assertMensajeDatos(datosValidos(), "no es válida");
    }

    @Test
    public void actualizarDatosConCarreraInexistenteDeberiaLanzarIllegalArgument() {
        when(repositorioRegistroMock.buscarUniversidad(1L)).thenReturn(new Universidad("UNLaM"));
        when(repositorioRegistroMock.buscarCarrera(2L)).thenReturn(null);

        assertMensajeDatos(datosValidos(), "no es válida");
    }

    // ---------- cambiarPassword ----------

    @Test
    public void cambiarPasswordConDatosValidosDeberiaGuardarElNuevoHash() {
        servicioPerfil.cambiarPassword(EMAIL, cambio("clave12345", "nuevaClave99", "nuevaClave99"));

        assertThat(usuario.getPassword(), startsWith("$2"));
        assertThat(usuario.getPassword(), not(equalTo("nuevaClave99")));
        assertThat(new BCryptPasswordEncoder().matches("nuevaClave99", usuario.getPassword()), is(true));
    }

    @Test
    public void cambiarPasswordConActualEnTextoPlanoDeberiaAceptarlaYHashearLaNueva() {
        usuario.setPassword("test");

        servicioPerfil.cambiarPassword(EMAIL, cambio("test", "nuevaClave99", "nuevaClave99"));

        assertThat(usuario.getPassword(), startsWith("$2"));
    }

    @Test
    public void cambiarPasswordConActualIncorrectaDeberiaLanzarIllegalArgumentYNoCambiar() {
        String hashAnterior = usuario.getPassword();

        assertMensajePassword(cambio("incorrecta", "nuevaClave99", "nuevaClave99"), "no es correcta");

        assertThat(usuario.getPassword(), equalTo(hashAnterior));
    }

    @Test
    public void cambiarPasswordConActualVaciaDeberiaLanzarIllegalArgument() {
        assertMensajePassword(cambio(" ", "nuevaClave99", "nuevaClave99"), "contraseña actual");
    }

    @Test
    public void cambiarPasswordConNuevaCortaDeberiaLanzarIllegalArgument() {
        assertMensajePassword(cambio("clave12345", "corta", "corta"), "al menos 8 caracteres");
    }

    @Test
    public void cambiarPasswordConNuevaNulaDeberiaLanzarIllegalArgument() {
        assertMensajePassword(cambio("clave12345", null, null), "al menos 8 caracteres");
    }

    @Test
    public void cambiarPasswordConConfirmacionDistintaDeberiaLanzarIllegalArgument() {
        assertMensajePassword(cambio("clave12345", "nuevaClave99", "otraCosa99"), "no coincide");
    }

    @Test
    public void cambiarPasswordIgualALaActualDeberiaLanzarIllegalArgument() {
        assertMensajePassword(cambio("clave12345", "clave12345", "clave12345"), "distinta de la actual");
    }

    @Test
    public void cambiarPasswordConGuardadaNulaDeberiaLanzarIllegalArgument() {
        usuario.setPassword(null);

        assertMensajePassword(cambio("clave12345", "nuevaClave99", "nuevaClave99"), "no es correcta");
    }

    // ---------- fotos ----------

    @Test
    public void guardarFotoValidaDeberiaPersistirlaConElIdDelUsuario() {
        byte[] contenido = { 1, 2, 3 };

        servicioPerfil.guardarFoto(EMAIL, "image/png", contenido);

        ArgumentCaptor<FotoPerfil> captor = ArgumentCaptor.forClass(FotoPerfil.class);
        verify(repositorioFotoMock).guardar(captor.capture());
        assertThat(captor.getValue().getUsuarioId(), equalTo(7L));
        assertThat(captor.getValue().getTipoContenido(), equalTo("image/png"));
        assertThat(captor.getValue().getDatos(), equalTo(contenido));
    }

    @Test
    public void guardarFotoConTipoNoPermitidoDeberiaLanzarIllegalArgument() {
        assertThrows(
                IllegalArgumentException.class,
                () -> servicioPerfil.guardarFoto(EMAIL, "text/html", new byte[] { 1 })
        );
        verify(repositorioFotoMock, never()).guardar(any(FotoPerfil.class));
    }

    @Test
    public void guardarFotoConTipoNuloDeberiaLanzarIllegalArgument() {
        assertThrows(
                IllegalArgumentException.class,
                () -> servicioPerfil.guardarFoto(EMAIL, null, new byte[] { 1 })
        );
    }

    @Test
    public void guardarFotoVaciaDeberiaLanzarIllegalArgument() {
        assertThrows(
                IllegalArgumentException.class,
                () -> servicioPerfil.guardarFoto(EMAIL, "image/png", new byte[0])
        );
    }

    @Test
    public void guardarFotoNulaDeberiaLanzarIllegalArgument() {
        assertThrows(
                IllegalArgumentException.class,
                () -> servicioPerfil.guardarFoto(EMAIL, "image/png", null)
        );
    }

    @Test
    public void guardarFotoMayorADosMegasDeberiaLanzarIllegalArgument() {
        byte[] grande = new byte[2 * 1024 * 1024 + 1];

        assertThrows(
                IllegalArgumentException.class,
                () -> servicioPerfil.guardarFoto(EMAIL, "image/jpeg", grande)
        );
        verify(repositorioFotoMock, never()).guardar(any(FotoPerfil.class));
    }

    @Test
    public void eliminarFotoDeberiaEliminarPorIdDelUsuario() {
        servicioPerfil.eliminarFoto(EMAIL);

        verify(repositorioFotoMock).eliminar(7L);
    }

    @Test
    public void obtenerFotoDeberiaBuscarPorIdDelUsuario() {
        FotoPerfil foto = new FotoPerfil(7L, "image/png", new byte[] { 1 });
        when(repositorioFotoMock.buscar(7L)).thenReturn(foto);

        assertThat(servicioPerfil.obtenerFoto(EMAIL), sameInstance(foto));
    }

    @Test
    public void tieneFotoDeberiaSerTrueSoloSiExisteLaFoto() {
        when(repositorioFotoMock.buscar(7L)).thenReturn(new FotoPerfil(7L, "image/png", new byte[] { 1 }));
        when(repositorioFotoMock.buscar(8L)).thenReturn(null);

        assertThat(servicioPerfil.tieneFoto(7L), is(true));
        assertThat(servicioPerfil.tieneFoto(8L), is(false));
    }

    // ---------- helpers ----------

    private DatosPerfil datosValidos() {
        DatosPerfil datos = new DatosPerfil();
        datos.setNombre("Ana");
        datos.setApellido("Pérez");
        datos.setUniversidadId(1L);
        datos.setCarreraId(2L);
        datos.setInteresesIds(List.of(3L));
        return datos;
    }

    private DatosCambioPassword cambio(String actual, String nueva, String confirmacion) {
        DatosCambioPassword datos = new DatosCambioPassword();
        datos.setActual(actual);
        datos.setNueva(nueva);
        datos.setConfirmacion(confirmacion);
        return datos;
    }

    private void assertMensajeDatos(DatosPerfil datos, String fragmento) {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicioPerfil.actualizarDatos(EMAIL, datos)
        );
        assertThat(error.getMessage(), containsString(fragmento));
    }

    private void assertMensajePassword(DatosCambioPassword datos, String fragmento) {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicioPerfil.cambiarPassword(EMAIL, datos)
        );
        assertThat(error.getMessage(), containsString(fragmento));
    }
}