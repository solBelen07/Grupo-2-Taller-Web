package com.tallerwebi.dominio.grupo;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoExistente;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.GrupoNoEncontrado;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NoEsAdministradorDelGrupo;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NoEsIntegranteDelGrupo;
import com.tallerwebi.dominio.excepcion.excepcionGrupo.NombreDeGrupoInvalido;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service("servicioGrupo")
@Transactional
public class ServicioGrupoImpl implements ServicioGrupo {

  private static final int LARGO_MAXIMO_NOMBRE = 50;

  private RepositorioGrupo repositorioGrupo;

  public ServicioGrupoImpl(RepositorioGrupo repositorioGrupo) {
    this.repositorioGrupo = repositorioGrupo;
  }

  @Override
  public Grupo buscarPorNombre(String nombre) throws GrupoNoEncontrado {
    if (repositorioGrupo.buscar(nombre) == null) {
      throw new GrupoNoEncontrado();
    }
    return repositorioGrupo.buscar(nombre);
  }

  @Override
  public List<Grupo> listarGrupos() {
    return repositorioGrupo.listar();
  }

  @Override
  public Integer cantidadParticipantes(String nombre) throws GrupoNoEncontrado {
    if (repositorioGrupo.buscar(nombre) == null) {
      throw new GrupoNoEncontrado();
    }
    return repositorioGrupo.cantidadParticipantes(nombre);
  }

  @Override
  public List<Grupo> listarGruposDelUsuario(String email) {
    return repositorioGrupo.listarGruposPorUsuario(email);
  }

  @Override
  public List<Usuario> listarIntegrantes(String nombre) throws GrupoNoEncontrado {
    if (repositorioGrupo.buscar(nombre) == null) {
      throw new GrupoNoEncontrado();
    }
    return repositorioGrupo.listarIntegrantes(nombre);
  }

  @Override
  public Grupo crearGrupo(String nombre, Usuario creador)
    throws NombreDeGrupoInvalido, GrupoExistente {
    String nombreValido = validarNombre(nombre);
    if (repositorioGrupo.buscar(nombreValido) != null) {
      throw new GrupoExistente();
    }

    // El que crea el grupo queda vinculado automáticamente como administrador y miembro.
    Grupo grupo = new Grupo();
    grupo.setNombre(nombreValido);
    grupo.setCreador(creador);
    repositorioGrupo.guardar(grupo);
    return grupo;
  }

  @Override
  public Grupo buscarGrupoAdministrado(String nombre, Usuario usuario)
    throws GrupoNoEncontrado, NoEsAdministradorDelGrupo {
    Grupo grupo = buscarPorNombre(nombre);
    if (!grupo.esAdministradoPor(usuario)) {
      throw new NoEsAdministradorDelGrupo();
    }
    return grupo;
  }

  @Override
  public void modificarGrupo(String nombreActual, String nombreNuevo, Usuario usuario)
    throws GrupoNoEncontrado, NoEsIntegranteDelGrupo, NombreDeGrupoInvalido, GrupoExistente {
    Grupo grupo = buscarGrupoDeIntegrante(nombreActual, usuario);
    String nombreValido = validarNombre(nombreNuevo);

    boolean cambiaDeNombre = !nombreValido.equals(grupo.getNombre());
    if (cambiaDeNombre && repositorioGrupo.buscar(nombreValido) != null) {
      throw new GrupoExistente();
    }

    grupo.setNombre(nombreValido);
    repositorioGrupo.modificar(grupo);
  }

  @Override
  public void eliminarGrupo(String nombre, Usuario usuario)
    throws GrupoNoEncontrado, NoEsAdministradorDelGrupo {
    Grupo grupo = buscarGrupoAdministrado(nombre, usuario);
    repositorioGrupo.eliminar(grupo);
  }

  // Devuelve el nombre sin espacios de mas, o lanza la excepcion con el motivo.
  private String validarNombre(String nombre) throws NombreDeGrupoInvalido {
    if (nombre == null || nombre.isBlank()) {
      throw new NombreDeGrupoInvalido("El nombre del grupo es obligatorio");
    }
    String nombreLimpio = nombre.trim();
    if (nombreLimpio.length() > LARGO_MAXIMO_NOMBRE) {
      throw new NombreDeGrupoInvalido(
        "El nombre no puede superar los " + LARGO_MAXIMO_NOMBRE + " caracteres"
      );
    }
    // El nombre se usa en la dirección del grupo (grupos/{nombre), así que no puede tener "/".
    if (nombreLimpio.contains("/")) {
      throw new NombreDeGrupoInvalido("El nombre no puede contener \"/\"");
    }
    return nombreLimpio;
  }

  @Override
  public Grupo buscarGrupoDeIntegrante(String nombre, Usuario usuario)
    throws GrupoNoEncontrado, NoEsIntegranteDelGrupo {
    Grupo grupo = buscarPorNombre(nombre);
    if (!esIntegrante(grupo, usuario)) {
      throw new NoEsIntegranteDelGrupo();
    }
    return grupo;
  }

  @Override
  public boolean esIntegrante(Grupo grupo, Usuario usuario) {
    if (usuario == null || usuario.getId() == null) {
      return false;
    }
    if (grupo.esAdministradoPor(usuario)) {
      return true;
    }
    List<Usuario> integrantes = repositorioGrupo.listarIntegrantes(grupo.getNombre());
    return (
      integrantes != null &&
      integrantes.stream().anyMatch(integrante -> usuario.getId().equals(integrante.getId()))
    );
  }

  @Override
  public SalidaDeGrupo salirDelGrupo(String nombre, Usuario usuario)
    throws GrupoNoEncontrado, NoEsIntegranteDelGrupo {
    Grupo grupo = buscarGrupoDeIntegrante(nombre, usuario);

    if (!grupo.esAdministradoPor(usuario)) {
      repositorioGrupo.quitarIntegrante(grupo, usuario);
      return SalidaDeGrupo.deMiembro();
    }

    // Sale el administrador: la administración pasa al integrante mas antiguo.
    Usuario nuevoAdministrador = repositorioGrupo.buscarIntegranteMasAntiguo(grupo, usuario);
    if (nuevoAdministrador == null) {
      // No queda nadie mas en el grupo.
      repositorioGrupo.eliminar(grupo);
      return SalidaDeGrupo.conGrupoEliminado();
    }
    grupo.setCreador(nuevoAdministrador);
    repositorioGrupo.modificar(grupo);
    repositorioGrupo.quitarIntegrante(grupo, usuario);
    return SalidaDeGrupo.conNuevoAdministrador(nuevoAdministrador);
  }
}
