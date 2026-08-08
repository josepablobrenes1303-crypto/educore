package edu.uam.educore.api;

import edu.uam.educore.api.Dtos.EmpleadoDto;
import edu.uam.educore.api.Dtos.EmpleadoRequest;
import edu.uam.educore.api.Dtos.EstudianteDto;
import edu.uam.educore.api.Dtos.EstudianteRequest;
import edu.uam.educore.api.Dtos.MatriculaRequest;
import edu.uam.educore.controller.EdificioController;
import edu.uam.educore.controller.EmpleadoController;
import edu.uam.educore.controller.EstudianteController;
import edu.uam.educore.controller.SeccionController;
import edu.uam.educore.dao.EdificioRepoSql;
import edu.uam.educore.dao.EmpleadoRepoSql;
import edu.uam.educore.dao.EstudianteRepoSql;
import edu.uam.educore.dao.ListaEstudianteRepo;
import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.dao.SeccionRepoSql;
import edu.uam.educore.db.ConfiguracionBD;
import edu.uam.educore.model.academico.Seccion;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.model.infraestructura.TipoAula;
import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.model.personas.Estudiante;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

/**
 * Arma EduCore como app web. Estudiante corre sobre base de datos (referencia) con su controlador
 * real. Empleado, Edificio/Aula y Sección son de cada grupo (P1) — estas rutas no llaman a ningún
 * controlador de nombre fijo, ver los TODO(estudiante · P1) en cada una.
 */
public class ServidorApi {

  public static void iniciar(int puerto) throws IOException {
    Repositorio<Estudiante> estudianteRepo;
    try {
      estudianteRepo = new EstudianteRepoSql(ConfiguracionBD.desdeArchivo(".env"));
    } catch (IOException e) {
      // Sin .env legible caemos a memoria para no bloquear el arranque en desarrollo.
      estudianteRepo = new ListaEstudianteRepo();
    }

    EstudianteController estudianteController = new EstudianteController(estudianteRepo);

    Repositorio<Empleado> empleadoRepo;
    try {
      empleadoRepo = new EmpleadoRepoSql(ConfiguracionBD.desdeArchivo(".env"));
    } catch (IOException e) {
      throw new RuntimeException("No se pudo inicializar el repositorio de empleados.", e);
    }

    EmpleadoController empleadoController = new EmpleadoController(empleadoRepo);

    Repositorio<Edificio> edificioRepo;

    try {
      edificioRepo = new EdificioRepoSql(ConfiguracionBD.desdeArchivo(".env"));
    } catch (IOException e) {
      throw new RuntimeException("No se pudo inicializar el repositorio de edificios.", e);
    }

    EdificioController edificioController = new EdificioController(edificioRepo);

    Repositorio<Seccion> seccionRepo;

    try {
      seccionRepo = new SeccionRepoSql(ConfiguracionBD.desdeArchivo(".env"));
    } catch (IOException e) {
      throw new RuntimeException("No se pudo inicializar el repositorio de secciones.", e);
    }

    SeccionController seccionController =
        new SeccionController(seccionRepo, empleadoRepo, estudianteRepo, edificioRepo);

    Javalin app =
        Javalin.create(
            cfg -> {
              cfg.bundledPlugins.enableDevLogging();
              cfg.spaRoot.addFile("/", "/web/index.html");

              cfg.routes.exception(
                  IllegalArgumentException.class,
                  (e, ctx) -> ctx.status(400).json(Map.of("error", e.getMessage())));
              cfg.routes.exception(
                  SQLIntegrityConstraintViolationException.class,
                  (e, ctx) ->
                      ctx.status(409)
                          .json(
                              Map.of(
                                  "error",
                                  "No se puede eliminar: el registro tiene datos asociados.")));
              cfg.routes.exception(
                  Exception.class,
                  (e, ctx) -> ctx.status(500).json(Map.of("error", e.getMessage())));

              registrarEstudiantes(cfg, estudianteController);
              registrarEmpleados(cfg, empleadoController);
              registrarEdificios(cfg, edificioController);
              registrarSecciones(cfg, seccionController);
              registrarMatricula(cfg);
              registrarReporte(cfg);
            });

    app.start(puerto);
    System.out.println("API EduCore escuchando en http://localhost:" + puerto);
  }

