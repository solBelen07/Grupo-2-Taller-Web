package com.tallerwebi.integracion.trabajosPractico;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tallerwebi.dominio.trabajosPracticos.EstadoTP;
import com.tallerwebi.dominio.trabajosPracticos.TipoTrabajo;
import com.tallerwebi.dominio.trabajosPracticos.TrabajoPractico;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
@Transactional
public class ControladorTrabajoPracticoAvanzarTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SessionFactory sessionFactory;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void avanzarDeberiaPasarDePendienteAEnCursoYAFinalizado() throws Exception {
        TrabajoPractico tp = persistirTp(EstadoTP.PENDIENTE);

        this.mockMvc.perform(post("/trabajos-practicos/avanzar/" + tp.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/trabajos-practicos"));
        assertThat(estadoGuardado(tp.getId()), equalTo(EstadoTP.EN_CURSO));

        this.mockMvc.perform(post("/trabajos-practicos/avanzar/" + tp.getId()))
                .andExpect(status().is3xxRedirection());
        assertThat(estadoGuardado(tp.getId()), equalTo(EstadoTP.FINALIZADO));
    }

    @Test
    public void avanzarUnFinalizadoNoDeberiaCambiarElEstado() throws Exception {
        TrabajoPractico tp = persistirTp(EstadoTP.FINALIZADO);

        this.mockMvc.perform(post("/trabajos-practicos/avanzar/" + tp.getId()))
                .andExpect(status().is3xxRedirection());

        assertThat(estadoGuardado(tp.getId()), equalTo(EstadoTP.FINALIZADO));
    }

    private TrabajoPractico persistirTp(EstadoTP estado) {
        TrabajoPractico tp = new TrabajoPractico();
        tp.setNombre("TP avanzar");
        tp.setMateria("Taller Web I");
        tp.setTipo(TipoTrabajo.INDIVIDUAL);
        tp.setEstado(estado);
        sessionFactory.getCurrentSession().persist(tp);
        sessionFactory.getCurrentSession().flush();
        return tp;
    }

    private EstadoTP estadoGuardado(Long id) {
        sessionFactory.getCurrentSession().flush();
        sessionFactory.getCurrentSession().clear();
        return sessionFactory.getCurrentSession().get(TrabajoPractico.class, id).getEstado();
    }
}