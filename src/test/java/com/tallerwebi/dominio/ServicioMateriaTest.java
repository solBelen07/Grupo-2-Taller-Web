package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.excepcionMateria.MateriaExistente;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.materia.ServicioMateriaImpl;
import com.tallerwebi.dominio.materialBibliografico.RepositorioMaterial;
import com.tallerwebi.dominio.parcial.RepositorioParcial;
import com.tallerwebi.dominio.sesionEstudio.RepositorioSesionEstudio;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

public class ServicioMateriaTest {

  private ServicioMateria servicioMateria;
  private RepositorioMateria repositorioMateriaMock;
  private RepositorioEvento repositorioEventoMock;
  private RepositorioParcial repositorioParcialMock;
  private RepositorioSesionEstudio repositorioSesionEstudioMock;
  private RepositorioMaterial repositorioMaterialMock;

  @BeforeEach
  public void init() {
    this.repositorioMateriaMock = mock(RepositorioMateria.class);
    this.repositorioEventoMock = mock(RepositorioEvento.class);
    this.repositorioParcialMock = mock(RepositorioParcial.class);
    this.repositorioSesionEstudioMock = mock(RepositorioSesionEstudio.class);
    this.repositorioMaterialMock = mock(RepositorioMaterial.class);
    this.servicioMateria =
      new ServicioMateriaImpl(
        this.repositorioMateriaMock,
        this.repositorioEventoMock,
        this.repositorioParcialMock,
        this.repositorioSesionEstudioMock,
        this.repositorioMaterialMock
      );
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
  public void crearMateriaQueNoExisteDeberiaGuardarla() throws MateriaExistente {
    Materia materia = new Materia();
    materia.setNombre("Taller Web I");
    when(this.repositorioMateriaMock.buscarPorNombre("Taller Web I")).thenReturn(null);

    this.servicioMateria.crearMateria(materia);

    verify(this.repositorioMateriaMock, times(1)).guardar(materia);
  }

  @Test
  public void crearMateriaExistenteDeberiaLanzarExcepcionYNoGuardar() {
    Materia materia = new Materia();
    materia.setNombre("Taller Web I");
    when(this.repositorioMateriaMock.buscarPorNombre("Taller Web I")).thenReturn(new Materia());

    assertThrows(MateriaExistente.class, () -> this.servicioMateria.crearMateria(materia));

    verify(this.repositorioMateriaMock, never()).guardar(any());
  }

  @Test
  public void obtenerMateriasPorUsuarioDeberiaDevolverLasDelRepositorio() {
    List<Materia> esperadas = List.of(new Materia(), new Materia());
    when(this.repositorioMateriaMock.obtenerPorUsuario(1L)).thenReturn(esperadas);

    List<Materia> obtenidas = this.servicioMateria.obtenerMateriasPorUsuario(1L);

    assertThat(obtenidas, equalTo(esperadas));
    verify(this.repositorioMateriaMock, times(1)).obtenerPorUsuario(1L);
  }

  @Test
  public void buscarMateriaPorNombreYUsuarioDeberiaDevolverLaDelRepositorio() {
    Materia esperada = new Materia();
    when(this.repositorioMateriaMock.buscarPorNombreYUsuario("Taller Web I", 1L))
      .thenReturn(esperada);

    Materia obtenida = this.servicioMateria.buscarMateriaPorNombreYUsuario("Taller Web I", 1L);

    assertThat(obtenida, equalTo(esperada));
  }

  @Test
  public void buscarMateriaPorIdDeberiaDevolverLaDelRepositorio() {
    Materia esperada = new Materia();
    when(this.repositorioMateriaMock.buscarPorId(10)).thenReturn(esperada);

    Materia obtenida = this.servicioMateria.buscarMateriaPorId(10);

    assertThat(obtenida, equalTo(esperada));
  }

  @Test
  public void buscarMateriaPorIdYUsuarioDeberiaDevolverLaDelRepositorio() {
    Materia esperada = new Materia();
    when(this.repositorioMateriaMock.buscarPorIdYUsuario(10, 1L)).thenReturn(esperada);

    Materia obtenida = this.servicioMateria.buscarMateriaPorIdYUsuario(10, 1L);

    assertThat(obtenida, equalTo(esperada));
  }

  @Test
  public void eliminarMateriaDeberiaBorrarDependenciasAntesQueLaMateria() {
    Materia materia = new Materia();
    materia.setId(10);
    materia.setNombre("Taller Web I");
    when(this.repositorioMateriaMock.buscarPorNombre("Taller Web I")).thenReturn(materia);

    this.servicioMateria.eliminarMateria("Taller Web I");

    InOrder orden = inOrder(
      this.repositorioSesionEstudioMock,
      this.repositorioParcialMock,
      this.repositorioEventoMock,
      this.repositorioMaterialMock,
      this.repositorioMateriaMock
    );
    orden.verify(this.repositorioSesionEstudioMock).eliminarPorMateriaId(10);
    orden.verify(this.repositorioParcialMock).eliminarPorMateriaId(10);
    orden.verify(this.repositorioEventoMock).eliminarPorMateriaId(10);
    orden.verify(this.repositorioMaterialMock).eliminarPorMateria(10);
    orden.verify(this.repositorioMateriaMock).eliminar(materia);
  }

  @Test
  public void eliminarMateriaInexistenteNoDeberiaBorrarNada() {
    when(this.repositorioMateriaMock.buscarPorNombre("Inexistente")).thenReturn(null);

    this.servicioMateria.eliminarMateria("Inexistente");

    verify(this.repositorioMateriaMock, never()).eliminar(any());
    verifyNoInteractions(
      this.repositorioSesionEstudioMock,
      this.repositorioParcialMock,
      this.repositorioEventoMock,
      this.repositorioMaterialMock
    );
  }
}
