package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.config.*;
import com.ceatformacion.demovitalink_v2.mapper.ConfigMedicoMapper;
import com.ceatformacion.demovitalink_v2.model.*;
import com.ceatformacion.demovitalink_v2.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfigMedicoService {

    private final ConfigMedicoRepository medicoRepo;
    private final ConfigAgendaRepository agendaRepo;
    private final ConfigNotificacionesRepository notisRepo;
    private final ConfigIntegracionesRepository integRepo;
    private final PlantillaMedicoRepository plantillaRepo;
    private final PasswordEncoder passwordEncoder;

    public ConfigMedicoService(ConfigMedicoRepository medicoRepo,
                               ConfigAgendaRepository agendaRepo,
                               ConfigNotificacionesRepository notisRepo,
                               ConfigIntegracionesRepository integRepo,
                               PlantillaMedicoRepository plantillaRepo,
                               PasswordEncoder passwordEncoder) {
        this.medicoRepo = medicoRepo;
        this.agendaRepo = agendaRepo;
        this.notisRepo = notisRepo;
        this.integRepo = integRepo;
        this.plantillaRepo = plantillaRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // ═══════════════════════════════════════════════════════════════
    // GET OR CREATE
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public ConfigMedicoDTO getOrCreate(Usuarios medico) {
        ConfigMedico cm = medicoRepo.findByMedico(medico).orElseGet(() -> bootstrap(medico));
        ConfigAgenda ca = agendaRepo.findByConfig(cm).orElse(null);
        ConfigNotificaciones cn = notisRepo.findByConfig(cm).orElse(null);
        ConfigIntegraciones ci = integRepo.findByConfig(cm).orElse(null);
        List<PlantillaMedico> plantillas = plantillaRepo.findByConfigOrderByNombreAsc(cm);

        return ConfigMedicoMapper.toDTO(cm, ca, cn, ci, plantillas);
    }

    // ═══════════════════════════════════════════════════════════════
    // SAVE ALL
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public ConfigMedicoDTO saveAll(Usuarios medico, ConfigMedicoDTO dto) {
        ConfigMedico cm = medicoRepo.findByMedico(medico).orElseGet(() -> bootstrap(medico));

        // Merge secciones principales (Perfil, UI, Chat, Privacidad, Centro, Seguridad flag)
        ConfigMedicoMapper.merge(cm, dto);
        cm = medicoRepo.save(cm);

        // AGENDA
        if (dto.agenda() != null) {
            ConfigMedico finalCm = cm;
            ConfigAgenda ca = agendaRepo.findByConfig(cm).orElseGet(() -> {
                ConfigAgenda x = new ConfigAgenda();
                x.setConfig(finalCm);
                return x;
            });
            ConfigMedicoMapper.merge(ca, dto.agenda());
            agendaRepo.save(ca);
        }

        // NOTIFICACIONES
        if (dto.notificaciones() != null) {
            ConfigMedico finalCm = cm;
            ConfigNotificaciones cn = notisRepo.findByConfig(cm).orElseGet(() -> {
                ConfigNotificaciones x = new ConfigNotificaciones();
                x.setConfig(finalCm);
                return x;
            });
            ConfigMedicoMapper.merge(cn, dto.notificaciones());
            notisRepo.save(cn);
        }

        // INTEGRACIONES
        if (dto.integraciones() != null) {
            ConfigMedico finalCm = cm;
            ConfigIntegraciones ci = integRepo.findByConfig(cm).orElseGet(() -> {
                ConfigIntegraciones x = new ConfigIntegraciones();
                x.setConfig(finalCm);
                return x;
            });
            ConfigMedicoMapper.merge(ci, dto.integraciones());
            integRepo.save(ci);
        }

        // PLANTILLAS (CRUD completo)
        if (dto.plantillas() != null) {
            savePlantillas(cm, dto.plantillas());
        }

        // SEGURIDAD: Cambio de contraseña
        if (dto.seguridad() != null) {
            handlePasswordChange(medico, dto.seguridad());
        }

        // Reload y devolver
        ConfigAgenda ca = agendaRepo.findByConfig(cm).orElse(null);
        ConfigNotificaciones cn = notisRepo.findByConfig(cm).orElse(null);
        ConfigIntegraciones ci = integRepo.findByConfig(cm).orElse(null);
        List<PlantillaMedico> plantillas = plantillaRepo.findByConfigOrderByNombreAsc(cm);

        return ConfigMedicoMapper.toDTO(cm, ca, cn, ci, plantillas);
    }

    // ═══════════════════════════════════════════════════════════════
    // PLANTILLAS CRUD
    // ═══════════════════════════════════════════════════════════════

    private void savePlantillas(ConfigMedico cm, List<PlantillaDTO> dtos) {
        // Obtener IDs existentes que vienen en el DTO
        List<Long> idsEnviados = dtos.stream()
                .map(PlantillaDTO::id)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        // Eliminar plantillas que ya no están en la lista
        List<PlantillaMedico> existentes = plantillaRepo.findByConfig(cm);
        for (PlantillaMedico p : existentes) {
            if (!idsEnviados.contains(p.getId())) {
                plantillaRepo.delete(p);
            }
        }

        // Crear o actualizar
        for (PlantillaDTO dto : dtos) {
            PlantillaMedico p;
            if (dto.id() != null) {
                // Actualizar existente
                p = plantillaRepo.findById(dto.id()).orElse(null);
                if (p == null || p.getConfig().getIdConfig() != cm.getIdConfig()) {
                    continue;
                }
            } else {
                // Crear nueva
                p = new PlantillaMedico();
                p.setConfig(cm);
            }

            p.setTipo(dto.tipo());
            p.setNombre(dto.nombre());
            p.setContenido(dto.contenido());
            plantillaRepo.save(p);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SEGURIDAD: CAMBIO DE CONTRASEÑA
    // ═══════════════════════════════════════════════════════════════

    private void handlePasswordChange(Usuarios medico, SeguridadDTO dto) {
        if (dto.nuevaPassword() == null || dto.nuevaPassword().isBlank()) {
            return; // No hay cambio de contraseña
        }

        // Validar que coincidan
        if (!dto.nuevaPassword().equals(dto.confirmarPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        // Validar longitud mínima
        if (dto.nuevaPassword().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        // Encriptar y guardar
        String encoded = passwordEncoder.encode(dto.nuevaPassword());
        medico.setPassword(encoded);
        // El usuario se guarda automáticamente al estar en el contexto de transacción
        // Si no, necesitarías inyectar UsuariosRepository y llamar .save(medico)
    }

    // ═══════════════════════════════════════════════════════════════
    // BOOTSTRAP (valores por defecto)
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    protected ConfigMedico bootstrap(Usuarios medico) {
        ConfigMedico cm = new ConfigMedico();
        cm.setMedico(medico);

        // UI defaults
        cm.setTema("auto");
        cm.setIdioma("es");
        cm.setZonaHoraria("Europe/Madrid");
        cm.setHome("dashboard");

        // Chat defaults
        cm.setChatEstado("DISPONIBLE");
        cm.setRespuestasRapidasJson("{\"hola\":\"Hola, ¿en qué puedo ayudarte?\",\"gracias\":\"Gracias, que tenga un buen día.\"}");

        // Privacidad defaults
        cm.setPrivacidadVisibilidad("PUBLICO");
        cm.setPrivacidadUsoDatos(true);
        cm.setPrivacidadBoletines(false);

        // Seguridad default
        cm.setTwoFactorEnabled(false);

        cm = medicoRepo.save(cm);

        // Agenda defaults
        ConfigAgenda ca = new ConfigAgenda();
        ca.setConfig(cm);
        ca.setDuracionGeneralMin(20);
        ca.setBufferMin(5);
        ca.setReglasJson("{\"antelacionMinHoras\":24,\"cancelacionMinHoras\":6,\"overbooking\":false}");
        ca.setDisponibilidadJson("{\"lunes\":[{\"desde\":\"09:00\",\"hasta\":\"14:00\",\"ubicacion\":\"consulta1\"}],\"martes\":[]}");
        ca.setInstruccionesPorTipoJson("{\"general\":\"Llegue 10' antes\",\"teleconsulta\":\"Conéctese 5' antes\"}");
        agendaRepo.save(ca);

        // Notificaciones defaults
        ConfigNotificaciones cn = new ConfigNotificaciones();
        cn.setConfig(cm);
        cn.setCanalesJson("{\"email\":true,\"sms\":false,\"inapp\":true}");
        cn.setEventosJson("{\"nuevaCita\":true,\"cancelacion\":true,\"noAsiste\":true,\"mensaje\":true}");
        cn.setSilencioDesde("22:00");
        cn.setSilencioHasta("07:00");
        cn.setPlantillasJson("{\"nuevaCita\":\"Hola {paciente}, su cita es el {fecha}.\"}");
        notisRepo.save(cn);

        // Integraciones defaults
        ConfigIntegraciones ci = new ConfigIntegraciones();
        ci.setConfig(cm);
        ci.setGoogleCalendar(false);
        ci.setOutlookCalendar(false);
        ci.setSmsProvider(false);
        ci.setApiKeysJson("{\"google\":\"\",\"outlook\":\"\",\"sms\":\"\"}");
        integRepo.save(ci);

        return cm;
    }
}