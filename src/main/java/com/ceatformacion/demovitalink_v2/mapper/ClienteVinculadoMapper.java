package com.ceatformacion.demovitalink_v2.mapper;

import com.ceatformacion.demovitalink_v2.dto.ClienteVinculadoDTO;
import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.model.Usuarios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

public class ClienteVinculadoMapper {

    public static ClienteVinculadoDTO toDTO(
            Clientes c, Usuarios u, LocalDateTime ultimaConsulta) {

        ClienteVinculadoDTO dto = new ClienteVinculadoDTO();
        dto.setClienteId(c.getIdCliente());                                   // :contentReference[oaicite:2]{index=2}
        if (u != null) {
            dto.setUsuarioId(u.getId_usuario());                               // :contentReference[oaicite:3]{index=3}
            dto.setUsername(u.getUsername());                                  // :contentReference[oaicite:4]{index=4}
        }

        // Datos personales → Clientes
        dto.setNombre(c.getNombre());
        dto.setApellidos(c.getApellidos());
        dto.setNumeroIdentificacion(c.getNumero_identificacion());
        dto.setNumeroTarjetaSanitaria(c.getNumero_tarjeta_sanitaria());
        dto.setTelefono(c.getTelefono());
        dto.setCorreoElectronico(c.getCorreoElectronico());                    // :contentReference[oaicite:5]{index=5}

        if (c.getNacimiento() != null) {
            dto.setEdad(Period.between(c.getNacimiento(), LocalDate.now()).getYears());
        }

        dto.setEstado(u != null ? "ACTIVO" : "INACTIVO");
        dto.setUltimaConsulta(ultimaConsulta);
        return dto;
    }
}