  // ── Estudiantes ──

  private static void registrarEstudiantes(JavalinConfig cfg, EstudianteController controller) {
    cfg.routes.get(
        "/api/estudiantes",
        ctx -> {
          List<EstudianteDto> lista =
              controller.listar().stream().map(EstudianteDto::desde).toList();
          ctx.json(lista);
        });

    cfg.routes.post(
        "/api/estudiantes",
        ctx -> {
          EstudianteRequest r = ctx.bodyAsClass(EstudianteRequest.class);
          Estudiante creado =
              "BECADO".equalsIgnoreCase(r.tipo())
                  ? controller.registrarBecado(
                      r.nombre(),
                      r.apellidos(),
                      r.email(),
                      r.carnet(),
                      r.porcentajeBeca() != null ? r.porcentajeBeca() : 0.0)
                  : controller.registrarRegular(r.nombre(), r.apellidos(), r.email(), r.carnet());
          ctx.status(201).json(EstudianteDto.desde(creado));
        });

    cfg.routes.put(
        "/api/estudiantes/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));
          EstudianteRequest r = ctx.bodyAsClass(EstudianteRequest.class);
          Estudiante e =
              controller.actualizar(
                  id, r.nombre(), r.apellidos(), r.email(), r.carnet(), r.porcentajeBeca());
          ctx.json(EstudianteDto.desde(e));
        });

