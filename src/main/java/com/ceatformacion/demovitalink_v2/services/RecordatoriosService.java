package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.RecordatoriosDTO;
import com.ceatformacion.demovitalink_v2.mapper.RecordatoriosMapper;
import com.ceatformacion.demovitalink_v2.model.Recordatorios;
import com.ceatformacion.demovitalink_v2.model.TipoRecordatorio;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.RecordatoriosRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class RecordatoriosService {

    private final RecordatoriosRepository repo;
    private final UsuariosRepository usuariosRepo;
    private final RecordatoriosMapper mapper;

    public RecordatoriosService(RecordatoriosRepository repo, UsuariosRepository usuariosRepo, RecordatoriosMapper mapper) {
        this.repo = repo;
        this.usuariosRepo = usuariosRepo;
        this.mapper = mapper;
    }

    public List<RecordatoriosDTO> listar(int id_usuario) {
        return repo.findAllByUsuarioId(id_usuario)
                .stream().map(mapper::toDTO).toList();
    }

    public List<RecordatoriosDTO> listarRango(int id_usuario, LocalDateTime desde, LocalDateTime hasta) {
        return repo.findAllByUsuarioIdAndFechaBetween(id_usuario, desde, hasta)
                .stream().map(mapper::toDTO).toList();
    }

    public List<RecordatoriosDTO> listarPorTipo(int id_usuario, TipoRecordatorio tipo) {
        return repo.findAllByUsuarioIdAndTipo(id_usuario, tipo)
                .stream().map(mapper::toDTO).toList();
    }


    public RecordatoriosDTO crear(RecordatoriosDTO dto) {
        Usuarios u = usuariosRepo.findById(dto.id_usuario()).orElseThrow();
        Recordatorios r = mapper.toEntity(dto, u);
        return mapper.toDTO(repo.save(r));
    }

    public RecordatoriosDTO actualizar(int id_recordatorio, RecordatoriosDTO dto) {
        Recordatorios r = repo.findById(id_recordatorio).orElseThrow();
        r.setTitulo(dto.titulo());
        r.setTipo(dto.tipo());
        r.setFechaHora(dto.fechaHora());
        r.setRepeticion(dto.repeticion());
        r.setCanal(dto.canal());
        r.setVinculoTipo(dto.vinculoTipo());
        r.setVinculoId(dto.vinculoId());
        r.setDescripcion(dto.descripcion());
        r.setCompletado(dto.completado());
        return mapper.toDTO(r);
    }

    public void eliminar(int id_recordatorio) { repo.deleteById(id_recordatorio); }

    public RecordatoriosDTO toggleCompletado(int id_recordatorio, boolean completado){
        Recordatorios r = repo.findById(id_recordatorio).orElseThrow();
        r.setCompletado(completado);
        return mapper.toDTO(r);
    }
}

