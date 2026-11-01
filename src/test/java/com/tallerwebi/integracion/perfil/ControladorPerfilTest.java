package com.tallerwebi.integracion.perfil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Interes;
import com.tallerwebi.dominio.catalogo.Universidad;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
@Transactional
public class ControladorPerfilTest {

    private static final String EMAIL = "perfil.test@test.com";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SessionFactory sessionFactory;

    private MockMvc mockMvc;
    private Usuario usuario;
    private Universidad universidad;
    private Carrera carrera;
    private Interes interes;

    @BeforeEach
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        universidad = new Universidad("UNLaM Perfil Test");
        carrera = new Carrera("Carrera Perfil Test");
        interes = new Interes("Interes Perfil Test");
        sessionFactory.getCurrentSession().persist(universidad);
        sessionFactory.getCurrentSession().persist(carrera);
        sessionFactory.getCurrentSession().persist(interes);

        usuario = new Usuario();
        usuario.setEmail(EMAIL);
        usuario.setNombre("Ana");
        usuario.setApellido("Pérez");
        usuario.setPassword(new BCryptPasswordEncoder().encode("clave12345"));
        usuario.setRol("USER");
        usuario.activar();
        sessionFactory.getCurrentSession().persist(usuario);
    }

    @Test
    public void sinSesionDeberiaRedirigirALogin() throws Exception {
        this.mockMvc.perform(get("/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void debeMostrarElPerfilConCatalogosYDatosDelUsuario() throws Exception {
        this.mockMvc.perform(get("/perfil").session(sesion()))
                .andExpect(status().isOk())
                .andExpect(view().name("paginas/perfil/perfil"))
                .andExpect(model().attributeExists("perfil", "datos", "cambioPassword"))
                .andExpect(model().attribute("tieneFoto", false))
                .andExpect(model().attribute("universidades", not(empty())))
                .andExpect(model().attribute("carreras", not(empty())))
                .andExpect(model().attribute("intereses", not(empty())));
    }

    @Test
    public void debeGuardarLosDatosYRedirigirAlPerfil() throws Exception {
        this.mockMvc.perform(
                        post("/perfil/datos")
                                .session(sesion())
                                .param("nombre", "Laura")
                                .param("apellido", "Gómez")
                                .param("universidadId", universidad.getId().toString())
                                .param("carreraId", carrera.getId().toString())
                                .param("interesesIds", interes.getId().toString())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"))
                .andExpect(flash().attribute("mensaje", "Datos actualizados"));

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        Usuario guardado = buscarUsuario();
        assertThat(guardado.getNombre(), equalTo("Laura"));
        assertThat(guardado.getApellido(), equalTo("Gómez"));
        assertThat(guardado.getEmail(), equalTo(EMAIL));
        assertThat(guardado.getUniversidad().getNombre(), equalTo("UNLaM Perfil Test"));
        assertThat(guardado.getCarrera().getNombre(), equalTo("Carrera Perfil Test"));
        assertThat(guardado.getIntereses(), hasSize(1));
    }

    @Test
    public void noDeberiaPermitirCambiarElEmailNiElRolPorParametros() throws Exception {
        this.mockMvc.perform(
                        post("/perfil/datos")
                                .session(sesion())
                                .param("nombre", "Laura")
                                .param("apellido", "Gómez")
                                .param("universidadId", universidad.getId().toString())
                                .param("carreraId", carrera.getId().toString())
                                .param("email", "otro@test.com")
                                .param("rol", "ADMIN")
                )
                .andExpect(status().is3xxRedirection());

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        Usuario guardado = buscarUsuario();
        assertThat(guardado.getEmail(), equalTo(EMAIL));
        assertThat(guardado.getRol(), equalTo("USER"));
    }

    @Test
    public void datosInvalidosDeberianRedirigirConMensajeDeError() throws Exception {
        this.mockMvc.perform(
                        post("/perfil/datos")
                                .session(sesion())
                                .param("nombre", " ")
                                .param("apellido", "Gómez")
                                .param("universidadId", universidad.getId().toString())
                                .param("carreraId", carrera.getId().toString())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"))
                .andExpect(flash().attribute("error", "Completá nombre y apellido"));
    }

    @Test
    public void debeCambiarLaPasswordConHash() throws Exception {
        this.mockMvc.perform(
                        post("/perfil/password")
                                .session(sesion())
                                .param("actual", "clave12345")
                                .param("nueva", "nuevaClave99")
                                .param("confirmacion", "nuevaClave99")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensaje", "Contraseña actualizada"));

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        String guardada = buscarUsuario().getPassword();
        assertThat(guardada, org.hamcrest.Matchers.startsWith("$2"));
        assertThat(new BCryptPasswordEncoder().matches("nuevaClave99", guardada), is(true));
    }

    @Test
    public void passwordActualIncorrectaNoDeberiaCambiarNada() throws Exception {
        this.mockMvc.perform(
                        post("/perfil/password")
                                .session(sesion())
                                .param("actual", "incorrecta")
                                .param("nueva", "nuevaClave99")
                                .param("confirmacion", "nuevaClave99")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "La contraseña actual no es correcta"));

        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        assertThat(
                new BCryptPasswordEncoder().matches("clave12345", buscarUsuario().getPassword()),
                is(true)
        );
    }

    @Test
    public void debeSubirVerYEliminarLaFoto() throws Exception {
        byte[] contenido = { 1, 2, 3, 4 };
        MockMultipartFile foto = new MockMultipartFile("foto", "f.png", "image/png", contenido);

        this.mockMvc.perform(multipart("/perfil/foto").file(foto).session(sesion()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensaje", "Foto actualizada"));

        this.mockMvc.perform(get("/perfil/foto").session(sesion()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(content().bytes(contenido));

        this.mockMvc.perform(post("/perfil/foto/eliminar").session(sesion()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("mensaje", "Foto eliminada"));

        this.mockMvc.perform(get("/perfil/foto").session(sesion())).andExpect(status().isNotFound());
    }

    @Test
    public void unArchivoQueNoEsImagenDeberiaSerRechazado() throws Exception {
        MockMultipartFile html = new MockMultipartFile("foto", "x.html", "text/html", new byte[] { 1 });

        this.mockMvc.perform(multipart("/perfil/foto").file(html).session(sesion()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("error", "La foto debe ser JPG, PNG o WEBP"));

        this.mockMvc.perform(get("/perfil/foto").session(sesion())).andExpect(status().isNotFound());
    }

    @Test
    public void verFotoSinSesionDeberiaResponder401() throws Exception {
        this.mockMvc.perform(get("/perfil/foto")).andExpect(status().isUnauthorized());
    }

    private MockHttpSession sesion() {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("USUARIO", usuario);
        return sesion;
    }

    private Usuario buscarUsuario() {
        return sessionFactory
                .getCurrentSession()
                .createQuery("from Usuario where email = :email", Usuario.class)
                .setParameter("email", EMAIL)
                .uniqueResult();
    }
}