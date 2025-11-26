package com.ceatformacion.demovitalink_v2.dto;

import java.time.LocalDateTime;

/**
 * DTO unificado para representar eventos del historial médico.
 * Agrupa síntomas, tratamientos y citas en una estructura común.
 */
public class EventoHistorialDTO {

    public enum TipoEvento {
        SINTOMA, TRATAMIENTO, CITA
    }

    private Integer id;
    private TipoEvento tipo;
    private LocalDateTime fecha;
    private String titulo;
    private String descripcion;
    private String zona;              // Solo para síntomas
    private String estado;            // Estado del tratamiento o cita
    private String urlDetalle;
    private String urlEditar;

    // Constructor vacío
    public EventoHistorialDTO() {}

    // Constructor completo
    public EventoHistorialDTO(Integer id, TipoEvento tipo, LocalDateTime fecha,
                              String titulo, String descripcion, String zona,
                              String estado, String urlDetalle, String urlEditar) {
        this.id = id;
        this.tipo = tipo;
        this.fecha = fecha;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.zona = zona;
        this.estado = estado;
        this.urlDetalle = urlDetalle;
        this.urlEditar = urlEditar;
    }

    // === Factory methods para cada tipo ===

    public static EventoHistorialDTO fromSintoma(int id, LocalDateTime fecha, String tipoSintoma,
                                                 String descripcion, String zona) {
        EventoHistorialDTO dto = new EventoHistorialDTO();
        dto.setId(id);
        dto.setTipo(TipoEvento.SINTOMA);
        dto.setFecha(fecha);
        dto.setTitulo(formatTipoSintoma(tipoSintoma));
        dto.setDescripcion(descripcion);
        dto.setZona(zona);
        dto.setUrlDetalle("/usuarios/sintomas/" + id);
        dto.setUrlEditar("/usuarios/sintomas/" + id + "/editar");
        return dto;
    }

    public static EventoHistorialDTO fromTratamiento(int id, LocalDateTime fecha, String nombre,
                                                     String observaciones, String estado) {
        EventoHistorialDTO dto = new EventoHistorialDTO();
        dto.setId(id);
        dto.setTipo(TipoEvento.TRATAMIENTO);
        dto.setFecha(fecha);
        dto.setTitulo(nombre != null ? nombre : "Tratamiento");
        dto.setDescripcion(observaciones);
        dto.setEstado(estado);
        dto.setUrlDetalle("/tratamientos/" + id);
        dto.setUrlEditar("/tratamientos/" + id + "/editar");
        return dto;
    }

    public static EventoHistorialDTO fromCita(int id, LocalDateTime fecha, String titulo,
                                              String descripcion, String estado) {
        EventoHistorialDTO dto = new EventoHistorialDTO();
        dto.setId(id);
        dto.setTipo(TipoEvento.CITA);
        dto.setFecha(fecha);
        dto.setTitulo(titulo != null ? titulo : "Cita médica");
        dto.setDescripcion(descripcion);
        dto.setEstado(estado);
        dto.setUrlDetalle("/citas/" + id);
        dto.setUrlEditar("/citas/" + id + "/editar");
        return dto;
    }

    // Formatea el enum de tipo síntoma a texto legible
    private static String formatTipoSintoma(String tipo) {
        if (tipo == null) return "Síntoma";
        return switch (tipo) {
            case "DOLOR_CABEZA" -> "Dolor de cabeza";
            case "FIEBRE" -> "Fiebre";
            case "NAUSEAS" -> "Náuseas";
            case "VOMITOS" -> "Vómitos";
            case "FATIGA" -> "Fatiga";
            case "DOLOR_ABDOMINAL" -> "Dolor abdominal";
            case "TOS" -> "Tos";
            case "OTRO" -> "Otro síntoma";
            default -> tipo.replace("_", " ").toLowerCase();
        };
    }

    // === Getters y Setters ===

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getUrlDetalle() { return urlDetalle; }
    public void setUrlDetalle(String urlDetalle) { this.urlDetalle = urlDetalle; }

    public String getUrlEditar() { return urlEditar; }
    public void setUrlEditar(String urlEditar) { this.urlEditar = urlEditar; }
}