package com.tallerwebi.presentacion.gemini;

import com.tallerwebi.dominio.gemini.ServicioGemini;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gemini")
public class ControladorGemini {

  @Autowired
  private ServicioGemini servicioGemini;

  @PostMapping("/preguntar")
  public ResponseEntity<?> preguntar(@RequestBody DatosGemini datosGemini) {
    String respuesta = servicioGemini.preguntar(datosGemini.getPregunta());
    datosGemini.setRespuesta(respuesta);
    return ResponseEntity.ok(datosGemini);
  }
}
