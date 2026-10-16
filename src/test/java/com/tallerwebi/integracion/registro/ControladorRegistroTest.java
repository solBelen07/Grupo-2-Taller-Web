package com.tallerwebi.integracion.registro;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
@Transactional
public class ControladorRegistroTest {

    private static final String EMAIL = "Ana.Perez@Test.com";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SessionFactory sessionFactory;

    private MockMvc mockMvc;
    private Universidad universidad;
    private Carrera carrera;
    private Interes interes;

    @BeforeEach
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        universidad = new Universidad("UNLaM Test");
        carrera = new Carrera("Ingeniería Test");
        interes = new Interes("Java Test");
        sessionFactory.getCurrentSession().persist(universidad);
        sessionFactory.getCurrentSession().persist(carrera);
        sessionFactory.getCurrentSession().persist(interes);
    }

    @Test
    public void debeMostrarElFormularioConLosCatalogosCargados() throws Exception {
        this.mockMvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("registro"))
                .andExpect(model().attributeExists("registro", "universidades", "carreras", "intereses"))
                .andExpect(model().attribute("universidades", hasSize(1)))
                .andExpect(model().attribute("carreras", hasSize(1)))
                .andExpect(model().attribute("intereses", hasSize(1)));
    }

    @Test
    public void debeRegistrarUsuarioConHashRolYDatosAcademicosYRedirigirALogin() throws Exception {
        this.mockMvc.perform(registroValido())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        Usuario guardado = buscarPorEmail("ana.perez@test.com");
        assertThat(guardado, notNullValue());
        assertThat(guardado.getNombre(), equalTo("Ana"));
        assertThat(guardado.getRol(), equalTo("USER"));
        assertThat(guardado.getActivo(), is(true));
        assertThat(guardado.getPassword(), not(equalTo("clave12345")));
        assertThat(guardado.getPassword(), startsWith("$2"));
        assertThat(guardado.getUniversidad().getNombre(), equalTo("UNLaM Test"));
        assertThat(guardado.getCarrera().getNombre(), equalTo("Ingeniería Test"));
        assertThat(guardado.getIntereses(), hasSize(1));
    }

    @Test
    public void debeRechazarUnEmailYaRegistrado() throws Exception {
        this.mockMvc.perform(registroValido()).andExpect(status().is3xxRedirection());

        this.mockMvc.perform(registroValido())
                .andExpect(status().isOk())
                .andExpect(view().name("registro"))
                .andExpect(model().attribute("error", "Ese email ya está registrado"))
                .andExpect(model().attributeExists("universidades", "carreras", "intereses"));
    }

    @Test
    public void debeRechazarUnEmailInvalido() throws Exception {
        this.mockMvc.perform(registro("no-es-un-email", "clave12345", carrera.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("registro"))
                .andExpect(model().attribute("error", "Ingresá un email válido"));
    }

    @Test
    public void debeRechazarUnaContrasenaCorta() throws Exception {
        this.mockMvc.perform(registro(EMAIL, "corta", carrera.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("registro"))
                .andExpect(model().attribute("error", containsString("al menos 8 caracteres")));
    }

    @Test
    public void debeRechazarUnaCarreraInexistente() throws Exception {
        this.mockMvc.perform(registro(EMAIL, "clave12345", 999999L))
                .andExpect(status().isOk())
                .andExpect(view().name("registro"))
                .andExpect(model().attributeExists("error"));
    }

    private MockHttpServletRequestBuilder registro(String email, String password, Long carreraId) {
        return post("/registro")
                .param("nombre", "Ana")
                .param("apellido", "Pérez")
                .param("email", email)
                .param("password", password)
                .param("universidadId", universidad.getId().toString())
                .param("carreraId", carreraId.toString())
                .param("interesesIds", interes.getId().toString());
    }

    private MockHttpServletRequestBuilder registroValido() {
        return registro(EMAIL, "clave12345", carrera.getId());
    }

    private Usuario buscarPorEmail(String email) {
        return sessionFactory
                .getCurrentSession()
                .createQuery("from Usuario where email = :email", Usuario.class)
                .setParameter("email", email)
                .uniqueResult();
    }
}