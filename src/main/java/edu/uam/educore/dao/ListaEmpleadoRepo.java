package edu.uam.educore.dao;

import edu.uam.educore.model.personas.Empleado;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ListaEmpleadoRepo extends Repositorio<Empleado> {

  private final List<Empleado> lista = new ArrayList<>();

  @Override
  public void guardar(Empleado e) {
    lista.add(e);
  }

  @Override
  public void actualizar(Empleado actualizado) {
    for (int i = 0; i < lista.size(); i++) {
      if (lista.get(i).getId() == actualizado.getId()) {
        lista.set(i, actualizado);
        return;
      }
    }
  }

  @Override
  public void eliminar(int id) {
    for (int i = 0; i < lista.size(); i++) {
      if (lista.get(i).getId() == id) {
        lista.remove(i);
        return;
      }
    }
  }

  @Override
  public Optional<Empleado> buscarPorId(int id) {
    for (Empleado e : lista) {
      if (e.getId() == id) {
        return Optional.of(e);
      }
    }
    return Optional.empty();
  }

  @Override
  public List<Empleado> buscarTodos() {
    return new ArrayList<>(lista);
  }
}
