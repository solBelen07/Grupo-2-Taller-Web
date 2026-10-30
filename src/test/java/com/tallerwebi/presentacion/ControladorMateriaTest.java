package com.tallerwebi.presentacion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionMateria.MateriaExistente;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.materialBibliografico.MaterialBibliografico;
import com.tallerwebi.dominio.materialBibliografico.ServicioMaterial;
import com.tallerwebi.presentacion.materia.ControladorMateria;
import com.tallerwebi.presentacion.materia.DatosMateria;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

public class ControladorMateriaTest {

  private ServicioMateria servicioMateriaMock;
  private ServicioMaterial servicioMaterialMock;
  private ControladorMateria controladorMateria;
  private Usuario usuarioMock;
  private MockHttpServletRequest request;

  @BeforeEach
  public void setUp() {
    servicioMateriaMock = mock(ServicioMateria.class);
    servicioMaterialMock = mock(ServicioMaterial.class);

    controladorMateria = new ControladorMateria(servicioMateriaMock, servicioMaterialMock);

    usuarioMock = mock(Usuario.class);
    when(usuarioMock.getId()).thenReturn(1L);

    request = new MockHttpServletRequest();
    request.getSession().setAttribute("USUARIO", usuarioMock);
  }

  private Materia crearMateriaDePrueba(Integer id, String nombre) {
    Materia materia = new Materia();
    materia.setId(id);
    materia.setNombre(nombre);
    materia.setUsuarios(List.of(usuarioMock));
    return materia;
  }

  @Test
  public void queSePuedaCrearUnaMateriaSinArchivos() throws IOException, MateriaExistente {
    DatosMateria datos = new DatosMateria();
    datos.setNombre("Taller Web I");

    ModelAndView resultado = controladorMateria.guardarMateria(datos, null, request);

    verify(servicioMateriaMock)
      .crearMateria(
        argThat(materia ->
          materia.getUsuarios() != null &&
          materia.getUsuarios().stream().anyMatch(usuario -> usuario.getId().equals(1L))
        )
      );
    verifyNoInteractions(servicioMaterialMock);
    assertEquals("redirect:/materias", resultado.getViewName());
  }

  @Test
  public void queSePuedaCrearUnaMateriaConUnArchivo() throws IOException, MateriaExistente {
    DatosMateria datos = new DatosMateria();
    datos.setNombre("Taller Web I");

    MultipartFile archivo = new MockMultipartFile(
      "archivos",
      "unidad1.pdf",
      "application/pdf",
      "Contenido de prueba".getBytes(StandardCharsets.UTF_8)
    );

    doAnswer(invocacion -> {
        Materia materia = invocacion.getArgument(0);
        materia.setId(1);
        return null;
      })
      .when(servicioMateriaMock)
      .crearMateria(any(Materia.class));

    ModelAndView resultado = controladorMateria.guardarMateria(
      datos,
      new MultipartFile[] { archivo },
      request
    );

    verify(servicioMateriaMock).crearMateria(any(Materia.class));
    verify(servicioMaterialMock).subirMaterial(1, archivo);
    assertEquals("redirect:/materias", resultado.getViewName());
  }

  @Test
  public void queSePuedaCrearUnaMateriaConVariosArchivos() throws IOException, MateriaExistente {
    DatosMateria datos = new DatosMateria();
    datos.setNombre("Base de Datos II");

    MultipartFile archivo1 = new MockMultipartFile(
      "archivos",
      "unidad1.pdf",
      "application/pdf",
      "Archivo 1".getBytes(StandardCharsets.UTF_8)
    );

    MultipartFile archivo2 = new MockMultipartFile(
      "archivos",
      "unidad2.pdf",
      "application/pdf",
      "Archivo 2".getBytes(StandardCharsets.UTF_8)
    );

    doAnswer(invocacion -> {
        Materia materia = invocacion.getArgument(0);
        materia.setId(5);
        return null;
      })
      .when(servicioMateriaMock)
      .crearMateria(any(Materia.class));

    controladorMateria.guardarMateria(datos, new MultipartFile[] { archivo1, archivo2 }, request);

    verify(servicioMaterialMock).subirMaterial(5, archivo1);
    verify(servicioMaterialMock).subirMaterial(5, archivo2);
    verify(servicioMaterialMock, times(2)).subirMaterial(eq(5), any(MultipartFile.class));
  }

  @Test
  public void queNoSeSubanArchivosVacios() throws IOException, MateriaExistente {
    DatosMateria datos = new DatosMateria();
    datos.setNombre("Taller Web I");

    MultipartFile archivoVacio = new MockMultipartFile(
      "archivos",
      "",
      "application/pdf",
      new byte[0]
    );

    controladorMateria.guardarMateria(datos, new MultipartFile[] { archivoVacio }, request);

    verify(servicioMateriaMock).crearMateria(any(Materia.class));
    verifyNoInteractions(servicioMaterialMock);
  }

