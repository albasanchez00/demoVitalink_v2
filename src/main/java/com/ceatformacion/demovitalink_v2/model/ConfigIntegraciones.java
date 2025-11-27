package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "config_integraciones")
public class ConfigIntegraciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id", nullable = false, unique = true)
    private ConfigMedico config;

    // Servicios externos habilitados
    private Boolean googleCalendar;
    private Boolean outlookCalendar;
    private Boolean smsProvider;

    /**
     * JSON con las API keys encriptadas o tokenizadas
     * Ejemplo: {"google": "AIza...", "outlook": "XXX", "sms": "key_123"}
     * NOTA: En producción, estas keys deberían estar encriptadas
     */
    @Column(columnDefinition = "JSON")
    private String apiKeysJson;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConfigMedico getConfig() {
        return config;
    }

    public void setConfig(ConfigMedico config) {
        this.config = config;
    }

    public Boolean getGoogleCalendar() {
        return googleCalendar;
    }

    public void setGoogleCalendar(Boolean googleCalendar) {
        this.googleCalendar = googleCalendar;
    }

    public Boolean getOutlookCalendar() {
        return outlookCalendar;
    }

    public void setOutlookCalendar(Boolean outlookCalendar) {
        this.outlookCalendar = outlookCalendar;
    }

    public Boolean getSmsProvider() {
        return smsProvider;
    }

    public void setSmsProvider(Boolean smsProvider) {
        this.smsProvider = smsProvider;
    }

    public String getApiKeysJson() {
        return apiKeysJson;
    }

    public void setApiKeysJson(String apiKeysJson) {
        this.apiKeysJson = apiKeysJson;
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