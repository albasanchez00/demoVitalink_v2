package com.ceatformacion.demovitalink_v2.dto.admin;

import lombok.Data;

/**
 * DTO para respuesta de creación de usuario
 * Se devuelve cuando se crea un usuario para un cliente
 */
@Data
public class UsuarioCreateResponseDTO {
    private Integer id;
    private String username;
    private String rol;
    private Integer clienteId;
}
