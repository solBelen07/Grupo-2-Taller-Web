package com.tallerwebi.presentacion.gemini;

import com.tallerwebi.dominio.gemini.ServicioGemini;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gemini")
public class ControladorGemini {

  private final ServicioGemini servicioGemini;

  public ControladorGemini(ServicioGemini servicioGemini) {
    this.servicioGemini = servicioGemini;
  }

  @PostMapping("/preguntar")
  public ResponseEntity<?> preguntar(@RequestBody GeminiDto geminiDto) {
    if (geminiDto.getPregunta() == null || geminiDto.getPregunta().isBlank()) {
      return ResponseEntity.badRequest().body("La pregunta no puede estar vacía.");
    }

    String respuesta;

    if (geminiDto.getReglaAdicional() != null && !geminiDto.getReglaAdicional().isBlank()) {
      respuesta =
        servicioGemini.preguntarConRegla(geminiDto.getPregunta(), geminiDto.getReglaAdicional());
    } else {
      respuesta = servicioGemini.preguntar(geminiDto.getPregunta());
    }

    geminiDto.setRespuesta(respuesta);
    return ResponseEntity.ok(geminiDto);
  }

  @PostMapping("/limpiar")
  public ResponseEntity<Void> limpiarContexto() {
    servicioGemini.limpiar();
    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
