package com.tallerwebi.dominio.logro;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.grupo.Grupo;
import com.tallerwebi.dominio.grupo.RepositorioGrupo;
import com.tallerwebi.dominio.invitacion.RepositorioInvitacion;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import com.tallerwebi.dominio.parcial.Parcial;
import com.tallerwebi.dominio.parcial.RepositorioParcial;
import com.tallerwebi.dominio.tecnicasestudio.RepositorioPomodoro;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;

@Service("servicioLogro")
@Transactional
public class ServicioLogroImpl implements ServicioLogro {

  static final String MARATONISTA = "Maratonista";
  static final String POPULAR = "Popular";
  static final Long MINUTOS_PARA_LOGRO_MARATONISTA = 3000L;
  static final int CANTIDAD_INVITACIONES_PARA_LOGRO_INVITADOR = 5;
  // Más logros, que se pueden ganar usando la app normalmente.
  static final String PRIMEROS_PASOS = "Primeros pasos";
  static final String PRIMER_VUELO = "Primer vuelo";
  static final String PILOTO_FRECUENTE = "Piloto frecuente";
  static final String CONSTANTE = "Constante";
  static final String IMPARABLE = "Imparable";
  static final String PLAN_CUMPLIDO = "Plan cumplido";
  static final String LIDER_DE_GRUPO = "Líder de grupo";
  static final long MINUTOS_PARA_PILOTO_FRECUENTE = 300L;
  static final int DIAS_PARA_CONSTANTE = 3;
  static final int DIAS_PARA_IMPARABLE = 7;
  static final int PORCENTAJE_PLAN_COMPLETO = 100;

  private final RepositorioLogro repositorioLogro;
  private final RepositorioLogroObtenido repositorioLogroObtenido;
  private final RepositorioInvitacion repositorioInvitacion;
  private final RepositorioPomodoro repositorioPomodoro;
  private final RepositorioMateria repositorioMateria;
  private final RepositorioParcial repositorioParcial;
  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioGrupo repositorioGrupo;

  // acá se DEFINEN los logros: el nombre de cada uno y la condición para ganarlo.
  // Para agregar un logro nuevo alcanza con sumar una línea aca (y su fila en la tabla Logro).
  private final Map<String, Predicate<Usuario>> condicionesDeLogros = new LinkedHashMap<>();

  public ServicioLogroImpl(
    RepositorioLogro repositorioLogro,
    RepositorioLogroObtenido repositorioLogroObtenido,
    RepositorioInvitacion repositorioInvitacion,
    RepositorioPomodoro repositorioPomodoro,
    RepositorioMateria repositorioMateria,
    RepositorioParcial repositorioParcial,
    RepositorioUsuario repositorioUsuario,
    RepositorioGrupo repositorioGrupo
  ) {
    this.repositorioLogro = repositorioLogro;
    this.repositorioLogroObtenido = repositorioLogroObtenido;
    this.repositorioInvitacion = repositorioInvitacion;
    this.repositorioPomodoro = repositorioPomodoro;
    this.repositorioMateria = repositorioMateria;
    this.repositorioParcial = repositorioParcial;
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioGrupo = repositorioGrupo;

    condicionesDeLogros.put(MARATONISTA, this::estudioMasDe50Horas);
    condicionesDeLogros.put(POPULAR, this::fueInvitadoMuchasVeces);
    condicionesDeLogros.put(PRIMEROS_PASOS, this::cargoSuPrimeraMateria);
    condicionesDeLogros.put(PRIMER_VUELO, usuario -> minutosEnFocusFlight(usuario) > 0);
    condicionesDeLogros.put(
      PILOTO_FRECUENTE,
      usuario -> minutosEnFocusFlight(usuario) >= MINUTOS_PARA_PILOTO_FRECUENTE
    );
    condicionesDeLogros.put(CONSTANTE, usuario -> recordDeRacha(usuario) >= DIAS_PARA_CONSTANTE);
    condicionesDeLogros.put(IMPARABLE, usuario -> recordDeRacha(usuario) >= DIAS_PARA_IMPARABLE);
    condicionesDeLogros.put(PLAN_CUMPLIDO, this::completoUnPlanDeParcial);
    condicionesDeLogros.put(LIDER_DE_GRUPO, this::creoUnGrupo);
  }