  @Test
  public void queNoSeSubanArchivosSiLaMateriaYaExiste() throws IOException, MateriaExistente {
    DatosMateria datos = new DatosMateria();
    datos.setNombre("Taller Web I");

    MultipartFile archivo = new MockMultipartFile(
      "archivos",
      "unidad1.pdf",
      "application/pdf",
      "Contenido".getBytes(StandardCharsets.UTF_8)
    );

    doThrow(new MateriaExistente()).when(servicioMateriaMock).crearMateria(any(Materia.class));

    ModelAndView resultado = controladorMateria.guardarMateria(
      datos,
      new MultipartFile[] { archivo },
      request
    );

    verifyNoInteractions(servicioMaterialMock);
    assertEquals("paginas/materia/materia-formulario", resultado.getViewName());
    assertNotNull(resultado.getModel().get("error"));
  }

  @Test
  public void queSePuedaEditarMateriaSinAgregarArchivos() throws IOException {
    DatosMateria datos = new DatosMateria();
    datos.setId(3);
    datos.setNombre("Taller Web I");

    Materia materia = crearMateriaDePrueba(3, "Taller Web I");

    when(servicioMateriaMock.buscarMateriaPorIdYUsuario(3, 1L)).thenReturn(materia);

    ModelAndView resultado = controladorMateria.guardarMateriaModificada(datos, null, request);

    verify(servicioMateriaMock).editarMateria(materia);
    verifyNoInteractions(servicioMaterialMock);
    assertEquals("redirect:/materias", resultado.getViewName());
  }

  @Test
  public void queSePuedanAgregarVariosArchivosAlEditar() throws IOException {
    DatosMateria datos = new DatosMateria();
    datos.setId(3);
    datos.setNombre("Taller Web I");

    MultipartFile archivo1 = new MockMultipartFile(
      "archivos",
      "apuntes.pdf",
      "application/pdf",
      "Apuntes".getBytes(StandardCharsets.UTF_8)
    );

    MultipartFile archivo2 = new MockMultipartFile(
      "archivos",
      "ejercicios.pdf",
      "application/pdf",
      "Ejercicios".getBytes(StandardCharsets.UTF_8)
    );

    Materia materia = crearMateriaDePrueba(3, "Taller Web I");

    when(servicioMateriaMock.buscarMateriaPorIdYUsuario(3, 1L)).thenReturn(materia);

    controladorMateria.guardarMateriaModificada(
      datos,
      new MultipartFile[] { archivo1, archivo2 },
      request
    );

    verify(servicioMateriaMock).editarMateria(materia);
    verify(servicioMaterialMock).subirMaterial(3, archivo1);
    verify(servicioMaterialMock).subirMaterial(3, archivo2);
    verify(servicioMaterialMock, times(2)).subirMaterial(eq(3), any(MultipartFile.class));
  }

  @Test
  public void queSeMuestrenLosMaterialesEnElDetalle() {
    Materia materia = crearMateriaDePrueba(2, "Taller Web I");

    MaterialBibliografico material = new MaterialBibliografico();
    material.setId(10);
    material.setNombreArchivo("unidad1.pdf");
    material.setMateria(materia);

    when(servicioMateriaMock.buscarMateriaPorIdYUsuario(2, 1L)).thenReturn(materia);
    when(servicioMaterialMock.obtenerPorMateria(2)).thenReturn(List.of(material));

    ModelAndView resultado = controladorMateria.verMateria(2, request);

    assertEquals("paginas/materia/materia-detalle", resultado.getViewName());
    assertEquals(materia, resultado.getModel().get("materia"));
    assertEquals(List.of(material), resultado.getModel().get("materiales"));
  }

  @Test
  public void queSeMuestrenLosArchivosAlEditarUnaMateria() {
    Materia materia = crearMateriaDePrueba(3, "Taller Web I");

    MaterialBibliografico material = new MaterialBibliografico();
    material.setId(8);
    material.setNombreArchivo("apuntes.pdf");
    material.setMateria(materia);

    when(servicioMateriaMock.buscarMateriaPorIdYUsuario(3, 1L)).thenReturn(materia);
    when(servicioMaterialMock.obtenerPorMateria(3)).thenReturn(List.of(material));

    ModelAndView resultado = controladorMateria.editarMateria(3, request);

    assertEquals("paginas/materia/materia-formulario", resultado.getViewName());
    assertEquals(List.of(material), resultado.getModel().get("materiales"));
  }

