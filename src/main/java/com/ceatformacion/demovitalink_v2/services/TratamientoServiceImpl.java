package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.TratamientosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TratamientoServiceImpl implements TratamientoService{
    @Autowired
    private TratamientosRepository tratamientosRepository;

    @Override
    public List<Tratamientos> obtenerTratamientosPorUsuario(Usuarios usuario) {
        return tratamientosRepository.findTratamientosByUsuario(usuario);
    }

    @Override
    public Optional<Tratamientos> obtenerPorId(int id_tratamiento) {
        return Optional.empty();
    }

    @Override
    public Tratamientos guardar(Tratamientos tratamientos) {
        return tratamientosRepository.save(tratamientos);
    }

    @Override
    public void eliminar(int id_tratamiento) {

    }
}
