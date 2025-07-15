package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;

import java.util.List;
import java.util.Optional;

public interface TratamientoService {
    List<Tratamientos> obtenerTratamientosPorUsuario(Usuarios usuario);
    Optional<Tratamientos> obtenerPorId(int id_tratamiento);
    Tratamientos guardar(Tratamientos tratamientos);
    void eliminar(int id_tratamiento);
}
