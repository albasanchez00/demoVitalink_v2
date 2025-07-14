package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import java.util.List;
import java.util.Optional;

public interface TratamientoService {
    List<Tratamientos> listarTodos();
    Optional<Tratamientos> obtenerPorId(int id_tratamiento);
    Tratamientos guardar(Tratamientos tratamientos);
    void eliminar(int id_tratamiento);
}
