package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.ConfigAdminDTO;
import com.ceatformacion.demovitalink_v2.model.ConfigGlobal;
import com.ceatformacion.demovitalink_v2.repository.ConfigGlobalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ConfigAdminServiceImpl implements ConfigAdminService {

    private static final long SINGLETON_ID = 1L;
    private final ConfigGlobalRepository repo;

    public ConfigAdminServiceImpl(ConfigGlobalRepository repo) {
        this.repo = repo;
    }

    @Override
    public ConfigAdminDTO getActual() {
        ConfigGlobal e = repo.findById(SINGLETON_ID).orElseGet(this::defaultsEntity);
        return toDto(fillDefaults(e));
    }

    @Transactional
    @Override
    public ConfigAdminDTO save(ConfigAdminDTO dto) {
        validate(dto);
        ConfigGlobal e = repo.findById(SINGLETON_ID).orElseGet(this::defaultsEntity);
        apply(e, normalize(dto));
        repo.save(e);
        return toDto(fillDefaults(e));
    }

    /* ================= Validación & normalización ================= */

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private void validate(ConfigAdminDTO c) {
        // Email
        if (c.mailFrom() != null && !c.mailFrom().isBlank() && !EMAIL.matcher(c.mailFrom()).matches())
            throw new IllegalArgumentException("Email remitente no válido.");

        // Seguridad
        if (c.pwdMinLen() != null && c.pwdMinLen() < 6)
            throw new IllegalArgumentException("Longitud mínima de contraseña debe ser >= 6.");

        // Horarios
        if (c.horarioInicio() != null && c.horarioFin() != null
                && !c.horarioInicio().isBlank() && !c.horarioFin().isBlank()
                && c.horarioInicio().compareTo(c.horarioFin()) >= 0)
            throw new IllegalArgumentException("El horario de inicio debe ser anterior al de fin.");

        // Timezone
        if (c.timezone() != null && !c.timezone().isBlank()) {
            try {
                ZoneId.of(c.timezone());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Zona horaria IANA inválida.");
            }
        }

        // SMTP
        if (c.smtpPort() != null && (c.smtpPort() < 1 || c.smtpPort() > 65535))
            throw new IllegalArgumentException("Puerto SMTP inválido.");

        // Límites
        if (c.maxPacientesPorMedico() != null && c.maxPacientesPorMedico() < 0)
            throw new IllegalArgumentException("Límite de pacientes por médico no puede ser negativo.");
        if (c.maxCitasDiarias() != null && c.maxCitasDiarias() < 0)
            throw new IllegalArgumentException("Límite de citas diarias no puede ser negativo.");
        if (c.maxArchivoMB() != null && c.maxArchivoMB() < 1)
            throw new IllegalArgumentException("Tamaño máximo de archivo debe ser al menos 1 MB.");

        // API
        if (c.apiRateLimitReq() != null && c.apiRateLimitReq() < 1)
            throw new IllegalArgumentException("Rate limit debe ser al menos 1 petición.");
    }

    private ConfigAdminDTO normalize(ConfigAdminDTO c) {
        return new ConfigAdminDTO(
                // Identidad
                trimOrNull(c.nombreSistema()),
                trimOrNull(c.logoUrl()),
                def(c.colorPrimario(), "#1b5bff"),
                def(c.colorSecundario(), "#00b894"),

                // Centro
                trimOrNull(c.centroNombre()),
                trimOrNull(c.centroCiudad()),
                def(c.timezone(), "Europe/Madrid"),

                // Legales
                def(c.privacidadUrl(), "/politicasPrivacidad"),
                def(c.terminosUrl(), "/terminosCondiciones"),
                def(c.cookiesUrl(), "/politicaCookies"),

                // Notificaciones
                trimOrNull(c.mailFrom()),
                bool(c.notifCitas()),
                bool(c.notifMedicacion()),
                bool(c.notifSintomas()),

                // Seguridad
                numOr(c.pwdMinLen(), 10),
                numOr(c.pwdExpireDays(), 0),
                bool(c.twoFactorEnabled()),

                // Citas
                numOr(c.citaDuracionMin(), 30),
                def(c.horarioInicio(), "08:00"),
                def(c.horarioFin(), "17:00"),
                c.diasNoLaborables() == null ? List.of() : c.diasNoLaborables().stream().map(String::trim).filter(s -> !s.isBlank()).toList(),

                // Integraciones SMTP
                trimOrNull(c.smtpHost()),
                numOr(c.smtpPort(), 587),
                trimOrNull(c.smtpUser()),
                trimOrNull(c.webhookBase()),

                // Usuarios
                numOr(c.maxUsuariosSistema(), 0),
                numOr(c.maxMedicosSistema(), 0),
                bool(c.registroAbierto()),
                bool(c.requiereAprobacion()),
                def(c.rolesDisponiblesCsv(), "ADMIN,MEDICO,USUARIO"),

                // Auditoría
                def(c.nivelAuditoria(), "BASICO"),
                numOr(c.retencionLogsDias(), 90),
                bool(c.auditarAccesos()),
                bool(c.auditarCambios()),
                bool(c.auditarErrores()),

                // Backup
                bool(c.backupAutomatico()),
                def(c.backupFrecuencia(), "DIARIO"),
                def(c.backupHora(), "03:00"),
                numOr(c.backupRetencionDias(), 30),
                def(c.backupDestino(), "LOCAL"),

                // UI
                def(c.fuentePrincipal(), "Inter"),
                numOr(c.tamanoFuenteBase(), 16),
                def(c.densidadUI(), "NORMAL"),
                boolTrue(c.mostrarAyuda()),
                boolTrue(c.animacionesUI()),

                // Límites
                numOr(c.maxPacientesPorMedico(), 0),
                numOr(c.maxCitasDiarias(), 0),
                numOr(c.maxAlmacenamientoMB(), 1024),
                numOr(c.maxArchivoMB(), 10),

                // Mantenimiento
                bool(c.modoMantenimiento()),
                trimOrNull(c.mensajeMantenimiento()),
                trimOrNull(c.mantenimientoProgramado()),
                trimOrNull(c.ipsBlanqueadasCsv()),

                // API
                boolTrue(c.apiHabilitada()),
                numOr(c.apiRateLimitReq(), 1000),
                numOr(c.apiTokenExpiraDias(), 365),
                bool(c.webhooksHabilitados()),
                trimOrNull(c.webhookSecretKey()),
                trimOrNull(c.webhookEventosCsv())
        );
    }

    /* ================= Mapping DTO ↔ Entity ================= */

    private void apply(ConfigGlobal e, ConfigAdminDTO c) {
        // Identidad
        e.setNombreSistema(c.nombreSistema());
        e.setLogoUrl(c.logoUrl());
        e.setColorPrimario(c.colorPrimario());
        e.setColorSecundario(c.colorSecundario());

        // Centro
        e.setCentroNombre(c.centroNombre());
        e.setCentroCiudad(c.centroCiudad());
        e.setTimezone(c.timezone());

        // Legales
        e.setPrivacidadUrl(c.privacidadUrl());
        e.setTerminosUrl(c.terminosUrl());
        e.setCookiesUrl(c.cookiesUrl());

        // Notificaciones
        e.setMailFrom(c.mailFrom());
        e.setNotifCitas(c.notifCitas());
        e.setNotifMedicacion(c.notifMedicacion());
        e.setNotifSintomas(c.notifSintomas());

        // Seguridad
        e.setPwdMinLen(c.pwdMinLen());
        e.setPwdExpireDays(c.pwdExpireDays());
        e.setTwoFactorEnabled(c.twoFactorEnabled());

        // Citas
        e.setCitaDuracionMin(c.citaDuracionMin());
        e.setHorarioInicio(c.horarioInicio());
        e.setHorarioFin(c.horarioFin());
        e.setDiasNoLaborablesList(c.diasNoLaborables());

        // Integraciones SMTP
        e.setSmtpHost(c.smtpHost());
        e.setSmtpPort(c.smtpPort());
        e.setSmtpUser(c.smtpUser());
        e.setWebhookBase(c.webhookBase());

        // Usuarios
        e.setMaxUsuariosSistema(c.maxUsuariosSistema());
        e.setMaxMedicosSistema(c.maxMedicosSistema());
        e.setRegistroAbierto(c.registroAbierto());
        e.setRequiereAprobacion(c.requiereAprobacion());
        e.setRolesDisponiblesCsv(c.rolesDisponiblesCsv());

        // Auditoría
        e.setNivelAuditoria(c.nivelAuditoria());
        e.setRetencionLogsDias(c.retencionLogsDias());
        e.setAuditarAccesos(c.auditarAccesos());
        e.setAuditarCambios(c.auditarCambios());
        e.setAuditarErrores(c.auditarErrores());

        // Backup
        e.setBackupAutomatico(c.backupAutomatico());
        e.setBackupFrecuencia(c.backupFrecuencia());
        e.setBackupHora(c.backupHora());
        e.setBackupRetencionDias(c.backupRetencionDias());
        e.setBackupDestino(c.backupDestino());

        // UI
        e.setFuentePrincipal(c.fuentePrincipal());
        e.setTamanoFuenteBase(c.tamanoFuenteBase());
        e.setDensidadUI(c.densidadUI());
        e.setMostrarAyuda(c.mostrarAyuda());
        e.setAnimacionesUI(c.animacionesUI());

        // Límites
        e.setMaxPacientesPorMedico(c.maxPacientesPorMedico());
        e.setMaxCitasDiarias(c.maxCitasDiarias());
        e.setMaxAlmacenamientoMB(c.maxAlmacenamientoMB());
        e.setMaxArchivoMB(c.maxArchivoMB());

        // Mantenimiento
        e.setModoMantenimiento(c.modoMantenimiento());
        e.setMensajeMantenimiento(c.mensajeMantenimiento());
        e.setMantenimientoProgramado(c.mantenimientoProgramado());
        e.setIpsBlanqueadasCsv(c.ipsBlanqueadasCsv());

        // API
        e.setApiHabilitada(c.apiHabilitada());
        e.setApiRateLimitReq(c.apiRateLimitReq());
        e.setApiTokenExpiraDias(c.apiTokenExpiraDias());
        e.setWebhooksHabilitados(c.webhooksHabilitados());
        e.setWebhookSecretKey(c.webhookSecretKey());
        e.setWebhookEventosCsv(c.webhookEventosCsv());
    }

    private ConfigAdminDTO toDto(ConfigGlobal e) {
        return new ConfigAdminDTO(
                // Identidad
                e.getNombreSistema(), e.getLogoUrl(), e.getColorPrimario(), e.getColorSecundario(),
                // Centro
                e.getCentroNombre(), e.getCentroCiudad(), e.getTimezone(),
                // Legales
                e.getPrivacidadUrl(), e.getTerminosUrl(), e.getCookiesUrl(),
                // Notificaciones
                e.getMailFrom(), e.getNotifCitas(), e.getNotifMedicacion(), e.getNotifSintomas(),
                // Seguridad
                e.getPwdMinLen(), e.getPwdExpireDays(), e.getTwoFactorEnabled(),
                // Citas
                e.getCitaDuracionMin(), e.getHorarioInicio(), e.getHorarioFin(), e.getDiasNoLaborablesList(),
                // Integraciones SMTP
                e.getSmtpHost(), e.getSmtpPort(), e.getSmtpUser(), e.getWebhookBase(),
                // Usuarios
                e.getMaxUsuariosSistema(), e.getMaxMedicosSistema(), e.getRegistroAbierto(),
                e.getRequiereAprobacion(), e.getRolesDisponiblesCsv(),
                // Auditoría
                e.getNivelAuditoria(), e.getRetencionLogsDias(), e.getAuditarAccesos(),
                e.getAuditarCambios(), e.getAuditarErrores(),
                // Backup
                e.getBackupAutomatico(), e.getBackupFrecuencia(), e.getBackupHora(),
                e.getBackupRetencionDias(), e.getBackupDestino(),
                // UI
                e.getFuentePrincipal(), e.getTamanoFuenteBase(), e.getDensidadUI(),
                e.getMostrarAyuda(), e.getAnimacionesUI(),
                // Límites
                e.getMaxPacientesPorMedico(), e.getMaxCitasDiarias(),
                e.getMaxAlmacenamientoMB(), e.getMaxArchivoMB(),
                // Mantenimiento
                e.getModoMantenimiento(), e.getMensajeMantenimiento(),
                e.getMantenimientoProgramado(), e.getIpsBlanqueadasCsv(),
                // API
                e.getApiHabilitada(), e.getApiRateLimitReq(), e.getApiTokenExpiraDias(),
                e.getWebhooksHabilitados(), e.getWebhookSecretKey(), e.getWebhookEventosCsv()
        );
    }

    /* ================= Defaults ================= */

    private ConfigGlobal defaultsEntity() {
        ConfigGlobal e = new ConfigGlobal();
        e.setIdConfig(SINGLETON_ID);

        // Identidad
        e.setNombreSistema("VitaLink");
        e.setColorPrimario("#1b5bff");
        e.setColorSecundario("#00b894");

        // Centro
        e.setCentroNombre("Hospital General");
        e.setCentroCiudad("Madrid");
        e.setTimezone("Europe/Madrid");

        // Legales
        e.setPrivacidadUrl("/politicasPrivacidad");
        e.setTerminosUrl("/terminosCondiciones");
        e.setCookiesUrl("/politicaCookies");

        // Notificaciones
        e.setMailFrom("no-reply@tudominio.com");
        e.setNotifCitas(true);
        e.setNotifMedicacion(false);
        e.setNotifSintomas(true);

        // Seguridad
        e.setPwdMinLen(10);
        e.setPwdExpireDays(0);
        e.setTwoFactorEnabled(false);

        // Citas
        e.setCitaDuracionMin(30);
        e.setHorarioInicio("08:00");
        e.setHorarioFin("17:00");
        e.setDiasNoLaborablesList(List.of());

        // Integraciones
        e.setSmtpPort(587);

        // Usuarios
        e.setMaxUsuariosSistema(0);
        e.setMaxMedicosSistema(0);
        e.setRegistroAbierto(true);
        e.setRequiereAprobacion(false);
        e.setRolesDisponiblesCsv("ADMIN,MEDICO,USUARIO");

        // Auditoría
        e.setNivelAuditoria("BASICO");
        e.setRetencionLogsDias(90);
        e.setAuditarAccesos(true);
        e.setAuditarCambios(true);
        e.setAuditarErrores(true);

        // Backup
        e.setBackupAutomatico(true);
        e.setBackupFrecuencia("DIARIO");
        e.setBackupHora("03:00");
        e.setBackupRetencionDias(30);
        e.setBackupDestino("LOCAL");

        // UI
        e.setFuentePrincipal("Inter");
        e.setTamanoFuenteBase(16);
        e.setDensidadUI("NORMAL");
        e.setMostrarAyuda(true);
        e.setAnimacionesUI(true);

        // Límites
        e.setMaxPacientesPorMedico(0);
        e.setMaxCitasDiarias(0);
        e.setMaxAlmacenamientoMB(1024);
        e.setMaxArchivoMB(10);

        // Mantenimiento
        e.setModoMantenimiento(false);

        // API
        e.setApiHabilitada(true);
        e.setApiRateLimitReq(1000);
        e.setApiTokenExpiraDias(365);
        e.setWebhooksHabilitados(false);

        return e;
    }

    private ConfigGlobal fillDefaults(ConfigGlobal e) {
        // Identidad
        if (e.getColorPrimario() == null) e.setColorPrimario("#1b5bff");
        if (e.getColorSecundario() == null) e.setColorSecundario("#00b894");

        // Centro
        if (e.getTimezone() == null) e.setTimezone("Europe/Madrid");

        // Legales
        if (e.getPrivacidadUrl() == null) e.setPrivacidadUrl("/politicasPrivacidad");
        if (e.getTerminosUrl() == null) e.setTerminosUrl("/terminosCondiciones");
        if (e.getCookiesUrl() == null) e.setCookiesUrl("/politicaCookies");

        // Seguridad
        if (e.getPwdMinLen() == null) e.setPwdMinLen(10);
        if (e.getPwdExpireDays() == null) e.setPwdExpireDays(0);

        // Citas
        if (e.getCitaDuracionMin() == null) e.setCitaDuracionMin(30);
        if (e.getHorarioInicio() == null) e.setHorarioInicio("08:00");
        if (e.getHorarioFin() == null) e.setHorarioFin("17:00");

        // Integraciones
        if (e.getSmtpPort() == null) e.setSmtpPort(587);

        // Usuarios
        if (e.getMaxUsuariosSistema() == null) e.setMaxUsuariosSistema(0);
        if (e.getMaxMedicosSistema() == null) e.setMaxMedicosSistema(0);
        if (e.getRolesDisponiblesCsv() == null) e.setRolesDisponiblesCsv("ADMIN,MEDICO,USUARIO");

        // Auditoría
        if (e.getNivelAuditoria() == null) e.setNivelAuditoria("BASICO");
        if (e.getRetencionLogsDias() == null) e.setRetencionLogsDias(90);

        // Backup
        if (e.getBackupFrecuencia() == null) e.setBackupFrecuencia("DIARIO");
        if (e.getBackupHora() == null) e.setBackupHora("03:00");
        if (e.getBackupRetencionDias() == null) e.setBackupRetencionDias(30);
        if (e.getBackupDestino() == null) e.setBackupDestino("LOCAL");

        // UI
        if (e.getFuentePrincipal() == null) e.setFuentePrincipal("Inter");
        if (e.getTamanoFuenteBase() == null) e.setTamanoFuenteBase(16);
        if (e.getDensidadUI() == null) e.setDensidadUI("NORMAL");
        if (e.getMostrarAyuda() == null) e.setMostrarAyuda(true);
        if (e.getAnimacionesUI() == null) e.setAnimacionesUI(true);

        // Límites
        if (e.getMaxPacientesPorMedico() == null) e.setMaxPacientesPorMedico(0);
        if (e.getMaxCitasDiarias() == null) e.setMaxCitasDiarias(0);
        if (e.getMaxAlmacenamientoMB() == null) e.setMaxAlmacenamientoMB(1024);
        if (e.getMaxArchivoMB() == null) e.setMaxArchivoMB(10);

        // API
        if (e.getApiHabilitada() == null) e.setApiHabilitada(true);
        if (e.getApiRateLimitReq() == null) e.setApiRateLimitReq(1000);
        if (e.getApiTokenExpiraDias() == null) e.setApiTokenExpiraDias(365);

        return e;
    }

    /* ================= Helpers ================= */

    private static Boolean bool(Boolean b) {
        return Boolean.TRUE.equals(b);
    }

    private static Boolean boolTrue(Boolean b) {
        return b == null || Boolean.TRUE.equals(b);
    }

    private static Integer numOr(Integer n, Integer d) {
        return n == null ? d : n;
    }

    private static String def(String v, String d) {
        return (v == null || v.isBlank()) ? d : v.trim();
    }

    private static String trimOrNull(String v) {
        return v == null ? null : v.trim();
    }
}