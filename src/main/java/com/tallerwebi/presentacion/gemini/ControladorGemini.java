package com.tallerwebi.presentacion.gemini;

import com.tallerwebi.dominio.excepcion.excepcionGemini.CuotaDeGeminiAgotada;
import com.tallerwebi.dominio.excepcion.excepcionGemini.ErrorDeGemini;
import com.tallerwebi.dominio.gemini.ServicioGemini;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/api/gemini")
public class ControladorGemini {

  @Autowired
  private ServicioGemini servicioGemini;

  @PostMapping("/preguntar")
  public ResponseEntity<?> preguntar(@RequestBody DatosGemini datosGemini) {
    try {
      String respuesta = servicioGemini.preguntar(datosGemini.getPregunta());
      datosGemini.setRespuesta(respuesta);
      return ResponseEntity.ok(datosGemini);
    } catch (CuotaDeGeminiAgotada e) {
      return ResponseEntity
        .status(503)
        .body("Alcanzamos el límite de uso de la IA por hoy. Intentá nuevamente más tarde.");
    } catch (ErrorDeGemini e) {
      return ResponseEntity
        .internalServerError()
        .body("No pude obtener una respuesta en este momento. Intentá nuevamente.");
    }
  }
}
