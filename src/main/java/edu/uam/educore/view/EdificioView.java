package edu.uam.educore.view;

import edu.uam.educore.controller.EdificioController;
import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.model.infraestructura.TipoAula;
import java.util.List;
import java.util.Scanner;

public class EdificioView extends VistaBase {
  private final EdificioController controller;

  public EdificioView(Scanner scanner, Repositorio<Edificio> repo) {
    super(scanner);
    this.controller = new EdificioController(repo);
  }

  public void iniciar() {
    boolean activo = true;
    while (activo) {
      int opcion = mostrarMenu();
      if (opcion == 1) registrarEdificio();
      else if (opcion == 2) listarEdificios();
      else if (opcion == 3) buscarEdificio();
      else if (opcion == 4) agregarAula();
      else if (opcion == 5) listarAulas();
      else if (opcion == 6) eliminarAula();
      else if (opcion == 7) eliminarEdificio();
      else if (opcion == 0) activo = false;
      else mostrarError("Opción inválida.");
    }
  }

  private void registrarEdificio() {
    try {
      Edificio e = controller.registrar(leerTexto("Código"), leerTexto("Nombre"));
      mostrarMensaje("Registrado — " + e.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void listarEdificios() {
    try {
      List<Edificio> lista = controller.listar();
      if (lista.isEmpty()) {
        mostrarMensaje("No hay edificios registrados.");
        return;
      }
      for (Edificio e : lista) System.out.println("  " + e.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void buscarEdificio() {
    int id = leerEntero("ID del edificio");
    try {
      Edificio e = controller.buscarPorId(id);
      if (e == null) {
        mostrarError("No existe edificio con ID " + id + ".");
        return;
      }
      System.out.println("  " + e.getInfo());
      for (Aula aula : e.listarAulas()) System.out.println("    " + aula.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void agregarAula() {
    try {
      Aula aula =
          controller.agregarAula(
              leerEntero("ID del edificio"),
              leerTexto("Número de aula"),
              leerEntero("Capacidad"),
              leerTipoAula());
      mostrarMensaje("Aula agregada — " + aula.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void listarAulas() {
    int id = leerEntero("ID del edificio");
    try {
      Edificio e = controller.buscarPorId(id);
      if (e == null) {
        mostrarError("No existe edificio con ID " + id + ".");
        return;
      }
      if (e.listarAulas().isEmpty()) {
        mostrarMensaje("El edificio no tiene aulas.");
        return;
      }
      for (Aula aula : e.listarAulas()) System.out.println("  " + aula.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void eliminarAula() {
    try {
      int edificioId = leerEntero("ID del edificio");
      int aulaId = leerEntero("ID del aula");
      if (!leerTexto("¿Confirma la eliminación? (s/n)").equalsIgnoreCase("s")) {
        mostrarMensaje("Operación cancelada.");
        return;
      }
      controller.eliminarAula(edificioId, aulaId);
      mostrarMensaje("Aula eliminada.");
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void eliminarEdificio() {
    try {
      int id = leerEntero("ID del edificio");
      if (!leerTexto("¿Confirma la eliminación? (s/n)").equalsIgnoreCase("s")) {
        mostrarMensaje("Operación cancelada.");
        return;
      }
      controller.eliminar(id);
      mostrarMensaje("Edificio eliminado.");
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private int mostrarMenu() {
    System.out.println("\n--- EDIFICIOS Y AULAS ---");
    System.out.println("1. Registrar edificio");
    System.out.println("2. Listar edificios");
    System.out.println("3. Buscar edificio por ID");
    System.out.println("4. Agregar aula");
    System.out.println("5. Listar aulas de edificio");
    System.out.println("6. Eliminar aula");
    System.out.println("7. Eliminar edificio");
    System.out.println("0. Volver");
    return leerEntero("Opción");
  }

  private TipoAula leerTipoAula() {
    TipoAula[] tipos = TipoAula.values();
    for (int i = 0; i < tipos.length; i++) System.out.println((i + 1) + ". " + tipos[i]);
    int opcion = leerEntero("Tipo");
    if (opcion < 1 || opcion > tipos.length) return null;
    return tipos[opcion - 1];
  }
}
