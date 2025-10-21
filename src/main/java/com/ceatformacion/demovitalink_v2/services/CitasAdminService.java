package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.EstadoCita;
import com.ceatformacion.demovitalink_v2.repository.CitasRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class CitasAdminService {

    private final CitasRepository repo;

    public CitasAdminService(CitasRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public Page<Citas> buscar(String q,
                              EstadoCita estado,
                              LocalDate desde,
                              LocalDate hasta,
                              Integer idPaciente,
                              Integer idMedico,
                              Pageable pageable) {
        String term = (q==null || q.isBlank()) ? null : q.trim();
        return repo.buscarAdmin(term, estado, desde, hasta, idPaciente, idMedico, pageable);
    }

    @Transactional(readOnly = true)
    public Citas getById(Integer id){
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
    }

    @Transactional
    public void cancelar(Integer id){
        Citas c = getById(id);
        c.setEstado(EstadoCita.CANCELADA);
        repo.save(c);
    }
}