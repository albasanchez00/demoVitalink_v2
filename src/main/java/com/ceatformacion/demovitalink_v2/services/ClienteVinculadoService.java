package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.ClienteVinculadoDTO;
import com.ceatformacion.demovitalink_v2.mapper.ClienteVinculadoMapper;
import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.repository.CitasRepository;
import com.ceatformacion.demovitalink_v2.repository.ClientesRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClienteVinculadoService {

    private final ClientesRepository clientesRepo;
    private final CitasRepository citasRepo;

    public ClienteVinculadoService(ClientesRepository clientesRepo, CitasRepository citasRepo) {
        this.clientesRepo = clientesRepo;
        this.citasRepo = citasRepo;
    }

    public Page<ClienteVinculadoDTO> buscarVinculados(
            Integer medicoId, String q, String estado, Pageable pageable) {

        // obtenemos los clientes del médico
        Page<Clientes> page = clientesRepo.buscarVinculados(medicoId, q, estado, pageable);

        // mapeamos a DTO y añadimos la última consulta (fecha + hora)
        List<ClienteVinculadoDTO> dtos = page.getContent().stream().map(c -> {
            var u = c.getUsuario(); // puede ser null (cliente sin cuenta de usuario)
            LocalDateTime ultima = null;

            if (u != null) {
                // buscamos la última cita de este usuario (paciente)
                List<Citas> citas = citasRepo.findTopByUsuarioOrderByFechaHoraDesc(u.getId_usuario());
                if (!citas.isEmpty()) {
                    var cita = citas.get(0);
                    ultima = LocalDateTime.of(cita.getFecha(), cita.getHora());
                }
            }

            return ClienteVinculadoMapper.toDTO(c, u, ultima);
        }).toList();

        // si hay orden por última consulta, aplicamos manualmente
        Sort.Order order = pageable.getSort().getOrderFor("ultimaConsulta");
        if (order != null) {
            dtos = dtos.stream().sorted((a, b) -> {
                var x = a.getUltimaConsulta();
                var y = b.getUltimaConsulta();
                int cmp = (x == null && y == null) ? 0 :
                        (x == null ? -1 : (y == null ? 1 : x.compareTo(y)));
                return order.getDirection().isAscending() ? cmp : -cmp;
            }).toList();
        }

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }
}