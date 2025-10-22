package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@Entity
@Table(name = "config_global")
public class ConfigGlobal {

    @Id
    @Column(name = "id_config")
    private Long idConfig = 1L; // singleton

    // Identidad & marca
    private String nombreSistema;
    private String logoUrl;
    private String colorPrimario;   // #RRGGBB
    private String colorSecundario; // #RRGGBB

    // Centro & TZ
    private String centroNombre;
    private String centroCiudad;
    private String timezone; // IANA: Europe/Madrid

    // Legales
    private String privacidadUrl;
    private String terminosUrl;
    private String cookiesUrl;

    // Notificaciones
    private String mailFrom;
    private Boolean notifCitas;
    private Boolean notifMedicacion;
    private Boolean notifSintomas;

    // Seguridad
    private Integer pwdMinLen;
    private Integer pwdExpireDays;
    private Boolean twoFactorEnabled;

    // Citas
    private Integer citaDuracionMin;
    private String horarioInicio; // HH:mm
    private String horarioFin;    // HH:mm

    @Column(length = 2000)
    private String diasNoLaborablesCsv; // persistimos CSV y lo convertimos en Service

    // Integraciones
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUser;
    private String webhookBase;

    @Version
    private Integer version;

    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    void touch() { this.updatedAt = LocalDateTime.now(); }

    // --- Getters/Setters ---

    public Long getIdConfig() { return idConfig; }
    public void setIdConfig(Long idConfig) { this.idConfig = idConfig; }

    public String getNombreSistema() { return nombreSistema; }
    public void setNombreSistema(String nombreSistema) { this.nombreSistema = nombreSistema; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getColorPrimario() { return colorPrimario; }
    public void setColorPrimario(String colorPrimario) { this.colorPrimario = colorPrimario; }

    public String getColorSecundario() { return colorSecundario; }
    public void setColorSecundario(String colorSecundario) { this.colorSecundario = colorSecundario; }

    public String getCentroNombre() { return centroNombre; }
    public void setCentroNombre(String centroNombre) { this.centroNombre = centroNombre; }

    public String getCentroCiudad() { return centroCiudad; }
    public void setCentroCiudad(String centroCiudad) { this.centroCiudad = centroCiudad; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getPrivacidadUrl() { return privacidadUrl; }
    public void setPrivacidadUrl(String privacidadUrl) { this.privacidadUrl = privacidadUrl; }

    public String getTerminosUrl() { return terminosUrl; }
    public void setTerminosUrl(String terminosUrl) { this.terminosUrl = terminosUrl; }

    public String getCookiesUrl() { return cookiesUrl; }
    public void setCookiesUrl(String cookiesUrl) { this.cookiesUrl = cookiesUrl; }

    public String getMailFrom() { return mailFrom; }
    public void setMailFrom(String mailFrom) { this.mailFrom = mailFrom; }

    public Boolean getNotifCitas() { return notifCitas; }
    public void setNotifCitas(Boolean notifCitas) { this.notifCitas = notifCitas; }

    public Boolean getNotifMedicacion() { return notifMedicacion; }
    public void setNotifMedicacion(Boolean notifMedicacion) { this.notifMedicacion = notifMedicacion; }

    public Boolean getNotifSintomas() { return notifSintomas; }
    public void setNotifSintomas(Boolean notifSintomas) { this.notifSintomas = notifSintomas; }

    public Integer getPwdMinLen() { return pwdMinLen; }
    public void setPwdMinLen(Integer pwdMinLen) { this.pwdMinLen = pwdMinLen; }

    public Integer getPwdExpireDays() { return pwdExpireDays; }
    public void setPwdExpireDays(Integer pwdExpireDays) { this.pwdExpireDays = pwdExpireDays; }

    public Boolean getTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(Boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public Integer getCitaDuracionMin() { return citaDuracionMin; }
    public void setCitaDuracionMin(Integer citaDuracionMin) { this.citaDuracionMin = citaDuracionMin; }

    public String getHorarioInicio() { return horarioInicio; }
    public void setHorarioInicio(String horarioInicio) { this.horarioInicio = horarioInicio; }

    public String getHorarioFin() { return horarioFin; }
    public void setHorarioFin(String horarioFin) { this.horarioFin = horarioFin; }

    public String getDiasNoLaborablesCsv() { return diasNoLaborablesCsv; }
    public void setDiasNoLaborablesCsv(String diasNoLaborablesCsv) { this.diasNoLaborablesCsv = diasNoLaborablesCsv; }

    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }

    public Integer getSmtpPort() { return smtpPort; }
    public void setSmtpPort(Integer smtpPort) { this.smtpPort = smtpPort; }

    public String getSmtpUser() { return smtpUser; }
    public void setSmtpUser(String smtpUser) { this.smtpUser = smtpUser; }

    public String getWebhookBase() { return webhookBase; }
    public void setWebhookBase(String webhookBase) { this.webhookBase = webhookBase; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helpers de CSV (útiles si los quieres usar desde el Service)
    @Transient
    public List<String> getDiasNoLaborablesList() {
        if (diasNoLaborablesCsv == null || diasNoLaborablesCsv.isBlank()) return new ArrayList<>();
        return Arrays.stream(diasNoLaborablesCsv.split(","))
                .map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    public void setDiasNoLaborablesList(List<String> list) {
        this.diasNoLaborablesCsv = (list == null || list.isEmpty())
                ? null : String.join(",", list);
    }
}