  @Test
  public void queSePuedaVisualizarUnPdf() {
    Materia materia = crearMateriaDePrueba(1, "Taller Web I");

    MaterialBibliografico material = new MaterialBibliografico();
    material.setId(10);
    material.setNombreArchivo("unidad1.pdf");
    material.setTipoArchivo("application/pdf");
    material.setMateria(materia);

    byte[] contenido = "Contenido de prueba".getBytes(StandardCharsets.UTF_8);

    when(servicioMaterialMock.buscarPorId(10)).thenReturn(material);
    when(servicioMaterialMock.descargarMaterial(10)).thenReturn(contenido);

    ResponseEntity<byte[]> respuesta = controladorMateria.verMaterial(10, request);

    assertEquals(200, respuesta.getStatusCode().value());
    assertEquals(MediaType.APPLICATION_PDF, respuesta.getHeaders().getContentType());
    assertArrayEquals(contenido, respuesta.getBody());
    assertTrue(respuesta.getHeaders().getFirst("Content-Disposition").startsWith("inline"));
    verify(servicioMaterialMock).descargarMaterial(10);
  }

  @Test
  public void queDevuelva404SiElMaterialNoExiste() {
    when(servicioMaterialMock.buscarPorId(99)).thenReturn(null);

    ResponseEntity<byte[]> respuesta = controladorMateria.verMaterial(99, request);

    assertEquals(404, respuesta.getStatusCode().value());
    verify(servicioMaterialMock, never()).descargarMaterial(anyInt());
  }

  @Test
  public void queUnDocumentoWordSeDescargue() {
    Materia materia = crearMateriaDePrueba(1, "Taller Web I");

    MaterialBibliografico material = new MaterialBibliografico();
    material.setId(20);
    material.setNombreArchivo("ejercicios.docx");
    material.setMateria(materia);

    byte[] contenido = "Prueba".getBytes(StandardCharsets.UTF_8);

    when(servicioMaterialMock.buscarPorId(20)).thenReturn(material);
    when(servicioMaterialMock.descargarMaterial(20)).thenReturn(contenido);

    ResponseEntity<byte[]> respuesta = controladorMateria.verMaterial(20, request);

    assertEquals(MediaType.APPLICATION_OCTET_STREAM, respuesta.getHeaders().getContentType());
    assertTrue(respuesta.getHeaders().getFirst("Content-Disposition").startsWith("attachment"));
  }

  @Test
  public void queSePuedaEliminarUnMaterial() {
    Materia materia = crearMateriaDePrueba(3, "Taller Web I");

    MaterialBibliografico material = new MaterialBibliografico();
    material.setId(10);
    material.setNombreArchivo("unidad1.pdf");
    material.setMateria(materia);

    when(servicioMaterialMock.buscarPorId(10)).thenReturn(material);

    ModelAndView resultado = controladorMateria.eliminarMaterial(10, request);

    verify(servicioMaterialMock).eliminarMaterial(10);
    assertEquals("redirect:/materias/detalle?id=3", resultado.getViewName());
  }

  @Test
  public void queNoSeElimineUnMaterialInexistente() {
    when(servicioMaterialMock.buscarPorId(99)).thenReturn(null);

    ModelAndView resultado = controladorMateria.eliminarMaterial(99, request);

    verify(servicioMaterialMock, never()).eliminarMaterial(anyInt());
    assertEquals("redirect:/materias", resultado.getViewName());
  }

  @Test
  public void queSeMuestreElFormularioParaCrearUnaMateria() {
    ModelAndView resultado = controladorMateria.nuevaMateria(request);

    assertEquals("paginas/materia/materia-formulario", resultado.getViewName());
    assertInstanceOf(DatosMateria.class, resultado.getModel().get("datosMateria"));
  }

  @Test
  public void queSePuedanListarMaterias() {
    Materia materia = crearMateriaDePrueba(1, "Taller Web I");

    when(servicioMateriaMock.obtenerMateriasPorUsuario(1L)).thenReturn(List.of(materia));

    ModelAndView resultado = controladorMateria.listarMaterias(request);

    assertEquals("paginas/materia/materias", resultado.getViewName());
    assertEquals(List.of(materia), resultado.getModel().get("materias"));
    verify(servicioMateriaMock).obtenerMateriasPorUsuario(1L);
  }

  @Test
  public void queSePuedaBuscarUnaMateriaPorNombre() {
    Materia materia = crearMateriaDePrueba(1, "Taller Web I");

    when(servicioMateriaMock.buscarMateriaPorNombreYUsuario("Taller Web I", 1L))
      .thenReturn(materia);

    when(servicioMateriaMock.obtenerMateriasPorUsuario(1L)).thenReturn(List.of(materia));

    ModelAndView resultado = controladorMateria.buscarMateria("Taller Web I", request);

    assertEquals("paginas/materia/materias", resultado.getViewName());
    assertSame(materia, resultado.getModel().get("materiaBuscada"));
    assertEquals("Taller Web I", resultado.getModel().get("nombreBuscado"));
  }
}
