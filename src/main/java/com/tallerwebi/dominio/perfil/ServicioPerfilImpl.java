package com.tallerwebi.dominio.perfil;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Universidad;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.registro.RepositorioRegistro;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("servicioPerfil")
@Transactional
public class ServicioPerfilImpl implements ServicioPerfil {

  private static final int LARGO_MINIMO_PASSWORD = 8;
  private static final long TAMANIO_MAXIMO_FOTO = 2L * 1024 * 1024;
  private static final Set<String> TIPOS_FOTO = Set.of("image/jpeg", "image/png", "image/webp");
  private static final String PREFIJO_BCRYPT = "$2";

  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioRegistro repositorioRegistro;
  private final RepositorioFotoPerfil repositorioFotoPerfil;
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Autowired
  public ServicioPerfilImpl(
    RepositorioUsuario repositorioUsuario,
    RepositorioRegistro repositorioRegistro,
    RepositorioFotoPerfil repositorioFotoPerfil
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioRegistro = repositorioRegistro;
    this.repositorioFotoPerfil = repositorioFotoPerfil;
  }

  @Override
  public Usuario obtenerUsuario(String email) {
    Usuario usuario = repositorioUsuario.buscar(email);
    if (usuario == null) {
      throw new UsuarioNoEncontrado();
    }
    return usuario;
  }

  @Override
  public Usuario actualizarDatos(String email, DatosPerfil datos) {
    Usuario usuario = obtenerUsuario(email);
    validarDatos(datos);

    Universidad universidad = repositorioRegistro.buscarUniversidad(datos.getUniversidadId());
    Carrera carrera = repositorioRegistro.buscarCarrera(datos.getCarreraId());
    if (universidad == null || carrera == null) {
      throw new IllegalArgumentException("La universidad o la carrera seleccionada no es válida");
    }

    usuario.setNombre(datos.getNombre().trim());
    usuario.setApellido(datos.getApellido().trim());
    usuario.setUniversidad(universidad);
    usuario.setCarrera(carrera);
    usuario.setIntereses(
      new HashSet<>(repositorioRegistro.buscarIntereses(datos.getInteresesIds()))
    );
    return usuario;
  }

  @Override
  public void cambiarPassword(String email, DatosCambioPassword datos) {
    Usuario usuario = obtenerUsuario(email);
    validarPasswordNueva(datos);
    if (!coincide(datos.getActual(), usuario.getPassword())) {
      throw new IllegalArgumentException("La contraseña actual no es correcta");
    }
    usuario.setPassword(passwordEncoder.encode(datos.getNueva()));
  }

  @Override
  public void guardarFoto(String email, String tipoContenido, byte[] contenido) {
    Usuario usuario = obtenerUsuario(email);
    if (tipoContenido == null || !TIPOS_FOTO.contains(tipoContenido)) {
      throw new IllegalArgumentException("La foto debe ser JPG, PNG o WEBP");
    }
    if (contenido == null || contenido.length == 0) {
      throw new IllegalArgumentException("Elegí una imagen");
    }
    if (contenido.length > TAMANIO_MAXIMO_FOTO) {
      throw new IllegalArgumentException("La foto no puede superar los 2 MB");
    }
    repositorioFotoPerfil.guardar(new FotoPerfil(usuario.getId(), tipoContenido, contenido));
  }

  @Override
  public void eliminarFoto(String email) {
    repositorioFotoPerfil.eliminar(obtenerUsuario(email).getId());
  }

  @Override
  public FotoPerfil obtenerFoto(String email) {
    return repositorioFotoPerfil.buscar(obtenerUsuario(email).getId());
  }

  @Override
  public boolean tieneFoto(Long usuarioId) {
    return repositorioFotoPerfil.buscar(usuarioId) != null;
  }

  private void validarDatos(DatosPerfil datos) {
    if (estaVacio(datos.getNombre()) || estaVacio(datos.getApellido())) {
      throw new IllegalArgumentException("Completá nombre y apellido");
    }
    if (datos.getUniversidadId() == null || datos.getCarreraId() == null) {
      throw new IllegalArgumentException("Seleccioná universidad y carrera");
    }
  }

  private void validarPasswordNueva(DatosCambioPassword datos) {
    validarActualPresente(datos.getActual());
    validarLargoNueva(datos.getNueva());
    validarConfirmacion(datos);
    validarDistintaDeLaActual(datos);
  }

  private void validarActualPresente(String actual) {
    if (estaVacio(actual)) {
      throw new IllegalArgumentException("Ingresá tu contraseña actual");
    }
  }

  private void validarLargoNueva(String nueva) {
    if (nueva == null || nueva.length() < LARGO_MINIMO_PASSWORD) {
      throw new IllegalArgumentException(
        "La nueva contraseña debe tener al menos " + LARGO_MINIMO_PASSWORD + " caracteres"
      );
    }
  }

  private void validarConfirmacion(DatosCambioPassword datos) {
    if (!datos.getNueva().equals(datos.getConfirmacion())) {
      throw new IllegalArgumentException("La confirmación no coincide con la nueva contraseña");
    }
  }

  private void validarDistintaDeLaActual(DatosCambioPassword datos) {
    if (datos.getNueva().equals(datos.getActual())) {
      throw new IllegalArgumentException("La nueva contraseña debe ser distinta de la actual");
    }
  }

  private boolean coincide(String ingresada, String guardada) {
    if (guardada == null) {
      return false;
    }
    if (guardada.startsWith(PREFIJO_BCRYPT)) {
      return passwordEncoder.matches(ingresada, guardada);
    }
    return guardada.equals(ingresada);
  }

  private boolean estaVacio(String texto) {
    return texto == null || texto.isBlank();
  }
}
