package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas",
        indexes = {
                @Index(name = "idx_citas_medico_fecha", columnList = "id_medico, fecha"),
                @Index(name = "idx_citas_usuario_fecha", columnList = "id_usuario, fecha")
        })
public class Citas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_cita;
    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoCita estado = EstadoCita.PENDIENTE;

    @Column(name = "duracion_minutos", nullable = false)
    private int duracionMinutos = 60;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuarios usuario; // paciente

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_medico", referencedColumnName = "id_usuario")
    private Usuarios medico;  // profesional


    // getters/setters...
    public Usuarios getMedico() { return medico; }
    public void setMedico(Usuarios medico) { this.medico = medico; }

    public int getId_cita() {
        return id_cita;
    }
    public void setId_cita(int id_cita) {
        this.id_cita = id_cita;
    }

    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }

    public int getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(int duracionMinutos) { this.duracionMinutos = duracionMinutos; }

    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getFecha() {
        return fecha;
    }
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }
    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}

    public Usuarios getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return "Citas{" +
                "id=" + id_cita +
                ", titulo='" + titulo + '\'' +
                ", fecha=" + fecha +
                ", hora=" + hora +
                ", estado=" + estado +
                ", paciente=" + (usuario != null ? usuario.getId_usuario() : null) +
                ", medico=" + (medico != null ? medico.getId_usuario() : null) +
                '}';
    }

}
