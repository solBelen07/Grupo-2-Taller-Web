package com.tallerwebi.presentacion.resumen;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionResumen.ResumenInvalido;
import com.tallerwebi.dominio.excepcion.excepcionResumen.ResumenNoGenerado;
import com.tallerwebi.dominio.resumen.ResultadoResumen;
import com.tallerwebi.dominio.resumen.Resumen;
import com.tallerwebi.dominio.resumen.ServicioResumen;
import com.tallerwebi.presentacion.gemini.DatosGemini;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/resumen")
public class ControladorResumen {

  private static final String VISTA = "paginas/resumen/resumen";
  private static final String REDIRIGIR_A_LOGIN = "redirect:/login";
  private static final String REDIRIGIR_A_RESUMEN = "redirect:/resumen";

  private final ServicioResumen servicioResumen;

  public ControladorResumen(ServicioResumen servicioResumen) {
    this.servicioResumen = servicioResumen;
  }

  @GetMapping
  public ModelAndView mostrarPantallaDeResumen(HttpServletRequest request) {
    Usuario usuario = obtenerUsuario(request);
    if (usuario == null) return new ModelAndView(REDIRIGIR_A_LOGIN);

    Map<String, Object> modelo = modelo(usuario, new DatosGemini());
    return new ModelAndView(VISTA, modelo);
  }

  @PostMapping("/resumir")
  public ModelAndView generarResumen(
    @ModelAttribute DatosGemini datosGemini,
    HttpServletRequest request
  ) {
    Usuario usuario = obtenerUsuario(request);
    if (usuario == null) return new ModelAndView(REDIRIGIR_A_LOGIN);

    String error = null;
    Boolean reutilizado = null;

    try {
      ResultadoResumen resultado = servicioResumen.resumir(usuario, datosGemini.getPregunta());
      datosGemini.setRespuesta(resultado.resumen().getRespuestaHtml());
      reutilizado = resultado.reutilizado();
    } catch (ResumenInvalido e) {
      error = e.getMessage();
    } catch (ResumenNoGenerado e) {
      error = e.getMessage();
    }

    Map<String, Object> modelo = modelo(usuario, datosGemini);
    modelo.put("error", error);
    modelo.put("reutilizado", reutilizado);
    return new ModelAndView(VISTA, modelo);
  }

  @GetMapping("/{id}")
  public ModelAndView verResumenDelHistorial(
    @PathVariable("id") Long id,
    HttpServletRequest request
  ) {
    Usuario usuario = obtenerUsuario(request);
    if (usuario == null) return new ModelAndView(REDIRIGIR_A_LOGIN);

    Resumen resumen = servicioResumen.buscarPorIdYUsuario(id, usuario.getId());
    if (resumen == null) return new ModelAndView(REDIRIGIR_A_RESUMEN);

    DatosGemini datos = new DatosGemini(resumen.getRespuestaHtml(), resumen.getTextoOriginal());
    Map<String, Object> modelo = modelo(usuario, datos);
    return new ModelAndView(VISTA, modelo);
  }

  private Usuario obtenerUsuario(HttpServletRequest request) {
    return (Usuario) request.getSession().getAttribute("USUARIO");
  }

  private Map<String, Object> modelo(Usuario usuario, DatosGemini datosGemini) {
    List<HistorialResumenDTO> historial = servicioResumen
      .obtenerHistorial(usuario.getId())
      .stream()
      .map(HistorialResumenDTO::desde)
      .toList();
    Map<String, Object> modelo = new ModelMap();
    modelo.put("datosGemini", datosGemini);
    modelo.put("historial", historial);
    return modelo;
  }
}
