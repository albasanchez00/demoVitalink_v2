package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "config_medico")
public class ConfigMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_config")
    private int idConfig;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuarios medico;

    // PERFIL
    private String nombreMostrar;
    private String especialidad;
    private String colegiado;

    @Column(length = 500)
    private String bio;

    private String firmaTexto;
    private String firmaImagenUrl;

    // UI
    private String tema;        // auto | light | dark
    private String idioma;      // es | en
    private String zonaHoraria; // Europe/Madrid
    private String home;        // dashboard | agenda | mensajes
    @Version
    private Integer version;

    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    void touch() { this.updatedAt = LocalDateTime.now(); }

    public int getIdConfig() {
        return idConfig;
    }

    public void setIdConfig(int idConfig) {
        this.idConfig = idConfig;
    }

    public Usuarios getMedico() {
        return medico;
    }

    public void setMedico(Usuarios medico) {
        this.medico = medico;
    }

    public String getNombreMostrar() {
        return nombreMostrar;
    }

    public void setNombreMostrar(String nombreMostrar) {
        this.nombreMostrar = nombreMostrar;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getColegiado() {
        return colegiado;
    }

    public void setColegiado(String colegiado) {
        this.colegiado = colegiado;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFirmaTexto() {
        return firmaTexto;
    }

    public void setFirmaTexto(String firmaTexto) {
        this.firmaTexto = firmaTexto;
    }

    public String getFirmaImagenUrl() {
        return firmaImagenUrl;
    }

    public void setFirmaImagenUrl(String firmaImagenUrl) {
        this.firmaImagenUrl = firmaImagenUrl;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(String zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
