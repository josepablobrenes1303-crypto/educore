package edu.uam.educore.socket;

import edu.uam.educore.db.ConfiguracionBD;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import edu.uam.educore.db.Conexion;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
/**
 * Servidor de Matrícula. Recibe por socket la orden MATRICULAR &lt;archivo&gt;, lee ese CSV del
 * directorio de entrada (una matrícula por renglón: carnet,codigoSeccion) y matricula todo el lote
 * en UNA transacción: si un renglón falla, revierte el lote completo.
 */
public class ServidorMatricula {

  private final ConfiguracionBD config;
  private final Path entradaDir;

  public ServidorMatricula(ConfiguracionBD config, String entradaDir) {
    this.config = config;
    this.entradaDir = Path.of(entradaDir);
  }

  public static void main(String[] args) throws Exception {
    ConfiguracionBD config = ConfiguracionBD.desdeArchivo(".env");
    String entrada = System.getenv("ENTRADA_DIR");
    int puerto = Integer.parseInt(System.getenv("MATRICULA_PORT"));
    new ServidorMatricula(config, entrada).escuchar(puerto);
  }

  public void escuchar(int puerto) throws IOException {
    try (ServerSocket servidor = new ServerSocket(puerto)) {
      System.out.println("Matricula escuchando en " + puerto);
      while (true) {
        try (Socket cliente = servidor.accept();
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out =
                new PrintWriter(cliente.getOutputStream(), true, StandardCharsets.UTF_8)) {
          atender(in, out);
        } catch (IOException e) {
          System.err.println("Error atendiendo cliente: " + e.getMessage());
        }
      }
    }
  }

  private void atender(BufferedReader in, PrintWriter out) throws IOException {
    String linea = in.readLine();
    if (linea == null || !linea.startsWith("MATRICULAR ")) {
      out.println("400 comando invalido");
      return;
    }
    String archivo = linea.substring("MATRICULAR ".length()).trim();
    try {
      int k = procesarLote(archivo);
      out.println("201 " + k);
    } catch (Exception e) {
      out.println("400 " + e.getMessage());
    }
  }

  /**
   * TODO(estudiante · T4/T5): matricular el lote en UNA transacción.
   *
   * <p>Pasos:
   *
   * <ol>
   *   <li>Leer el CSV de entrada (entradaDir.resolve(archivo)) con un BufferedReader; cada renglón
   *       es "carnet,codigoSeccion".
   *   <li>Abrir conexión y con.setAutoCommit(false).
   *   <li>Por cada renglón: buscar el estudiante por carnet (si no existe, es un error), buscar la
   *       sección por código y su cupo (aula.capacidad), validar cupo y duplicado, e insertar en
   *       matricula.
   *   <li>Si todo pasa: con.commit() y devolver la cantidad. Si algo falla: con.rollback() y
   *       relanzar.
   * </ol>
   *
   * <p>Cómo distinguir los cuatro casos de error (carnet inexistente, sección inexistente, cupo
   * lleno, matrícula duplicada) queda a su criterio de diseño — no hay una jerarquía de
   * excepciones provista. Ver "Puntos extra" en el enunciado si quieren diseñar la suya.
   *
   * <p>Referencia del patrón JDBC: EstudianteRepoSql.
   */
 private int procesarLote(String archivo) throws Exception {
  Path ruta = entradaDir.resolve(archivo);

  if (!Files.exists(ruta)) {
    throw new IllegalArgumentException(
        "No existe el archivo: " + archivo);
  }

  List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);

  try (Connection con =
      Conexion.getConnection(
          config.url(),
          config.usuario(),
          config.contrasena())) {

    con.setAutoCommit(false);

    try {
      int matriculados = 0;

      for (String linea : lineas) {
        if (linea == null || linea.trim().isEmpty()) {
          continue;
        }

        String[] partes = linea.split(",");

        if (partes.length != 2) {
          throw new IllegalArgumentException(
              "Formato inválido: " + linea);
        }

        String carnet = partes[0].trim();
        String codigoSeccion = partes[1].trim();

        int estudianteId;

        try (PreparedStatement ps =
            con.prepareStatement(
                "SELECT id FROM estudiante WHERE carnet=?")) {

          ps.setString(1, carnet);

          try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
              throw new IllegalArgumentException(
                  "No existe estudiante con carnet " + carnet);
            }

            estudianteId = rs.getInt("id");
          }
        }

        int seccionId;
        int capacidad;

        String sqlSeccion =
            "SELECT s.id, a.capacidad "
                + "FROM seccion s "
                + "JOIN aula a ON s.aula_id = a.id "
                + "WHERE s.codigo=?";

        try (PreparedStatement ps = con.prepareStatement(sqlSeccion)) {
          ps.setString(1, codigoSeccion);

          try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
              throw new IllegalArgumentException(
                  "No existe sección con código " + codigoSeccion);
            }

            seccionId = rs.getInt("id");
            capacidad = rs.getInt("capacidad");
          }
        }

        try (PreparedStatement ps =
            con.prepareStatement(
                "SELECT COUNT(*) FROM matricula "
                    + "WHERE seccion_id=? AND estudiante_id=?")) {

          ps.setInt(1, seccionId);
          ps.setInt(2, estudianteId);

          try (ResultSet rs = ps.executeQuery()) {
            rs.next();

            if (rs.getInt(1) > 0) {
              throw new IllegalArgumentException(
                  "El estudiante "
                      + carnet
                      + " ya está matriculado en "
                      + codigoSeccion);
            }
          }
        }

        try (PreparedStatement ps =
            con.prepareStatement(
                "SELECT COUNT(*) FROM matricula WHERE seccion_id=?")) {

          ps.setInt(1, seccionId);

          try (ResultSet rs = ps.executeQuery()) {
            rs.next();

            if (rs.getInt(1) >= capacidad) {
              throw new IllegalArgumentException(
                  "La sección " + codigoSeccion + " está llena");
            }
          }
        }

        try (PreparedStatement ps =
            con.prepareStatement(
                "INSERT INTO matricula "
                    + "(seccion_id, estudiante_id) VALUES (?, ?)")) {

          ps.setInt(1, seccionId);
          ps.setInt(2, estudianteId);
          ps.executeUpdate();
        }

        matriculados++;
      }

      con.commit();
      return matriculados;

    } catch (Exception e) {
      con.rollback();
      throw e;

    } finally {
      con.setAutoCommit(true);
    }
  }
 }
}
