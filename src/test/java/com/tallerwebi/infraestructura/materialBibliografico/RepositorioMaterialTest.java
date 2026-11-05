package com.tallerwebi.infraestructura.materialBibliografico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materialBibliografico.MaterialBibliografico;
import com.tallerwebi.dominio.materialBibliografico.RepositorioMaterial;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
@Transactional
@Rollback
public class RepositorioMaterialTest {

  private RepositorioMaterial repositorioMaterial;

  @Autowired
  private SessionFactory sessionFactory;

  @BeforeEach
  public void inicializar() {
    repositorioMaterial = new RepositorioMaterialImpl(sessionFactory);
  }

  @Test
  public void guardarMaterialDeberiaPersistirElMaterial() {
    Materia materia = dadoQueExisteUnaMateria("Taller Web I");
    MaterialBibliografico material = dadoQueTengoUnMaterial(
      "archivo.pdf",
      "clave-guardar",
      materia
    );

    repositorioMaterial.guardar(material);
    prepararLecturaDesdeBaseDeDatos();

    assertNotNull(material.getId());
    MaterialBibliografico obtenido = sessionFactory
      .getCurrentSession()
      .get(MaterialBibliografico.class, material.getId());
    assertNotNull(obtenido);
    assertEquals("archivo.pdf", obtenido.getNombreArchivo());
    assertEquals("clave-guardar", obtenido.getClaveR2());
    assertEquals(Long.valueOf(1024L), obtenido.getTamanio());
    assertEquals(materia.getId(), obtenido.getMateria().getId());
  }

  @Test
  public void buscarPorIdDeberiaDevolverElMaterialGuardado() {
    Materia materia = dadoQueExisteUnaMateria("Materia para buscar");
    MaterialBibliografico material = dadoQueExisteUnMaterial("buscar.pdf", "clave-buscar", materia);
    prepararLecturaDesdeBaseDeDatos();

    MaterialBibliografico obtenido = repositorioMaterial.buscarPorId(material.getId());

    assertNotNull(obtenido);
    assertEquals(material.getId(), obtenido.getId());
    assertEquals("buscar.pdf", obtenido.getNombreArchivo());
    assertEquals("clave-buscar", obtenido.getClaveR2());
  }

  @Test
  public void buscarPorIdDeberiaDevolverNullSiNoExiste() {
    MaterialBibliografico obtenido = repositorioMaterial.buscarPorId(-1);

    assertNull(obtenido);
  }

  @Test
  public void buscarPorMateriaDeberiaFiltrarYOrdenarDelMasNuevoAlMasAntiguo() {
    Materia materia = dadoQueExisteUnaMateria("Materia consultada");
    Materia otraMateria = dadoQueExisteUnaMateria("Otra materia");
    MaterialBibliografico antiguo = dadoQueExisteUnMaterial(
      "antiguo.pdf",
      "clave-antiguo",
      materia
    );
    MaterialBibliografico nuevo = dadoQueExisteUnMaterial("nuevo.pdf", "clave-nuevo", materia);
    MaterialBibliografico intermedio = dadoQueExisteUnMaterial("medio.pdf", "clave-medio", materia);
    dadoQueExisteUnMaterial("ajeno.pdf", "clave-ajeno", otraMateria);
    dadoQueElMaterialTieneFecha(antiguo, LocalDateTime.of(2026, 1, 1, 10, 0));
    dadoQueElMaterialTieneFecha(nuevo, LocalDateTime.of(2026, 1, 3, 10, 0));
    dadoQueElMaterialTieneFecha(intermedio, LocalDateTime.of(2026, 1, 2, 10, 0));
    prepararLecturaDesdeBaseDeDatos();

    List<MaterialBibliografico> obtenidos = repositorioMaterial.buscarPorMateria(materia.getId());

    assertEquals(3, obtenidos.size());
    assertEquals(nuevo.getId(), obtenidos.get(0).getId());
    assertEquals(intermedio.getId(), obtenidos.get(1).getId());
    assertEquals(antiguo.getId(), obtenidos.get(2).getId());
    for (MaterialBibliografico obtenido : obtenidos) {
      assertEquals(materia.getId(), obtenido.getMateria().getId());
    }
  }

  @Test
  public void buscarPorMateriaDeberiaDevolverListaVaciaSiNoTieneMateriales() {
    Materia materia = dadoQueExisteUnaMateria("Sin materiales");
    Materia otraMateria = dadoQueExisteUnaMateria("Con materiales");
    dadoQueExisteUnMaterial("otro.pdf", "clave-otro", otraMateria);
    prepararLecturaDesdeBaseDeDatos();

    List<MaterialBibliografico> obtenidos = repositorioMaterial.buscarPorMateria(materia.getId());

    assertTrue(obtenidos.isEmpty());
  }

