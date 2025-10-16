package com.ceatformacion.demovitalink_v2.dto.config;

public record ConfigMedicoDTO(
        PerfilDTO perfil,
        UiDTO ui,
        AgendaDTO agenda,
        NotificacionesDTO notificaciones,
        Integer version
) {}
