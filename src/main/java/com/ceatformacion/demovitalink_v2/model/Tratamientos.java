package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Tratamientos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_tratamiento;
    private String nombre_tratamiento;
    private String formula;
    private String dosis;
    private String frecuencia;
    private String duracion;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;
    private boolean toma_alimentos;
    private String observaciones;
    private String estado_tratamiento;
    private String sintomas;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuarios usuario;

    public Usuarios getUsuario() {return usuario;}
    public void setUsuario(Usuarios usuario) {this.usuario = usuario;}

    public int getId_tratamiento() {return id_tratamiento;}
    public void setId_tratamiento(int id_tratamiento) {this.id_tratamiento = id_tratamiento;}

    public String getNombre_tratamiento() {return nombre_tratamiento;}
    public void setNombre_tratamiento(String nombre_tratamiento) {this.nombre_tratamiento = nombre_tratamiento;}

    public String getFormula() {return formula;}
    public void setFormula(String formula) {this.formula = formula;}

    public String getDosis() {return dosis;}
    public void setDosis(String dosis) {this.dosis = dosis;}

    public String getFrecuencia() {return frecuencia;}
    public void setFrecuencia(String frecuencia) {this.frecuencia = frecuencia;}

    public String getDuracion() {return duracion;}
    public void setDuracion(String duracion) {this.duracion = duracion;}

    public LocalDate getFecha_inicio() {return fecha_inicio;}
    public void setFecha_inicio(LocalDate fecha_inicio) {this.fecha_inicio = fecha_inicio;}

    public LocalDate getFecha_fin() {return fecha_fin;}
    public void setFecha_fin(LocalDate fecha_fin) {this.fecha_fin = fecha_fin;}

    public boolean isToma_alimentos() {return toma_alimentos;}
    public void setToma_alimentos(boolean toma_alimentos) {this.toma_alimentos = toma_alimentos;}

    public String getObservaciones() {return observaciones;}
    public void setObservaciones(String observaciones) {this.observaciones = observaciones;}

    public String getEstado_tratamiento() {return estado_tratamiento;}
    public void setEstado_tratamiento(String estado_tratamiento) {this.estado_tratamiento = estado_tratamiento;}

    public String getSintomas() {return sintomas;}
    public void setSintomas(String sintomas) {this.sintomas = sintomas;}


    @Override
    public String toString() {
        return "Tratamientos{" +
                "\nId → " + id_tratamiento +
                "\nUsuario → " + usuario +
                "\nNombre tratamiento → " + nombre_tratamiento +
                "\nFormula → " + formula +
                "\nDosis → " + dosis +
                "\nFrecuencia → " + frecuencia +
                "\nDuracion → " + duracion +
                "\nFecha inicio → " + fecha_inicio +
                "\nFecha fin → " + fecha_fin +
                "\nToma alimentos → " + toma_alimentos +
                "\nObservaciones → " + observaciones +
                "\nEstado tratamiento → " + estado_tratamiento +
                "\nSintomas → " + sintomas + "\n" +
                '}';
    }
}
