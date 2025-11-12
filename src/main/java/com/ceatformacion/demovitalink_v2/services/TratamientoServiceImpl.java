package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.TratamientosRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    // 🆕 ADMIN
    @Override
    public Page<Tratamientos> buscarAdmin(String q, String estado, Integer idUsuario, Pageable pageable) {
        String qNorm = (q==null || q.isBlank()) ? null : q.trim();
        String estNorm = (estado==null || estado.isBlank()) ? null : estado.trim();
        return repo.buscarAdmin(qNorm, estNorm, idUsuario, pageable);
    }

    @Transactional
    public void actualizarAdmin(Integer id, Map<String, Object> body) {
        Tratamientos t = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tratamiento no encontrado"));

        if (body.containsKey("nombre_tratamiento"))
            t.setNombre_tratamiento((String) body.get("nombre_tratamiento"));
        if (body.containsKey("estado_tratamiento"))
            t.setEstado_tratamiento((String) body.get("estado_tratamiento"));
        if (body.containsKey("fecha_inicio"))
            t.setFecha_inicio(LocalDate.parse((String) body.get("fecha_inicio")));
        if (body.containsKey("fecha_fin"))
            t.setFecha_fin(LocalDate.parse((String) body.get("fecha_fin")));

        repo.save(t);
    }

    @Transactional
    public void eliminarAdmin(Integer id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Tratamiento no encontrado");
        }
        repo.deleteById(id);
    }
}
