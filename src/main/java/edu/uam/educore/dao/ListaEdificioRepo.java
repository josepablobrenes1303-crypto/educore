package edu.uam.educore.dao;

import edu.uam.educore.model.infraestructura.Edificio;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ListaEdificioRepo extends Repositorio<Edificio> {
  private final List<Edificio> lista = new ArrayList<>();

  @Override
  public void guardar(Edificio e) {
    lista.add(e);
  }

  @Override
  public void actualizar(Edificio actualizado) {
    for (int i = 0; i < lista.size(); i++) {
      if (lista.get(i).getId() == actualizado.getId()) {
        lista.set(i, actualizado);
        return;
      }
    }
  }

  @Override
  public void eliminar(int id) {
    lista.removeIf(e -> e.getId() == id);
  }

  @Override
  public Optional<Edificio> buscarPorId(int id) {
    for (Edificio e : lista) {
      if (e.getId() == id) return Optional.of(e);
    }
    return Optional.empty();
  }

  @Override
  public List<Edificio> buscarTodos() {
    return new ArrayList<>(lista);
  }
}
