package edu.uam.educore.controller;

import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.model.infraestructura.TipoAula;
import java.util.List;
import java.util.Optional;

public class EdificioController {
  private final Repositorio<Edificio> repo;
  private int proximoEdificioId = 1;
  private int proximoAulaId = 1;

  public EdificioController(Repositorio<Edificio> repo) {
    this.repo = repo;
  }

  public Edificio registrar(String codigo, String nombre) throws Exception {
    validarTexto(codigo, "El código del edificio es obligatorio.");
    validarTexto(nombre, "El nombre del edificio es obligatorio.");
    Edificio edificio = new Edificio(proximoEdificioId, codigo, nombre);
    repo.guardar(edificio);
    proximoEdificioId++;
    return edificio;
  }

  public List<Edificio> listar() throws Exception {
    return repo.buscarTodos();
  }

  public Edificio buscarPorId(int id) throws Exception {
    Optional<Edificio> resultado = repo.buscarPorId(id);
    return resultado.orElse(null);
  }

  public Aula agregarAula(int edificioId, String numero, int capacidad, TipoAula tipo)
      throws Exception {
    Edificio edificio = buscarPorId(edificioId);
    if (edificio == null)
      throw new IllegalArgumentException("No existe edificio con ID " + edificioId + ".");
    validarTexto(numero, "El número del aula es obligatorio.");
    if (capacidad <= 0) throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
    if (tipo == null) throw new IllegalArgumentException("El tipo de aula es obligatorio.");
    Aula aula = new Aula(proximoAulaId, numero, capacidad, tipo, edificio);
    edificio.agregarAula(aula);
    repo.actualizar(edificio);
    proximoAulaId++;
    return aula;
  }

  public void eliminarAula(int edificioId, int aulaId) throws Exception {
    Edificio edificio = buscarPorId(edificioId);
    if (edificio == null)
      throw new IllegalArgumentException("No existe edificio con ID " + edificioId + ".");
    if (edificio.buscarAulaPorId(aulaId) == null)
      throw new IllegalArgumentException("No existe aula con ID " + aulaId + " en ese edificio.");
    edificio.removerAula(aulaId);
    repo.actualizar(edificio);
  }

  public void eliminar(int id) throws Exception {
    Edificio edificio = buscarPorId(id);
    if (edificio == null)
      throw new IllegalArgumentException("No existe edificio con ID " + id + ".");
    if (!edificio.listarAulas().isEmpty())
      throw new IllegalArgumentException(
          "No se puede eliminar un edificio que todavía tiene aulas.");
    repo.eliminar(id);
  }

  private void validarTexto(String valor, String mensaje) {
    if (valor == null || valor.trim().isEmpty()) throw new IllegalArgumentException(mensaje);
  }
}
