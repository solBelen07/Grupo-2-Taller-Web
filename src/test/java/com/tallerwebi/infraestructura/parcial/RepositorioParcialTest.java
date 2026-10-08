package com.tallerwebi.infraestructura.parcial;

import static org.junit.jupiter.api.Assertions.*;

import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.parcial.Parcial;
import com.tallerwebi.dominio.parcial.RepositorioParcial;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
@Transactional
@Rollback
public class RepositorioParcialTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioParcial repositorioParcial;

  @BeforeEach
  public void init() {
    repositorioParcial = new RepositorioParcialImpl(sessionFactory);
  }

  @Test
  public void queSePuedaGuardarUnParcial() {
    Materia materia = dadoQueExisteUnaMateria();

    Parcial parcial = new Parcial();
    parcial.setMateria(materia);
    parcial.setFecha(LocalDate.of(2026, 10, 20));
    parcial.setHorario(LocalTime.of(18, 0));
    parcial.setCantidadDiasEstudio(5);
    parcial.setHorasPorDia(2);

    repositorioParcial.guardar(parcial);

    assertNotNull(parcial.getId());
  }

  @Test
  public void queSePuedaBuscarUnParcialPorId() {
    Materia materia = dadoQueExisteUnaMateria();

    Parcial parcial = dadoQueExisteUnParcial(materia);

    Parcial parcialBuscado = repositorioParcial.buscarPorId(parcial.getId());

    assertNotNull(parcialBuscado);
    assertEquals(parcial.getId(), parcialBuscado.getId());
  }

  @Test
  public void queSePuedanObtenerTodosLosParciales() {
    Materia materia = dadoQueExisteUnaMateria();

    dadoQueExisteUnParcial(materia);
    dadoQueExisteUnParcial(materia);

    List<Parcial> parciales = repositorioParcial.obtenerTodos();

    assertNotNull(parciales);
    assertEquals(2, parciales.size());
  }

  @Test
  public void queSePuedaModificarUnParcial() {
    Materia materia = dadoQueExisteUnaMateria();

    Parcial parcial = dadoQueExisteUnParcial(materia);

    parcial.setCantidadDiasEstudio(10);
    parcial.setHorasPorDia(3);

    repositorioParcial.modificar(parcial);

    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    Parcial parcialModificado = repositorioParcial.buscarPorId(parcial.getId());

    assertEquals(10, parcialModificado.getCantidadDiasEstudio());
    assertEquals(3, parcialModificado.getHorasPorDia());
  }

  @Test
  public void queSePuedaEliminarUnParcial() {
    Materia materia = dadoQueExisteUnaMateria();

    Parcial parcial = dadoQueExisteUnParcial(materia);

    Integer idParcial = parcial.getId();

    repositorioParcial.eliminar(parcial);

    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();

    Parcial parcialEliminado = repositorioParcial.buscarPorId(idParcial);

    assertNull(parcialEliminado);
  }

  private Materia dadoQueExisteUnaMateria() {
    Materia materia = new Materia();

    materia.setNombre("Programacion Web");
    materia.setDescripcion("Materia de programación");
    materia.setDocente("Profesor");

    Session session = sessionFactory.getCurrentSession();
    session.persist(materia);

    return materia;
  }

  private Parcial dadoQueExisteUnParcial(Materia materia) {
    Parcial parcial = new Parcial();

    parcial.setMateria(materia);
    parcial.setFecha(LocalDate.of(2026, 10, 20));
    parcial.setHorario(LocalTime.of(18, 0));
    parcial.setCantidadDiasEstudio(5);
    parcial.setHorasPorDia(2);

    repositorioParcial.guardar(parcial);

    return parcial;
  }
}