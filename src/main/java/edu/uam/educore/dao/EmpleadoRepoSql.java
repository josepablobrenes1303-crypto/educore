
package edu.uam.educore.dao;
import edu.uam.educore.db.Conexion;
import edu.uam.educore.db.ConfiguracionBD;
import edu.uam.educore.enums.TipoEmpleado;
import edu.uam.educore.model.personas.Empleado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class EmpleadoRepoSql extends Repositorio<Empleado> {
private final ConfiguracionBD config;

public EmpleadoRepoSql(ConfiguracionBD config) {
  this.config = config;
}

private Connection abrir() throws Exception {
  return Conexion.getConnection(config.url(), config.usuario(), config.contrasena());
}

@Override
public void guardar(Empleado e) throws Exception {
  String sql =
      "INSERT INTO empleado (nombre, apellidos, email, salario, fecha_ingreso, tipo)"
          + " VALUES (?, ?, ?, ?, ?, ?)";

  try (Connection con = abrir();
      PreparedStatement ps =
          con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

    ps.setString(1, e.getNombre());
    ps.setString(2, e.getApellidos());
    ps.setString(3, e.getEmail());
    ps.setDouble(4, e.getSalario());
    ps.setDate(5, java.sql.Date.valueOf(e.getFechaIngreso()));
    ps.setString(6, e.getTipoEmpleado().name());

    ps.executeUpdate();

    try (ResultSet claves = ps.getGeneratedKeys()) {
      if (claves.next()) {
        e.setId(claves.getInt(1));
      }
    }
  }
}

@Override
public void actualizar(Empleado e) throws Exception {
  String sql =
      "UPDATE empleado SET nombre=?, apellidos=?, email=?, salario=?, fecha_ingreso=?, tipo=?"
          + " WHERE id=?";

  try (Connection con = abrir();
      PreparedStatement ps = con.prepareStatement(sql)) {

    ps.setString(1, e.getNombre());
    ps.setString(2, e.getApellidos());
    ps.setString(3, e.getEmail());
    ps.setDouble(4, e.getSalario());
    ps.setDate(5, java.sql.Date.valueOf(e.getFechaIngreso()));
    ps.setString(6, e.getTipoEmpleado().name());
    ps.setInt(7, e.getId());

    ps.executeUpdate();
  }
}
@Override
public void eliminar(int id) throws Exception {
  try (Connection con = abrir();
      PreparedStatement ps =
          con.prepareStatement("DELETE FROM empleado WHERE id=?")) {

    ps.setInt(1, id);
    ps.executeUpdate();
  }
}
@Override
public Optional<Empleado> buscarPorId(int id) throws Exception {

  try (Connection con = abrir();
      PreparedStatement ps =
          con.prepareStatement("SELECT * FROM empleado WHERE id=?")) {

    ps.setInt(1, id);

    try (ResultSet rs = ps.executeQuery()) {

      if (rs.next()) {
        return Optional.of(mapear(rs));
      }

      return Optional.empty();
    }
  }
}
@Override
public List<Empleado> buscarTodos() throws Exception {

  List<Empleado> lista = new ArrayList<>();

  try (Connection con = abrir();
      PreparedStatement ps =
          con.prepareStatement("SELECT * FROM empleado");
      ResultSet rs = ps.executeQuery()) {

    while (rs.next()) {
      lista.add(mapear(rs));
    }
  }

  return lista;
}
private Empleado mapear(ResultSet rs) throws Exception {

  return new Empleado(
      rs.getInt("id"),
      rs.getString("nombre"),
      rs.getString("apellidos"),
      rs.getString("email"),
      rs.getDouble("salario"),
      rs.getDate("fecha_ingreso").toLocalDate(),
      TipoEmpleado.valueOf(rs.getString("tipo")));
}

}
