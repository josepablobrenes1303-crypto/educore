package edu.uam.educore.view;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public abstract class VistaBase {

  protected final Scanner scanner;

  protected VistaBase(Scanner scanner) {
    this.scanner = scanner;
  }

  // ── Salida ────────────────────────────────────────────────────────────────

  protected void mostrarMensaje(String msg) {
    System.out.println("[INFO] " + msg);
  }

  protected void mostrarError(String err) {
    System.out.println("[ERROR] " + err);
  }

  // ── Entrada ───────────────────────────────────────────────────────────────

  protected String leerTexto(String etiqueta) {
    System.out.print(etiqueta + ": ");
    return scanner.nextLine().trim();
  }

  protected int leerEntero(String etiqueta) {
    System.out.print(etiqueta + ": ");
    return leerEntero();
  }

  protected int leerEntero() {
    try {
      return Integer.parseInt(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  protected double leerDecimal(String etiqueta) {
    System.out.print(etiqueta + ": ");
    try {
      return Double.parseDouble(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  protected LocalDate leerFecha(String etiqueta) {
    System.out.print(etiqueta + ": ");
    try {
      return LocalDate.parse(scanner.nextLine().trim());
    } catch (DateTimeParseException e) {
      mostrarError("Fecha inválida. Use el formato AAAA-MM-DD.");
      return null;
    }
  }
}
