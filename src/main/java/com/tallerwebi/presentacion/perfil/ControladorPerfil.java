package com.tallerwebi.presentacion.perfil;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.catalogo.Interes;
import com.tallerwebi.dominio.catalogo.ServicioCatalogo;
import com.tallerwebi.dominio.perfil.DatosCambioPassword;
import com.tallerwebi.dominio.perfil.DatosPerfil;
import com.tallerwebi.dominio.perfil.FotoPerfil;
import com.tallerwebi.dominio.perfil.ServicioPerfil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class ControladorPerfil {

  private static final String SESION_USUARIO = "USUARIO";
  private static final String REDIRECT_PERFIL = "redirect:/perfil";
  private static final String REDIRECT_LOGIN = "redirect:/login";
  private static final String CLAVE_ERROR = "error";
  private static final String CLAVE_MENSAJE = "mensaje";

  private final ServicioPerfil servicioPerfil;
  private final ServicioCatalogo servicioCatalogo;

  @Autowired
  public ControladorPerfil(ServicioPerfil servicioPerfil, ServicioCatalogo servicioCatalogo) {
    this.servicioPerfil = servicioPerfil;
    this.servicioCatalogo = servicioCatalogo;
  }

  @GetMapping
  public ModelAndView verPerfil(HttpServletRequest request) {
    String email = emailEnSesion(request);
    if (email == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Usuario usuario = servicioPerfil.obtenerUsuario(email);
    request.getSession().setAttribute(SESION_USUARIO, usuario);

    Map<String, Object> modelo = new ModelMap();
    modelo.put("perfil", usuario);
    modelo.put("datos", datosDe(usuario));
    modelo.put("cambioPassword", new DatosCambioPassword());
    modelo.put("universidades", servicioCatalogo.obtenerUniversidades());
    modelo.put("carreras", servicioCatalogo.obtenerCarreras());
    modelo.put("intereses", servicioCatalogo.obtenerIntereses());
    modelo.put("tieneFoto", servicioPerfil.tieneFoto(usuario.getId()));
    modelo.put("fotoVersion", System.currentTimeMillis());
    return new ModelAndView("paginas/perfil/perfil", modelo);
  }

  @PostMapping("/datos")
  public ModelAndView guardarDatos(
    @ModelAttribute("datos") DatosPerfil datos,
    HttpServletRequest request,
    RedirectAttributes flash
  ) {
    String email = emailEnSesion(request);
    if (email == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    try {
      Usuario actualizado = servicioPerfil.actualizarDatos(email, datos);
      request.getSession().setAttribute(SESION_USUARIO, actualizado);
      flash.addFlashAttribute(CLAVE_MENSAJE, "Datos actualizados");
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute(CLAVE_ERROR, e.getMessage());
    }
    return new ModelAndView(REDIRECT_PERFIL);
  }

  @PostMapping("/password")
  public ModelAndView cambiarPassword(
    @ModelAttribute("cambioPassword") DatosCambioPassword cambio,
    HttpServletRequest request,
    RedirectAttributes flash
  ) {
    String email = emailEnSesion(request);
    if (email == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    try {
      servicioPerfil.cambiarPassword(email, cambio);
      flash.addFlashAttribute(CLAVE_MENSAJE, "Contraseña actualizada");
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute(CLAVE_ERROR, e.getMessage());
    }
    return new ModelAndView(REDIRECT_PERFIL);
  }

  @PostMapping("/foto")
  public ModelAndView subirFoto(
    @RequestParam("foto") MultipartFile foto,
    HttpServletRequest request,
    RedirectAttributes flash
  ) {
    String email = emailEnSesion(request);
    if (email == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    try {
      servicioPerfil.guardarFoto(email, foto.getContentType(), foto.getBytes());
      flash.addFlashAttribute(CLAVE_MENSAJE, "Foto actualizada");
    } catch (IllegalArgumentException e) {
      flash.addFlashAttribute(CLAVE_ERROR, e.getMessage());
    } catch (IOException e) {
      flash.addFlashAttribute(CLAVE_ERROR, "No se pudo leer la imagen");
    }
    return new ModelAndView(REDIRECT_PERFIL);
  }

  @PostMapping("/foto/eliminar")
  public ModelAndView eliminarFoto(HttpServletRequest request, RedirectAttributes flash) {
    String email = emailEnSesion(request);
    if (email == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    servicioPerfil.eliminarFoto(email);
    flash.addFlashAttribute(CLAVE_MENSAJE, "Foto eliminada");
    return new ModelAndView(REDIRECT_PERFIL);
  }

  @GetMapping("/foto")
  public ResponseEntity<byte[]> verFoto(HttpServletRequest request) {
    String email = emailEnSesion(request);
    if (email == null) {
      return ResponseEntity.status(401).build();
    }
    FotoPerfil foto = servicioPerfil.obtenerFoto(email);
    if (foto == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity
      .ok()
      .contentType(MediaType.parseMediaType(foto.getTipoContenido()))
      .cacheControl(CacheControl.noCache())
      .header("X-Content-Type-Options", "nosniff")
      .body(foto.getDatos());
  }

  private String emailEnSesion(HttpServletRequest request) {
    HttpSession sesion = request.getSession(false);
    if (sesion == null) {
      return null;
    }
    Object usuario = sesion.getAttribute(SESION_USUARIO);
    return usuario instanceof Usuario ? ((Usuario) usuario).getEmail() : null;
  }

  private DatosPerfil datosDe(Usuario usuario) {
    DatosPerfil datos = new DatosPerfil();
    datos.setNombre(usuario.getNombre());
    datos.setApellido(usuario.getApellido());
    if (usuario.getUniversidad() != null) {
      datos.setUniversidadId(usuario.getUniversidad().getId());
    }
    if (usuario.getCarrera() != null) {
      datos.setCarreraId(usuario.getCarrera().getId());
    }
    for (Interes interes : usuario.getIntereses()) {
      datos.getInteresesIds().add(interes.getId());
    }
    return datos;
  }
}
