package edu.uam.educore.model.infraestructura;

import java.util.ArrayList;
import java.util.List;

public class Edificio {
  private final int id;
  private String codigo;
  private String nombre;
  private final List<Aula> aulas = new ArrayList<>();

  public Edificio(int id, String codigo, String nombre) {
    this.id = id;
    this.codigo = codigo;
    this.nombre = nombre;
  }

  public int getId() {
    return id;
  }

  public String getCodigo() {
    return codigo;
  }

  public String getNombre() {
    return nombre;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public void agregarAula(Aula aula) {
    aulas.add(aula);
  }

  public void removerAula(int aulaId) {
    aulas.removeIf(a -> a.getId() == aulaId);
  }

  public List<Aula> listarAulas() {
    return new ArrayList<>(aulas);
  }

  public Aula buscarAulaPorNumero(String numero) {
    for (Aula aula : aulas) {
      if (aula.getNumero().equalsIgnoreCase(numero)) {
        return aula;
      }
    }
    return null;
  }

  public Aula buscarAulaPorId(int aulaId) {
    for (Aula aula : aulas) {
      if (aula.getId() == aulaId) {
        return aula;
      }
    }
    return null;
  }

  public String getInfo() {
    return String.format(
        "Edificio #%d | Código: %s | Nombre: %s | Aulas: %d", id, codigo, nombre, aulas.size());
  }
}
