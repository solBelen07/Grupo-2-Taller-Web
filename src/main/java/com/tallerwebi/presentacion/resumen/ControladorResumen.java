package com.tallerwebi.presentacion.resumen;

import com.tallerwebi.dominio.gemini.ServicioGemini;
import com.tallerwebi.presentacion.gemini.DatosGemini;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/resumen")
public class ControladorResumen {

  private ServicioGemini servicioGemini;

  @Autowired
  public ControladorResumen(ServicioGemini servicioGemini) {
    this.servicioGemini = servicioGemini;
  }

  @GetMapping
  public ModelAndView mostrarPantallaDeResumen() {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("datosGemini", new DatosGemini());
    return new ModelAndView("paginas/resumen/resumen", modelo);
  }

  @PostMapping("/resumir")
  public ModelAndView generarResumen(@ModelAttribute DatosGemini datosGemini) {
    Map<String, Object> modelo = new ModelMap();
    String respuesta = servicioGemini.preguntar(datosGemini.getPregunta());
    DatosGemini datosRespuesta = datosGemini;
    datosRespuesta.setRespuesta(respuesta);
    modelo.put("datosGemini", datosRespuesta);
    return new ModelAndView("paginas/resumen/resumen", modelo);
  }
}
