package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "config_notificaciones")
public class ConfigNotificaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id", nullable = false, unique = true)
    private ConfigMedico config;

    /** { "email": true, "sms": false, "inapp": true } */
    @Column(columnDefinition = "JSON")
    private String canalesJson;

    /** { "nuevaCita": true, "cancelacion": true, ... } */
    @Column(columnDefinition = "JSON")
    private String eventosJson;

    /** HH:mm */
    private String silencioDesde; // "22:00"
    private String silencioHasta; // "07:00"

    /** Plantillas por evento (con variables) */
    @Column(columnDefinition = "JSON")
    private String plantillasJson;

    @Version
    private Integer version;

    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    void touch(){ updatedAt = LocalDateTime.now(); }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ConfigMedico getConfig() {
        return config;
    }

    public void setConfig(ConfigMedico config) {
        this.config = config;
    }

    public String getCanalesJson() {
        return canalesJson;
    }

    public void setCanalesJson(String canalesJson) {
        this.canalesJson = canalesJson;
    }

    public String getEventosJson() {
        return eventosJson;
    }

    public void setEventosJson(String eventosJson) {
        this.eventosJson = eventosJson;
    }

    public String getSilencioDesde() {
        return silencioDesde;
    }

    public void setSilencioDesde(String silencioDesde) {
        this.silencioDesde = silencioDesde;
    }

    public String getSilencioHasta() {
        return silencioHasta;
    }

    public void setSilencioHasta(String silencioHasta) {
        this.silencioHasta = silencioHasta;
    }

    public String getPlantillasJson() {
        return plantillasJson;
    }

    public void setPlantillasJson(String plantillasJson) {
        this.plantillasJson = plantillasJson;
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
