package com.tallerwebi.presentacion.grupo;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.ServicioGrupo;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorGrupo {

  private ServicioGrupo servicioGrupo;

  @RequestMapping(value = "/grupos", method = RequestMethod.GET)
  public ModelAndView verGrupos(HttpServletRequest request) {
    // solo se listan los grupos del usuario logueado.
    Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
    if (usuario == null) {
      return new ModelAndView("redirect:/login");
    }

    Map<String, Object> modelo = new ModelMap();
    try {
      modelo.put("datosGrupo", servicioGrupo.listarGruposDelUsuario(usuario.getEmail()));
    } catch (Exception e) {
      modelo.put("error", "No se pudieron cargar los grupos");
    }
    return new ModelAndView("paginas/grupo/grupos", modelo);
  }

  @GetMapping("/grupos/{nombre}")
  public ModelAndView verGrupo(@PathVariable("nombre") String nombre) {
    Map<String, Object> modelo = new ModelMap();
    try {
      Grupo grupo = servicioGrupo.buscarPorNombre(nombre);
      DatosGrupo datosGrupo = new DatosGrupo();
      datosGrupo.setNombre(grupo.getNombre());
      datosGrupo.setCantidadDeIntegrantes(servicioGrupo.cantidadParticipantes(nombre));
      datosGrupo.setIntegrantes(armarIntegrantes(servicioGrupo.listarIntegrantes(nombre)));
      modelo.put("datosGrupo", datosGrupo);
      return new ModelAndView("paginas/grupo/grupo-detalle", modelo);
    } catch (GrupoNoEncontrado e) {
      modelo.put("error", "El grupo no existe");
      return new ModelAndView("paginas/grupo/grupo-detalle", modelo);
    }
  }

  @GetMapping("/grupos/{nombre}/invitar")
  public ModelAndView irAInvitar(@PathVariable("nombre") String grupoAInvitar) {
    Map<String, Object> modelo = new ModelMap();
    try {
      Grupo grupo = servicioGrupo.buscarPorNombre(grupoAInvitar);
      DatosGrupo datosGrupo = new DatosGrupo();
      datosGrupo.setNombre(grupo.getNombre());
      modelo.put("datosGrupo", datosGrupo);
      return new ModelAndView("paginas/grupo/grupo-invitar", modelo);
    } catch (GrupoNoEncontrado e) {
      modelo.put("error", "El grupo no existe");
      return new ModelAndView("paginas/grupo/grupo-invitar", modelo);
    }
  }

  @Autowired
  public ControladorGrupo(ServicioGrupo servicioGrupo) {
    this.servicioGrupo = servicioGrupo;
  }

  private List<DatosIntegrante> armarIntegrantes(List<Usuario> usuarios) {
    List<DatosIntegrante> integrantes = new ArrayList<>();
    if (usuarios != null) {
      for (Usuario usuario : usuarios) {
        integrantes.add(
          new DatosIntegrante(usuario.getNombre(), usuario.getApellido(), usuario.getEmail())
        );
      }
    }
    return integrantes;
  }
}
