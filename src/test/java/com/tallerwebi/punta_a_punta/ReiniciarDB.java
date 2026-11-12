package com.tallerwebi.punta_a_punta;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;

public class ReiniciarDB {

  private static final String DB_NAME = entorno("DB_NAME", "tallerwebi");

  public static void limpiarBaseDeDatos() {
    String dataSql = Paths.get("src/main/resources/data.sql").toAbsolutePath().toString();
    String mysql = comandoMysql();

    String generarTruncates = String.format(
      "%s -N -B -e \"SELECT CONCAT('TRUNCATE TABLE ', table_name, ';') " +
      "FROM information_schema.tables " +
      "WHERE table_schema = '%s' AND table_type = 'BASE TABLE'\"",
      mysql,
      DB_NAME
    );

    String comando =
      "set -o pipefail; " +
      generarTruncates +
      " | { echo 'SET FOREIGN_KEY_CHECKS = 0;'; cat; echo 'SET FOREIGN_KEY_CHECKS = 1;'; cat '" +
      dataSql +
      "'; } | " +
      mysql;

    ejecutarComando(comando, null);
    System.out.println("Base de datos reiniciada con data.sql");
  }

  /** Ejecuta sentencias SQL sueltas contra la base de datos del contenedor. */
  public static void ejecutarSql(String sql) {
    ejecutarComando(comandoMysql(), sql);
  }

  /**
   * Simula que pasaron {@code minutos} desde que empezaron los vuelos en curso, corriendo su hora
   * de inicio hacia atrás. El servidor los da por completados la próxima vez que consulta el timer.
   */
  public static void adelantarVuelosEnCurso(int minutos) {
    ejecutarSql(
      "UPDATE pomodoro SET fecha_inicio = fecha_inicio - INTERVAL " +
      minutos +
      " MINUTE WHERE estado = 'EN_CURSO';"
    );
  }

  /**
   * Mueve las sesiones de estudio pendientes del seed a {@code fecha}, para que los tests no
   * dependan del día en que se ejecutan (data.sql tiene fechas fijas).
   */
  public static void moverSesionesPendientesA(LocalDate fecha) {
    ejecutarSql(
      "UPDATE SesionEstudio SET fecha = '" +
      fecha +
      "' WHERE completada = false OR completada IS NULL;"
    );
  }

  private static String comandoMysql() {
    return String.format(
      "docker exec -i tallerwebi-mysql mysql -h %s -P %s -u %s -p%s %s",
      entorno("DB_HOST", "localhost"),
      entorno("DB_PORT", "3306"),
      entorno("DB_USER", "user"),
      entorno("DB_PASSWORD", "user"),
      DB_NAME
    );
  }

  private static void ejecutarComando(String comando, String entrada) {
    try {
      Process process = new ProcessBuilder("/bin/bash", "-c", comando)
        .redirectErrorStream(true)
        .start();

      try (OutputStream stdin = process.getOutputStream()) {
        if (entrada != null) stdin.write(entrada.getBytes(StandardCharsets.UTF_8));
      }

      String salida = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      int exitCode = process.waitFor();

      if (exitCode != 0) {
        throw new IllegalStateException(
          "Error ejecutando comando en la base de datos. Exit code: " + exitCode + "\n" + salida
        );
      }
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo ejecutar el comando en la base de datos", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Se interrumpió el comando en la base de datos", e);
    }
  }

  private static String entorno(String variable, String porDefecto) {
    String valor = System.getenv(variable);
    return valor != null ? valor : porDefecto;
  }
}
