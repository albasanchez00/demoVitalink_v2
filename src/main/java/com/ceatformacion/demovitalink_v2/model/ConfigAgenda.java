package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "config_agenda")
public class ConfigAgenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_id", nullable = false, unique = true)
    private ConfigMedico config;

    // Ej. duración general en minutos y buffer entre citas
    private Integer duracionGeneralMin = 20;
    private Integer bufferMin = 5;

    /** JSON con reglas (antelación, cancelación, overbooking, etc.) */
    @Column(columnDefinition = "JSON")
    private String reglasJson;

    /** JSON con disponibilidad por día (bloques) */
    @Column(columnDefinition = "JSON")
    private String disponibilidadJson;

    /** JSON con instrucciones por tipo de cita */
    @Column(columnDefinition = "JSON")
    private String instruccionesPorTipoJson;

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

    public Integer getDuracionGeneralMin() {
        return duracionGeneralMin;
    }

    public void setDuracionGeneralMin(Integer duracionGeneralMin) {
        this.duracionGeneralMin = duracionGeneralMin;
    }

    public Integer getBufferMin() {
        return bufferMin;
    }

    public void setBufferMin(Integer bufferMin) {
        this.bufferMin = bufferMin;
    }

    public String getReglasJson() {
        return reglasJson;
    }

    public void setReglasJson(String reglasJson) {
        this.reglasJson = reglasJson;
    }

    public String getDisponibilidadJson() {
        return disponibilidadJson;
    }

    public void setDisponibilidadJson(String disponibilidadJson) {
        this.disponibilidadJson = disponibilidadJson;
    }

    public String getInstruccionesPorTipoJson() {
        return instruccionesPorTipoJson;
    }

    public void setInstruccionesPorTipoJson(String instruccionesPorTipoJson) {
        this.instruccionesPorTipoJson = instruccionesPorTipoJson;
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
