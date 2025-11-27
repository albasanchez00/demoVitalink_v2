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

    // ═══════════════════════════════════════════════════════════════
    // IDENTIDAD & MARCA
    // ═══════════════════════════════════════════════════════════════
    private String nombreSistema;
    private String logoUrl;
    private String colorPrimario;   // #RRGGBB
    private String colorSecundario; // #RRGGBB

    // ═══════════════════════════════════════════════════════════════
    // CENTRO & TZ
    // ═══════════════════════════════════════════════════════════════
    private String centroNombre;
    private String centroCiudad;
    private String timezone; // IANA: Europe/Madrid

    // ═══════════════════════════════════════════════════════════════
    // LEGALES
    // ═══════════════════════════════════════════════════════════════
    private String privacidadUrl;
    private String terminosUrl;
    private String cookiesUrl;

    // ═══════════════════════════════════════════════════════════════
    // NOTIFICACIONES
    // ═══════════════════════════════════════════════════════════════
    private String mailFrom;
    private Boolean notifCitas;
    private Boolean notifMedicacion;
    private Boolean notifSintomas;

    // ═══════════════════════════════════════════════════════════════
    // SEGURIDAD
    // ═══════════════════════════════════════════════════════════════
    private Integer pwdMinLen;
    private Integer pwdExpireDays;
    private Boolean twoFactorEnabled;

    // ═══════════════════════════════════════════════════════════════
    // CITAS
    // ═══════════════════════════════════════════════════════════════
    private Integer citaDuracionMin;
    private String horarioInicio; // HH:mm
    private String horarioFin;    // HH:mm

    @Column(length = 2000)
    private String diasNoLaborablesCsv;

    // ═══════════════════════════════════════════════════════════════
    // INTEGRACIONES (SMTP, etc.)
    // ═══════════════════════════════════════════════════════════════
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUser;
    private String webhookBase;

    // ═══════════════════════════════════════════════════════════════
    // USUARIOS (nuevo)
    // ═══════════════════════════════════════════════════════════════
    private Integer maxUsuariosSistema;
    private Integer maxMedicosSistema;
    private Boolean registroAbierto;
    private Boolean requiereAprobacion;

    @Column(length = 500)
    private String rolesDisponiblesCsv;  // "ADMIN,MEDICO,USUARIO"

    // ═══════════════════════════════════════════════════════════════
    // AUDITORÍA / LOGS (nuevo)
    // ═══════════════════════════════════════════════════════════════
    private String nivelAuditoria;       // NINGUNO | BASICO | COMPLETO
    private Integer retencionLogsDias;
    private Boolean auditarAccesos;
    private Boolean auditarCambios;
    private Boolean auditarErrores;

    // ═══════════════════════════════════════════════════════════════
    // BACKUP (nuevo)
    // ═══════════════════════════════════════════════════════════════
    private Boolean backupAutomatico;
    private String backupFrecuencia;     // DIARIO | SEMANAL | MENSUAL
    private String backupHora;           // HH:mm
    private Integer backupRetencionDias;
    private String backupDestino;        // LOCAL | S3 | GCS

    // ═══════════════════════════════════════════════════════════════
    // PERSONALIZACIÓN UI (nuevo)
    // ═══════════════════════════════════════════════════════════════
    private String fuentePrincipal;
    private Integer tamanoFuenteBase;
    private String densidadUI;           // COMPACTA | NORMAL | ESPACIOSA
    private Boolean mostrarAyuda;
    private Boolean animacionesUI;

    // ═══════════════════════════════════════════════════════════════
    // LÍMITES / CUOTAS (nuevo)
    // ═══════════════════════════════════════════════════════════════
    private Integer maxPacientesPorMedico;
    private Integer maxCitasDiarias;
    private Integer maxAlmacenamientoMB;
    private Integer maxArchivoMB;

    // ═══════════════════════════════════════════════════════════════
    // MANTENIMIENTO (nuevo)
    // ═══════════════════════════════════════════════════════════════
    private Boolean modoMantenimiento;

    @Column(length = 1000)
    private String mensajeMantenimiento;

    private String mantenimientoProgramado; // ISO datetime

    @Column(length = 500)
    private String ipsBlanqueadasCsv;

    // ═══════════════════════════════════════════════════════════════
    // API / WEBHOOKS (nuevo)
    // ═══════════════════════════════════════════════════════════════
    private Boolean apiHabilitada;
    private Integer apiRateLimitReq;
    private Integer apiTokenExpiraDias;
    private Boolean webhooksHabilitados;
    private String webhookSecretKey;

    @Column(length = 500)
    private String webhookEventosCsv;

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
    // GETTERS & SETTERS - IDENTIDAD
    // ═══════════════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - CENTRO & TZ
    // ═══════════════════════════════════════════════════════════════

    public String getCentroNombre() { return centroNombre; }
    public void setCentroNombre(String centroNombre) { this.centroNombre = centroNombre; }

    public String getCentroCiudad() { return centroCiudad; }
    public void setCentroCiudad(String centroCiudad) { this.centroCiudad = centroCiudad; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - LEGALES
    // ═══════════════════════════════════════════════════════════════

    public String getPrivacidadUrl() { return privacidadUrl; }
    public void setPrivacidadUrl(String privacidadUrl) { this.privacidadUrl = privacidadUrl; }

    public String getTerminosUrl() { return terminosUrl; }
    public void setTerminosUrl(String terminosUrl) { this.terminosUrl = terminosUrl; }

    public String getCookiesUrl() { return cookiesUrl; }
    public void setCookiesUrl(String cookiesUrl) { this.cookiesUrl = cookiesUrl; }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - NOTIFICACIONES
    // ═══════════════════════════════════════════════════════════════

    public String getMailFrom() { return mailFrom; }
    public void setMailFrom(String mailFrom) { this.mailFrom = mailFrom; }

    public Boolean getNotifCitas() { return notifCitas; }
    public void setNotifCitas(Boolean notifCitas) { this.notifCitas = notifCitas; }

    public Boolean getNotifMedicacion() { return notifMedicacion; }
    public void setNotifMedicacion(Boolean notifMedicacion) { this.notifMedicacion = notifMedicacion; }

    public Boolean getNotifSintomas() { return notifSintomas; }
    public void setNotifSintomas(Boolean notifSintomas) { this.notifSintomas = notifSintomas; }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - SEGURIDAD
    // ═══════════════════════════════════════════════════════════════

    public Integer getPwdMinLen() { return pwdMinLen; }
    public void setPwdMinLen(Integer pwdMinLen) { this.pwdMinLen = pwdMinLen; }

    public Integer getPwdExpireDays() { return pwdExpireDays; }
    public void setPwdExpireDays(Integer pwdExpireDays) { this.pwdExpireDays = pwdExpireDays; }

    public Boolean getTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(Boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - CITAS
    // ═══════════════════════════════════════════════════════════════

    public Integer getCitaDuracionMin() { return citaDuracionMin; }
    public void setCitaDuracionMin(Integer citaDuracionMin) { this.citaDuracionMin = citaDuracionMin; }

    public String getHorarioInicio() { return horarioInicio; }
    public void setHorarioInicio(String horarioInicio) { this.horarioInicio = horarioInicio; }

    public String getHorarioFin() { return horarioFin; }
    public void setHorarioFin(String horarioFin) { this.horarioFin = horarioFin; }

    public String getDiasNoLaborablesCsv() { return diasNoLaborablesCsv; }
    public void setDiasNoLaborablesCsv(String diasNoLaborablesCsv) { this.diasNoLaborablesCsv = diasNoLaborablesCsv; }

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

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - INTEGRACIONES SMTP
    // ═══════════════════════════════════════════════════════════════

    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }

    public Integer getSmtpPort() { return smtpPort; }
    public void setSmtpPort(Integer smtpPort) { this.smtpPort = smtpPort; }

    public String getSmtpUser() { return smtpUser; }
    public void setSmtpUser(String smtpUser) { this.smtpUser = smtpUser; }

    public String getWebhookBase() { return webhookBase; }
    public void setWebhookBase(String webhookBase) { this.webhookBase = webhookBase; }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - USUARIOS
    // ═══════════════════════════════════════════════════════════════

    public Integer getMaxUsuariosSistema() { return maxUsuariosSistema; }
    public void setMaxUsuariosSistema(Integer maxUsuariosSistema) { this.maxUsuariosSistema = maxUsuariosSistema; }

    public Integer getMaxMedicosSistema() { return maxMedicosSistema; }
    public void setMaxMedicosSistema(Integer maxMedicosSistema) { this.maxMedicosSistema = maxMedicosSistema; }

    public Boolean getRegistroAbierto() { return registroAbierto; }
    public void setRegistroAbierto(Boolean registroAbierto) { this.registroAbierto = registroAbierto; }

    public Boolean getRequiereAprobacion() { return requiereAprobacion; }
    public void setRequiereAprobacion(Boolean requiereAprobacion) { this.requiereAprobacion = requiereAprobacion; }

    public String getRolesDisponiblesCsv() { return rolesDisponiblesCsv; }
    public void setRolesDisponiblesCsv(String rolesDisponiblesCsv) { this.rolesDisponiblesCsv = rolesDisponiblesCsv; }

    @Transient
    public List<String> getRolesDisponiblesList() {
        if (rolesDisponiblesCsv == null || rolesDisponiblesCsv.isBlank()) return new ArrayList<>();
        return Arrays.stream(rolesDisponiblesCsv.split(","))
                .map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    public void setRolesDisponiblesList(List<String> list) {
        this.rolesDisponiblesCsv = (list == null || list.isEmpty())
                ? null : String.join(",", list);
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - AUDITORÍA
    // ═══════════════════════════════════════════════════════════════

    public String getNivelAuditoria() { return nivelAuditoria; }
    public void setNivelAuditoria(String nivelAuditoria) { this.nivelAuditoria = nivelAuditoria; }

    public Integer getRetencionLogsDias() { return retencionLogsDias; }
    public void setRetencionLogsDias(Integer retencionLogsDias) { this.retencionLogsDias = retencionLogsDias; }

    public Boolean getAuditarAccesos() { return auditarAccesos; }
    public void setAuditarAccesos(Boolean auditarAccesos) { this.auditarAccesos = auditarAccesos; }

    public Boolean getAuditarCambios() { return auditarCambios; }
    public void setAuditarCambios(Boolean auditarCambios) { this.auditarCambios = auditarCambios; }

    public Boolean getAuditarErrores() { return auditarErrores; }
    public void setAuditarErrores(Boolean auditarErrores) { this.auditarErrores = auditarErrores; }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - BACKUP
    // ═══════════════════════════════════════════════════════════════

    public Boolean getBackupAutomatico() { return backupAutomatico; }
    public void setBackupAutomatico(Boolean backupAutomatico) { this.backupAutomatico = backupAutomatico; }

    public String getBackupFrecuencia() { return backupFrecuencia; }
    public void setBackupFrecuencia(String backupFrecuencia) { this.backupFrecuencia = backupFrecuencia; }

    public String getBackupHora() { return backupHora; }
    public void setBackupHora(String backupHora) { this.backupHora = backupHora; }

    public Integer getBackupRetencionDias() { return backupRetencionDias; }
    public void setBackupRetencionDias(Integer backupRetencionDias) { this.backupRetencionDias = backupRetencionDias; }

    public String getBackupDestino() { return backupDestino; }
    public void setBackupDestino(String backupDestino) { this.backupDestino = backupDestino; }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - PERSONALIZACIÓN UI
    // ═══════════════════════════════════════════════════════════════

    public String getFuentePrincipal() { return fuentePrincipal; }
    public void setFuentePrincipal(String fuentePrincipal) { this.fuentePrincipal = fuentePrincipal; }

    public Integer getTamanoFuenteBase() { return tamanoFuenteBase; }
    public void setTamanoFuenteBase(Integer tamanoFuenteBase) { this.tamanoFuenteBase = tamanoFuenteBase; }

    public String getDensidadUI() { return densidadUI; }
    public void setDensidadUI(String densidadUI) { this.densidadUI = densidadUI; }

    public Boolean getMostrarAyuda() { return mostrarAyuda; }
    public void setMostrarAyuda(Boolean mostrarAyuda) { this.mostrarAyuda = mostrarAyuda; }

    public Boolean getAnimacionesUI() { return animacionesUI; }
    public void setAnimacionesUI(Boolean animacionesUI) { this.animacionesUI = animacionesUI; }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - LÍMITES / CUOTAS
    // ═══════════════════════════════════════════════════════════════

    public Integer getMaxPacientesPorMedico() { return maxPacientesPorMedico; }
    public void setMaxPacientesPorMedico(Integer maxPacientesPorMedico) { this.maxPacientesPorMedico = maxPacientesPorMedico; }

    public Integer getMaxCitasDiarias() { return maxCitasDiarias; }
    public void setMaxCitasDiarias(Integer maxCitasDiarias) { this.maxCitasDiarias = maxCitasDiarias; }

    public Integer getMaxAlmacenamientoMB() { return maxAlmacenamientoMB; }
    public void setMaxAlmacenamientoMB(Integer maxAlmacenamientoMB) { this.maxAlmacenamientoMB = maxAlmacenamientoMB; }

    public Integer getMaxArchivoMB() { return maxArchivoMB; }
    public void setMaxArchivoMB(Integer maxArchivoMB) { this.maxArchivoMB = maxArchivoMB; }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - MANTENIMIENTO
    // ═══════════════════════════════════════════════════════════════

    public Boolean getModoMantenimiento() { return modoMantenimiento; }
    public void setModoMantenimiento(Boolean modoMantenimiento) { this.modoMantenimiento = modoMantenimiento; }

    public String getMensajeMantenimiento() { return mensajeMantenimiento; }
    public void setMensajeMantenimiento(String mensajeMantenimiento) { this.mensajeMantenimiento = mensajeMantenimiento; }

    public String getMantenimientoProgramado() { return mantenimientoProgramado; }
    public void setMantenimientoProgramado(String mantenimientoProgramado) { this.mantenimientoProgramado = mantenimientoProgramado; }

    public String getIpsBlanqueadasCsv() { return ipsBlanqueadasCsv; }
    public void setIpsBlanqueadasCsv(String ipsBlanqueadasCsv) { this.ipsBlanqueadasCsv = ipsBlanqueadasCsv; }

    @Transient
    public List<String> getIpsBlanqueadasList() {
        if (ipsBlanqueadasCsv == null || ipsBlanqueadasCsv.isBlank()) return new ArrayList<>();
        return Arrays.stream(ipsBlanqueadasCsv.split(","))
                .map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    public void setIpsBlanqueadasList(List<String> list) {
        this.ipsBlanqueadasCsv = (list == null || list.isEmpty())
                ? null : String.join(",", list);
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - API / WEBHOOKS
    // ═══════════════════════════════════════════════════════════════

    public Boolean getApiHabilitada() { return apiHabilitada; }
    public void setApiHabilitada(Boolean apiHabilitada) { this.apiHabilitada = apiHabilitada; }

    public Integer getApiRateLimitReq() { return apiRateLimitReq; }
    public void setApiRateLimitReq(Integer apiRateLimitReq) { this.apiRateLimitReq = apiRateLimitReq; }

    public Integer getApiTokenExpiraDias() { return apiTokenExpiraDias; }
    public void setApiTokenExpiraDias(Integer apiTokenExpiraDias) { this.apiTokenExpiraDias = apiTokenExpiraDias; }

    public Boolean getWebhooksHabilitados() { return webhooksHabilitados; }
    public void setWebhooksHabilitados(Boolean webhooksHabilitados) { this.webhooksHabilitados = webhooksHabilitados; }

    public String getWebhookSecretKey() { return webhookSecretKey; }
    public void setWebhookSecretKey(String webhookSecretKey) { this.webhookSecretKey = webhookSecretKey; }

    public String getWebhookEventosCsv() { return webhookEventosCsv; }
    public void setWebhookEventosCsv(String webhookEventosCsv) { this.webhookEventosCsv = webhookEventosCsv; }

    @Transient
    public List<String> getWebhookEventosList() {
        if (webhookEventosCsv == null || webhookEventosCsv.isBlank()) return new ArrayList<>();
        return Arrays.stream(webhookEventosCsv.split(","))
                .map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    public void setWebhookEventosList(List<String> list) {
        this.webhookEventosCsv = (list == null || list.isEmpty())
                ? null : String.join(",", list);
    }

    // ═══════════════════════════════════════════════════════════════
    // GETTERS & SETTERS - METADATA
    // ═══════════════════════════════════════════════════════════════

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}