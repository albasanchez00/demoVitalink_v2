package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Citas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_cita;
    private String titulo;
    private LocalDate fecha; // Solo fecha
    private LocalTime hora;  // Solo hora
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuarios usuario;

    public int getId_cita() {
        return id_cita;
    }

    public void setId_cita(int id_cita) {
        this.id_cita = id_cita;
    }

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
                "\nId Cita → " + id_cita +
                "\nPaciente → " + titulo +
                "\nFecha → " + fecha +
                "\nHora → " + hora +
                "\nTipo → " + descripcion +
                "\nUsuario → " + usuario +
                "\n}";
    }
}
