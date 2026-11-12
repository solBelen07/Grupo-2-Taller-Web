package com.tallerwebi.dominio.recomendacion;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.materia.Materia;
import com.tallerwebi.dominio.materia.RepositorioMateria;
import com.tallerwebi.dominio.tarea.ServicioTarea;
import com.tallerwebi.dominio.tarea.Tarea;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service("servicioRecomendacion")
@Transactional
public class ServicioRecomendacionImpl implements ServicioRecomendacion {

  static final int MAXIMO_RECOMENDACIONES = 5;
  static final double COMPLEMENTARIEDAD = 0.5;
  static final double CUMPLIMIENTO = 0.25;
  static final double APORTE = 0.25;
  private static final String ESTADO_COMPLETADA = "COMPLETADA";
  private static final double PORCENTAJE = 100.0;

  private final RepositorioMateria repositorioMateria;
  private final ServicioTarea servicioTarea;

  public ServicioRecomendacionImpl(
    RepositorioMateria repositorioMateria,
    ServicioTarea servicioTarea
  ) {
    this.repositorioMateria = repositorioMateria;
    this.servicioTarea = servicioTarea;
  }

  @Override
  public MatrizCompetencias obtenerMatrizDeCompetencias(Usuario usuario) {
    return armarMatriz(
      usuario,
      this.repositorioMateria.obtenerPorUsuario(usuario.getId()),
      this.servicioTarea.obtenerTodas()
    );
  }

  @Override
  public double obtenerAporteEnGrupos(Usuario usuario) {
    return calcularAporteEnGrupos(usuario, this.servicioTarea.obtenerTodas());
  }

  @Override
  public List<Recomendado> recomendarCompanieros(Usuario usuario) {
    List<Materia> misMaterias = this.repositorioMateria.obtenerPorUsuario(usuario.getId());
    List<Tarea> tareas = this.servicioTarea.obtenerTodas();
    MatrizCompetencias miMatriz = armarMatriz(usuario, misMaterias, tareas);

    return companierosDeMisMaterias(usuario, misMaterias)
      .stream()
      .map(companero -> evaluar(miMatriz, companero, tareas))
      .filter(recomendado -> !recomendado.getAportaEn().isEmpty())
      .filter(recomendado -> recomendado.getCumplimiento() >= miMatriz.cumplimientoGeneral())
      .sorted(Comparator.comparingDouble(Recomendado::getCompatibilidad).reversed())
      .limit(MAXIMO_RECOMENDACIONES)
      .collect(Collectors.toList());
  }

  private Collection<Usuario> companierosDeMisMaterias(Usuario usuario, List<Materia> materias) {
    Map<Long, Usuario> candidatos = new LinkedHashMap<>();
    materias.forEach(materia ->
      materia
        .getUsuarios()
        .stream()
        .filter(otro -> !Objects.equals(otro.getId(), usuario.getId()))
        .forEach(otro -> candidatos.putIfAbsent(otro.getId(), otro))
    );
    return candidatos.values();
  }

  private Recomendado evaluar(MatrizCompetencias miMatriz, Usuario companero, List<Tarea> tareas) {
    MatrizCompetencias suMatriz = armarMatriz(
      companero,
      this.repositorioMateria.obtenerPorUsuario(companero.getId()),
      tareas
    );

    double complementariedad = miMatriz
      .getCompetencias()
      .stream()
      .mapToDouble(competencia ->
        Math.max(0.0, suMatriz.nivelEn(competencia) - miMatriz.nivelEn(competencia))
      )
      .average()
      .orElse(0.0);

    List<String> aportaEn = miMatriz
      .getCompetencias()
      .stream()
      .filter(competencia -> suMatriz.nivelEn(competencia) > miMatriz.nivelEn(competencia))
      .collect(Collectors.toList());

    double cumplimiento = suMatriz.cumplimientoGeneral();
    double aporte = calcularAporteEnGrupos(companero, tareas);
    double compatibilidad =
      COMPLEMENTARIEDAD * complementariedad + CUMPLIMIENTO * cumplimiento + APORTE * aporte;

    return new Recomendado(companero, compatibilidad, cumplimiento, aporte, aportaEn);
  }

  private double calcularAporteEnGrupos(Usuario usuario, List<Tarea> tareas) {
    List<Tarea> tareasDeGrupo = tareas
      .stream()
      .filter(tarea -> tarea.getGrupo() != null)
      .filter(tarea -> Objects.equals(usuario.getEmail(), tarea.getResponsable()))
      .collect(Collectors.toList());

    return porcentajeCompletadas(tareasDeGrupo);
  }

  private MatrizCompetencias armarMatriz(
    Usuario usuario,
    List<Materia> materias,
    List<Tarea> tareas
  ) {
    Map<String, Double> niveles = new HashMap<>();
    materias.forEach(materia ->
      niveles.put(materia.getNombre(), nivelEnMateria(usuario, materia, tareas))
    );

    return new MatrizCompetencias(niveles);
  }

  private double nivelEnMateria(Usuario usuario, Materia materia, List<Tarea> tareas) {
    return porcentajeCompletadas(
      tareas
        .stream()
        .filter(tarea -> Objects.equals(usuario.getEmail(), tarea.getResponsable()))
        .filter(tarea -> materia.getNombre().equalsIgnoreCase(tarea.getMateria()))
        .collect(Collectors.toList())
    );
  }

  private double porcentajeCompletadas(List<Tarea> tareas) {
    long completadas = tareas
      .stream()
      .filter(tarea -> ESTADO_COMPLETADA.equals(tarea.getEstado()))
      .count();

    return tareas.isEmpty() ? 0.0 : (completadas * PORCENTAJE) / tareas.size();
  }
}