  @Test
  public void eliminarDeberiaBorrarSoloElMaterialIndicado() {
    Materia materia = dadoQueExisteUnaMateria("Materia para eliminar");
    MaterialBibliografico eliminado = dadoQueExisteUnMaterial(
      "borrar.pdf",
      "clave-borrar",
      materia
    );
    MaterialBibliografico conservado = dadoQueExisteUnMaterial(
      "conservar.pdf",
      "clave-conservar",
      materia
    );
    sessionFactory.getCurrentSession().flush();

    repositorioMaterial.eliminar(eliminado);
    prepararLecturaDesdeBaseDeDatos();

    assertNull(
      sessionFactory.getCurrentSession().get(MaterialBibliografico.class, eliminado.getId())
    );
    assertNotNull(
      sessionFactory.getCurrentSession().get(MaterialBibliografico.class, conservado.getId())
    );
    assertNotNull(sessionFactory.getCurrentSession().get(Materia.class, materia.getId()));
  }

  @Test
  public void eliminarPorMateriaDeberiaBorrarTodosSusMaterialesYConservarLosAjenos() {
    Materia materia = dadoQueExisteUnaMateria("Materia a vaciar");
    Materia otraMateria = dadoQueExisteUnaMateria("Materia a conservar");
    MaterialBibliografico primero = dadoQueExisteUnMaterial("uno.pdf", "clave-uno", materia);
    MaterialBibliografico segundo = dadoQueExisteUnMaterial("dos.pdf", "clave-dos", materia);
    MaterialBibliografico ajeno = dadoQueExisteUnMaterial(
      "ajeno.pdf",
      "clave-ajeno-borrado",
      otraMateria
    );
    sessionFactory.getCurrentSession().flush();

    repositorioMaterial.eliminarPorMateria(materia.getId());
    // El DELETE HQL masivo no actualiza los objetos que siguen en la sesion.
    sessionFactory.getCurrentSession().clear();

    assertNull(
      sessionFactory.getCurrentSession().get(MaterialBibliografico.class, primero.getId())
    );
    assertNull(
      sessionFactory.getCurrentSession().get(MaterialBibliografico.class, segundo.getId())
    );
    assertNotNull(
      sessionFactory.getCurrentSession().get(MaterialBibliografico.class, ajeno.getId())
    );
    assertNotNull(sessionFactory.getCurrentSession().get(Materia.class, materia.getId()));
    assertTrue(repositorioMaterial.buscarPorMateria(materia.getId()).isEmpty());
  }

  @Test
  public void eliminarPorMateriaSinMaterialesNoDeberiaAfectarOtrosMateriales() {
    Materia vacia = dadoQueExisteUnaMateria("Materia vacia");
    Materia otraMateria = dadoQueExisteUnaMateria("Materia con archivo");
    MaterialBibliografico ajeno = dadoQueExisteUnMaterial(
      "archivo.pdf",
      "clave-ajeno-vacia",
      otraMateria
    );
    sessionFactory.getCurrentSession().flush();

    repositorioMaterial.eliminarPorMateria(vacia.getId());
    sessionFactory.getCurrentSession().clear();

    assertTrue(repositorioMaterial.buscarPorMateria(vacia.getId()).isEmpty());
    assertNotNull(
      sessionFactory.getCurrentSession().get(MaterialBibliografico.class, ajeno.getId())
    );
  }

  // DADO QUE
  private Materia dadoQueExisteUnaMateria(String nombre) {
    Materia materia = new Materia();
    materia.setNombre(nombre);
    sessionFactory.getCurrentSession().persist(materia);
    return materia;
  }

  private MaterialBibliografico dadoQueTengoUnMaterial(
    String nombre,
    String clave,
    Materia materia
  ) {
    return new MaterialBibliografico(nombre, "application/pdf", 1024L, clave, materia);
  }

  private MaterialBibliografico dadoQueExisteUnMaterial(
    String nombre,
    String clave,
    Materia materia
  ) {
    MaterialBibliografico material = dadoQueTengoUnMaterial(nombre, clave, materia);
    sessionFactory.getCurrentSession().persist(material);
    return material;
  }

  private void dadoQueElMaterialTieneFecha(MaterialBibliografico material, LocalDateTime fecha) {
    ReflectionTestUtils.setField(material, "fechaSubida", fecha);
  }

  private void prepararLecturaDesdeBaseDeDatos() {
    sessionFactory.getCurrentSession().flush();
    sessionFactory.getCurrentSession().clear();
  }
}
