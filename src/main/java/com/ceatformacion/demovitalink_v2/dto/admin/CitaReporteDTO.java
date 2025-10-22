package com.ceatformacion.demovitalink_v2.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.Objects;

public class CitaReporteDTO {

    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    private String paciente;
    private String profesional;
    private String estado;

    public CitaReporteDTO() { }

    public CitaReporteDTO(Long id, LocalDate fecha, String paciente, String profesional, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.paciente = paciente;
        this.profesional = profesional;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getProfesional() {
        return profesional;
    }

    public void setProfesional(String profesional) {
        this.profesional = profesional;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CitaReporteDTO that)) return false;
        return Objects.equals(id, that.id)
                && Objects.equals(fecha, that.fecha)
                && Objects.equals(paciente, that.paciente)
                && Objects.equals(profesional, that.profesional)
                && Objects.equals(estado, that.estado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fecha, paciente, profesional, estado);
    }

    @Override
    public String toString() {
        return "CitaReporteDTO{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", paciente='" + paciente + '\'' +
                ", profesional='" + profesional + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}