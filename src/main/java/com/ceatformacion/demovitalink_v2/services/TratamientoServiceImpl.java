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
    @Autowired private TratamientosRepository repo;

    @Override
    public List<Tratamientos> obtenerTratamientosPorUsuario(Usuarios usuario) {
        return repo.findTratamientosByUsuario(usuario);
    }

    @Override
    public List<Tratamientos> obtenerTratamientosPorIdUsuario(int id_usuario) {
        return repo.findByUsuarioId(id_usuario);
    }
    // TratamientoServiceImpl.java
    @Override
    public List<Tratamientos> obtenerTodos() {
        return repo.findAll();
    }
    public Optional<Tratamientos> buscarPorId(Integer id_tratamiento){
        return repo.findById(id_tratamiento);
    }

    @Override
    public void finalizar(Integer id) {
        Tratamientos t = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tratamiento no encontrado"));
        t.setEstado_tratamiento("Finalizado");
        if (t.getFecha_fin() == null) {
            t.setFecha_fin(java.time.LocalDate.now());
        }
        repo.save(t);
    }
    @Override
    public void eliminar(Integer id) {
        repo.deleteById(id);
    }

    @Override
    public Optional<Tratamientos> obtenerPorId(int id_tratamiento) {
        return repo.findById(id_tratamiento);
    }

    @Override
    public Tratamientos guardar(Tratamientos tratamientos) {
        return repo.save(tratamientos);
    }

    @Override
    public void eliminar(int id_tratamiento) {
        repo.deleteById(id_tratamiento);
    }
}
