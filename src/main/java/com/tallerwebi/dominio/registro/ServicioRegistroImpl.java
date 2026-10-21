package com.tallerwebi.dominio.registro;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.catalogo.Carrera;
import com.tallerwebi.dominio.catalogo.Universidad;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("servicioRegistro")
@Transactional
public class ServicioRegistroImpl implements ServicioRegistro {

  private static final int LARGO_MINIMO_PASSWORD = 8;
  private static final String ROL_USUARIO = "USER";

  private final RepositorioRegistro repositorioRegistro;
  private final RepositorioUsuario repositorioUsuario;
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Autowired
  public ServicioRegistroImpl(
    RepositorioRegistro repositorioRegistro,
    RepositorioUsuario repositorioUsuario
  ) {
    this.repositorioRegistro = repositorioRegistro;
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public void registrar(RegistroDTO registro) throws UsuarioExistente {
    validarCampos(registro);

    String email = registro.getEmail().trim().toLowerCase(Locale.ROOT);
    if (repositorioUsuario.buscar(email) != null) {
      throw new UsuarioExistente();
    }

    Universidad universidad = repositorioRegistro.buscarUniversidad(registro.getUniversidadId());
    Carrera carrera = repositorioRegistro.buscarCarrera(registro.getCarreraId());
    if (universidad == null || carrera == null) {
      throw new IllegalArgumentException("La universidad o la carrera seleccionada no es válida");
    }

    Usuario usuario = new Usuario();
    usuario.setNombre(registro.getNombre().trim());
    usuario.setApellido(registro.getApellido().trim());
    usuario.setEmail(email);
    usuario.setPassword(passwordEncoder.encode(registro.getPassword()));
    usuario.setRol(ROL_USUARIO);
    usuario.setUniversidad(universidad);
    usuario.setCarrera(carrera);
    usuario.setIntereses(
      new HashSet<>(repositorioRegistro.buscarIntereses(registro.getInteresesIds()))
    );
    usuario.activar();

    repositorioUsuario.guardar(usuario);
  }

  private static final Pattern PATRON_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  private void validarCampos(RegistroDTO registro) {
    validarNombre(registro);
    validarEmail(registro.getEmail());
    validarPassword(registro.getPassword());
    validarSeleccion(registro);
  }

  private void validarNombre(RegistroDTO registro) {
    if (estaVacio(registro.getNombre()) || estaVacio(registro.getApellido())) {
      throw new IllegalArgumentException("Completá nombre y apellido");
    }
  }

  private void validarEmail(String email) {
    if (estaVacio(email) || !PATRON_EMAIL.matcher(email.trim()).matches()) {
      throw new IllegalArgumentException("Ingresá un email válido");
    }
  }

  private void validarPassword(String password) {
    if (password == null || password.length() < LARGO_MINIMO_PASSWORD) {
      throw new IllegalArgumentException(
        "La contraseña debe tener al menos " + LARGO_MINIMO_PASSWORD + " caracteres"
      );
    }
  }

  private void validarSeleccion(RegistroDTO registro) {
    if (registro.getUniversidadId() == null || registro.getCarreraId() == null) {
      throw new IllegalArgumentException("Seleccioná universidad y carrera");
    }
  }

  private boolean estaVacio(String texto) {
    return texto == null || texto.isBlank();
  }
}
