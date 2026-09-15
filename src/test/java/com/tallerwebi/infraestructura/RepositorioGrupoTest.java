package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Grupo;
import com.tallerwebi.dominio.RepositorioGrupo;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioGrupoTest {
    @Autowired
    private SessionFactory sessionFactory;
    private RepositorioGrupo repositorioGrupo;
    private Grupo grupo;

    @BeforeEach
    public void init() {
        repositorioGrupo = new RepositorioGrupoImpl(sessionFactory);
        grupo = new Grupo();
        grupo.setNombre("grupo-existente");
        this.sessionFactory.getCurrentSession().persist(grupo);
    }

    @Test
    @Transactional
    @Rollback
    public void deberiaBuscarUnGrupoExistentePorNombre(){
        String grupoBuscado = "grupo-existente";
        repositorioGrupo.buscar(grupoBuscado);
        assertThat(repositorioGrupo.buscar(grupoBuscado).getId(), equalTo(grupo.getId()));
    }

    @Test
    @Transactional
    @Rollback
    public void deberiaArrojarExcepcionCuandoBuscoUnGrupoInexistentePorNombre(){
        String grupoBuscado = "grupo-inexistente";
        assertThat(repositorioGrupo.buscar(grupoBuscado), equalTo(null));
    }
}
