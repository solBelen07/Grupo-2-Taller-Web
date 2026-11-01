package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.transaction.Transactional;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

  private static final String PREFIJO_BCRYPT = "$2";

  private final RepositorioUsuario repositorioUsuario;
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Autowired
  public ServicioLoginImpl(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public Usuario consultarUsuario(String email, String password) {
    if (email == null || password == null) {
      return null;
    }
    String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
    Usuario usuario = repositorioUsuario.buscar(emailNormalizado);
    if (usuario == null && !email.equals(emailNormalizado)) {
      usuario = repositorioUsuario.buscar(email);
    }
    if (usuario == null || usuario.getPassword() == null) {
      return null;
    }
    return coincide(password, usuario.getPassword()) ? usuario : null;
  }

  @Override
  public void registrar(Usuario usuario) throws UsuarioExistente {
    if (repositorioUsuario.buscar(usuario.getEmail()) != null) {
      throw new UsuarioExistente();
    }
    repositorioUsuario.guardar(usuario);
  }

  private boolean coincide(String ingresada, String guardada) {
    if (guardada.startsWith(PREFIJO_BCRYPT)) {
      return passwordEncoder.matches(ingresada, guardada);
    }
    return guardada.equals(ingresada);
  }
}
