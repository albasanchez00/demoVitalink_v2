package com.ceatformacion.demovitalink_v2.dto;

import java.time.LocalDateTime;

public class ClienteVinculadoDTO {
    private Integer clienteId;            // Clientes.idCliente
    private Integer usuarioId;            // Usuarios.id_usuario (si existe cuenta)
    private String  username;             // Usuarios.username (si existe)

    private String  nombre;               // Clientes.nombre
    private String  apellidos;            // Clientes.apellidos
    private String  numeroIdentificacion; // Clientes.numero_identificacion
    private String  numeroTarjetaSanitaria; // Clientes.numero_tarjeta_sanitaria
    private String  telefono;             // Clientes.telefono
    private String  correoElectronico;    // Clientes.correoElectronico
    private Integer edad;                 // calculada desde Clientes.nacimiento

    // Conveniencia para filtros/etiquetas en la tabla:
    // ACTIVO = tiene cuenta de usuario vinculada; INACTIVO = sin cuenta
    private String  estado;

    // Para ordenar por actividad clínica:
    private LocalDateTime ultimaConsulta;

    // getters/setters...

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getNumeroTarjetaSanitaria() {
        return numeroTarjetaSanitaria;
    }

    public void setNumeroTarjetaSanitaria(String numeroTarjetaSanitaria) {
        this.numeroTarjetaSanitaria = numeroTarjetaSanitaria;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getUltimaConsulta() {
        return ultimaConsulta;
    }

    public void setUltimaConsulta(LocalDateTime ultimaConsulta) {
        this.ultimaConsulta = ultimaConsulta;
    }
}
