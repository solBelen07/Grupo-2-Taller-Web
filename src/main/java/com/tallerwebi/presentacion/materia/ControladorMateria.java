package com.tallerwebi.presentacion.materia;

import com.tallerwebi.dominio.excepcion.excepcionMateria.MateriaExistente;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.ServicioMateria;
import com.tallerwebi.dominio.materialBibliografico.MaterialBibliografico;
import com.tallerwebi.dominio.materialBibliografico.ServicioMaterial;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorMateria {

  private static final String DATOS_MATERIA = "datosMateria";
  private final ServicioMateria servicioMateria;
  private final ServicioMaterial servicioMaterial;

  public ControladorMateria(ServicioMateria servicioMateria, ServicioMaterial servicioMaterial) {
    this.servicioMateria = servicioMateria;
    this.servicioMaterial = servicioMaterial;
  }

  @RequestMapping(path = "/materias/nueva", method = RequestMethod.GET)
  public ModelAndView nuevaMateria() {
    Map<String, Object> model = new ModelMap();

    model.put(DATOS_MATERIA, new DatosMateria());

    return new ModelAndView("paginas/materia/materia-formulario", model);
  }

  @RequestMapping(path = "/materias/guardar", method = RequestMethod.POST)
  public ModelAndView guardarMateria(
    @ModelAttribute(DATOS_MATERIA) DatosMateria datosMateria,
    @RequestParam(value = "archivos", required = false) MultipartFile[] archivos
  ) throws IOException {
    Materia materia = new Materia();

    materia.setNombre(datosMateria.getNombre());
    materia.setDescripcion(datosMateria.getDescripcion());
    materia.setDocente(datosMateria.getDocente());
    materia.setColor(datosMateria.getColor());
    materia.setDias(datosMateria.getDias());
    materia.setHorario(datosMateria.getHorario());
    materia.setMaterialBibliografico(datosMateria.getMaterialBibliografico());

    try {
      servicioMateria.crearMateria(materia);
    } catch (MateriaExistente e) {
      Map<String, Object> model = new ModelMap();

      model.put("error", "Ya existe una materia con ese nombre");
      model.put(DATOS_MATERIA, datosMateria);

      return new ModelAndView("paginas/materia/materia-formulario", model);
    }

    agregarArchivos(materia.getId(), archivos);

    return new ModelAndView("redirect:/materias");
  }

  @RequestMapping(path = "/materias", method = RequestMethod.GET)
  public ModelAndView listarMaterias() {
    Map<String, Object> model = new ModelMap();

    model.put("materias", servicioMateria.obtenerMaterias());

    return new ModelAndView("paginas/materia/materias", model);
  }

  @RequestMapping(path = "/materias/detalle", method = RequestMethod.GET)
  public ModelAndView verMateria(@RequestParam("id") Integer id) {
    Materia materia = servicioMateria.buscarMateriaPorId(id);

    Map<String, Object> model = new ModelMap();

    model.put("materia", materia);

    model.put("materiales", servicioMaterial.obtenerPorMateria(id));

    return new ModelAndView("paginas/materia/materia-detalle", model);
  }

  @RequestMapping(path = "/materias/editar", method = RequestMethod.GET)
  public ModelAndView editarMateria(@RequestParam("id") Integer id) {
    Materia materia = servicioMateria.buscarMateriaPorId(id);

    DatosMateria datosMateria = new DatosMateria();

    datosMateria.setId(materia.getId());
    datosMateria.setNombre(materia.getNombre());
    datosMateria.setDescripcion(materia.getDescripcion());
    datosMateria.setDocente(materia.getDocente());
    datosMateria.setColor(materia.getColor());
    datosMateria.setDias(materia.getDias());
    datosMateria.setHorario(materia.getHorario());
    datosMateria.setMaterialBibliografico(materia.getMaterialBibliografico());

    Map<String, Object> model = new ModelMap();
    model.put(DATOS_MATERIA, datosMateria);
    model.put("materiales", servicioMaterial.obtenerPorMateria(id));

    return new ModelAndView("paginas/materia/materia-formulario", model);
  }

  @RequestMapping(path = "/materias/modificar", method = RequestMethod.POST)
  public ModelAndView guardarMateriaModificada(
    @ModelAttribute(DATOS_MATERIA) DatosMateria datosMateria,
    @RequestParam(value = "archivos", required = false) MultipartFile[] archivos
  ) throws IOException {
    Materia materia = new Materia();

    materia.setId(datosMateria.getId());
    materia.setNombre(datosMateria.getNombre());
    materia.setDescripcion(datosMateria.getDescripcion());
    materia.setDocente(datosMateria.getDocente());
    materia.setColor(datosMateria.getColor());
    materia.setDias(datosMateria.getDias());
    materia.setHorario(datosMateria.getHorario());
    materia.setMaterialBibliografico(datosMateria.getMaterialBibliografico());

    servicioMateria.editarMateria(materia);

    agregarArchivos(materia.getId(), archivos);

    return new ModelAndView("redirect:/materias");
  }

  @RequestMapping(path = "/materias/buscar", method = RequestMethod.GET)
  public ModelAndView buscarMateria(@RequestParam("nombre") String nombre) {
    Materia materia = servicioMateria.buscarMateriaPorNombre(nombre);

    Map<String, Object> model = new ModelMap();

    model.put("materiaBuscada", materia);
    model.put("nombreBuscado", nombre);

    return new ModelAndView("paginas/materia/materias", model);
  }

  @GetMapping("/materias/material/{id}")
  public ResponseEntity<byte[]> verMaterial(@PathVariable("id") Integer id) {
    MaterialBibliografico material = servicioMaterial.buscarPorId(id);

    if (material == null) {
      return ResponseEntity.notFound().build();
    }

    byte[] contenido = servicioMaterial.descargarMaterial(id);
    String nombre = material.getNombreArchivo();
    boolean esPdf = nombre != null && nombre.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf");
    MediaType tipoContenido = esPdf
      ? MediaType.APPLICATION_PDF
      : MediaType.APPLICATION_OCTET_STREAM;

    String disposicion = ContentDisposition
      .builder(esPdf ? "inline" : "attachment")
      .filename(nombre, StandardCharsets.UTF_8)
      .build()
      .toString();

    return ResponseEntity
      .ok()
      .header(HttpHeaders.CONTENT_DISPOSITION, disposicion)
      .header("X-Content-Type-Options", "nosniff")
      .contentType(tipoContenido)
      .contentLength(contenido.length)
      .body(contenido);
  }

  @PostMapping("/materias/material/eliminar")
  public ModelAndView eliminarMaterial(@RequestParam("id") Integer id) {
    MaterialBibliografico material = servicioMaterial.buscarPorId(id);

    if (material == null) {
      return new ModelAndView("redirect:/materias");
    }

    Integer materiaId = material.getMateria().getId();
    servicioMaterial.eliminarMaterial(id);
    return new ModelAndView("redirect:/materias/detalle?id=" + materiaId);
  }

  private void agregarArchivos(Integer materiaId, MultipartFile[] archivos) throws IOException {
    if (archivos == null) {
      return;
    }

    for (MultipartFile archivo : archivos) {
      if (archivo != null && !archivo.isEmpty()) {
        servicioMaterial.subirMaterial(materiaId, archivo);
      }
    }
  }
}
