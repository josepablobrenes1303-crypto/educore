package edu.uam.educore.controller;

import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.enums.TipoEmpleado;
import edu.uam.educore.model.academico.Seccion;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.model.personas.Estudiante;
import java.util.List;
import java.util.Optional;

public class SeccionController {
  private final Repositorio<Seccion> seccionRepo;
  private final Repositorio<Empleado> empleadoRepo;
  private final Repositorio<Estudiante> estudianteRepo;
  private final Repositorio<Edificio> edificioRepo;
  private int proximoId = 1;

  public SeccionController(
      Repositorio<Seccion> seccionRepo,
      Repositorio<Empleado> empleadoRepo,
      Repositorio<Estudiante> estudianteRepo,
      Repositorio<Edificio> edificioRepo) {
    this.seccionRepo = seccionRepo;
    this.empleadoRepo = empleadoRepo;
    this.estudianteRepo = estudianteRepo;
    this.edificioRepo = edificioRepo;
  }

  public Seccion registrar(String codigo, String nombre, int aulaId, int docenteId)
      throws Exception {
    validarTexto(codigo, "El código de la sección es obligatorio.");
    validarTexto(nombre, "El nombre del curso es obligatorio.");
    Empleado docente = empleadoRepo.buscarPorId(docenteId).orElse(null);
    if (docente == null)
      throw new IllegalArgumentException("No existe empleado con ID " + docenteId + ".");
    if (docente.getTipoEmpleado() != TipoEmpleado.DOCENTE)
      throw new IllegalArgumentException("El empleado seleccionado no es DOCENTE.");
    Aula aula = buscarAulaPorId(aulaId);
    if (aula == null) throw new IllegalArgumentException("No existe aula con ID " + aulaId + ".");
    Seccion seccion = new Seccion(proximoId, codigo, nombre, docente, aula);
    seccionRepo.guardar(seccion);
    proximoId++;
    return seccion;
  }

  public List<Seccion> listar() throws Exception {
    return seccionRepo.buscarTodos();
  }

  public Seccion buscarPorId(int id) throws Exception {
    Optional<Seccion> resultado = seccionRepo.buscarPorId(id);
    return resultado.orElse(null);
  }

  public Seccion agregarEstudiante(int seccionId, int estudianteId) throws Exception {
    Seccion seccion = buscarPorId(seccionId);
    if (seccion == null)
      throw new IllegalArgumentException("No existe sección con ID " + seccionId + ".");
    Estudiante estudiante = estudianteRepo.buscarPorId(estudianteId).orElse(null);
    if (estudiante == null)
      throw new IllegalArgumentException("No existe estudiante con ID " + estudianteId + ".");
    seccion.agregarEstudiante(estudiante);
    seccionRepo.actualizar(seccion);
    return seccion;
  }

  public Seccion removerEstudiante(int seccionId, int estudianteId) throws Exception {
    Seccion seccion = buscarPorId(seccionId);
    if (seccion == null)
      throw new IllegalArgumentException("No existe sección con ID " + seccionId + ".");
    seccion.removerEstudiante(estudianteId);
    seccionRepo.actualizar(seccion);
    return seccion;
  }

  public void eliminar(int id) throws Exception {
    Seccion seccion = buscarPorId(id);
    if (seccion == null) throw new IllegalArgumentException("No existe sección con ID " + id + ".");
    if (!seccion.listarEstudiantes().isEmpty())
      throw new IllegalArgumentException(
          "No se puede eliminar una sección con estudiantes inscritos.");
    seccionRepo.eliminar(id);
  }

  private Aula buscarAulaPorId(int aulaId) throws Exception {
    for (Edificio edificio : edificioRepo.buscarTodos()) {
      Aula aula = edificio.buscarAulaPorId(aulaId);
      if (aula != null) return aula;
    }
    return null;
  }

  private void validarTexto(String valor, String mensaje) {
    if (valor == null || valor.trim().isEmpty()) throw new IllegalArgumentException(mensaje);
  }
}
