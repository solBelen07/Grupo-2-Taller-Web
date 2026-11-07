package com.tallerwebi.dominio.resumen;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionResumen.ResumenInvalido;
import com.tallerwebi.dominio.excepcion.excepcionResumen.ResumenNoGenerado;
import com.tallerwebi.dominio.gemini.ServicioGemini;
import com.tallerwebi.dominio.gemini.errores.ErroresGemini;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("ServicioResumen")
public class ServicioResumenImpl implements ServicioResumen {

  public static final int MAXIMO_CARACTERES = 5000;

  private static final String ALGORITMO_MARCA = "SHA-256";
  private static final Pattern ESPACIOS = Pattern.compile("\\s+");

  private final RepositorioResumen repositorioResumen;
  private final ServicioGemini servicioGemini;
  private final Clock reloj;

  public ServicioResumenImpl(
    RepositorioResumen repositorioResumen,
    ServicioGemini servicioGemini,
    Clock reloj
  ) {
    this.repositorioResumen = repositorioResumen;
    this.servicioGemini = servicioGemini;
    this.reloj = reloj;
  }

  @Override
  @Transactional
  public ResultadoResumen resumir(Usuario usuario, String texto)
    throws ResumenInvalido, ResumenNoGenerado {
    String textoLimpio = validarYLimpiar(texto);
    String marca = calcularMarca(textoLimpio);

    Resumen existente = this.repositorioResumen.buscarPorUsuarioYMarca(usuario.getId(), marca);
    if (existente != null) {
      this.repositorioResumen.actualizar(existente);
      return new ResultadoResumen(existente, true);
    }
    String respuesta = servicioGemini.preguntar(textoLimpio);

    if (ErroresGemini.esFallida(respuesta)) throw new ResumenNoGenerado(
      "No pudimos generar el resumen en este momento. Intentá nuevamente en unos minutos."
    );

    Resumen nuevo = new Resumen(usuario, textoLimpio, marca, respuesta, LocalDateTime.now(reloj));
    this.repositorioResumen.guardar(nuevo);

    return new ResultadoResumen(nuevo, false);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Resumen> obtenerHistorial(Long usuarioId) {
    return this.repositorioResumen.buscarPorUsuario(usuarioId);
  }

  @Override
  @Transactional(readOnly = true)
  public Resumen buscarPorIdYUsuario(Long id, Long usuarioId) {
    return repositorioResumen.buscarPorIdYUsuario(id, usuarioId);
  }

  private String validarYLimpiar(String texto) throws ResumenInvalido {
    if (texto == null || texto.isBlank()) throw new ResumenInvalido(
      "Ingrese el texto que quiere resumir."
    );
    String textoLimpio = texto.strip();

    if (textoLimpio.length() > MAXIMO_CARACTERES) throw new ResumenInvalido(
      "El texto supera los " + MAXIMO_CARACTERES + " caracteres permitidos."
    );
    return textoLimpio;
  }

  private static String calcularMarca(String texto) {
    return calcularMarca(texto, ALGORITMO_MARCA);
  }

  static String calcularMarca(String texto, String algoritmo) {
    String normalizado = ESPACIOS.matcher(texto).replaceAll(" ");
    try {
      byte[] resumen = MessageDigest
        .getInstance(algoritmo)
        .digest(normalizado.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(resumen);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Algoritmo de huella no disponible: " + algoritmo, e);
    }
  }
}
