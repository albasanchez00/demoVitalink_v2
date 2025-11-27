package com.ceatformacion.demovitalink_v2.dto;

import java.util.List;

public record ConfigAdminDTO(
        // ═══════════════════════════════════════════════════════════════
        // IDENTIDAD & MARCA (existente)
        // ═══════════════════════════════════════════════════════════════
        String nombreSistema,
        String logoUrl,
        String colorPrimario,
        String colorSecundario,

        // ═══════════════════════════════════════════════════════════════
        // CENTRO & TZ (existente)
        // ═══════════════════════════════════════════════════════════════
        String centroNombre,
        String centroCiudad,
        String timezone,

        // ═══════════════════════════════════════════════════════════════
        // LEGALES (existente)
        // ═══════════════════════════════════════════════════════════════
        String privacidadUrl,
        String terminosUrl,
        String cookiesUrl,

        // ═══════════════════════════════════════════════════════════════
        // NOTIFICACIONES (existente)
        // ═══════════════════════════════════════════════════════════════
        String mailFrom,
        Boolean notifCitas,
        Boolean notifMedicacion,
        Boolean notifSintomas,

        // ═══════════════════════════════════════════════════════════════
        // SEGURIDAD (existente)
        // ═══════════════════════════════════════════════════════════════
        Integer pwdMinLen,
        Integer pwdExpireDays,
        Boolean twoFactorEnabled,

        // ═══════════════════════════════════════════════════════════════
        // CITAS (existente)
        // ═══════════════════════════════════════════════════════════════
        Integer citaDuracionMin,
        String horarioInicio,
        String horarioFin,
        List<String> diasNoLaborables,

        // ═══════════════════════════════════════════════════════════════
        // INTEGRACIONES (existente, expandido)
        // ═══════════════════════════════════════════════════════════════
        String smtpHost,
        Integer smtpPort,
        String smtpUser,
        String webhookBase,

        // ═══════════════════════════════════════════════════════════════
        // USUARIOS (nuevo)
        // ═══════════════════════════════════════════════════════════════
        Integer maxUsuariosSistema,
        Integer maxMedicosSistema,
        Boolean registroAbierto,
        Boolean requiereAprobacion,
        String rolesDisponiblesCsv,    // "ADMIN,MEDICO,USUARIO"

        // ═══════════════════════════════════════════════════════════════
        // AUDITORÍA / LOGS (nuevo)
        // ═══════════════════════════════════════════════════════════════
        String nivelAuditoria,         // NINGUNO | BASICO | COMPLETO
        Integer retencionLogsDias,
        Boolean auditarAccesos,
        Boolean auditarCambios,
        Boolean auditarErrores,

        // ═══════════════════════════════════════════════════════════════
        // BACKUP (nuevo)
        // ═══════════════════════════════════════════════════════════════
        Boolean backupAutomatico,
        String backupFrecuencia,       // DIARIO | SEMANAL | MENSUAL
        String backupHora,             // HH:mm
        Integer backupRetencionDias,
        String backupDestino,          // LOCAL | S3 | GCS

        // ═══════════════════════════════════════════════════════════════
        // PERSONALIZACIÓN UI (nuevo)
        // ═══════════════════════════════════════════════════════════════
        String fuentePrincipal,
        Integer tamanoFuenteBase,
        String densidadUI,             // COMPACTA | NORMAL | ESPACIOSA
        Boolean mostrarAyuda,
        Boolean animacionesUI,

        // ═══════════════════════════════════════════════════════════════
        // LÍMITES / CUOTAS (nuevo)
        // ═══════════════════════════════════════════════════════════════
        Integer maxPacientesPorMedico,
        Integer maxCitasDiarias,
        Integer maxAlmacenamientoMB,
        Integer maxArchivoMB,

        // ═══════════════════════════════════════════════════════════════
        // MANTENIMIENTO (nuevo)
        // ═══════════════════════════════════════════════════════════════
        Boolean modoMantenimiento,
        String mensajeMantenimiento,
        String mantenimientoProgramado, // ISO datetime
        String ipsBlanqueadasCsv,       // "192.168.1.1,10.0.0.1"

        // ═══════════════════════════════════════════════════════════════
        // API / WEBHOOKS (nuevo, expandido)
        // ═══════════════════════════════════════════════════════════════
        Boolean apiHabilitada,
        Integer apiRateLimitReq,
        Integer apiTokenExpiraDias,
        Boolean webhooksHabilitados,
        String webhookSecretKey,
        String webhookEventosCsv       // "CITA_CREADA,CITA_CANCELADA,USUARIO_REGISTRADO"
) {}