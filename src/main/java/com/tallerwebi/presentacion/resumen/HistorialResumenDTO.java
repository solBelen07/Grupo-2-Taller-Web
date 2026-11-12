package com.tallerwebi.presentacion.resumen;

import com.tallerwebi.dominio.resumen.Resumen;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class HistorialResumenDTO {

  private static final int LARGO_VISTA_PREVIA = 90;
  private static final Pattern ESPACIOS = Pattern.compile("\\s+");
  private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern(
    "dd/MM/yyyy HH:mm"
  );

  private final Long id;
  private final String vistaPrevia;
  private final String fecha;

  public static HistorialResumenDTO desde(Resumen resumen) {
    return new HistorialResumenDTO(
      resumen.getId(),
      armarVistaPrevia(resumen.getTextoOriginal()),
      resumen.getFechaCreacion().format(FORMATO_FECHA)
    );
  }

  private static String armarVistaPrevia(String texto) {
    String enUnaLinea = ESPACIOS.matcher(texto).replaceAll(" ").strip();
    if (enUnaLinea.length() <= LARGO_VISTA_PREVIA) return enUnaLinea;

    return enUnaLinea.substring(0, LARGO_VISTA_PREVIA).strip() + "…";
  }
}
