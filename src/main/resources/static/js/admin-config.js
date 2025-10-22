/* =========================================================================
 *  ADMIN — Configuraciones Generales (ROLE_ADMIN)
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
    const $dirtyBadge  = qs('#dirtyBadge', root);

    // Refs inputs (mapear ID -> key)
    const fields = {
        nombreSistema: qs('#cfg_nombreSistema', root),
        logoUrl: qs('#cfg_logoUrl', root),
        colorPrimario: qs('#cfg_colorPrimario', root),
        colorSecundario: qs('#cfg_colorSecundario', root),

        centroNombre: qs('#cfg_centroNombre', root),
        centroCiudad: qs('#cfg_centroCiudad', root),
        timezone: qs('#cfg_timezone', root),

        privacidadUrl: qs('#cfg_privacidadUrl', root),
        terminosUrl: qs('#cfg_terminosUrl', root),
        cookiesUrl: qs('#cfg_cookiesUrl', root),

        mailFrom: qs('#cfg_mailFrom', root),
        notifCitas: qs('#cfg_notifCitas', root),
        notifMedicacion: qs('#cfg_notifMedicacion', root),
        notifSintomas: qs('#cfg_notifSintomas', root),

        pwdMinLen: qs('#cfg_pwdMinLen', root),
        pwdExpireDays: qs('#cfg_pwdExpireDays', root),
        twoFactorEnabled: qs('#cfg_2faEnabled', root),

        citaDuracionMin: qs('#cfg_citaDuracion', root),
        horarioInicio: qs('#cfg_horarioInicio', root),
        horarioFin: qs('#cfg_horarioFin', root),
        diasNoLaborablesCsv: qs('#cfg_diasNoLaborables', root),

        smtpHost: qs('#cfg_smtpHost', root),
        smtpPort: qs('#cfg_smtpPort', root),
        smtpUser: qs('#cfg_smtpUser', root),
        webhookBase: qs('#cfg_webhookBase', root),
    };

    // Tabs accesibles
    initTabs();

    // Eventos de cambio -> dirty
    Object.values(fields).forEach(inp => {
        if (!inp) return;
        inp.addEventListener('input', markDirty);
        if (inp.type === 'checkbox') inp.addEventListener('change', markDirty);
    });

    // Preview de marca
    const $previewName = qs('#brandPreviewName', root);
    const $previewImg  = qs('#brandPreviewImg', root);
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
        $previewName.style.color = s;
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

    async function loadConfig() {
        const data = await safeFetchJSON('/api/admin/config', { headers: { 'Accept': 'application/json' } });
        original = data;
        fillForm(data);
        setDirty(false);
    }

    function fillForm(cfg = {}) {
        assign(fields.nombreSistema, cfg.nombreSistema);
        assign(fields.logoUrl, cfg.logoUrl);
        assign(fields.colorPrimario, cfg.colorPrimario || '#1b5bff');
        assign(fields.colorSecundario, cfg.colorSecundario || '#00b894');

        assign(fields.centroNombre, cfg.centroNombre);
        assign(fields.centroCiudad, cfg.centroCiudad);
        assign(fields.timezone, cfg.timezone || 'Europe/Madrid');

        assign(fields.privacidadUrl, cfg.privacidadUrl || '/politicasPrivacidad');
        assign(fields.terminosUrl, cfg.terminosUrl || '/terminosCondiciones');
        assign(fields.cookiesUrl, cfg.cookiesUrl || '/politicaCookies');

        assign(fields.mailFrom, cfg.mailFrom);
        check(fields.notifCitas, cfg.notifCitas);
        check(fields.notifMedicacion, cfg.notifMedicacion);
        check(fields.notifSintomas, cfg.notifSintomas);

        assignNum(fields.pwdMinLen, cfg.pwdMinLen ?? 10);
        assignNum(fields.pwdExpireDays, cfg.pwdExpireDays ?? 0);
        check(fields.twoFactorEnabled, cfg.twoFactorEnabled);

        assignNum(fields.citaDuracionMin, cfg.citaDuracionMin ?? 30);
        assign(fields.horarioInicio, cfg.horarioInicio || '08:00');
        assign(fields.horarioFin, cfg.horarioFin || '17:00');
        assign(fields.diasNoLaborablesCsv, (cfg.diasNoLaborables || []).join(','));

        assign(fields.smtpHost, cfg.smtpHost);
        assignNum(fields.smtpPort, cfg.smtpPort ?? 587);
        assign(fields.smtpUser, cfg.smtpUser);
        assign(fields.webhookBase, cfg.webhookBase);

        // preview
        $previewName.textContent = fields.nombreSistema.value || 'VitaLink';
        $previewImg.src = fields.logoUrl.value || '';
        updateBrandPreview();
    }

    function readForm() {
        return {
            nombreSistema: val(fields.nombreSistema),
            logoUrl: val(fields.logoUrl),
            colorPrimario: val(fields.colorPrimario),
            colorSecundario: val(fields.colorSecundario),

            centroNombre: val(fields.centroNombre),
            centroCiudad: val(fields.centroCiudad),
            timezone: val(fields.timezone),

            privacidadUrl: val(fields.privacidadUrl),
            terminosUrl: val(fields.terminosUrl),
            cookiesUrl: val(fields.cookiesUrl),

            mailFrom: val(fields.mailFrom),
            notifCitas: bool(fields.notifCitas),
            notifMedicacion: bool(fields.notifMedicacion),
            notifSintomas: bool(fields.notifSintomas),

            pwdMinLen: int(fields.pwdMinLen),
            pwdExpireDays: int(fields.pwdExpireDays),
            twoFactorEnabled: bool(fields.twoFactorEnabled),

            citaDuracionMin: int(fields.citaDuracionMin),
            horarioInicio: val(fields.horarioInicio),
            horarioFin: val(fields.horarioFin),
            diasNoLaborables: parseCsvDates(val(fields.diasNoLaborablesCsv)),

            smtpHost: val(fields.smtpHost),
            smtpPort: int(fields.smtpPort),
            smtpUser: val(fields.smtpUser),
            webhookBase: val(fields.webhookBase),
        };
    }

    function validate(cfg) {
        const errs = [];
        // ejemplos de validación mínima
        if (cfg.mailFrom && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(cfg.mailFrom)) errs.push('Email remitente no válido.');
        if (cfg.pwdMinLen < 6) errs.push('Longitud mínima de contraseña debe ser >= 6.');
        if (cfg.horarioInicio && cfg.horarioFin && cfg.horarioInicio >= cfg.horarioFin) errs.push('Horario: inicio debe ser menor que fin.');
        if (cfg.timezone && !/^[A-Za-z]+\/[A-Za-z_]+/.test(cfg.timezone)) errs.push('Zona horaria (IANA) con formato inválido.');
        return errs;
    }

    async function onSave() {
        const cfg = readForm();
        const errors = validate(cfg);
        if (errors.length) {
            alert('Corrige los siguientes errores:\n\n- ' + errors.join('\n- '));
            return;
        }
        try {
            const res = await safeFetchJSON('/api/admin/config', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify(cfg)
            });
            original = res;
            setDirty(false);
            toast('Configuración guardada correctamente.');
        } catch (e) {
            console.error(e);
            alert('No se pudo guardar la configuración.\n' + e);
        }
    }

    function onRevert() {
        if (!original) return;
        fillForm(original);
        setDirty(false);
    }

    function markDirty() { setDirty(true); }
    function setDirty(v) {
        dirty = !!v;
        $btnGuardar.disabled = !dirty;
        $btnRevertir.disabled = !dirty;
        $dirtyBadge.style.display = dirty ? '' : 'none';
    }

    // Utils
    function val(i){ return i?.value?.trim?.() ?? ''; }
    function int(i){ return Number.parseInt(val(i) || '0', 10); }
    function bool(i){ return !!i?.checked; }
    function assign(i, v){ if (i) i.value = v ?? ''; }
    function assignNum(i, v){ if (i) i.value = (v ?? '').toString(); }
    function check(i, v){ if (i) i.checked = !!v; }
    function parseCsvDates(csv){ return (csv||'').split(',').map(s=>s.trim()).filter(Boolean); }

    function toast(msg){
        // reemplázalo por tu sistema de notificaciones si tienes
        console.log('TOAST:', msg);
    }

    function initTabs() {
        const tabs = Array.from(root.querySelectorAll('[role=tab]'));
        const panels = Array.from(root.querySelectorAll('[role=tabpanel]'));
        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                tabs.forEach(t => t.setAttribute('aria-selected', String(t===tab)));
                panels.forEach(p => p.hidden = (p.id !== tab.getAttribute('aria-controls')));
                // persistencia simple
                sessionStorage.setItem('admin-config-tab', tab.id);
            });
        });
        // restaurar última pestaña
        const last = sessionStorage.getItem('admin-config-tab');
        const toClick = last ? root.querySelector('#'+last) : tabs[0];
        toClick?.click();
    }
})();
