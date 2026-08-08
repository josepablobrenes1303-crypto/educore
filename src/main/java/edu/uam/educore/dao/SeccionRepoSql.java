package edu.uam.educore.dao;

import edu.uam.educore.db.Conexion;
import edu.uam.educore.db.ConfiguracionBD;
import edu.uam.educore.enums.TipoEmpleado;
import edu.uam.educore.model.academico.Seccion;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.model.infraestructura.TipoAula;
import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.model.personas.Estudiante;
import edu.uam.educore.model.personas.EstudianteBecado;
import edu.uam.educore.model.personas.EstudianteRegular;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeccionRepoSql extends Repositorio<Seccion> {

  private final ConfiguracionBD config;

  public SeccionRepoSql(ConfiguracionBD config) {
    this.config = config;
  }

  private Connection abrir() throws Exception {
    return Conexion.getConnection(config.url(), config.usuario(), config.contrasena());
  }

  @Override
  public void guardar(Seccion seccion) throws Exception {
    String sql =
        "INSERT INTO seccion (codigo, nombre, docente_id, aula_id) " + "VALUES (?, ?, ?, ?)";

    try (Connection con = abrir();
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      ps.setString(1, seccion.getCodigo());
      ps.setString(2, seccion.getNombre());
      ps.setInt(3, seccion.getDocente().getId());
      ps.setInt(4, seccion.getAula().getId());

      ps.executeUpdate();

      try (ResultSet claves = ps.getGeneratedKeys()) {
        if (claves.next()) {
          seccion.setId(claves.getInt(1));
        }
      }
    }
  }

  @Override
  public void actualizar(Seccion seccion) throws Exception {
    String sql = "UPDATE seccion SET codigo=?, nombre=?, docente_id=?, aula_id=? " + "WHERE id=?";

    try (Connection con = abrir()) {
      con.setAutoCommit(false);

      try {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
          ps.setString(1, seccion.getCodigo());
          ps.setString(2, seccion.getNombre());
          ps.setInt(3, seccion.getDocente().getId());
          ps.setInt(4, seccion.getAula().getId());
          ps.setInt(5, seccion.getId());

          ps.executeUpdate();
        }

        try (PreparedStatement ps =
            con.prepareStatement("DELETE FROM matricula WHERE seccion_id=?")) {
          ps.setInt(1, seccion.getId());
          ps.executeUpdate();
        }

        String sqlMatricula = "INSERT INTO matricula (seccion_id, estudiante_id) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sqlMatricula)) {
          for (Estudiante estudiante : seccion.listarEstudiantes()) {
            ps.setInt(1, seccion.getId());
            ps.setInt(2, estudiante.getId());
            ps.addBatch();
          }

          ps.executeBatch();
        }

        con.commit();

      } catch (Exception e) {
        con.rollback();
        throw e;

      } finally {
        con.setAutoCommit(true);
      }
    }
  }

  @Override
  public void eliminar(int id) throws Exception {
    String sql = "DELETE FROM seccion WHERE id=?";

    try (Connection con = abrir();
        PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setInt(1, id);
      ps.executeUpdate();
    }
  }

  @Override
  public Optional<Seccion> buscarPorId(int id) throws Exception {
    String sql =
        "SELECT s.id, s.codigo, s.nombre, "
            + "e.id AS docente_id, e.nombre AS docente_nombre, "
            + "e.apellidos AS docente_apellidos, e.email AS docente_email, "
            + "e.salario, e.fecha_ingreso, e.tipo AS docente_tipo, "
            + "a.id AS aula_id, a.numero, a.capacidad, a.tipo AS aula_tipo, "
            + "ed.id AS edificio_id, ed.codigo AS edificio_codigo, "
            + "ed.nombre AS edificio_nombre "
            + "FROM seccion s "
            + "JOIN empleado e ON s.docente_id = e.id "
            + "JOIN aula a ON s.aula_id = a.id "
            + "JOIN edificio ed ON a.edificio_id = ed.id "
            + "WHERE s.id=?";

    try (Connection con = abrir();
        PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setInt(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Seccion seccion = mapear(rs);
          cargarEstudiantes(con, seccion);
          return Optional.of(seccion);
        }
      }
    }

    return Optional.empty();
  }

  @Override
  public List<Seccion> buscarTodos() throws Exception {
    List<Seccion> secciones = new ArrayList<>();

    String sql =
        "SELECT s.id, s.codigo, s.nombre, "
            + "e.id AS docente_id, e.nombre AS docente_nombre, "
            + "e.apellidos AS docente_apellidos, e.email AS docente_email, "
            + "e.salario, e.fecha_ingreso, e.tipo AS docente_tipo, "
            + "a.id AS aula_id, a.numero, a.capacidad, a.tipo AS aula_tipo, "
            + "ed.id AS edificio_id, ed.codigo AS edificio_codigo, "
            + "ed.nombre AS edificio_nombre "
            + "FROM seccion s "
            + "JOIN empleado e ON s.docente_id = e.id "
            + "JOIN aula a ON s.aula_id = a.id "
            + "JOIN edificio ed ON a.edificio_id = ed.id "
            + "ORDER BY s.id";

    try (Connection con = abrir();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        Seccion seccion = mapear(rs);
        cargarEstudiantes(con, seccion);
        secciones.add(seccion);
      }
    }

    return secciones;
  }

  private Seccion mapear(ResultSet rs) throws Exception {

    Edificio edificio =
        new Edificio(
            rs.getInt("edificio_id"),
            rs.getString("edificio_codigo"),
            rs.getString("edificio_nombre"));

    Aula aula =
        new Aula(
            rs.getInt("aula_id"),
            rs.getString("numero"),
            rs.getInt("capacidad"),
            TipoAula.valueOf(rs.getString("aula_tipo")),
            edificio);

    edificio.agregarAula(aula);

    Empleado docente =
        new Empleado(
            rs.getInt("docente_id"),
            rs.getString("docente_nombre"),
            rs.getString("docente_apellidos"),
            rs.getString("docente_email"),
            rs.getDouble("salario"),
            rs.getDate("fecha_ingreso").toLocalDate(),
            TipoEmpleado.valueOf(rs.getString("docente_tipo")));

    return new Seccion(
        rs.getInt("id"), rs.getString("codigo"), rs.getString("nombre"), docente, aula);
  }

  private void cargarEstudiantes(Connection con, Seccion seccion) throws Exception {

    String sql =
        "SELECT e.id, e.tipo, e.nombre, e.apellidos, e.email, "
            + "e.carnet, e.porcentaje_beca "
            + "FROM estudiante e "
            + "JOIN matricula m ON m.estudiante_id = e.id "
            + "WHERE m.seccion_id=? "
            + "ORDER BY e.id";

    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setInt(1, seccion.getId());

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {

          Estudiante estudiante;

          if ("BECADO".equals(rs.getString("tipo"))) {
            estudiante =
                new EstudianteBecado(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellidos"),
                    rs.getString("email"),
                    rs.getString("carnet"),
                    rs.getDouble("porcentaje_beca"));
          } else {
            estudiante =
                new EstudianteRegular(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellidos"),
                    rs.getString("email"),
                    rs.getString("carnet"));
          }

          seccion.agregarEstudiante(estudiante);
        }
      }
    }
  }
}
