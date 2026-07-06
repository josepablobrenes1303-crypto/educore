package edu.uam.educore.view;

import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.model.academico.Seccion;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.model.personas.Estudiante;
import java.util.Scanner;

public class AcademicoView extends VistaBase {
  private final EdificioView edificioView;
  private final SeccionView seccionView;

  public AcademicoView(
      Scanner scanner,
      Repositorio<Edificio> edificioRepo,
      Repositorio<Seccion> seccionRepo,
      Repositorio<Empleado> empleadoRepo,
      Repositorio<Estudiante> estudianteRepo) {
    super(scanner);
    this.edificioView = new EdificioView(scanner, edificioRepo);
    this.seccionView =
        new SeccionView(scanner, seccionRepo, empleadoRepo, estudianteRepo, edificioRepo);
  }

  public void iniciar() {
    boolean activo = true;
    while (activo) {
      System.out.println("\n--- GESTIÓN ACADÉMICA ---");
      System.out.println("1. Edificios y aulas");
      System.out.println("2. Secciones");
      System.out.println("0. Volver al menú principal");
      int opcion = leerEntero("Opción");
      if (opcion == 1) edificioView.iniciar();
      else if (opcion == 2) seccionView.iniciar();
      else if (opcion == 0) activo = false;
      else mostrarError("Opción inválida.");
    }
  }
}
