package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.config.AgendaDTO;
import com.ceatformacion.demovitalink_v2.dto.config.ConfigMedicoDTO;
import com.ceatformacion.demovitalink_v2.dto.config.NotificacionesDTO;
import com.ceatformacion.demovitalink_v2.mapper.ConfigMedicoMapper;
import com.ceatformacion.demovitalink_v2.model.ConfigAgenda;
import com.ceatformacion.demovitalink_v2.model.ConfigMedico;
import com.ceatformacion.demovitalink_v2.model.ConfigNotificaciones;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.ConfigAgendaRepository;
import com.ceatformacion.demovitalink_v2.repository.ConfigMedicoRepository;
import com.ceatformacion.demovitalink_v2.repository.ConfigNotificacionesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfigMedicoService {

    private final ConfigMedicoRepository medicoRepo;
    private final ConfigAgendaRepository agendaRepo;
    private final ConfigNotificacionesRepository notisRepo;

    public ConfigMedicoService(ConfigMedicoRepository medicoRepo,
                               ConfigAgendaRepository agendaRepo,
                               ConfigNotificacionesRepository notisRepo) {
        this.medicoRepo = medicoRepo;
        this.agendaRepo = agendaRepo;
        this.notisRepo = notisRepo;
    }

    // IMPORTANTE: transacción NO readOnly
    @Transactional
    public ConfigMedicoDTO getOrCreate(Usuarios medico) {
        ConfigMedico cm = medicoRepo.findByMedico(medico).orElseGet(() -> bootstrap(medico));
        ConfigAgenda ca = agendaRepo.findByConfig(cm).orElse(null);
        ConfigNotificaciones cn = notisRepo.findByConfig(cm).orElse(null);
        return ConfigMedicoMapper.toDTO(cm, ca, cn);
    }

    @Transactional
    public ConfigMedicoDTO saveAll(Usuarios medico, ConfigMedicoDTO dto) {
        ConfigMedico cm = medicoRepo.findByMedico(medico).orElseGet(() -> bootstrap(medico));
        ConfigMedicoMapper.merge(cm, dto);
        cm = medicoRepo.save(cm);

        // Agenda
        AgendaDTO adto = dto.agenda();
        if (adto != null) {
            ConfigMedico finalCm = cm;
            ConfigAgenda ca = agendaRepo.findByConfig(cm).orElseGet(() -> {
                ConfigAgenda x = new ConfigAgenda();
                x.setConfig(finalCm);
                return x;
            });
            ConfigMedicoMapper.merge(ca, adto);
            agendaRepo.save(ca);
        }

        // Notificaciones
        NotificacionesDTO ndto = dto.notificaciones();
        if (ndto != null) {
            ConfigMedico finalCm1 = cm;
            ConfigNotificaciones cn = notisRepo.findByConfig(cm).orElseGet(() -> {
                ConfigNotificaciones x = new ConfigNotificaciones();
                x.setConfig(finalCm1);
                return x;
            });
            ConfigMedicoMapper.merge(cn, ndto);
            notisRepo.save(cn);
        }

        ConfigAgenda ca = agendaRepo.findByConfig(cm).orElse(null);
        ConfigNotificaciones cn = notisRepo.findByConfig(cm).orElse(null);
        return ConfigMedicoMapper.toDTO(cm, ca, cn);
    }

    /** Semillas por defecto cuando no existe config para el médico */
    @Transactional
    protected ConfigMedico bootstrap(Usuarios medico) {
        ConfigMedico cm = new ConfigMedico();
        cm.setMedico(medico);
        cm.setTema("auto"); cm.setIdioma("es");
        cm.setZonaHoraria("Europe/Madrid"); cm.setHome("dashboard");
        cm = medicoRepo.save(cm);

        ConfigAgenda ca = new ConfigAgenda();
        ca.setConfig(cm);
        ca.setDuracionGeneralMin(20);
        ca.setBufferMin(5);
        ca.setReglasJson("{\"antelacionMinHoras\":24,\"cancelacionMinHoras\":6,\"overbooking\":false}");
        ca.setDisponibilidadJson("{\"lunes\":[{\"desde\":\"09:00\",\"hasta\":\"14:00\",\"ubicacion\":\"consulta1\"}],\"martes\":[]}");
        ca.setInstruccionesPorTipoJson("{\"general\":\"Llegue 10' antes\",\"teleconsulta\":\"Conéctese 5' antes\"}");
        agendaRepo.save(ca);

        ConfigNotificaciones cn = new ConfigNotificaciones();
        cn.setConfig(cm);
        cn.setCanalesJson("{\"email\":true,\"sms\":false,\"inapp\":true}");
        cn.setEventosJson("{\"nuevaCita\":true,\"cancelacion\":true,\"noAsiste\":true,\"mensaje\":true}");
        cn.setSilencioDesde("22:00"); cn.setSilencioHasta("07:00");
        cn.setPlantillasJson("{\"nuevaCita\":\"Hola {paciente}, su cita es el {fecha}.\"}");
        notisRepo.save(cn);

        return cm;
    }
}