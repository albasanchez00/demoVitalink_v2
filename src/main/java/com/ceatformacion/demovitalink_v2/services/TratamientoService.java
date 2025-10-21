package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TratamientoService {
    List<Tratamientos> obtenerTratamientosPorUsuario(Usuarios usuario);
    List<Tratamientos> obtenerTratamientosPorIdUsuario(int id_usuario); // <- nuevo
    Optional<Tratamientos> obtenerPorId(int id_tratamiento);
    Tratamientos guardar(Tratamientos tratamientos);
    void eliminar(int id_tratamiento);
    List<Tratamientos> obtenerTodos();
    Optional<Tratamientos> buscarPorId(Integer id_tratamiento);
    void finalizar(Integer id);
    void eliminar(Integer id);
    // 🆕 ADMIN
    Page<Tratamientos> buscarAdmin(String q, String estado, Integer idUsuario, Pageable pageable);
}
