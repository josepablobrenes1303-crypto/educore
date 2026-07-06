package edu.uam.educore.model.personas;

import static org.junit.jupiter.api.Assertions.*;

import edu.uam.educore.enums.TipoEmpleado;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EmpleadoTest {

  @Test
  void creaEmpleadoDocenteCorrectamente() {
    Empleado empleado =
        new Empleado(
            1,
            "Ana",
            "Mora",
            "ana@uam.ac.cr",
            500000,
            LocalDate.of(2024, 1, 10),
            TipoEmpleado.DOCENTE);
    assertEquals(1, empleado.getId());
    assertEquals(500000, empleado.getSalario());
    assertEquals(TipoEmpleado.DOCENTE, empleado.getTipoEmpleado());
  }

  @Test
  void getTipoRetornaTipoEmpleado() {
    Empleado empleado =
        new Empleado(
            2,
            "Luis",
            "Rojas",
            "luis@uam.ac.cr",
            400000,
            LocalDate.of(2023, 5, 20),
            TipoEmpleado.ADMINISTRATIVO);
    assertEquals("ADMINISTRATIVO", empleado.getTipo());
  }

  @Test
  void getInfoIncluyeDatosPrincipales() {
    Empleado empleado =
        new Empleado(
            3,
            "María",
            "Solís",
            "maria@uam.ac.cr",
            450000,
            LocalDate.of(2022, 3, 1),
            TipoEmpleado.GUARDA);
    String info = empleado.getInfo();
    assertTrue(info.contains("GUARDA"));
    assertTrue(info.contains("María"));
    assertTrue(info.contains("Solís"));
    assertTrue(info.contains("450000"));
  }
}
