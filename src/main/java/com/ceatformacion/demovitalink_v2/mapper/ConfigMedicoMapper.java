package com.ceatformacion.demovitalink_v2.mapper;

import com.ceatformacion.demovitalink_v2.dto.config.*;
import com.ceatformacion.demovitalink_v2.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfigMedicoMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ═══════════════════════════════════════════════════════════════
    // TO DTO (Entity → DTO)
    // ═══════════════════════════════════════════════════════════════

    public static ConfigMedicoDTO toDTO(ConfigMedico cm,
                                        ConfigAgenda ca,
                                        ConfigNotificaciones cn,
                                        ConfigIntegraciones ci,
                                        List<PlantillaMedico> plantillas) {

        // PERFIL
        PerfilDTO perfil = new PerfilDTO(
                cm.getNombreMostrar(),
                cm.getEspecialidad(),
                cm.getColegiado(),
                cm.getBio(),
                cm.getFirmaTexto(),
                cm.getFirmaImagenUrl()
        );

        // UI
        UiDTO ui = new UiDTO(
                cm.getTema(),
                cm.getIdioma(),
                cm.getZonaHoraria(),
                cm.getHome()
        );

        // AGENDA
        AgendaDTO agenda = null;
        if (ca != null) {
            ReglasDTO reglas = read(ca.getReglasJson(), ReglasDTO.class,
                    new ReglasDTO(24, 6, false));
            Map<String, List<BloqueDTO>> disp = read(ca.getDisponibilidadJson(),
                    new TypeReference<>() {}, new HashMap<>());
            Map<String, String> instr = read(ca.getInstruccionesPorTipoJson(),
                    new TypeReference<>() {}, new HashMap<>());
            agenda = new AgendaDTO(ca.getDuracionGeneralMin(), ca.getBufferMin(), reglas, disp, instr);
        }

        // NOTIFICACIONES
        NotificacionesDTO notis = null;
        if (cn != null) {
            CanalesDTO canales = read(cn.getCanalesJson(), CanalesDTO.class,
                    new CanalesDTO(true, false, true));
            EventosDTO eventos = read(cn.getEventosJson(), EventosDTO.class,
                    new EventosDTO(true, true, true, true));
            Map<String, String> plantillasNotis = read(cn.getPlantillasJson(),
                    new TypeReference<>() {}, new HashMap<>());
            notis = new NotificacionesDTO(canales, eventos, cn.getSilencioDesde(), cn.getSilencioHasta(), plantillasNotis);
        }

        // CHAT
        ChatDTO chat = new ChatDTO(
                cm.getChatEstado(),
                cm.getChatFirma(),
                read(cm.getRespuestasRapidasJson(), new TypeReference<>() {}, new HashMap<>())
        );

        // PRIVACIDAD
        PrivacidadDTO privacidad = new PrivacidadDTO(
                cm.getPrivacidadVisibilidad(),
                cm.getPrivacidadUsoDatos(),
                cm.getPrivacidadBoletines()
        );

        // CENTRO
        CentroDTO centro = new CentroDTO(
                cm.getCentroNombre(),
                cm.getCentroTelefono(),
                cm.getCentroDireccion(),
                cm.getCentroHorario(),
                read(cm.getCentroServiciosJson(), new TypeReference<>() {}, new HashMap<>())
        );

        // INTEGRACIONES
        IntegracionesDTO integraciones = null;
        if (ci != null) {
            integraciones = new IntegracionesDTO(
                    ci.getGoogleCalendar(),
                    ci.getOutlookCalendar(),
                    ci.getSmsProvider(),
                    read(ci.getApiKeysJson(), new TypeReference<>() {}, new HashMap<>())
            );
        }

        // SEGURIDAD (solo devolvemos el flag 2FA, nunca passwords)
        SeguridadDTO seguridad = new SeguridadDTO(null, null, cm.getTwoFactorEnabled());

        // PLANTILLAS
        List<PlantillaDTO> plantillasDTO = null;
        if (plantillas != null) {
            plantillasDTO = plantillas.stream()
                    .map(p -> new PlantillaDTO(p.getId(), p.getTipo(), p.getNombre(), p.getContenido()))
                    .collect(Collectors.toList());
        }

        return new ConfigMedicoDTO(
                perfil, ui, agenda, notis,
                chat, privacidad, centro, integraciones, seguridad, plantillasDTO,
                cm.getVersion()
        );
    }

    /**
     * Sobrecarga para compatibilidad con código existente (4 secciones)
     */
    public static ConfigMedicoDTO toDTO(ConfigMedico cm, ConfigAgenda ca, ConfigNotificaciones cn) {
        return toDTO(cm, ca, cn, null, null);
    }

    // ═══════════════════════════════════════════════════════════════
    // FROM DTO (Merge DTO → Entity)
    // ═══════════════════════════════════════════════════════════════

    public static void merge(ConfigMedico cm, ConfigMedicoDTO dto) {
        // PERFIL
        if (dto.perfil() != null) {
            var p = dto.perfil();
            cm.setNombreMostrar(p.nombreMostrar());
            cm.setEspecialidad(p.especialidad());
            cm.setColegiado(p.colegiado());
            cm.setBio(p.bio());
            cm.setFirmaTexto(p.firmaTexto());
            cm.setFirmaImagenUrl(p.firmaImagenUrl());
        }

        // UI
        if (dto.ui() != null) {
            var ui = dto.ui();
            cm.setTema(nullSafe(ui.tema(), cm.getTema()));
            cm.setIdioma(nullSafe(ui.idioma(), cm.getIdioma()));
            cm.setZonaHoraria(nullSafe(ui.zonaHoraria(), cm.getZonaHoraria()));
            cm.setHome(nullSafe(ui.home(), cm.getHome()));
        }

        // CHAT
        if (dto.chat() != null) {
            var c = dto.chat();
            cm.setChatEstado(nullSafe(c.estado(), cm.getChatEstado()));
            cm.setChatFirma(nullSafe(c.firmaChat(), cm.getChatFirma()));
            if (c.respuestasRapidas() != null) {
                cm.setRespuestasRapidasJson(write(c.respuestasRapidas()));
            }
        }

        // PRIVACIDAD
        if (dto.privacidad() != null) {
            var pr = dto.privacidad();
            cm.setPrivacidadVisibilidad(nullSafe(pr.visibilidad(), cm.getPrivacidadVisibilidad()));
            if (pr.usoDatos() != null) cm.setPrivacidadUsoDatos(pr.usoDatos());
            if (pr.boletines() != null) cm.setPrivacidadBoletines(pr.boletines());
        }

        // CENTRO
        if (dto.centro() != null) {
            var ce = dto.centro();
            cm.setCentroNombre(nullSafe(ce.nombreCentro(), cm.getCentroNombre()));
            cm.setCentroTelefono(nullSafe(ce.telefonoCentro(), cm.getCentroTelefono()));
            cm.setCentroDireccion(nullSafe(ce.direccionCentro(), cm.getCentroDireccion()));
            cm.setCentroHorario(nullSafe(ce.horarioCentro(), cm.getCentroHorario()));
            if (ce.servicios() != null) {
                cm.setCentroServiciosJson(write(ce.servicios()));
            }
        }

        // SEGURIDAD (solo 2FA, password se maneja aparte)
        if (dto.seguridad() != null && dto.seguridad().activar2FA() != null) {
            cm.setTwoFactorEnabled(dto.seguridad().activar2FA());
        }
    }

    public static void merge(ConfigAgenda ca, AgendaDTO dto) {
        if (dto == null) return;
        if (dto.duracionGeneralMin() != null) ca.setDuracionGeneralMin(dto.duracionGeneralMin());
        if (dto.bufferMin() != null) ca.setBufferMin(dto.bufferMin());

        writeIfNotNull(() -> dto.reglas(), v -> ca.setReglasJson(write(v)));
        writeIfNotNull(() -> dto.disponibilidad(), v -> ca.setDisponibilidadJson(write(v)));
        writeIfNotNull(() -> dto.instruccionesPorTipo(), v -> ca.setInstruccionesPorTipoJson(write(v)));
    }

    public static void merge(ConfigNotificaciones cn, NotificacionesDTO dto) {
        if (dto == null) return;

        cn.setCanalesJson(write(dto.canales()));
        cn.setEventosJson(write(dto.eventos()));
        cn.setPlantillasJson(write(dto.plantillas()));
        cn.setSilencioDesde(dto.silencioDesde());
        cn.setSilencioHasta(dto.silencioHasta());
    }

    public static void merge(ConfigIntegraciones ci, IntegracionesDTO dto) {
        if (dto == null) return;

        if (dto.googleCalendar() != null) ci.setGoogleCalendar(dto.googleCalendar());
        if (dto.outlookCalendar() != null) ci.setOutlookCalendar(dto.outlookCalendar());
        if (dto.smsProvider() != null) ci.setSmsProvider(dto.smsProvider());
        if (dto.apiKeys() != null) ci.setApiKeysJson(write(dto.apiKeys()));
    }

    // ═══════════════════════════════════════════════════════════════
    // JSON HELPERS
    // ═══════════════════════════════════════════════════════════════

    private static <T> T read(String json, Class<T> type, T def) {
        try {
            return json == null ? def : MAPPER.readValue(json, type);
        } catch (Exception e) {
            return def;
        }
    }

    private static <T> T read(String json, TypeReference<T> type, T def) {
        try {
            return json == null ? def : MAPPER.readValue(json, type);
        } catch (Exception e) {
            return def;
        }
    }

    private static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private static <T> void writeIfNotNull(Supplier<T> getter, Consumer<T> writer) {
        T v = getter.get();
        if (v != null) writer.accept(v);
    }

    private static String nullSafe(String v, String fallback) {
        return v != null ? v : fallback;
    }
}