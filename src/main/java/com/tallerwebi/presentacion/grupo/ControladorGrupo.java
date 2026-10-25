package com.tallerwebi.presentacion.grupo;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoExistente;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NoEsAdministradorDelGrupo;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NoEsIntegranteDelGrupo;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NombreDeGrupoInvalido;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.SalidaDeGrupo;
import com.tallerwebi.dominio.grupo.ServicioGrupo;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorGrupo {

  private static final String REDIRECCION_LOGIN = "redirect:/login";
  private static final String REDIRECCION_GRUPOS = "redirect:/grupos";
  private static final String VISTA_FORMULARIO = "paginas/grupo/grupo-formulario";
  private static final String MENSAJE_EXITO = "exito";
  private static final String MENSAJE_ERROR = "error";
  private static final String NOMBRE = "nombre";
  private static final String NOMBRE_ACTUAL = "nombreActual";
  private static final String GRUPO_NO_EXISTE = "El grupo no existe";

  private ServicioGrupo servicioGrupo;

  @RequestMapping(value = "/grupos", method = RequestMethod.GET)
  public ModelAndView verGrupos(HttpServletRequest request) {
    // solo se listan los grupos del usuario logueado.
    Usuario usuario = obtenerUsuarioLogueado(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECCION_LOGIN);
    }

    Map<String, Object> modelo = new ModelMap();
    try {
      modelo.put("datosGrupo", servicioGrupo.listarGruposDelUsuario(usuario.getEmail()));
    } catch (Exception e) {
      modelo.put(MENSAJE_ERROR, "No se pudieron cargar los grupos");
    }
    return new ModelAndView("paginas/grupo/grupos", modelo);
  }

  @GetMapping("/grupos/{nombre}")
  public ModelAndView verGrupo(@PathVariable(NOMBRE) String nombre, HttpServletRequest request) {
    Usuario usuario = obtenerUsuarioLogueado(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECCION_LOGIN);
    }
    Map<String, Object> modelo = new ModelMap();
    try {
      Grupo grupo = servicioGrupo.buscarPorNombre(nombre);
      DatosGrupo datosGrupo = new DatosGrupo();
      datosGrupo.setNombre(grupo.getNombre());
      datosGrupo.setCantidadDeIntegrantes(servicioGrupo.cantidadParticipantes(nombre));
      datosGrupo.setIntegrantes(armarIntegrantes(servicioGrupo.listarIntegrantes(nombre), grupo));
      // Para el panel "Opciones": solo los integrantes pueden editar o salir.
      datosGrupo.setSoyIntegrante(servicioGrupo.esIntegrante(grupo, usuario));
      datosGrupo.setSoyAdministrador(grupo.esAdministradoPor(usuario));
      modelo.put("datosGrupo", datosGrupo);
      return new ModelAndView("paginas/grupo/grupo-detalle", modelo);
    } catch (GrupoNoEncontrado e) {
      modelo.put(MENSAJE_ERROR, "El grupo no existe");
      return new ModelAndView("paginas/grupo/grupo-detalle", modelo);
    }
  }

  @GetMapping("/grupos/{nombre}/invitar")
  public ModelAndView irAInvitar(@PathVariable(NOMBRE) String grupoAInvitar) {
    Map<String, Object> modelo = new ModelMap();
    try {
      Grupo grupo = servicioGrupo.buscarPorNombre(grupoAInvitar);
      DatosGrupo datosGrupo = new DatosGrupo();
      datosGrupo.setNombre(grupo.getNombre());
      modelo.put("datosGrupo", datosGrupo);
      return new ModelAndView("paginas/grupo/grupo-invitar", modelo);
    } catch (GrupoNoEncontrado e) {
      modelo.put(MENSAJE_ERROR, "El grupo no existe");
      return new ModelAndView("paginas/grupo/grupo-invitar", modelo);
    }
  }

  @Autowired
  public ControladorGrupo(ServicioGrupo servicioGrupo) {
    this.servicioGrupo = servicioGrupo;
  }

  private List<DatosIntegrante> armarIntegrantes(List<Usuario> usuarios, Grupo grupo) {
    List<DatosIntegrante> integrantes = new ArrayList<>();
    if (usuarios != null) {
      for (Usuario usuario : usuarios) {
        integrantes.add(
          new DatosIntegrante(
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getEmail(),
            grupo.esAdministradoPor(usuario)
          )
        );
      }
    }
    return integrantes;
  }

  //  ABM de grupo

  // Alta: muestra el formulario vacio.
  @GetMapping("/grupos/nuevo")
  public ModelAndView irANuevoGrupo(HttpServletRequest request) {
    if (obtenerUsuarioLogueado(request) == null) {
      return new ModelAndView(REDIRECCION_LOGIN);
    }
    return armarFormulario(null, "", null);
  }

  // Alta: crea el grupo con el usuario logueado como administrador.
  @PostMapping("/grupos/crear")
  public ModelAndView crearGrupo(
    @RequestParam(NOMBRE) String nombre,
    HttpServletRequest request,
    RedirectAttributes redirectAttributes
  ) {
    Usuario usuario = obtenerUsuarioLogueado(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECCION_LOGIN);
    }
    try {
      Grupo grupo = servicioGrupo.crearGrupo(nombre, usuario);
      redirectAttributes.addFlashAttribute(
        MENSAJE_EXITO,
        "Creaste el grupo " + grupo.getNombre() + ". Sos su administrador/a."
      );
      return new ModelAndView(REDIRECCION_GRUPOS);
    } catch (NombreDeGrupoInvalido | GrupoExistente e) {
      return armarFormulario(null, nombre, e.getMessage());
    }
  }

  // Modificacion: muestra el formulario con el nombre actual (solo el administrador).
  @GetMapping("/grupos/{nombreActual}/editar")
  public ModelAndView irAEditarGrupo(
    @PathVariable(NOMBRE_ACTUAL) String nombreActual,
    HttpServletRequest request,
    RedirectAttributes redirectAttributes
  ) {
    Usuario usuario = obtenerUsuarioLogueado(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECCION_LOGIN);
    }
    try {
      Grupo grupo = servicioGrupo.buscarGrupoDeIntegrante(nombreActual, usuario);
      return armarFormulario(grupo.getNombre(), grupo.getNombre(), null);
    } catch (GrupoNoEncontrado e) {
      redirectAttributes.addFlashAttribute(MENSAJE_ERROR, GRUPO_NO_EXISTE);
    } catch (NoEsIntegranteDelGrupo e) {
      redirectAttributes.addFlashAttribute(MENSAJE_ERROR, e.getMessage());
    }
    return new ModelAndView(REDIRECCION_GRUPOS);
  }

  // Modificacion: guarda el nombre nuevo.
  @PostMapping("/grupos/{nombreActual}/editar")
  public ModelAndView editarGrupo(
    @PathVariable(NOMBRE_ACTUAL) String nombreActual,
    @RequestParam(NOMBRE) String nombreNuevo,
    HttpServletRequest request,
    RedirectAttributes redirectAttributes
  ) {
    Usuario usuario = obtenerUsuarioLogueado(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECCION_LOGIN);
    }
    try {
      servicioGrupo.modificarGrupo(nombreActual, nombreNuevo, usuario);
      redirectAttributes.addFlashAttribute(MENSAJE_EXITO, "Nombre del grupo actualizado");
      // Vuelve al detalle del grupo, ya con el nombre nuevo. Se pasa como atributo para que
      // Spring arme bien la dirección aunque el nombre tenga espacios o tildes.
      redirectAttributes.addAttribute(NOMBRE, nombreNuevo.trim());
      return new ModelAndView("redirect:/grupos/{nombre}");
    } catch (NombreDeGrupoInvalido | GrupoExistente e) {
      return armarFormulario(nombreActual, nombreNuevo, e.getMessage());
    } catch (GrupoNoEncontrado e) {
      redirectAttributes.addFlashAttribute(MENSAJE_ERROR, GRUPO_NO_EXISTE);
    } catch (NoEsIntegranteDelGrupo e) {
      redirectAttributes.addFlashAttribute(MENSAJE_ERROR, e.getMessage());
    }
    return new ModelAndView(REDIRECCION_GRUPOS);
  }

  // Baja: elimina el grupo (solo el administrador).
  @PostMapping("/grupos/{nombreActual}/eliminar")
  public ModelAndView eliminarGrupo(
    @PathVariable(NOMBRE_ACTUAL) String nombreActual,
    HttpServletRequest request,
    RedirectAttributes redirectAttributes
  ) {
    Usuario usuario = obtenerUsuarioLogueado(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECCION_LOGIN);
    }
    try {
      servicioGrupo.eliminarGrupo(nombreActual, usuario);
      redirectAttributes.addFlashAttribute(MENSAJE_EXITO, "Grupo eliminado");
    } catch (GrupoNoEncontrado e) {
      redirectAttributes.addFlashAttribute(MENSAJE_ERROR, GRUPO_NO_EXISTE);
    } catch (NoEsAdministradorDelGrupo e) {
      redirectAttributes.addFlashAttribute(MENSAJE_ERROR, e.getMessage());
    }
    return new ModelAndView(REDIRECCION_GRUPOS);
  }

  // Salir del grupo: si sale el administrador, la administracion pasa a otro integrante.
  @PostMapping("/grupos/{nombreActual}/salir")
  public ModelAndView salirDelGrupo(
    @PathVariable(NOMBRE_ACTUAL) String nombreActual,
    HttpServletRequest request,
    RedirectAttributes redirectAttributes
  ) {
    Usuario usuario = obtenerUsuarioLogueado(request);
    if (usuario == null) {
      return new ModelAndView(REDIRECCION_LOGIN);
    }
    try {
      SalidaDeGrupo salida = servicioGrupo.salirDelGrupo(nombreActual, usuario);
      redirectAttributes.addFlashAttribute(MENSAJE_EXITO, mensajeDeSalida(salida));
    } catch (GrupoNoEncontrado e) {
      redirectAttributes.addFlashAttribute(MENSAJE_ERROR, GRUPO_NO_EXISTE);
    } catch (NoEsIntegranteDelGrupo e) {
      redirectAttributes.addFlashAttribute(MENSAJE_ERROR, e.getMessage());
    }
    return new ModelAndView(REDIRECCION_GRUPOS);
  }

  private String mensajeDeSalida(SalidaDeGrupo salida) {
    if (salida.isGrupoEliminado()) {
      return "Saliste del grupo. Como eras el único integrante, el grupo se eliminó.";
    }
    Usuario nuevo = salida.getNuevoAdministrador();
    if (nuevo != null) {
      return (
        "Saliste del grupo. Ahora " +
        nuevo.getNombre() +
        " " +
        nuevo.getApellido() +
        " es administrador/a."
      );
    }
    return "Saliste del grupo.";
  }

  // nombreOriginal == null significa alta; si tiene valor, es la edicion de ese grupo.
  private ModelAndView armarFormulario(String nombreOriginal, String nombre, String error) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("nombreOriginal", nombreOriginal);
    modelo.put(NOMBRE, nombre);
    if (error != null) {
      modelo.put(MENSAJE_ERROR, error);
    }
    return new ModelAndView(VISTA_FORMULARIO, modelo);
  }

  private Usuario obtenerUsuarioLogueado(HttpServletRequest request) {
    return (Usuario) request.getSession().getAttribute("USUARIO");
  }
}
