package com.tallerwebi.infraestructura.tarea;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.tallerwebi.dominio.tarea.RepositorioTarea;
import com.tallerwebi.dominio.tarea.Tarea;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioTareaImplTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioTarea repositorioTarea;

  @BeforeEach
  public void init() {
    repositorioTarea = new RepositorioTareaImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnaNuevaTarea() {
    // preparacion
    Tarea tarea = new Tarea();
    tarea.setTitulo("Estudiar para el parcial");
    tarea.setMateria("Programación Web");
    tarea.setEstado("PENDIENTE");
    tarea.setHorasPlanificadas(10);
    tarea.setHorasRealizadas(0);
    tarea.setResponsable("Camila");

    // ejecucion
    repositorioTarea.guardar(tarea);

    // validacion
    Tarea tareaObtenida = sessionFactory.getCurrentSession().find(Tarea.class, tarea.getId());

    assertThat(tareaObtenida.getTitulo(), is(equalTo("Estudiar para el parcial")));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarTodasLasTareas() {
    // preparacion
    Tarea tarea1 = new Tarea();
    tarea1.setTitulo("Tarea 1");

    Tarea tarea2 = new Tarea();
    tarea2.setTitulo("Tarea 2");

    sessionFactory.getCurrentSession().persist(tarea1);
    sessionFactory.getCurrentSession().persist(tarea2);

    // ejecucion
    List<Tarea> tareas = repositorioTarea.buscarTodas();

    // validacion
    assertThat(tareas.size(), is(equalTo(2)));
  }
}
