package edu.uam.educore.view;

import edu.uam.educore.controller.EmpleadoController;
import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.enums.TipoEmpleado;
import edu.uam.educore.model.personas.Empleado;
import java.util.List;
import java.util.Scanner;

public class EmpleadoView extends VistaBase {
  private final EmpleadoController controller;

  public EmpleadoView(Scanner scanner, Repositorio<Empleado> repo) {
    super(scanner);
    this.controller = new EmpleadoController(repo);
  }

  public void iniciar() {
    boolean activo = true;
    while (activo) {
      int opcion = mostrarMenu();
      if (opcion == 1) registrar();
      else if (opcion == 2) listar();
      else if (opcion == 3) buscar();
      else if (opcion == 4) actualizar();
      else if (opcion == 5) eliminar();
      else if (opcion == 0) activo = false;
      else mostrarError("Opción inválida.");
    }
  }

  private void registrar() {
    try {
      Empleado e =
          controller.registrar(
              leerTexto("Nombre"),
              leerTexto("Apellidos"),
              leerTexto("Email"),
              leerDecimal("Salario"),
              leerFecha("Fecha de ingreso (AAAA-MM-DD)"),
              leerTipoEmpleado());
      mostrarMensaje("Registrado — ID: " + e.getId() + "\n  " + e.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void listar() {
    try {
      List<Empleado> lista = controller.listar();
      if (lista.isEmpty()) {
        mostrarMensaje("No hay empleados registrados.");
        return;
      }
      System.out.println("\n--- EMPLEADOS REGISTRADOS (" + lista.size() + ") ---");
      for (Empleado e : lista) System.out.println("  " + e.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void buscar() {
    int id = leerEntero("ID del empleado");
    try {
      Empleado e = controller.buscarPorId(id);
      if (e == null) mostrarError("No existe empleado con ID " + id + ".");
      else System.out.println("\n  " + e.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void actualizar() {
    int id = leerEntero("ID del empleado a actualizar");
    try {
      Empleado existente = controller.buscarPorId(id);
      if (existente == null) {
        mostrarError("No existe empleado con ID " + id + ".");
        return;
      }
      System.out.println("\nDatos actuales:\n  " + existente.getInfo());
      Empleado actualizado =
          controller.actualizar(
              id,
              leerTexto("Nombre"),
              leerTexto("Apellidos"),
              leerTexto("Email"),
              leerDecimal("Salario"),
              leerFecha("Fecha de ingreso (AAAA-MM-DD)"),
              leerTipoEmpleado());
      mostrarMensaje("Actualizado — " + actualizado.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void eliminar() {
    int id = leerEntero("ID del empleado a eliminar");
    try {
      Empleado existente = controller.buscarPorId(id);
      if (existente == null) {
        mostrarError("No existe empleado con ID " + id + ".");
        return;
      }
      System.out.println("\n  " + existente.getInfo());
      if (!leerTexto("¿Confirma la eliminación? (s/n)").equalsIgnoreCase("s")) {
        mostrarMensaje("Operación cancelada.");
        return;
      }
      controller.eliminar(id);
      mostrarMensaje("Empleado con ID " + id + " eliminado.");
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private int mostrarMenu() {
    System.out.println("\n--- GESTIÓN DE EMPLEADOS ---");
    System.out.println("1. Registrar empleado");
    System.out.println("2. Listar todos");
    System.out.println("3. Buscar por ID");
    System.out.println("4. Actualizar empleado");
    System.out.println("5. Eliminar empleado");
    System.out.println("0. Volver al menú principal");
    System.out.print("Opción: ");
    return leerEntero();
  }

  private TipoEmpleado leerTipoEmpleado() {
    System.out.println("\nTipo de empleado:");
    TipoEmpleado[] tipos = TipoEmpleado.values();
    for (int i = 0; i < tipos.length; i++) System.out.println((i + 1) + ". " + tipos[i]);
    int opcion = leerEntero("Tipo");
    if (opcion < 1 || opcion > tipos.length) return null;
    return tipos[opcion - 1];
  }
}
