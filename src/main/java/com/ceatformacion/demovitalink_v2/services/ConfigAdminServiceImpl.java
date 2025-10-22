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
        if (c.mailFrom()!=null && !c.mailFrom().isBlank() && !EMAIL.matcher(c.mailFrom()).matches())
            throw new IllegalArgumentException("Email remitente no válido.");
        if (c.pwdMinLen()!=null && c.pwdMinLen()<6)
            throw new IllegalArgumentException("Longitud mínima de contraseña debe ser >= 6.");
        if (c.horarioInicio()!=null && c.horarioFin()!=null
                && !c.horarioInicio().isBlank() && !c.horarioFin().isBlank()
                && c.horarioInicio().compareTo(c.horarioFin())>=0)
            throw new IllegalArgumentException("El horario de inicio debe ser anterior al de fin.");
        if (c.timezone()!=null && !c.timezone().isBlank()) {
            try { ZoneId.of(c.timezone()); } catch (Exception ex) {
                throw new IllegalArgumentException("Zona horaria IANA inválida.");
            }
        }
        if (c.smtpPort()!=null && (c.smtpPort()<1 || c.smtpPort()>65535))
            throw new IllegalArgumentException("Puerto SMTP inválido.");
    }

    private ConfigAdminDTO normalize(ConfigAdminDTO c){
        return new ConfigAdminDTO(
                trimOrNull(c.nombreSistema()),
                trimOrNull(c.logoUrl()),
                def(c.colorPrimario(), "#1b5bff"),
                def(c.colorSecundario(), "#00b894"),

                trimOrNull(c.centroNombre()),
                trimOrNull(c.centroCiudad()),
                def(c.timezone(), "Europe/Madrid"),

                def(c.privacidadUrl(), "/politicasPrivacidad"),
                def(c.terminosUrl(), "/terminosCondiciones"),
                def(c.cookiesUrl(), "/politicaCookies"),

                trimOrNull(c.mailFrom()),
                bool(c.notifCitas()),
                bool(c.notifMedicacion()),
                bool(c.notifSintomas()),

                numOr(c.pwdMinLen(), 10),
                numOr(c.pwdExpireDays(), 0),
                bool(c.twoFactorEnabled()),

                numOr(c.citaDuracionMin(), 30),
                def(c.horarioInicio(), "08:00"),
                def(c.horarioFin(), "17:00"),
                c.diasNoLaborables()==null? List.of() : c.diasNoLaborables().stream().map(String::trim).filter(s->!s.isBlank()).toList(),

                trimOrNull(c.smtpHost()),
                numOr(c.smtpPort(), 587),
                trimOrNull(c.smtpUser()),
                trimOrNull(c.webhookBase())
        );
    }

    /* ================= Mapping DTO ↔ Entity ================= */

    private void apply(ConfigGlobal e, ConfigAdminDTO c){
        e.setNombreSistema(c.nombreSistema());
        e.setLogoUrl(c.logoUrl());
        e.setColorPrimario(c.colorPrimario());
        e.setColorSecundario(c.colorSecundario());

        e.setCentroNombre(c.centroNombre());
        e.setCentroCiudad(c.centroCiudad());
        e.setTimezone(c.timezone());

        e.setPrivacidadUrl(c.privacidadUrl());
        e.setTerminosUrl(c.terminosUrl());
        e.setCookiesUrl(c.cookiesUrl());

        e.setMailFrom(c.mailFrom());
        e.setNotifCitas(c.notifCitas());
        e.setNotifMedicacion(c.notifMedicacion());
        e.setNotifSintomas(c.notifSintomas());

        e.setPwdMinLen(c.pwdMinLen());
        e.setPwdExpireDays(c.pwdExpireDays());
        e.setTwoFactorEnabled(c.twoFactorEnabled());

        e.setCitaDuracionMin(c.citaDuracionMin());
        e.setHorarioInicio(c.horarioInicio());
        e.setHorarioFin(c.horarioFin());
        e.setDiasNoLaborablesList(c.diasNoLaborables());

        e.setSmtpHost(c.smtpHost());
        e.setSmtpPort(c.smtpPort());
        e.setSmtpUser(c.smtpUser());
        e.setWebhookBase(c.webhookBase());
    }

    private ConfigAdminDTO toDto(ConfigGlobal e){
        return new ConfigAdminDTO(
                e.getNombreSistema(), e.getLogoUrl(), e.getColorPrimario(), e.getColorSecundario(),
                e.getCentroNombre(), e.getCentroCiudad(), e.getTimezone(),
                e.getPrivacidadUrl(), e.getTerminosUrl(), e.getCookiesUrl(),
                e.getMailFrom(), e.getNotifCitas(), e.getNotifMedicacion(), e.getNotifSintomas(),
                e.getPwdMinLen(), e.getPwdExpireDays(), e.getTwoFactorEnabled(),
                e.getCitaDuracionMin(), e.getHorarioInicio(), e.getHorarioFin(), e.getDiasNoLaborablesList(),
                e.getSmtpHost(), e.getSmtpPort(), e.getSmtpUser(), e.getWebhookBase()
        );
    }

    /* ================= Defaults ================= */

    private ConfigGlobal defaultsEntity(){
        ConfigGlobal e = new ConfigGlobal();
        e.setIdConfig(SINGLETON_ID);
        e.setNombreSistema("VitaLink");
        e.setColorPrimario("#1b5bff");
        e.setColorSecundario("#00b894");
        e.setCentroNombre("Hospital General");
        e.setCentroCiudad("Madrid");
        e.setTimezone("Europe/Madrid");
        e.setPrivacidadUrl("/politicasPrivacidad");
        e.setTerminosUrl("/terminosCondiciones");
        e.setCookiesUrl("/politicaCookies");
        e.setMailFrom("no-reply@tudominio.com");
        e.setNotifCitas(true);
        e.setNotifMedicacion(false);
        e.setNotifSintomas(true);
        e.setPwdMinLen(10);
        e.setPwdExpireDays(0);
        e.setTwoFactorEnabled(false);
        e.setCitaDuracionMin(30);
        e.setHorarioInicio("08:00");
        e.setHorarioFin("17:00");
        e.setDiasNoLaborablesList(List.of());
        e.setSmtpPort(587);
        return e;
    }

    private ConfigGlobal fillDefaults(ConfigGlobal e){
        if (e.getColorPrimario()==null) e.setColorPrimario("#1b5bff");
        if (e.getColorSecundario()==null) e.setColorSecundario("#00b894");
        if (e.getTimezone()==null) e.setTimezone("Europe/Madrid");
        if (e.getPrivacidadUrl()==null) e.setPrivacidadUrl("/politicasPrivacidad");
        if (e.getTerminosUrl()==null) e.setTerminosUrl("/terminosCondiciones");
        if (e.getCookiesUrl()==null) e.setCookiesUrl("/politicaCookies");
        if (e.getPwdMinLen()==null) e.setPwdMinLen(10);
        if (e.getPwdExpireDays()==null) e.setPwdExpireDays(0);
        if (e.getCitaDuracionMin()==null) e.setCitaDuracionMin(30);
        if (e.getHorarioInicio()==null) e.setHorarioInicio("08:00");
        if (e.getHorarioFin()==null) e.setHorarioFin("17:00");
        if (e.getSmtpPort()==null) e.setSmtpPort(587);
        return e;
    }

    private static Boolean bool(Boolean b){ return Boolean.TRUE.equals(b); }
    private static Integer numOr(Integer n, Integer d){ return n==null? d : n; }
    private static String def(String v, String d){ return (v==null || v.isBlank())? d : v.trim(); }
    private static String trimOrNull(String v){ return v==null? null : v.trim(); }
}