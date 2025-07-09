package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.CitasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CitasService {
    @Autowired
    private CitasRepository citasRepository;

    public void guardarCita(Citas cita) {
        citasRepository.save(cita);
    }

    public List<Citas> obtenerCitasPorUsuario(Usuarios usuario) {
        return citasRepository.findCitasByUsuario(usuario);
    }
}
