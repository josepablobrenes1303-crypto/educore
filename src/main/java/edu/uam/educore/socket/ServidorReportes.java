package edu.uam.educore.socket;

import edu.uam.educore.db.Conexion;
import edu.uam.educore.db.ConfiguracionBD;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servidor de Reportes. Ante la orden REPORTE cuenta las entidades del sistema en la base de datos,
 * escribe un TXT con el resumen en el directorio de salida y devuelve su contenido por el socket.
 */
public class ServidorReportes {

  private final ConfiguracionBD config;
  private final Path salidaDir;

  public ServidorReportes(ConfiguracionBD config, String salidaDir) {
    this.config = config;
    this.salidaDir = Path.of(salidaDir);
  }

  public static void main(String[] args) throws Exception {
    ConfiguracionBD config = ConfiguracionBD.desdeArchivo(".env");
    String salida = System.getenv("SALIDA_DIR");
    int puerto = Integer.parseInt(System.getenv("REPORTE_PORT"));
    new ServidorReportes(config, salida).escuchar(puerto);
  }

  public void escuchar(int puerto) throws IOException {
    try (ServerSocket servidor = new ServerSocket(puerto)) {
      System.out.println("Reportes escuchando en " + puerto);
      while (true) {
        try (Socket cliente = servidor.accept();
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out =
                new PrintWriter(cliente.getOutputStream(), true, StandardCharsets.UTF_8)) {
          atender(in, out);
        } catch (Exception e) {
          System.err.println("Error atendiendo cliente: " + e.getMessage());
        }
      }
    }
  }

  private void atender(BufferedReader in, PrintWriter out) throws IOException {
    String linea = in.readLine();
    if (linea == null || !linea.trim().equals("REPORTE")) {
      out.println("400 comando invalido");
      return;
    }
    try {
      String contenido = generarYGuardar();
      String[] lineas = contenido.split("\n");
      out.println("200 " + lineas.length);
      for (String l : lineas) {
        out.println(l);
      }
    } catch (Exception e) {
      out.println("500 " + e.getMessage());
    }
  }

  /**
   * TODO(estudiante · T4): generar el reporte.
   *
   * <p>Contar en la base de datos (estudiante, empleado, seccion, aula, matricula), armar un texto,
   * ESCRIBIRLO como TXT en salidaDir (Files.createDirectories + Files.writeString con timestamp) y
   * devolver su contenido. Referencia del patrón: ServidorMatricula para la parte de socket;
   * consultas COUNT(*).
   */
  private String generarYGuardar() throws Exception {
    int estudiantes;
    int empleados;
    int secciones;
    int aulas;
    int matriculas;

    try (Connection con =
        Conexion.getConnection(config.url(), config.usuario(), config.contrasena())) {

      estudiantes = contar(con, "estudiante");
      empleados = contar(con, "empleado");
      secciones = contar(con, "seccion");
      aulas = contar(con, "aula");
      matriculas = contar(con, "matricula");
    }

    String contenido =
        "REPORTE EDUCORE\n"
            + "Estudiantes: "
            + estudiantes
            + "\n"
            + "Empleados: "
            + empleados
            + "\n"
            + "Secciones: "
            + secciones
            + "\n"
            + "Aulas: "
            + aulas
            + "\n"
            + "Matriculas: "
            + matriculas;

    Files.createDirectories(salidaDir);

    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

    Path archivo = salidaDir.resolve("reporte_" + timestamp + ".txt");

    Files.writeString(archivo, contenido, StandardCharsets.UTF_8);

    return contenido;
  }

  private int contar(Connection con, String tabla) throws Exception {
    String sql = "SELECT COUNT(*) FROM " + tabla;

    try (PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      rs.next();
      return rs.getInt(1);
    }
  }
}
