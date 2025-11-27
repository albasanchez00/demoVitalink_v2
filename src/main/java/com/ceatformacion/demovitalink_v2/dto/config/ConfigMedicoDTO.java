package com.ceatformacion.demovitalink_v2.dto.config;

import java.util.List;

public record ConfigMedicoDTO(
        // Secciones existentes
        PerfilDTO perfil,
        UiDTO ui,
        AgendaDTO agenda,
        NotificacionesDTO notificaciones,

        // Nuevas secciones
        ChatDTO chat,
        PrivacidadDTO privacidad,
        CentroDTO centro,
        IntegracionesDTO integraciones,
        SeguridadDTO seguridad,
        List<PlantillaDTO> plantillas,

        Integer version
) {
    // Constructor de compatibilidad con código existente (4 secciones + version)
    public ConfigMedicoDTO(PerfilDTO perfil, UiDTO ui, AgendaDTO agenda,
                           NotificacionesDTO notificaciones, Integer version) {
        this(perfil, ui, agenda, notificaciones, null, null, null, null, null, null, version);
    }
}
