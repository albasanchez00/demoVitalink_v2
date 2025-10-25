package com.ceatformacion.demovitalink_v2.mapper;

import com.ceatformacion.demovitalink_v2.dto.config.*;
import com.ceatformacion.demovitalink_v2.model.ConfigAgenda;
import com.ceatformacion.demovitalink_v2.model.ConfigMedico;
import com.ceatformacion.demovitalink_v2.model.ConfigNotificaciones;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigMedicoMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /* ====== To DTO ====== */
    public static ConfigMedicoDTO toDTO(ConfigMedico cm, ConfigAgenda ca, ConfigNotificaciones cn) {
        PerfilDTO perfil = new PerfilDTO(
                cm.getNombreMostrar(),
                cm.getEspecialidad(),
                cm.getColegiado(),
                cm.getBio(),
                cm.getFirmaTexto(),
                cm.getFirmaImagenUrl()
        );
        UiDTO ui = new UiDTO(cm.getTema(), cm.getIdioma(), cm.getZonaHoraria(), cm.getHome());

        AgendaDTO agenda = null;
        if (ca != null) {
            ReglasDTO reglas = read(ca.getReglasJson(), ReglasDTO.class, new ReglasDTO(24, 6, false));
            Map<String, List<BloqueDTO>> disp = read(ca.getDisponibilidadJson(),
                    new TypeReference<>() {}, new HashMap<>());
            Map<String,String> instr = read(ca.getInstruccionesPorTipoJson(),
                    new TypeReference<>() {}, new HashMap<>());
            agenda = new AgendaDTO(ca.getDuracionGeneralMin(), ca.getBufferMin(), reglas, disp, instr);
        }

        NotificacionesDTO notis = null;
        if (cn != null) {
            CanalesDTO canales = read(cn.getCanalesJson(), CanalesDTO.class, new CanalesDTO(true,false,true));
            EventosDTO eventos = read(cn.getEventosJson(), EventosDTO.class,
                    new EventosDTO(true,true,true,true));
            Map<String,String> plantillas = read(cn.getPlantillasJson(),
                    new TypeReference<>() {}, new HashMap<>());
            notis = new NotificacionesDTO(canales, eventos, cn.getSilencioDesde(), cn.getSilencioHasta(), plantillas);
        }

        return new ConfigMedicoDTO(perfil, ui, agenda, notis, cm.getVersion());
    }

    /* ====== From DTO (merge) ====== */
    public static void merge(ConfigMedico cm, ConfigMedicoDTO dto) {
        if (dto.perfil() != null) {
            var p = dto.perfil();
            cm.setNombreMostrar(p.nombreMostrar());
            cm.setEspecialidad(p.especialidad());
            cm.setColegiado(p.colegiado());
            cm.setBio(p.bio());
            cm.setFirmaTexto(p.firmaTexto());
            cm.setFirmaImagenUrl(p.firmaImagenUrl());
        }
        if (dto.ui() != null) {
            var ui = dto.ui();
            cm.setTema(nullSafe(ui.tema(), cm.getTema()));
            cm.setIdioma(nullSafe(ui.idioma(), cm.getIdioma()));
            cm.setZonaHoraria(nullSafe(ui.zonaHoraria(), cm.getZonaHoraria()));
            cm.setHome(nullSafe(ui.home(), cm.getHome()));
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
        writeIfNotNull(() -> dto.canales(), v -> cn.setCanalesJson(write(v)));
        writeIfNotNull(() -> dto.eventos(), v -> cn.setEventosJson(write(v)));
        if (dto.silencioDesde() != null) cn.setSilencioDesde(dto.silencioDesde());
        if (dto.silencioHasta() != null) cn.setSilencioHasta(dto.silencioHasta());
        writeIfNotNull(() -> dto.plantillas(), v -> cn.setPlantillasJson(write(v)));
    }

    /* ====== JSON helpers ====== */
    private static <T> T read(String json, Class<T> type, T def) {
        try { return json == null ? def : MAPPER.readValue(json, type); }
        catch (Exception e) { return def; }
    }
    private static <T> T read(String json, TypeReference<T> type, T def) {
        try { return json == null ? def : MAPPER.readValue(json, type); }
        catch (Exception e) { return def; }
    }
    private static String write(Object value) {
        try { return MAPPER.writeValueAsString(value); }
        catch (Exception e) { return null; }
    }
    private static <T> void writeIfNotNull(Supplier<T> getter, Consumer<T> writer){
        T v = getter.get(); if (v != null) writer.accept(v);
    }

    private static String nullSafe(String v, String fallback) { return v != null ? v : fallback; }
}