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
import com.tallerwebi.dominio.parcial.RepositorioParcial;
import com.tallerwebi.dominio.sesionEstudio.RepositorioSesionEstudio;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioMateriaTest {

  private ServicioMateria servicioMateria;
  private RepositorioMateria repositorioMateriaMock;
  private RepositorioEvento repositorioEventoMock;
  private RepositorioParcial repositorioParcialMock;
  private RepositorioSesionEstudio repositorioSesionEstudioMock;

  @BeforeEach
  public void init() {
    this.repositorioMateriaMock = mock(RepositorioMateria.class);
    this.repositorioEventoMock = mock(RepositorioEvento.class);
    this.repositorioParcialMock = mock(RepositorioParcial.class);
    this.repositorioSesionEstudioMock = mock(RepositorioSesionEstudio.class);
    this.servicioMateria =
      new ServicioMateriaImpl(
        this.repositorioMateriaMock,
        this.repositorioEventoMock,
        this.repositorioParcialMock,
        this.repositorioSesionEstudioMock
      );
  }

  @Test
  public void queSePuedaCrearUnaMateria() throws MateriaExistente {
    Materia materia = new Materia();
    materia.setNombre("Taller Web I");
    materia.setDocente("Spizzirri");

    when(this.repositorioMateriaMock.buscarPorNombre("Taller Web I")).thenReturn(null);

    servicioMateria.crearMateria(materia);

    verify(this.repositorioMateriaMock, times(1)).buscarPorNombre("Taller Web I");

    verify(this.repositorioMateriaMock, times(1)).guardar(materia);
  }

  @Test
  public void noDeberiaCrearUnaMateriaSiYaExiste() {
    String nombre = "Taller Web I";

    Materia materia = new Materia();
    materia.setNombre(nombre);

    Materia materiaExistente = new Materia();
    materiaExistente.setNombre(nombre);

    when(this.repositorioMateriaMock.buscarPorNombre(nombre)).thenReturn(materiaExistente);

    assertThrows(MateriaExistente.class, () -> this.servicioMateria.crearMateria(materia));
    verify(this.repositorioMateriaMock, times(1)).buscarPorNombre(nombre);
    verify(this.repositorioMateriaMock, never()).guardar(materia);
  }

  @Test
  public void queNoSePuedaCrearUnaMateriaSiYaExiste() {
    Materia materia = new Materia();
    materia.setNombre("Taller Web I");

    Materia materiaExistente = new Materia();
    materiaExistente.setNombre("Taller Web I");

    when(this.repositorioMateriaMock.buscarPorNombre("Taller Web I")).thenReturn(materiaExistente);

    assertThrows(MateriaExistente.class, () -> servicioMateria.crearMateria(materia));

    verify(this.repositorioMateriaMock, times(1)).buscarPorNombre("Taller Web I");

    verify(this.repositorioMateriaMock, never()).guardar(any(Materia.class));
  }

  @Test
  public void consultarMateriaDeberiaLlamarAlRepositorioMateria() {
    String nombre = "Taller Web I";

    Materia materiaEsperada = new Materia();

    when(this.repositorioMateriaMock.buscarPorNombre(nombre)).thenReturn(materiaEsperada);

    Materia materiaObtenida = this.servicioMateria.buscarMateriaPorNombre(nombre);

    assertThat(materiaObtenida, equalTo(materiaEsperada));

    verify(this.repositorioMateriaMock, times(1)).buscarPorNombre(nombre);
  }

  @Test
  public void queSePuedaBuscarUnaMateriaPorId() {
    Integer id = 1;

    Materia materiaEsperada = new Materia();
    materiaEsperada.setId(id);
    materiaEsperada.setNombre("Taller Web I");

    when(this.repositorioMateriaMock.buscarPorId(id)).thenReturn(materiaEsperada);

    Materia materiaObtenida = this.servicioMateria.buscarMateriaPorId(id);

    assertThat(materiaObtenida, equalTo(materiaEsperada));

    verify(this.repositorioMateriaMock, times(1)).buscarPorId(id);
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

    assertThat(materiasObtenidas, equalTo(materiasEsperadas));

    assertEquals(4, materiasObtenidas.size());

    assertEquals("Taller Web I", materiasObtenidas.get(0).getNombre());

    assertEquals("Base de Datos II", materiasObtenidas.get(1).getNombre());

    assertEquals("Visualización e Interfaces", materiasObtenidas.get(2).getNombre());

    assertEquals("Programación Web II", materiasObtenidas.get(3).getNombre());
  }

  @Test
  public void queSePuedaEditarElNombreDeUnaMateria() {
    Materia materia = new Materia();

    materia.setNombre("Taller Web I");

    materia.setNombre("Base de Datos II");

    servicioMateria.editarMateria(materia);

    assertThat(materia.getNombre(), equalTo("Base de Datos II"));

    verify(this.repositorioMateriaMock, times(1)).modificar(materia);
  }

  @Test
  public void queSePuedaEliminarUnaMateriaQueExiste() {
    String nombre = "Base de Datos II";

    Materia materia = new Materia();
    materia.setNombre(nombre);

    when(this.repositorioMateriaMock.buscarPorNombre(nombre)).thenReturn(materia);

    servicioMateria.eliminarMateria(nombre);

    verify(this.repositorioMateriaMock, times(1)).buscarPorNombre(nombre);

    verify(this.repositorioMateriaMock, times(1)).eliminar(materia);
  }

  @Test
  public void queNoSeElimineUnaMateriaQueNoExiste() {
    String nombre = "Materia inexistente";

    when(this.repositorioMateriaMock.buscarPorNombre(nombre)).thenReturn(null);

    servicioMateria.eliminarMateria(nombre);

    verify(this.repositorioMateriaMock, times(1)).buscarPorNombre(nombre);

    verify(this.repositorioMateriaMock, never()).eliminar(any(Materia.class));
  }
}
