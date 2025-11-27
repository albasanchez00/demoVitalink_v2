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

    // ═══════════════════════════════════════════════════════════════
    // PERFIL
    // ═══════════════════════════════════════════════════════════════
    private String nombreMostrar;
    private String especialidad;
    private String colegiado;

    @Column(length = 500)
    private String bio;

    private String firmaTexto;
    private String firmaImagenUrl;

    // ═══════════════════════════════════════════════════════════════
    // UI / PREFERENCIAS
    // ═══════════════════════════════════════════════════════════════
    private String tema;        // auto | light | dark
    private String idioma;      // es | en
    private String zonaHoraria; // Europe/Madrid
    private String home;        // dashboard | agenda | mensajes

    // ═══════════════════════════════════════════════════════════════
    // CHAT / MENSAJERÍA
    // ═══════════════════════════════════════════════════════════════
    private String chatEstado;      // DISPONIBLE | OCUPADO | AUSENTE
    private String chatFirma;       // "Dr./Dra. {nombre}"

    @Column(columnDefinition = "JSON")
    private String respuestasRapidasJson;  // {"hola": "Hola...", "gracias": "Gracias..."}

    // ═══════════════════════════════════════════════════════════════
    // PRIVACIDAD
    // ═══════════════════════════════════════════════════════════════
    private String privacidadVisibilidad;  // PUBLICO | LIMITADO | PRIVADO
    private Boolean privacidadUsoDatos;    // Permitir uso estadístico
    private Boolean privacidadBoletines;   // Recibir boletines

    // ═══════════════════════════════════════════════════════════════
    // CENTRO
    // ═══════════════════════════════════════════════════════════════
    private String centroNombre;
    private String centroTelefono;
    private String centroDireccion;
    private String centroHorario;

    @Column(columnDefinition = "JSON")
    private String centroServiciosJson;  // {"cardiologia": true, "pediatria": false, ...}

    // ═══════════════════════════════════════════════════════════════
    // SEGURIDAD (solo flag 2FA, password se maneja aparte)
    // ═══════════════════════════════════════════════════════════════
    private Boolean twoFactorEnabled;

    // ═══════════════════════════════════════════════════════════════
    // METADATA
    // ═══════════════════════════════════════════════════════════════
    @Version
    private Integer version;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════

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

    // --- PERFIL ---
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

    // --- UI ---
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

    // --- CHAT ---
    public String getChatEstado() {
        return chatEstado;
    }

    public void setChatEstado(String chatEstado) {
        this.chatEstado = chatEstado;
    }

    public String getChatFirma() {
        return chatFirma;
    }

    public void setChatFirma(String chatFirma) {
        this.chatFirma = chatFirma;
    }

    public String getRespuestasRapidasJson() {
        return respuestasRapidasJson;
    }

    public void setRespuestasRapidasJson(String respuestasRapidasJson) {
        this.respuestasRapidasJson = respuestasRapidasJson;
    }

    // --- PRIVACIDAD ---
    public String getPrivacidadVisibilidad() {
        return privacidadVisibilidad;
    }

    public void setPrivacidadVisibilidad(String privacidadVisibilidad) {
        this.privacidadVisibilidad = privacidadVisibilidad;
    }

    public Boolean getPrivacidadUsoDatos() {
        return privacidadUsoDatos;
    }

    public void setPrivacidadUsoDatos(Boolean privacidadUsoDatos) {
        this.privacidadUsoDatos = privacidadUsoDatos;
    }

    public Boolean getPrivacidadBoletines() {
        return privacidadBoletines;
    }

    public void setPrivacidadBoletines(Boolean privacidadBoletines) {
        this.privacidadBoletines = privacidadBoletines;
    }

    // --- CENTRO ---
    public String getCentroNombre() {
        return centroNombre;
    }

    public void setCentroNombre(String centroNombre) {
        this.centroNombre = centroNombre;
    }

    public String getCentroTelefono() {
        return centroTelefono;
    }

    public void setCentroTelefono(String centroTelefono) {
        this.centroTelefono = centroTelefono;
    }

    public String getCentroDireccion() {
        return centroDireccion;
    }

    public void setCentroDireccion(String centroDireccion) {
        this.centroDireccion = centroDireccion;
    }

    public String getCentroHorario() {
        return centroHorario;
    }

    public void setCentroHorario(String centroHorario) {
        this.centroHorario = centroHorario;
    }

    public String getCentroServiciosJson() {
        return centroServiciosJson;
    }

    public void setCentroServiciosJson(String centroServiciosJson) {
        this.centroServiciosJson = centroServiciosJson;
    }

    // --- SEGURIDAD ---
    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(Boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    // --- METADATA ---
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