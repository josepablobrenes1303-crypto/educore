package edu.uam.educore.view;

import edu.uam.educore.controller.SeccionController;
import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.model.academico.Seccion;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.model.personas.Estudiante;
import java.util.List;
import java.util.Scanner;

public class SeccionView extends VistaBase {
  private final SeccionController controller;

  public SeccionView(
      Scanner scanner,
      Repositorio<Seccion> seccionRepo,
      Repositorio<Empleado> empleadoRepo,
      Repositorio<Estudiante> estudianteRepo,
      Repositorio<Edificio> edificioRepo) {
    super(scanner);
    this.controller =
        new SeccionController(seccionRepo, empleadoRepo, estudianteRepo, edificioRepo);
  }

  public void iniciar() {
    boolean activo = true;
    while (activo) {
      int opcion = mostrarMenu();
      if (opcion == 1) registrar();
      else if (opcion == 2) listar();
      else if (opcion == 3) agregarEstudiante();
      else if (opcion == 4) removerEstudiante();
      else if (opcion == 5) eliminar();
      else if (opcion == 0) activo = false;
      else mostrarError("Opción inválida.");
    }
  }

  private void registrar() {
    try {
      Seccion s =
          controller.registrar(
              leerTexto("Código"),
              leerTexto("Nombre del curso"),
              leerEntero("ID del aula"),
              leerEntero("ID del docente"));
      mostrarMensaje("Registrada — " + s.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void listar() {
    try {
      List<Seccion> lista = controller.listar();
      if (lista.isEmpty()) {
        mostrarMensaje("No hay secciones registradas.");
        return;
      }
      for (Seccion s : lista) System.out.println("  " + s.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void agregarEstudiante() {
    try {
      Seccion s =
          controller.agregarEstudiante(
              leerEntero("ID de la sección"), leerEntero("ID del estudiante"));
      mostrarMensaje("Estudiante inscrito — " + s.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void removerEstudiante() {
    try {
      Seccion s =
          controller.removerEstudiante(
              leerEntero("ID de la sección"), leerEntero("ID del estudiante"));
      mostrarMensaje("Estudiante removido — " + s.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void eliminar() {
    try {
      int id = leerEntero("ID de la sección");
      if (!leerTexto("¿Confirma la eliminación? (s/n)").equalsIgnoreCase("s")) {
        mostrarMensaje("Operación cancelada.");
        return;
      }
      controller.eliminar(id);
      mostrarMensaje("Sección eliminada.");
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private int mostrarMenu() {
    System.out.println("\n--- SECCIONES ---");
    System.out.println("1. Registrar sección");
    System.out.println("2. Listar secciones");
    System.out.println("3. Agregar estudiante");
    System.out.println("4. Remover estudiante");
    System.out.println("5. Eliminar sección");
    System.out.println("0. Volver");
    return leerEntero("Opción");
  }
}
