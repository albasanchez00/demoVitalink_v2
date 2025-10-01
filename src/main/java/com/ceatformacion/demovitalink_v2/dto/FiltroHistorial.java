package com.ceatformacion.demovitalink_v2.dto;

import java.time.LocalDate;

public class FiltroHistorial {
    private LocalDate desde;
    private LocalDate hasta;
    private String tipo;              // SINTOMA | TRATAMIENTO | CITA | null
    private String zona;
    private Integer intensidad;       // 1..10
    private String estadoTratamiento; // ACTIVO | PAUSADO | FINALIZADO | null
    private Boolean mostrarAvanzados; // true/false

    // getters & setters

    public LocalDate getDesde() {
        return desde;
    }

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public Integer getIntensidad() {
        return intensidad;
    }

    public void setIntensidad(Integer intensidad) {
        this.intensidad = intensidad;
    }

    public String getEstadoTratamiento() {
        return estadoTratamiento;
    }

    public void setEstadoTratamiento(String estadoTratamiento) {
        this.estadoTratamiento = estadoTratamiento;
    }

    public Boolean getMostrarAvanzados() {
        return mostrarAvanzados;
    }

    public void setMostrarAvanzados(Boolean mostrarAvanzados) {
        this.mostrarAvanzados = mostrarAvanzados;
    }
}
