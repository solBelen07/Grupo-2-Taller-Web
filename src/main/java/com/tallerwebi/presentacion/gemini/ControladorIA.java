package com.tallerwebi.presentacion.gemini;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorIA {

  @GetMapping("/ia")
  public String mostrarChatIA() {
    return "paginas/ia/ia";
  }
}
