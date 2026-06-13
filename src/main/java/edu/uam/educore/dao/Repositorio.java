package edu.uam.educore.dao;

import java.util.List;
import java.util.Optional;

public abstract class Repositorio<T> {
  public abstract void guardar(T entidad) throws Exception;

  public abstract void actualizar(T entidad) throws Exception;

  public abstract void eliminar(int id) throws Exception;

  public abstract Optional<T> buscarPorId(int id) throws Exception;

  public abstract List<T> buscarTodos() throws Exception;
}
