/* =========================================================================
 *  ADMIN — Configuraciones Generales (ROLE_ADMIN)
 *  Versión completa con todas las secciones
 * ========================================================================= */
(() => {
    const root = document.getElementById('admin-config');
    if (!root) return;

    // Helpers mínimos si $common no existe
    const { qs, $$, safeFetchJSON } = window.$common || {
        qs: (s, r = document) => r.querySelector(s),
        $$: (s, r = document) => Array.from(r.querySelectorAll(s)),
        safeFetchJSON: (u, o) => fetch(u, o).then(r => r.ok ? r.json() : r.text().then(t => Promise.reject(t)))
    };

    // Estado
    let dirty = false;
    let original = null;

    // Refs acciones
    const $btnGuardar = qs('#btnGuardar', root);
    const $btnRevertir = qs('#btnRevertir', root);
    const $dirtyBadge = qs('#dirtyBadge', root);

    // ═══════════════════════════════════════════════════════════════
    // REFS DE INPUTS (mapear ID -> key del DTO)
    // ═══════════════════════════════════════════════════════════════
    const fields = {
        // Identidad
        nombreSistema: qs('#cfg_nombreSistema', root),
        logoUrl: qs('#cfg_logoUrl', root),
        colorPrimario: qs('#cfg_colorPrimario', root),
        colorSecundario: qs('#cfg_colorSecundario', root),

        // Centro
        centroNombre: qs('#cfg_centroNombre', root),
        centroCiudad: qs('#cfg_centroCiudad', root),
        timezone: qs('#cfg_timezone', root),

        // Legales
        privacidadUrl: qs('#cfg_privacidadUrl', root),
        terminosUrl: qs('#cfg_terminosUrl', root),
        cookiesUrl: qs('#cfg_cookiesUrl', root),

        // Notificaciones
        mailFrom: qs('#cfg_mailFrom', root),
        notifCitas: qs('#cfg_notifCitas', root),
        notifMedicacion: qs('#cfg_notifMedicacion', root),
        notifSintomas: qs('#cfg_notifSintomas', root),

        // Seguridad
        pwdMinLen: qs('#cfg_pwdMinLen', root),
        pwdExpireDays: qs('#cfg_pwdExpireDays', root),
        twoFactorEnabled: qs('#cfg_2faEnabled', root),

        // Citas
        citaDuracionMin: qs('#cfg_citaDuracion', root),
        horarioInicio: qs('#cfg_horarioInicio', root),
        horarioFin: qs('#cfg_horarioFin', root),
        diasNoLaborablesCsv: qs('#cfg_diasNoLaborables', root),

        // Integraciones SMTP
        smtpHost: qs('#cfg_smtpHost', root),
        smtpPort: qs('#cfg_smtpPort', root),
        smtpUser: qs('#cfg_smtpUser', root),
        webhookBase: qs('#cfg_webhookBase', root),

        // ─────────────────────────────────────────────────────────────
        // NUEVOS CAMPOS
        // ─────────────────────────────────────────────────────────────

        // Usuarios
        maxUsuariosSistema: qs('#cfg_maxUsuariosSistema', root),
        maxMedicosSistema: qs('#cfg_maxMedicosSistema', root),
        registroAbierto: qs('#cfg_registroAbierto', root),
        requiereAprobacion: qs('#cfg_requiereAprobacion', root),
        rolesDisponiblesCsv: qs('#cfg_rolesDisponibles', root),

        // Auditoría
        nivelAuditoria: qs('#cfg_nivelAuditoria', root),
        retencionLogsDias: qs('#cfg_retencionLogsDias', root),
        auditarAccesos: qs('#cfg_auditarAccesos', root),
        auditarCambios: qs('#cfg_auditarCambios', root),
        auditarErrores: qs('#cfg_auditarErrores', root),

        // Backup
        backupAutomatico: qs('#cfg_backupAutomatico', root),
        backupFrecuencia: qs('#cfg_backupFrecuencia', root),
        backupHora: qs('#cfg_backupHora', root),
        backupRetencionDias: qs('#cfg_backupRetencionDias', root),
        backupDestino: qs('#cfg_backupDestino', root),

        // UI
        fuentePrincipal: qs('#cfg_fuentePrincipal', root),
        tamanoFuenteBase: qs('#cfg_tamanoFuenteBase', root),
        densidadUI: qs('#cfg_densidadUI', root),
        mostrarAyuda: qs('#cfg_mostrarAyuda', root),
        animacionesUI: qs('#cfg_animacionesUI', root),

        // Límites
        maxPacientesPorMedico: qs('#cfg_maxPacientesPorMedico', root),
        maxCitasDiarias: qs('#cfg_maxCitasDiarias', root),
        maxAlmacenamientoMB: qs('#cfg_maxAlmacenamientoMB', root),
        maxArchivoMB: qs('#cfg_maxArchivoMB', root),

        // Mantenimiento
        modoMantenimiento: qs('#cfg_modoMantenimiento', root),
        mensajeMantenimiento: qs('#cfg_mensajeMantenimiento', root),
        mantenimientoProgramado: qs('#cfg_mantenimientoProgramado', root),
        ipsBlanqueadasCsv: qs('#cfg_ipsBlanqueadas', root),

        // API
        apiHabilitada: qs('#cfg_apiHabilitada', root),
        apiRateLimitReq: qs('#cfg_apiRateLimitReq', root),
        apiTokenExpiraDias: qs('#cfg_apiTokenExpiraDias', root),
        webhooksHabilitados: qs('#cfg_webhooksHabilitados', root),
        webhookSecretKey: qs('#cfg_webhookSecretKey', root),
        webhookEventosCsv: qs('#cfg_webhookEventos', root),
    };

    // Tabs accesibles
    initTabs();

    // Eventos de cambio -> dirty
    Object.values(fields).forEach(inp => {
        if (!inp) return;
        inp.addEventListener('input', markDirty);
        if (inp.type === 'checkbox') inp.addEventListener('change', markDirty);
        if (inp.tagName === 'SELECT') inp.addEventListener('change', markDirty);
    });

    // Preview de marca
    const $previewName = qs('#brandPreviewName', root);
    const $previewImg = qs('#brandPreviewImg', root);
    ['input', 'change'].forEach(ev => {
        fields.nombreSistema?.addEventListener(ev, () => $previewName.textContent = fields.nombreSistema.value || 'VitaLink');
        fields.logoUrl?.addEventListener(ev, () => $previewImg.src = fields.logoUrl.value || '');
        fields.colorPrimario?.addEventListener(ev, updateBrandPreview);
        fields.colorSecundario?.addEventListener(ev, updateBrandPreview);
    });

    function updateBrandPreview() {
        const p = fields.colorPrimario?.value || '#1b5bff';
        const s = fields.colorSecundario?.value || '#00b894';
        const box = qs('#brandPreview', root);
        if (box) box.style = `--c1:${p};--c2:${s};border:1px solid var(--c1); padding:.4rem .6rem; border-radius:12px; display:inline-flex; align-items:center; gap:.5rem; background: linear-gradient(90deg, var(--c1)10%, transparent 10%);`;
        if ($previewName) $previewName.style.color = s;
    }

    // Botones
    $btnGuardar.addEventListener('click', onSave);
    $btnRevertir.addEventListener('click', onRevert);

    // Aviso al salir
    window.addEventListener('beforeunload', (e) => {
        if (!dirty) return;
        e.preventDefault();
        e.returnValue = '';
    });

    // Carga inicial
    loadConfig().catch(err => console.error('Error cargando config:', err));

    // ═══════════════════════════════════════════════════════════════
    // LOAD CONFIG
    // ═══════════════════════════════════════════════════════════════
    async function loadConfig() {
        const data = await safeFetchJSON('/api/admin/config', { headers: { 'Accept': 'application/json' } });
        original = data;
        fillForm(data);
        setDirty(false);
    }

    // ═══════════════════════════════════════════════════════════════
    // FILL FORM
    // ═══════════════════════════════════════════════════════════════
    function fillForm(cfg = {}) {
        // Identidad
        assign(fields.nombreSistema, cfg.nombreSistema);
        assign(fields.logoUrl, cfg.logoUrl);
        assign(fields.colorPrimario, cfg.colorPrimario || '#1b5bff');
        assign(fields.colorSecundario, cfg.colorSecundario || '#00b894');

        // Centro
        assign(fields.centroNombre, cfg.centroNombre);
        assign(fields.centroCiudad, cfg.centroCiudad);
        assign(fields.timezone, cfg.timezone || 'Europe/Madrid');

        // Legales
        assign(fields.privacidadUrl, cfg.privacidadUrl || '/politicasPrivacidad');
        assign(fields.terminosUrl, cfg.terminosUrl || '/terminosCondiciones');
        assign(fields.cookiesUrl, cfg.cookiesUrl || '/politicaCookies');

        // Notificaciones
        assign(fields.mailFrom, cfg.mailFrom);
        check(fields.notifCitas, cfg.notifCitas);
        check(fields.notifMedicacion, cfg.notifMedicacion);
        check(fields.notifSintomas, cfg.notifSintomas);

        // Seguridad
        assignNum(fields.pwdMinLen, cfg.pwdMinLen ?? 10);
        assignNum(fields.pwdExpireDays, cfg.pwdExpireDays ?? 0);
        check(fields.twoFactorEnabled, cfg.twoFactorEnabled);

        // Citas
        assignNum(fields.citaDuracionMin, cfg.citaDuracionMin ?? 30);
        assign(fields.horarioInicio, cfg.horarioInicio || '08:00');
        assign(fields.horarioFin, cfg.horarioFin || '17:00');
        assign(fields.diasNoLaborablesCsv, (cfg.diasNoLaborables || []).join(','));

        // Integraciones SMTP
        assign(fields.smtpHost, cfg.smtpHost);
        assignNum(fields.smtpPort, cfg.smtpPort ?? 587);
        assign(fields.smtpUser, cfg.smtpUser);
        assign(fields.webhookBase, cfg.webhookBase);

        // ─────────────────────────────────────────────────────────────
        // NUEVOS CAMPOS
        // ─────────────────────────────────────────────────────────────

        // Usuarios
        assignNum(fields.maxUsuariosSistema, cfg.maxUsuariosSistema ?? 0);
        assignNum(fields.maxMedicosSistema, cfg.maxMedicosSistema ?? 0);
        check(fields.registroAbierto, cfg.registroAbierto);
        check(fields.requiereAprobacion, cfg.requiereAprobacion);
        assign(fields.rolesDisponiblesCsv, cfg.rolesDisponiblesCsv || 'ADMIN,MEDICO,USUARIO');

        // Auditoría
        assign(fields.nivelAuditoria, cfg.nivelAuditoria || 'BASICO');
        assignNum(fields.retencionLogsDias, cfg.retencionLogsDias ?? 90);
        check(fields.auditarAccesos, cfg.auditarAccesos);
        check(fields.auditarCambios, cfg.auditarCambios);
        check(fields.auditarErrores, cfg.auditarErrores);

        // Backup
        check(fields.backupAutomatico, cfg.backupAutomatico);
        assign(fields.backupFrecuencia, cfg.backupFrecuencia || 'DIARIO');
        assign(fields.backupHora, cfg.backupHora || '03:00');
        assignNum(fields.backupRetencionDias, cfg.backupRetencionDias ?? 30);
        assign(fields.backupDestino, cfg.backupDestino || 'LOCAL');

        // UI
        assign(fields.fuentePrincipal, cfg.fuentePrincipal || 'Inter');
        assignNum(fields.tamanoFuenteBase, cfg.tamanoFuenteBase ?? 16);
        assign(fields.densidadUI, cfg.densidadUI || 'NORMAL');
        check(fields.mostrarAyuda, cfg.mostrarAyuda ?? true);
        check(fields.animacionesUI, cfg.animacionesUI ?? true);

        // Límites
        assignNum(fields.maxPacientesPorMedico, cfg.maxPacientesPorMedico ?? 0);
        assignNum(fields.maxCitasDiarias, cfg.maxCitasDiarias ?? 0);
        assignNum(fields.maxAlmacenamientoMB, cfg.maxAlmacenamientoMB ?? 1024);
        assignNum(fields.maxArchivoMB, cfg.maxArchivoMB ?? 10);

        // Mantenimiento
        check(fields.modoMantenimiento, cfg.modoMantenimiento);
        assign(fields.mensajeMantenimiento, cfg.mensajeMantenimiento);
        assign(fields.mantenimientoProgramado, cfg.mantenimientoProgramado);
        assign(fields.ipsBlanqueadasCsv, cfg.ipsBlanqueadasCsv);

        // API
        check(fields.apiHabilitada, cfg.apiHabilitada ?? true);
        assignNum(fields.apiRateLimitReq, cfg.apiRateLimitReq ?? 1000);
        assignNum(fields.apiTokenExpiraDias, cfg.apiTokenExpiraDias ?? 365);
        check(fields.webhooksHabilitados, cfg.webhooksHabilitados);
        assign(fields.webhookSecretKey, cfg.webhookSecretKey);
        assign(fields.webhookEventosCsv, cfg.webhookEventosCsv);

        // Preview
        if ($previewName) $previewName.textContent = fields.nombreSistema?.value || 'VitaLink';
        if ($previewImg) $previewImg.src = fields.logoUrl?.value || '';
        updateBrandPreview();
    }

    // ═══════════════════════════════════════════════════════════════
    // READ FORM
    // ═══════════════════════════════════════════════════════════════
    function readForm() {
        return {
            // Identidad
            nombreSistema: val(fields.nombreSistema),
            logoUrl: val(fields.logoUrl),
            colorPrimario: val(fields.colorPrimario),
            colorSecundario: val(fields.colorSecundario),

            // Centro
            centroNombre: val(fields.centroNombre),
            centroCiudad: val(fields.centroCiudad),
            timezone: val(fields.timezone),

            // Legales
            privacidadUrl: val(fields.privacidadUrl),
            terminosUrl: val(fields.terminosUrl),
            cookiesUrl: val(fields.cookiesUrl),

            // Notificaciones
            mailFrom: val(fields.mailFrom),
            notifCitas: bool(fields.notifCitas),
            notifMedicacion: bool(fields.notifMedicacion),
            notifSintomas: bool(fields.notifSintomas),

            // Seguridad
            pwdMinLen: int(fields.pwdMinLen),
            pwdExpireDays: int(fields.pwdExpireDays),
            twoFactorEnabled: bool(fields.twoFactorEnabled),

            // Citas
            citaDuracionMin: int(fields.citaDuracionMin),
            horarioInicio: val(fields.horarioInicio),
            horarioFin: val(fields.horarioFin),
            diasNoLaborables: parseCsv(val(fields.diasNoLaborablesCsv)),

            // Integraciones SMTP
            smtpHost: val(fields.smtpHost),
            smtpPort: int(fields.smtpPort),
            smtpUser: val(fields.smtpUser),
            webhookBase: val(fields.webhookBase),

            // ─────────────────────────────────────────────────────────────
            // NUEVOS CAMPOS
            // ─────────────────────────────────────────────────────────────

            // Usuarios
            maxUsuariosSistema: int(fields.maxUsuariosSistema),
            maxMedicosSistema: int(fields.maxMedicosSistema),
            registroAbierto: bool(fields.registroAbierto),
            requiereAprobacion: bool(fields.requiereAprobacion),
            rolesDisponiblesCsv: val(fields.rolesDisponiblesCsv),

            // Auditoría
            nivelAuditoria: val(fields.nivelAuditoria),
            retencionLogsDias: int(fields.retencionLogsDias),
            auditarAccesos: bool(fields.auditarAccesos),
            auditarCambios: bool(fields.auditarCambios),
            auditarErrores: bool(fields.auditarErrores),

            // Backup
            backupAutomatico: bool(fields.backupAutomatico),
            backupFrecuencia: val(fields.backupFrecuencia),
            backupHora: val(fields.backupHora),
            backupRetencionDias: int(fields.backupRetencionDias),
            backupDestino: val(fields.backupDestino),

            // UI
            fuentePrincipal: val(fields.fuentePrincipal),
            tamanoFuenteBase: int(fields.tamanoFuenteBase),
            densidadUI: val(fields.densidadUI),
            mostrarAyuda: bool(fields.mostrarAyuda),
            animacionesUI: bool(fields.animacionesUI),

            // Límites
            maxPacientesPorMedico: int(fields.maxPacientesPorMedico),
            maxCitasDiarias: int(fields.maxCitasDiarias),
            maxAlmacenamientoMB: int(fields.maxAlmacenamientoMB),
            maxArchivoMB: int(fields.maxArchivoMB),

            // Mantenimiento
            modoMantenimiento: bool(fields.modoMantenimiento),
            mensajeMantenimiento: val(fields.mensajeMantenimiento),
            mantenimientoProgramado: val(fields.mantenimientoProgramado),
            ipsBlanqueadasCsv: val(fields.ipsBlanqueadasCsv),

            // API
            apiHabilitada: bool(fields.apiHabilitada),
            apiRateLimitReq: int(fields.apiRateLimitReq),
            apiTokenExpiraDias: int(fields.apiTokenExpiraDias),
            webhooksHabilitados: bool(fields.webhooksHabilitados),
            webhookSecretKey: val(fields.webhookSecretKey),
            webhookEventosCsv: val(fields.webhookEventosCsv),
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // VALIDATE
    // ═══════════════════════════════════════════════════════════════
    function validate(cfg) {
        const errs = [];

        // Email
        if (cfg.mailFrom && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cfg.mailFrom))
            errs.push('Email remitente no válido.');

        // Seguridad
        if (cfg.pwdMinLen < 6)
            errs.push('Longitud mínima de contraseña debe ser >= 6.');

        // Horarios
        if (cfg.horarioInicio && cfg.horarioFin && cfg.horarioInicio >= cfg.horarioFin)
            errs.push('Horario: inicio debe ser menor que fin.');

        // Timezone
        if (cfg.timezone && !/^[A-Za-z]+\/[A-Za-z_]+/.test(cfg.timezone))
            errs.push('Zona horaria (IANA) con formato inválido.');

        // SMTP
        if (cfg.smtpPort && (cfg.smtpPort < 1 || cfg.smtpPort > 65535))
            errs.push('Puerto SMTP inválido (1-65535).');

        // Límites
        if (cfg.maxArchivoMB < 1)
            errs.push('Tamaño máximo de archivo debe ser al menos 1 MB.');

        // API
        if (cfg.apiRateLimitReq < 1)
            errs.push('Rate limit debe ser al menos 1 petición/hora.');

        return errs;
    }

    // ═══════════════════════════════════════════════════════════════
    // SAVE
    // ═══════════════════════════════════════════════════════════════
    async function onSave() {
        const cfg = readForm();
        const errors = validate(cfg);

        if (errors.length) {
            alert('Corrige los siguientes errores:\n\n- ' + errors.join('\n- '));
            return;
        }

        try {
            $btnGuardar.disabled = true;
            $btnGuardar.textContent = 'Guardando...';

            const res = await safeFetchJSON('/api/admin/config', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify(cfg)
            });

            original = res;
            fillForm(res);
            setDirty(false);
            toast('Configuración guardada correctamente.');
        } catch (e) {
            console.error(e);
            alert('No se pudo guardar la configuración.\n' + e);
        } finally {
            $btnGuardar.textContent = 'Guardar cambios';
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // REVERT
    // ═══════════════════════════════════════════════════════════════
    function onRevert() {
        if (!original) return;
        if (!confirm('¿Descartar todos los cambios?')) return;
        fillForm(original);
        setDirty(false);
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════
    function markDirty() { setDirty(true); }

    function setDirty(v) {
        dirty = !!v;
        $btnGuardar.disabled = !dirty;
        $btnRevertir.disabled = !dirty;
        $dirtyBadge.style.display = dirty ? '' : 'none';
    }

    function val(i) { return i?.value?.trim?.() ?? ''; }
    function int(i) { return Number.parseInt(val(i) || '0', 10); }
    function bool(i) { return !!i?.checked; }
    function assign(i, v) { if (i) i.value = v ?? ''; }
    function assignNum(i, v) { if (i) i.value = (v ?? '').toString(); }
    function check(i, v) { if (i) i.checked = !!v; }
    function parseCsv(csv) { return (csv || '').split(',').map(s => s.trim()).filter(Boolean); }

    function toast(msg) {
        // Notificación simple (reemplazar por tu sistema si tienes)
        const toast = document.createElement('div');
        toast.className = 'toast toast--success';
        toast.textContent = msg;
        toast.style.cssText = 'position:fixed;bottom:2rem;right:2rem;background:#00b894;color:#fff;padding:.75rem 1.5rem;border-radius:8px;z-index:9999;animation:fadeIn .3s ease';
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
    }

    function initTabs() {
        const tabs = Array.from(root.querySelectorAll('[role=tab]'));
        const panels = Array.from(root.querySelectorAll('[role=tabpanel]'));

        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                tabs.forEach(t => t.setAttribute('aria-selected', String(t === tab)));
                panels.forEach(p => p.hidden = (p.id !== tab.getAttribute('aria-controls')));
                sessionStorage.setItem('admin-config-tab', tab.id);
            });

            // Navegación por teclado
            tab.addEventListener('keydown', (e) => {
                const i = tabs.indexOf(tab);
                if (e.key === 'ArrowRight') {
                    e.preventDefault();
                    tabs[(i + 1) % tabs.length].focus();
                    tabs[(i + 1) % tabs.length].click();
                }
                if (e.key === 'ArrowLeft') {
                    e.preventDefault();
                    tabs[(i - 1 + tabs.length) % tabs.length].focus();
                    tabs[(i - 1 + tabs.length) % tabs.length].click();
                }
            });
        });

        // Restaurar última pestaña
        const last = sessionStorage.getItem('admin-config-tab');
        const toClick = last ? root.querySelector('#' + last) : tabs[0];
        toClick?.click();
    }
})();