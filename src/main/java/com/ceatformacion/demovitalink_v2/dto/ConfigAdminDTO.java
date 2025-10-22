package com.ceatformacion.demovitalink_v2.dto;


import java.util.List;

public record ConfigAdminDTO(
        String nombreSistema, String logoUrl, String colorPrimario, String colorSecundario,
        String centroNombre, String centroCiudad, String timezone,
        String privacidadUrl, String terminosUrl, String cookiesUrl,
        String mailFrom, Boolean notifCitas, Boolean notifMedicacion, Boolean notifSintomas,
        Integer pwdMinLen, Integer pwdExpireDays, Boolean twoFactorEnabled,
        Integer citaDuracionMin, String horarioInicio, String horarioFin, List<String> diasNoLaborables,
        String smtpHost, Integer smtpPort, String smtpUser, String webhookBase
) {}