  @Override
  public List<Logro> obtenerLogros() {
    return repositorioLogro.listar();
  }

  @Override
  public void otorgarLogrosAlcanzados(Usuario usuario) {
    for (Map.Entry<String, Predicate<Usuario>> definicion : condicionesDeLogros.entrySet()) {
      String nombreLogro = definicion.getKey();
      boolean yaLoTiene = repositorioLogroObtenido.existe(usuario.getId(), nombreLogro);
      if (!yaLoTiene && definicion.getValue().test(usuario)) {
        registrarLogro(usuario, nombreLogro);
      }
    }
  }

  @Override
  public List<LogroObtenido> obtenerLogrosObtenidos(Usuario usuario) {
    return repositorioLogroObtenido.buscarPorUsuario(usuario.getId());
  }

  private void registrarLogro(Usuario usuario, String nombreLogro) {
    Logro logro = repositorioLogro.buscar(nombreLogro);
    if (logro != null) {
      repositorioLogroObtenido.guardar(new LogroObtenido(usuario, logro, LocalDate.now()));
    }
  }

  // Condiciones de cada logro

  private boolean estudioMasDe50Horas(Usuario usuario) {
    long minutos = repositorioPomodoro.calcularMinutosCompletados(usuario.getId());
    return minutos > MINUTOS_PARA_LOGRO_MARATONISTA;
  }

  private boolean fueInvitadoMuchasVeces(Usuario usuario) {
    Integer invitaciones = repositorioInvitacion.cantidadDeInvitacionesHistoricoPorReceptor(
      usuario.getEmail()
    );
    return invitaciones != null && invitaciones > CANTIDAD_INVITACIONES_PARA_LOGRO_INVITADOR;
  }

  private boolean cargoSuPrimeraMateria(Usuario usuario) {
    Long materias = repositorioMateria.obtenerCantidadMateriasPorUsuario(usuario.getId());
    return materias != null && materias >= 1;
  }

  private long minutosEnFocusFlight(Usuario usuario) {
    return repositorioPomodoro.calcularMinutosCompletados(usuario.getId());
  }

  // La racha se lee de la base
  private int recordDeRacha(Usuario usuario) {
    Usuario persistido = repositorioUsuario.buscarPorId(usuario.getId());
    if (persistido == null || persistido.getRacha() == null) {
      return 0;
    }
    Integer record = persistido.getRacha().getRecordRacha();
    return record == null ? 0 : record;
  }

  // Algún parcial de sus materias con todas las sesiones del plan completadas.
  private boolean completoUnPlanDeParcial(Usuario usuario) {
    return repositorioParcial
      .obtenerTodos()
      .stream()
      .filter(parcial -> esDeSusMaterias(parcial, usuario))
      .anyMatch(parcial -> parcial.getPorcentajeCompletado() >= PORCENTAJE_PLAN_COMPLETO);
  }

  private static boolean esDeSusMaterias(Parcial parcial, Usuario usuario) {
    return (
      parcial.getMateria() != null &&
      parcial.getMateria().getUsuarios() != null &&
      parcial
        .getMateria()
        .getUsuarios()
        .stream()
        .anyMatch(integrante -> usuario.getId().equals(integrante.getId()))
    );
  }

  private boolean creoUnGrupo(Usuario usuario) {
    List<Grupo> grupos = repositorioGrupo.listarGruposPorUsuario(usuario.getEmail());
    return grupos != null && grupos.stream().anyMatch(grupo -> grupo.esAdministradoPor(usuario));
  }
}