    cfg.routes.delete(
        "/api/estudiantes/{id}",
        ctx -> {
          controller.eliminar(Integer.parseInt(ctx.pathParam("id")));
          ctx.status(204);
        });
  }

  // ── Empleados (P1 de cada grupo — sin controlador de nombre fijo) ──

  private static void registrarEmpleados(JavalinConfig cfg, EmpleadoController controller) {

    cfg.routes.get(
        "/api/empleados",
        ctx -> {
          List<EmpleadoDto> lista = EmpleadoDto.listaDesde(controller.listar());
          ctx.json(lista);
        });

    cfg.routes.post(
        "/api/empleados",
        ctx -> {
          EmpleadoRequest r = ctx.bodyAsClass(EmpleadoRequest.class);

          Empleado creado =
              controller.registrar(
                  r.nombre(),
                  r.apellidos(),
                  r.email(),
                  r.salario(),
                  java.time.LocalDate.parse(r.fechaIngreso()),
                  r.tipo());

          ctx.status(201).json(EmpleadoDto.desde(creado));
        });

    cfg.routes.put(
        "/api/empleados/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));
          EmpleadoRequest r = ctx.bodyAsClass(EmpleadoRequest.class);

          Empleado actualizado =
              controller.actualizar(
                  id,
                  r.nombre(),
                  r.apellidos(),
                  r.email(),
                  r.salario(),
                  java.time.LocalDate.parse(r.fechaIngreso()),
                  r.tipo());

          ctx.json(EmpleadoDto.desde(actualizado));
        });

    cfg.routes.delete(
        "/api/empleados/{id}",
        ctx -> {
          controller.eliminar(Integer.parseInt(ctx.pathParam("id")));
          ctx.status(204);
        });
  }

  // ── Edificios / Aulas (P1 de cada grupo — sin controlador de nombre fijo) ──

  private static void registrarEdificios(JavalinConfig cfg, EdificioController edificioController) {

    cfg.routes.get(
        "/api/edificios",
        ctx -> {
          List<Edificio> edificios = edificioController.listar();
          ctx.json(Dtos.EdificioDto.listaDesde(edificios));
        });

    cfg.routes.post(
        "/api/edificios",
        ctx -> {
          Map<String, String> body = ctx.bodyAsClass(Map.class);

          String codigo = body.get("codigo");
          String nombre = body.get("nombre");

          Edificio creado = edificioController.registrar(codigo, nombre);

          ctx.status(201).json(creado);
        });

    cfg.routes.put(
        "/api/edificios/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));

          Map<String, String> body = ctx.bodyAsClass(Map.class);

          String codigo = body.get("codigo");
          String nombre = body.get("nombre");

          Edificio actualizado = edificioController.actualizar(id, codigo, nombre);

          ctx.json(actualizado);
        });

    cfg.routes.delete(
        "/api/edificios/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));

          edificioController.eliminar(id);

          ctx.status(204);
        });

    cfg.routes.post(
        "/api/edificios/{id}/aulas",
        ctx -> {
          int edificioId = Integer.parseInt(ctx.pathParam("id"));

          Map<String, Object> body = ctx.bodyAsClass(Map.class);

          String numero = (String) body.get("numero");
          int capacidad = ((Number) body.get("capacidad")).intValue();
          TipoAula tipo = TipoAula.valueOf(((String) body.get("tipo")).toUpperCase());

          Aula aula = edificioController.agregarAula(edificioId, numero, capacidad, tipo);

          ctx.status(201).json(aula);
        });

    cfg.routes.put(
        "/api/edificios/{id}/aulas/{aulaId}",
        ctx -> {
          int edificioId = Integer.parseInt(ctx.pathParam("id"));
          int aulaId = Integer.parseInt(ctx.pathParam("aulaId"));

          Map<String, Object> body = ctx.bodyAsClass(Map.class);

          String numero = (String) body.get("numero");
          int capacidad = ((Number) body.get("capacidad")).intValue();
          TipoAula tipo = TipoAula.valueOf(((String) body.get("tipo")).toUpperCase());

          Aula aula =
              edificioController.actualizarAula(edificioId, aulaId, numero, capacidad, tipo);

          ctx.json(aula);
        });

    cfg.routes.delete(
        "/api/edificios/{id}/aulas/{aulaId}",
        ctx -> {
          int edificioId = Integer.parseInt(ctx.pathParam("id"));
          int aulaId = Integer.parseInt(ctx.pathParam("aulaId"));

          edificioController.eliminarAula(edificioId, aulaId);

          ctx.status(204);
        });
  }

  // ── Secciones (P1 de cada grupo — sin controlador de nombre fijo) ──

  private static void registrarSecciones(JavalinConfig cfg, SeccionController seccionController) {
    cfg.routes.get(
        "/api/secciones",
        ctx -> {
          List<Seccion> secciones = seccionController.listar();
          ctx.json(Dtos.SeccionDto.listaDesde(secciones));
        });

    cfg.routes.post(
        "/api/secciones",
        ctx -> {
          Dtos.SeccionRequest r = ctx.bodyAsClass(Dtos.SeccionRequest.class);

          Seccion creada =
              seccionController.registrar(r.codigo(), r.nombre(), r.aulaId(), r.docenteId());

          ctx.status(201).json(Dtos.SeccionDto.desde(creada));
        });

    cfg.routes.post(
        "/api/secciones/{id}/estudiantes",
        ctx -> {
          int seccionId = Integer.parseInt(ctx.pathParam("id"));

          Dtos.InscripcionRequest r = ctx.bodyAsClass(Dtos.InscripcionRequest.class);

          Seccion actualizada = seccionController.agregarEstudiante(seccionId, r.estudianteId());

          ctx.json(Dtos.SeccionDto.desde(actualizada));
        });

    cfg.routes.put(
        "/api/secciones/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));

          Dtos.SeccionRequest r = ctx.bodyAsClass(Dtos.SeccionRequest.class);

          Seccion actualizada =
              seccionController.actualizar(id, r.codigo(), r.nombre(), r.aulaId(), r.docenteId());

          ctx.json(Dtos.SeccionDto.desde(actualizada));
        });

    cfg.routes.delete(
        "/api/secciones/{id}",
        ctx -> {
          int id = Integer.parseInt(ctx.pathParam("id"));

          seccionController.eliminar(id);

          ctx.status(204);
        });

    cfg.routes.delete(
        "/api/secciones/{id}/estudiantes/{estudianteId}",
        ctx -> {
          int seccionId = Integer.parseInt(ctx.pathParam("id"));

          int estudianteId = Integer.parseInt(ctx.pathParam("estudianteId"));

          seccionController.removerEstudiante(seccionId, estudianteId);

          ctx.status(204);
        });
  }

  // ── Matrícula (puente HTTP→socket) ──

  private static void registrarMatricula(JavalinConfig cfg) {
    cfg.routes.post(
        "/api/matricula",
        ctx -> {
          // Bridge HTTP→socket (provisto por el docente como ejemplo de sockets + archivos):
          // guarda el CSV que sube la SPA en ENTRADA_DIR y delega el lote al ServidorMatricula.
          // La lógica de la transacción vive en ServidorMatricula.procesarLote (la implementan
          // los estudiantes).
          MatriculaRequest r = ctx.bodyAsClass(MatriculaRequest.class);
          String archivo = r.archivo() != null ? r.archivo() : "matriculas.csv";
          String contenido = r.contenido() != null ? r.contenido() : "";
          Path entrada = Path.of(System.getenv("ENTRADA_DIR"));
          Files.createDirectories(entrada);
          Files.writeString(entrada.resolve(archivo), contenido);
          String host = System.getenv("MATRICULA_HOST");
          int puertoMatricula = Integer.parseInt(System.getenv("MATRICULA_PORT"));
          try (Socket socket = new Socket(host, puertoMatricula);
              PrintWriter out =
                  new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
              BufferedReader in =
                  new BufferedReader(
                      new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            out.println("MATRICULAR " + archivo);
            String respuesta = in.readLine();
            ctx.json(
                Map.of(
                    "respuesta",
                    respuesta != null ? respuesta : "sin respuesta del servicio de matricula"));
          }
        });
  }

  // ── Reporte (puente HTTP→socket) ──

  private static void registrarReporte(JavalinConfig cfg) {
    cfg.routes.post(
        "/api/reporte",
        ctx -> {
          // Bridge HTTP→socket (provisto por el docente como ejemplo de sockets + archivos):
          // pide el reporte al ServidorReportes y devuelve el TXT que la SPA descarga. La lógica
          // de conteo y escritura del archivo vive en ServidorReportes.generarYGuardar (la
          // implementan los estudiantes).
          String host = System.getenv("REPORTE_HOST");
          int puertoReporte = Integer.parseInt(System.getenv("REPORTE_PORT"));
          try (Socket socket = new Socket(host, puertoReporte);
              PrintWriter out =
                  new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
              BufferedReader in =
                  new BufferedReader(
                      new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            out.println("REPORTE");
            String encabezado = in.readLine(); // "200 <n>" en éxito, "500 <msg>" en error
            if (encabezado == null || !encabezado.startsWith("200 ")) {
              ctx.status(502).json(Map.of("error", "reporte no disponible: " + encabezado));
              return;
            }
            int lineas = Integer.parseInt(encabezado.substring("200 ".length()).trim());
            StringBuilder contenido = new StringBuilder();
            for (int i = 0; i < lineas; i++) {
              String linea = in.readLine();
              contenido.append(linea == null ? "" : linea).append("\n");
            }
            ctx.contentType("text/plain; charset=utf-8");
            ctx.header("Content-Disposition", "attachment; filename=\"reporte.txt\"");
            ctx.result(contenido.toString());
          }
        });
  }
}
