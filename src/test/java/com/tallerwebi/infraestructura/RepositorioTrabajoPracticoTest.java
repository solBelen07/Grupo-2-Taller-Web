package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.RepositorioTrabajoPractico;
import com.tallerwebi.dominio.TipoTrabajo;
import com.tallerwebi.dominio.TrabajoPractico;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
  classes = { HibernateInfraestructuraTestConfig.class, RepositorioTrabajoPracticoImpl.class }
)
@Transactional
public class RepositorioTrabajoPracticoTest {

  @Autowired
  private RepositorioTrabajoPractico repositorioTrabajoPractico;

  private TrabajoPractico tp1;
  private TrabajoPractico tp2;

  @BeforeEach
  public void setUp() {
    tp1 = new TrabajoPractico();
    tp1.setNombre("TP Integrador");
    tp1.setMateria("Taller Web I");
    tp1.setTipo(TipoTrabajo.GRUPAL);
    tp1.setDescripcion("Desarrollo web");

    tp2 = new TrabajoPractico();
    tp2.setNombre("TP Parcial");
    tp2.setMateria("Matemática");
    tp2.setTipo(TipoTrabajo.INDIVIDUAL);
    tp2.setDescripcion("Ejercicios prácticos");

    repositorioTrabajoPractico.save(tp1);
    repositorioTrabajoPractico.save(tp2);
  }

  @Test
  public void deberiaEncontrarTrabajosPracticosPorMateria() {
    // Ejecución
    List<TrabajoPractico> resultados = repositorioTrabajoPractico.buscarPorMateria("Taller Web I");

    // Verificación
    assertThat(resultados, notNullValue());
    assertThat(resultados, hasSize(1));
    assertThat(resultados.getFirst().getMateria(), equalTo("Taller Web I"));
  }

  @Test
  public void deberiaEncontrarTrabajoPracticoPorNombre() {
    // Ejecución
    List<TrabajoPractico> resultados = repositorioTrabajoPractico.buscarPorNombre("TP Parcial");

    // Verificación
    assertThat(resultados, notNullValue());
    assertThat(resultados, hasSize(1));
    assertThat(resultados.getFirst().getNombre(), equalTo("TP Parcial"));
  }

  @Test
  public void deberiaEncontrarTrabajosPracticosPorTipoIndividualOGrupal() {
    // Ejecución
    List<TrabajoPractico> resultadosGrupar = repositorioTrabajoPractico.buscarPorTipo(
      TipoTrabajo.GRUPAL
    );

    // Verificación
    assertThat(resultadosGrupar, notNullValue());
    assertThat(resultadosGrupar, hasSize(1));
    assertThat(resultadosGrupar.getFirst().getTipo(), equalTo(TipoTrabajo.GRUPAL));
  }

  @Test
  public void deberiaActualizarTrabajoPractico() {
    // 1. Preparación
    TrabajoPractico tp = new TrabajoPractico();
    tp.setNombre("TP Inicial");
    tp.setMateria("Taller Web I");
    tp.setTipo(TipoTrabajo.INDIVIDUAL);

    repositorioTrabajoPractico.save(tp);
    Long idGenerado = tp.getId();

    // 2. Modificación
    TrabajoPractico tpRecuperado = repositorioTrabajoPractico.buscarPorId(idGenerado);
    tpRecuperado.setNombre("TP Actualizado");

    // 3. Verificación
    TrabajoPractico tpVerificado = repositorioTrabajoPractico.buscarPorId(idGenerado);
    assertThat(tpVerificado.getNombre(), equalTo("TP Actualizado"));
  }

  @Test
  public void deberiaRetornarListaVaciaSiLaMateriaNoExiste() {
    // Ejecución
    List<TrabajoPractico> resultados = repositorioTrabajoPractico.buscarPorMateria(
      "Materia Inexistente"
    );

    // Verificación
    assertThat(resultados, notNullValue());
    assertThat(resultados, hasSize(0));
  }
}
