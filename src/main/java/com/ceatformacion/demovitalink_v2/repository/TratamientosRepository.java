package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TratamientosRepository extends JpaRepository <Tratamientos, Integer>{
    List<Tratamientos> findCitasByUsuario(Usuarios usuario);
}
