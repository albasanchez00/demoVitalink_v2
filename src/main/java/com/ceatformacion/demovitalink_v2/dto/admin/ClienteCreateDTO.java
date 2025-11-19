package com.ceatformacion.demovitalink_v2.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para crear un nuevo cliente
 * Validaciones según tus reglas de negocio
 */
@Data
public class ClienteCreateDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Size(max = 150, message = "Los apellidos no pueden exceder 150 caracteres")
    private String apellidos;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo no es válido")
    @Size(max = 150)
    private String correoElectronico;

    @Size(max = 15, message = "El teléfono no puede exceder 15 caracteres")
    private String telefono;

    @Size(max = 255)
    private String direccion;

    @Size(max = 10)
    private String cp_id;

    @Size(max = 100)
    private String ciudad_id;
}