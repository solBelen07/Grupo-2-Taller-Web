package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.materia.ServicioMateriaImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioMateriaTest {

  private ServicioMateria servicioMateria;
  private RepositorioMateria repositorioMateriaMock;

  @BeforeEach
  public void init() {
    this.repositorioMateriaMock = mock(RepositorioMateria.class);
    this.servicioMateria = new ServicioMateriaImpl(this.repositorioMateriaMock);
  }

  /*@Test
  public void queSePuedaCrearUnaMateria() {
    Materia materia = new Materia();
    materia.setNombre("Taller Web I");
    materia.setDocente("Spizzirri");

    servicioMateria.crearMateria(materia);

    verify(repositorioMateriaMock, times(1)).guardar(materia);
  }*/

  @Test
  public void consultarMateriaDeberiaLlamarAlRepositorioMateria() {
    String nombre = "Taller Web I";

    Materia materiaEsperada = new Materia();
    when(this.repositorioMateriaMock.buscarPorNombre(nombre)).thenReturn(materiaEsperada);

    Materia materiaObtenida = this.servicioMateria.buscarMateriaPorNombre(nombre);

    // validacion
    assertThat(materiaObtenida, equalTo(materiaEsperada));
    verify(this.repositorioMateriaMock, times(1)).buscarPorNombre(nombre);
  }

  @Test
  public void queSePuedanObtenerTodasLasMaterias() {
    Materia materia1 = new Materia();
    Materia materia2 = new Materia();
    Materia materia3 = new Materia();
    Materia materia4 = new Materia();

    materia1.setNombre("Taller Web I");
    materia2.setNombre("Base de Datos II");
    materia3.setNombre("Visualización e Interfaces");
    materia4.setNombre("Programación Web II");

    List<Materia> materiasEsperadas = List.of(materia1, materia2, materia3, materia4);

    when(this.repositorioMateriaMock.obtenerTodas()).thenReturn(materiasEsperadas);

    List<Materia> materiasObtenidas = servicioMateria.obtenerMaterias();

    // validacion
    assertThat(materiasObtenidas, equalTo(materiasEsperadas));
    assertEquals(4, materiasObtenidas.size());
    assertEquals("Taller Web I", materiasObtenidas.get(0).getNombre());
    assertEquals("Base de Datos II", materiasObtenidas.get(1).getNombre());
    assertEquals("Visualización e Interfaces", materiasObtenidas.get(2).getNombre());
    assertEquals("Programación Web II", materiasObtenidas.get(3).getNombre());
  }

  @Test
  public void queSePuedaEditarElNombreDeUnaMateria() {
    Materia materia1 = new Materia();

    materia1.setNombre("Taller Web I");

    materia1.setNombre("Base de Datos II");

    servicioMateria.editarMateria(materia1);

    assertThat(materia1.getNombre(), equalTo("Base de Datos II"));
    verify(this.repositorioMateriaMock, times(1)).modificar(materia1);
  }

  @Test
  public void queSePuedaEliminarUnaMateriaQueExiste() {
    String nombre1 = "Taller Web I";
    String nombre2 = "Base de Datos II";

    Materia materia1 = new Materia();
    Materia materia2 = new Materia();

    materia1.setNombre(nombre1);
    materia2.setNombre(nombre2);

    when(this.repositorioMateriaMock.buscarPorNombre(nombre2)).thenReturn(materia2);

    servicioMateria.eliminarMateria(nombre2);

    verify(this.repositorioMateriaMock, times(1)).buscarPorNombre(nombre2);
    verify(this.repositorioMateriaMock, times(1)).eliminar(materia2);
  }
}
