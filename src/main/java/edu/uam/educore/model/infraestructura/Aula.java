package edu.uam.educore.model.infraestructura;

public class Aula {
  private final int id;
  private String numero;
  private int capacidad;
  private TipoAula tipo;
  private Edificio edificio;

  public Aula(int id, String numero, int capacidad, TipoAula tipo, Edificio edificio) {
    this.id = id;
    this.numero = numero;
    this.capacidad = capacidad;
    this.tipo = tipo;
    this.edificio = edificio;
  }

  public int getId() {
    return id;
  }

  public String getNumero() {
    return numero;
  }

  public int getCapacidad() {
    return capacidad;
  }

  public TipoAula getTipo() {
    return tipo;
  }

  public Edificio getEdificio() {
    return edificio;
  }

  public void setNumero(String numero) {
    this.numero = numero;
  }

  public void setCapacidad(int capacidad) {
    this.capacidad = capacidad;
  }

  public void setTipo(TipoAula tipo) {
    this.tipo = tipo;
  }

  public void setEdificio(Edificio edificio) {
    this.edificio = edificio;
  }

  public String getInfo() {
    return String.format(
        "Aula #%d | Número: %s | Capacidad: %d | Tipo: %s | Edificio: %s",
        id, numero, capacidad, tipo, edificio.getCodigo());
  }
}
