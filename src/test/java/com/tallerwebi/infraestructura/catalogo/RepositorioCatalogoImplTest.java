package com.tallerwebi.infraestructura.catalogo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Interes;
import com.tallerwebi.dominio.catalogo.RepositorioCatalogo;
import com.tallerwebi.dominio.catalogo.Universidad;
import com.tallerwebi.infraestructura.catalogo.RepositorioCatalogoImpl;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { HibernateTestConfig.class })
@Transactional
public class RepositorioCatalogoImplTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioCatalogo repositorioCatalogo;

  @BeforeEach
  public void init() {
    repositorioCatalogo = new RepositorioCatalogoImpl(sessionFactory);
  }

  @Test
  public void obtenerUniversidadesDeberiaDevolverlasOrdenadasPorNombre() {
    // Preparación
    persistir(new Universidad("Zeta Catalogo Test"));
    persistir(new Universidad("Alfa Catalogo Test"));
    persistir(new Universidad("Medio Catalogo Test"));

    // Ejecución
    List<Universidad> resultado = repositorioCatalogo.obtenerUniversidades();

    // Validación
    List<String> nombres = resultado.stream().map(Universidad::getNombre).toList();
    assertThat(
      nombres,
      containsInRelativeOrder("Alfa Catalogo Test", "Medio Catalogo Test", "Zeta Catalogo Test")
    );
  }

  @Test
  public void obtenerCarrerasDeberiaDevolverlasOrdenadasPorNombre() {
    persistir(new Carrera("Zeta Catalogo Test"));
    persistir(new Carrera("Alfa Catalogo Test"));
    persistir(new Carrera("Medio Catalogo Test"));

    List<Carrera> resultado = repositorioCatalogo.obtenerCarreras();

    List<String> nombres = resultado.stream().map(Carrera::getNombre).toList();
    assertThat(
      nombres,
      containsInRelativeOrder("Alfa Catalogo Test", "Medio Catalogo Test", "Zeta Catalogo Test")
    );
  }

  @Test
  public void obtenerInteresesDeberiaDevolverlosOrdenadosPorNombre() {
    persistir(new Interes("Zeta Catalogo Test"));
    persistir(new Interes("Alfa Catalogo Test"));
    persistir(new Interes("Medio Catalogo Test"));

    List<Interes> resultado = repositorioCatalogo.obtenerIntereses();

    List<String> nombres = resultado.stream().map(Interes::getNombre).toList();
    assertThat(
      nombres,
      containsInRelativeOrder("Alfa Catalogo Test", "Medio Catalogo Test", "Zeta Catalogo Test")
    );
  }

  @Test
  public void obtenerCatalogosSinDatosPropiosNoDeberiaFallar() {
    assertThat(repositorioCatalogo.obtenerUniversidades(), notNullValue());
    assertThat(repositorioCatalogo.obtenerCarreras(), notNullValue());
    assertThat(repositorioCatalogo.obtenerIntereses(), notNullValue());
  }

  private void persistir(Object entidad) {
    sessionFactory.getCurrentSession().persist(entidad);
  }
}
