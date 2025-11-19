package com.ceatformacion.demovitalink_v2.dto.admin;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ClienteListDTO {
    private Integer id;
    private Integer idCliente; // alias para compatibilidad con frontend

    private String nombre;
    private String apellidos;

    @Email
    private String correoElectronico;
    private String email; // alias

    private String telefono;
    private String direccion;
    private String ciudad_id;
    private String cp_id; // alias - código postal

    // Información del usuario asociado (si existe)
    private Integer usuarioId;
    private String usuarioUsername;
}
