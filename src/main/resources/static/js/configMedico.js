/* =========================================================================
 *  CONFIGURACIÓN DEL MÉDICO — JS principal (COMPLETO)
 *  - Tabs accesibles + persistencia pestaña
 *  - Carga inicial (GET /api/medico/config)
 *  - Guardado (PUT /api/medico/config)
 *  - Estado "dirty" + aviso al salir
 *  - Previsualización firma (y hook opcional de upload)
 *  - TODAS las secciones: Perfil, UI, Agenda, Notificaciones,
 *    Chat, Plantillas, Integraciones, Seguridad, Privacidad, Centro
 *  - Sin dependencias externas
 * ========================================================================= */
(() => {
    const root = document.querySelector("#main-configMedico");
    if (!root) return;

    /* ------------------------- Utils DOM ------------------------- */
    const $ = (sel, r = root) => r.querySelector(sel);
    const $$ = (sel, r = root) => Array.from(r.querySelectorAll(sel));
    const byId = (id) => document.getElementById(id);

    /* ------------------------- Elementos ------------------------- */
    const tabs = $$(".tabs [role=tab]", root);
    const panels = $$(".tab-panels > section", root);
    const btnSave = $("#btnGuardar", root);
    const statusEl = $("#estadoGuardado", root);

    // Formularios
    const formPerfil = $("#form-perfil", root);
    const formUi = $("#form-ui", root);
    const formAgenda = $("#form-agenda", root);
    const formNotis = $("#form-notis", root);
    const formChat = $("#form-chat", root);
    const formPlantillas = $("#form-plantillas", root);
    const formIntegraciones = $("#form-integraciones", root);
    const formSeguridad = $("#form-seguridad", root);
    const formPrivacidad = $("#form-privacidad", root);
    const formCentro = $("#form-centro", root);

    // Firma (imagen)
    const firmaInput = $("#firmaImg", root);
    const firmaPreview = $("#firmaPreview", root);

    /* ------------------------- Estado ------------------------- */
    let state = null;   // Copia de lo recibido del back
    let dirty = false;  // ¿Hay cambios sin guardar?
    const TAB_KEY = "cfgMedico.activeTab";

    /* ------------------------- Helpers ------------------------- */
    function setDirty(v = true) {
        dirty = v;
        btnSave.disabled = !dirty;
        statusEl.textContent = v ? "Cambios sin guardar…" : "Todo guardado ✓";
    }

    // CSRF (si aplica)
    function getCsrfHeader() {
        const meta = document.querySelector('meta[name="_csrf"]');
        return meta ? { "X-CSRF-TOKEN": meta.getAttribute("content") } : {};
    }

    // JSON helpers
    function isValidJson(text) {
        if (!text || !text.trim()) return false;
        try {
            JSON.parse(text);
            return true;
        } catch {
            return false;
        }
    }

    function parseJsonSafe(text, fallback = {}) {
        if (!text || !text.trim()) return fallback;
        try {
            return JSON.parse(text);
        } catch {
            return fallback;
        }
    }

    function prettyJson(obj) {
        try {
            return JSON.stringify(obj, null, 2);
        } catch {
            return "{}";
        }
    }

    function prettyOrOriginal(text) {
        try {
            return JSON.stringify(JSON.parse(text), null, 2);
        } catch {
            return text;
        }
    }

    /* ═══════════════════════════════════════════════════════════════
     * BUILD PAYLOAD FROM FORMS
     * ═══════════════════════════════════════════════════════════════ */
    function buildPayloadFromForms() {

        // ─────────────────────────────────────────────────────────────
        // PERFIL
        // ─────────────────────────────────────────────────────────────
        const perfil = {
            nombreMostrar: byId("nombreMostrar")?.value || null,
            especialidad: byId("especialidad")?.value || null,
            colegiado: byId("colegiado")?.value || null,
            bio: byId("bio")?.value || null,
            firmaTexto: byId("firmaTexto")?.value || null,
            firmaImagenUrl: state?.perfil?.firmaImagenUrl || null
        };

        // ─────────────────────────────────────────────────────────────
        // UI
        // ─────────────────────────────────────────────────────────────
        const ui = {
            tema: byId("tema")?.value || "auto",
            idioma: byId("idioma")?.value || "es",
            zonaHoraria: byId("zona")?.value || "Europe/Madrid",
            home: byId("home")?.value || "dashboard"
        };

        // ─────────────────────────────────────────────────────────────
        // AGENDA
        // ─────────────────────────────────────────────────────────────
        let agenda = null;
        if (formAgenda) {
            const reglasText = byId("reglasJson")?.value?.trim() || "{}";
            const disponibilidadText = byId("disponibilidadJson")?.value?.trim() || "{}";
            const instruccionesText = byId("instruccionesPorTipoJson")?.value?.trim() || "{}";

            const invalids = [];
            if (!isValidJson(reglasText)) invalids.push("Reglas");
            if (!isValidJson(disponibilidadText)) invalids.push("Disponibilidad");
            if (!isValidJson(instruccionesText)) invalids.push("Instrucciones por tipo");

            if (invalids.length) {
                throw new Error("JSON inválido en: " + invalids.join(", "));
            }

            agenda = {
                duracionGeneralMin: Number(byId("duracion")?.value) || 20,
                bufferMin: Number(byId("buffer")?.value) || 5,
                reglas: parseJsonSafe(reglasText, { antelacionMinHoras: 24, cancelacionMinHoras: 6, overbooking: false }),
                disponibilidad: parseJsonSafe(disponibilidadText, {}),
                instruccionesPorTipo: parseJsonSafe(instruccionesText, {})
            };
        }

        // ─────────────────────────────────────────────────────────────
        // NOTIFICACIONES
        // ─────────────────────────────────────────────────────────────
        let notificaciones = null;
        if (formNotis) {
            const canales = {
                email: !!formNotis.querySelector('input[name="notificaciones.canales.email"]')?.checked,
                sms: !!formNotis.querySelector('input[name="notificaciones.canales.sms"]')?.checked,
                inapp: !!formNotis.querySelector('input[name="notificaciones.canales.inapp"]')?.checked
            };

            const eventos = {
                nuevaCita: !!formNotis.querySelector('input[name="notificaciones.eventos.nuevaCita"]')?.checked,
                cancelacion: !!formNotis.querySelector('input[name="notificaciones.eventos.cancelacion"]')?.checked,
                noAsiste: !!formNotis.querySelector('input[name="notificaciones.eventos.noAsiste"]')?.checked,
                mensaje: !!formNotis.querySelector('input[name="notificaciones.eventos.mensaje"]')?.checked
            };

            const plantillasText = byId("plantillasJson")?.value?.trim() || "{}";
            if (!isValidJson(plantillasText)) {
                throw new Error("JSON inválido en: Plantillas de notificaciones");
            }

            notificaciones = {
                canales,
                eventos,
                silencioDesde: byId("silencioDesde")?.value || "22:00",
                silencioHasta: byId("silencioHasta")?.value || "07:00",
                plantillas: parseJsonSafe(plantillasText, {})
            };
        }

        // ─────────────────────────────────────────────────────────────
        // CHAT / MENSAJERÍA
        // ─────────────────────────────────────────────────────────────
        let chat = null;
        if (formChat) {
            const respuestasText = byId("respuestasRapidasJson")?.value?.trim() || "{}";
            if (!isValidJson(respuestasText)) {
                throw new Error("JSON inválido en: Respuestas rápidas");
            }

            chat = {
                estado: byId("estadoChat")?.value || "DISPONIBLE",
                firmaChat: byId("firmaChat")?.value || null,
                respuestasRapidas: parseJsonSafe(respuestasText, {})
            };
        }

        // ─────────────────────────────────────────────────────────────
        // PLANTILLAS (lista)
        // ─────────────────────────────────────────────────────────────
        let plantillas = null;
        if (formPlantillas) {
            // Por ahora solo una plantilla desde el form
            // En el futuro se puede hacer una lista dinámica
            const tipo = byId("tipoPlantilla")?.value || "INFORME";
            const nombre = byId("nombrePlantilla")?.value?.trim();
            const contenido = byId("contenidoPlantilla")?.value?.trim();

            if (nombre && contenido) {
                plantillas = [{
                    id: state?.plantillas?.[0]?.id || null, // Mantener ID si existe
                    tipo,
                    nombre,
                    contenido
                }];
            } else {
                plantillas = state?.plantillas || [];
            }
        }

        // ─────────────────────────────────────────────────────────────
        // INTEGRACIONES
        // ─────────────────────────────────────────────────────────────
        let integraciones = null;
        if (formIntegraciones) {
            const apiKeysText = byId("apiKeysJson")?.value?.trim() || "{}";
            if (!isValidJson(apiKeysText)) {
                throw new Error("JSON inválido en: API Keys");
            }

            integraciones = {
                googleCalendar: !!formIntegraciones.querySelector('input[name="integraciones.googleCalendar"]')?.checked,
                outlookCalendar: !!formIntegraciones.querySelector('input[name="integraciones.outlookCalendar"]')?.checked,
                smsProvider: !!formIntegraciones.querySelector('input[name="integraciones.smsProvider"]')?.checked,
                apiKeys: parseJsonSafe(apiKeysText, {})
            };
        }

        // ─────────────────────────────────────────────────────────────
        // SEGURIDAD
        // ─────────────────────────────────────────────────────────────
        let seguridad = null;
        if (formSeguridad) {
            const nueva = byId("nuevaPassword")?.value || null;
            const confirmar = byId("confirmarPassword")?.value || null;
            const twoFA = !!formSeguridad.querySelector('input[name="seguridad.activar2FA"]')?.checked;

            // Validación local de contraseñas
            if (nueva && nueva !== confirmar) {
                throw new Error("Las contraseñas no coinciden");
            }
            if (nueva && nueva.length < 6) {
                throw new Error("La contraseña debe tener al menos 6 caracteres");
            }

            seguridad = {
                nuevaPassword: nueva,
                confirmarPassword: confirmar,
                activar2FA: twoFA
            };
        }

        // ─────────────────────────────────────────────────────────────
        // PRIVACIDAD
        // ─────────────────────────────────────────────────────────────
        let privacidad = null;
        if (formPrivacidad) {
            const visibilidadRadio = formPrivacidad.querySelector('input[name="privacidad.visibilidad"]:checked');

            privacidad = {
                visibilidad: visibilidadRadio?.value || "PUBLICO",
                usoDatos: !!formPrivacidad.querySelector('input[name="privacidad.usoDatos"]')?.checked,
                boletines: !!formPrivacidad.querySelector('input[name="privacidad.boletines"]')?.checked
            };
        }

        // ─────────────────────────────────────────────────────────────
        // CENTRO
        // ─────────────────────────────────────────────────────────────
        let centro = null;
        if (formCentro) {
            const serviciosText = byId("serviciosCentro")?.value?.trim() || "{}";
            if (!isValidJson(serviciosText)) {
                throw new Error("JSON inválido en: Servicios del centro");
            }

            centro = {
                nombreCentro: byId("nombreCentro")?.value || null,
                telefonoCentro: byId("telefonoCentro")?.value || null,
                direccionCentro: byId("direccionCentro")?.value || null,
                horarioCentro: byId("horarioCentro")?.value || null,
                servicios: parseJsonSafe(serviciosText, {})
            };
        }

        // ═════════════════════════════════════════════════════════════
        // PAYLOAD FINAL
        // ═════════════════════════════════════════════════════════════
        return {
            perfil,
            ui,
            agenda,
            notificaciones,
            chat,
            privacidad,
            centro,
            integraciones,
            seguridad,
            plantillas,
            version: state?.version || null
        };
    }

    /* ═══════════════════════════════════════════════════════════════
     * POPULATE FORMS FROM STATE
     * ═══════════════════════════════════════════════════════════════ */
    function populateFromState(data) {
        if (!data) return;

        // ─────────────────────────────────────────────────────────────
        // PERFIL
        // ─────────────────────────────────────────────────────────────
        if (data.perfil) {
            const p = data.perfil;
            if (byId("nombreMostrar")) byId("nombreMostrar").value = p.nombreMostrar || "";
            if (byId("especialidad")) byId("especialidad").value = p.especialidad || "";
            if (byId("colegiado")) byId("colegiado").value = p.colegiado || "";
            if (byId("bio")) byId("bio").value = p.bio || "";
            if (byId("firmaTexto")) byId("firmaTexto").value = p.firmaTexto || "";

            if (p.firmaImagenUrl && firmaPreview) {
                firmaPreview.innerHTML = "";
                const img = document.createElement("img");
                img.alt = "Firma actual";
                img.loading = "lazy";
                img.style.maxWidth = "220px";
                img.style.maxHeight = "90px";
                img.src = p.firmaImagenUrl;
                firmaPreview.appendChild(img);
            }
        }

        // ─────────────────────────────────────────────────────────────
        // UI
        // ─────────────────────────────────────────────────────────────
        if (data.ui) {
            const u = data.ui;
            if (byId("tema")) byId("tema").value = u.tema || "auto";
            if (byId("idioma")) byId("idioma").value = u.idioma || "es";
            if (byId("zona")) byId("zona").value = u.zonaHoraria || "Europe/Madrid";
            if (byId("home")) byId("home").value = u.home || "dashboard";
        }

        // ─────────────────────────────────────────────────────────────
        // AGENDA
        // ─────────────────────────────────────────────────────────────
        if (data.agenda) {
            const a = data.agenda;
            if (byId("duracion")) byId("duracion").value = a.duracionGeneralMin ?? 20;
            if (byId("buffer")) byId("buffer").value = a.bufferMin ?? 5;

            if (byId("reglasJson")) {
                byId("reglasJson").value = prettyJson(a.reglas || { antelacionMinHoras: 24, cancelacionMinHoras: 6, overbooking: false });
            }
            if (byId("disponibilidadJson")) {
                byId("disponibilidadJson").value = prettyJson(a.disponibilidad || {});
            }
            if (byId("instruccionesPorTipoJson")) {
                byId("instruccionesPorTipoJson").value = prettyJson(a.instruccionesPorTipo || {});
            }
        }

        // ─────────────────────────────────────────────────────────────
        // NOTIFICACIONES
        // ─────────────────────────────────────────────────────────────
        if (data.notificaciones && formNotis) {
            const n = data.notificaciones;

            // Canales
            if (n.canales) {
                const emailCb = formNotis.querySelector('input[name="notificaciones.canales.email"]');
                const smsCb = formNotis.querySelector('input[name="notificaciones.canales.sms"]');
                const inappCb = formNotis.querySelector('input[name="notificaciones.canales.inapp"]');

                if (emailCb) emailCb.checked = !!n.canales.email;
                if (smsCb) smsCb.checked = !!n.canales.sms;
                if (inappCb) inappCb.checked = !!n.canales.inapp;
            }

            // Eventos
            if (n.eventos) {
                const nuevaCitaCb = formNotis.querySelector('input[name="notificaciones.eventos.nuevaCita"]');
                const cancelacionCb = formNotis.querySelector('input[name="notificaciones.eventos.cancelacion"]');
                const noAsisteCb = formNotis.querySelector('input[name="notificaciones.eventos.noAsiste"]');
                const mensajeCb = formNotis.querySelector('input[name="notificaciones.eventos.mensaje"]');

                if (nuevaCitaCb) nuevaCitaCb.checked = !!n.eventos.nuevaCita;
                if (cancelacionCb) cancelacionCb.checked = !!n.eventos.cancelacion;
                if (noAsisteCb) noAsisteCb.checked = !!n.eventos.noAsiste;
                if (mensajeCb) mensajeCb.checked = !!n.eventos.mensaje;
            }

            // Silencio
            if (byId("silencioDesde")) byId("silencioDesde").value = n.silencioDesde || "22:00";
            if (byId("silencioHasta")) byId("silencioHasta").value = n.silencioHasta || "07:00";

            // Plantillas notis
            if (byId("plantillasJson")) {
                byId("plantillasJson").value = prettyJson(n.plantillas || {});
            }
        }

        // ─────────────────────────────────────────────────────────────
        // CHAT
        // ─────────────────────────────────────────────────────────────
        if (data.chat && formChat) {
            const c = data.chat;
            if (byId("estadoChat")) byId("estadoChat").value = c.estado || "DISPONIBLE";
            if (byId("firmaChat")) byId("firmaChat").value = c.firmaChat || "";
            if (byId("respuestasRapidasJson")) {
                byId("respuestasRapidasJson").value = prettyJson(c.respuestasRapidas || {});
            }
        }

        // ─────────────────────────────────────────────────────────────
        // PLANTILLAS (primera de la lista)
        // ─────────────────────────────────────────────────────────────
        if (data.plantillas && data.plantillas.length > 0 && formPlantillas) {
            const p = data.plantillas[0];
            if (byId("tipoPlantilla")) byId("tipoPlantilla").value = p.tipo || "INFORME";
            if (byId("nombrePlantilla")) byId("nombrePlantilla").value = p.nombre || "";
            if (byId("contenidoPlantilla")) byId("contenidoPlantilla").value = p.contenido || "";
        }

        // ─────────────────────────────────────────────────────────────
        // INTEGRACIONES
        // ─────────────────────────────────────────────────────────────
        if (data.integraciones && formIntegraciones) {
            const i = data.integraciones;

            const googleCb = formIntegraciones.querySelector('input[name="integraciones.googleCalendar"]');
            const outlookCb = formIntegraciones.querySelector('input[name="integraciones.outlookCalendar"]');
            const smsCb = formIntegraciones.querySelector('input[name="integraciones.smsProvider"]');

            if (googleCb) googleCb.checked = !!i.googleCalendar;
            if (outlookCb) outlookCb.checked = !!i.outlookCalendar;
            if (smsCb) smsCb.checked = !!i.smsProvider;

            if (byId("apiKeysJson")) {
                byId("apiKeysJson").value = prettyJson(i.apiKeys || {});
            }
        }

        // ─────────────────────────────────────────────────────────────
        // SEGURIDAD
        // ─────────────────────────────────────────────────────────────
        if (data.seguridad && formSeguridad) {
            // Las contraseñas nunca se pueblan por seguridad
            if (byId("nuevaPassword")) byId("nuevaPassword").value = "";
            if (byId("confirmarPassword")) byId("confirmarPassword").value = "";

            const twoFACb = formSeguridad.querySelector('input[name="seguridad.activar2FA"]');
            if (twoFACb) twoFACb.checked = !!data.seguridad.activar2FA;
        }

        // ─────────────────────────────────────────────────────────────
        // PRIVACIDAD
        // ─────────────────────────────────────────────────────────────
        if (data.privacidad && formPrivacidad) {
            const pr = data.privacidad;

            // Visibilidad (radio buttons)
            const visRadio = formPrivacidad.querySelector(`input[name="privacidad.visibilidad"][value="${pr.visibilidad || 'PUBLICO'}"]`);
            if (visRadio) visRadio.checked = true;

            // Checkboxes
            const usoDatosCb = formPrivacidad.querySelector('input[name="privacidad.usoDatos"]');
            const boletinesCb = formPrivacidad.querySelector('input[name="privacidad.boletines"]');

            if (usoDatosCb) usoDatosCb.checked = !!pr.usoDatos;
            if (boletinesCb) boletinesCb.checked = !!pr.boletines;
        }

        // ─────────────────────────────────────────────────────────────
        // CENTRO
        // ─────────────────────────────────────────────────────────────
        if (data.centro && formCentro) {
            const ce = data.centro;
            if (byId("nombreCentro")) byId("nombreCentro").value = ce.nombreCentro || "";
            if (byId("telefonoCentro")) byId("telefonoCentro").value = ce.telefonoCentro || "";
            if (byId("direccionCentro")) byId("direccionCentro").value = ce.direccionCentro || "";
            if (byId("horarioCentro")) byId("horarioCentro").value = ce.horarioCentro || "";
            if (byId("serviciosCentro")) {
                byId("serviciosCentro").value = prettyJson(ce.servicios || {});
            }
        }
    }

    /* ------------------------- Tabs accesibles ------------------------- */
    function switchTo(tabKey) {
        const btn = tabs.find((t) => t.dataset.tab === tabKey) || tabs[0];
        const panelId = `tab-${btn.dataset.tab}`;

        tabs.forEach((t) => {
            const sel = t === btn;
            t.setAttribute("aria-selected", String(sel));
            t.tabIndex = sel ? 0 : -1;
        });
        panels.forEach((p) => (p.hidden = p.id !== panelId));
        localStorage.setItem(TAB_KEY, btn.dataset.tab);
    }

    function getSavedTab() {
        return localStorage.getItem(TAB_KEY) || tabs[0]?.dataset.tab || "perfil";
    }

    tabs.forEach((t) => {
        t.addEventListener("click", () => switchTo(t.dataset.tab));
        t.addEventListener("keydown", (e) => {
            const i = tabs.indexOf(t);
            if (e.key === "ArrowRight") {
                e.preventDefault();
                tabs[(i + 1) % tabs.length].focus();
                tabs[(i + 1) % tabs.length].click();
            }
            if (e.key === "ArrowLeft") {
                e.preventDefault();
                tabs[(i - 1 + tabs.length) % tabs.length].focus();
                tabs[(i - 1 + tabs.length) % tabs.length].click();
            }
            if (e.key === "Home") {
                e.preventDefault();
                tabs[0].focus();
                tabs[0].click();
            }
            if (e.key === "End") {
                e.preventDefault();
                tabs[tabs.length - 1].focus();
                tabs[tabs.length - 1].click();
            }
        });
    });

    /* ------------------------- Eventos de cambio ------------------------- */
    root.addEventListener("input", (e) => {
        if (e.target.closest(".tab-panels")) setDirty(true);
    });

    window.addEventListener("beforeunload", (e) => {
        if (!dirty) return;
        e.preventDefault();
        e.returnValue = "";
    });

    /* ------------------------- Firma: preview + upload ------------------------- */
    if (firmaInput && firmaPreview) {
        firmaInput.addEventListener("change", async () => {
            firmaPreview.innerHTML = "";
            const file = firmaInput.files?.[0];
            if (!file) return;

            if (!/^image\//.test(file.type) || file.size > 1024 * 1024) {
                firmaPreview.textContent = "Archivo no válido (solo imagen ≤ 1MB).";
                firmaInput.value = "";
                return;
            }

            // Vista previa local
            const img = document.createElement("img");
            img.alt = "Vista previa de firma";
            img.loading = "lazy";
            img.style.maxWidth = "220px";
            img.style.maxHeight = "90px";
            img.src = URL.createObjectURL(file);
            firmaPreview.appendChild(img);

            // TODO: Implementar endpoint de subida si es necesario
        });
    }

    /* ------------------------- Auto-format JSON on blur ------------------------- */
    function setupJsonAutoFormat() {
        const jsonFields = [
            "#reglasJson",
            "#disponibilidadJson",
            "#instruccionesPorTipoJson",
            "#plantillasJson",
            "#respuestasRapidasJson",
            "#apiKeysJson",
            "#serviciosCentro"
        ];

        jsonFields.forEach(sel => {
            const el = $(sel);
            if (!el) return;
            el.addEventListener("blur", () => {
                const text = (el.value || "").trim();
                if (text) {
                    el.value = prettyOrOriginal(text);
                }
            });
        });
    }

    /* ------------------------- Guardar (PUT) ------------------------- */
    btnSave.addEventListener("click", async () => {
        btnSave.disabled = true;
        statusEl.textContent = "Guardando…";

        try {
            const payload = buildPayloadFromForms();

            console.log("Enviando payload:", JSON.stringify(payload, null, 2)); // Debug

            const res = await fetch("/api/medico/config", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    ...getCsrfHeader()
                },
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                const errorText = await res.text();
                console.error("Error response:", errorText);
                throw new Error(`HTTP ${res.status}: ${errorText}`);
            }

            state = await res.json();
            console.log("Respuesta del servidor:", state); // Debug

            // Repoblar formularios con datos normalizados del backend
            populateFromState(state);

            // Limpiar campos de contraseña después de guardar exitoso
            if (byId("nuevaPassword")) byId("nuevaPassword").value = "";
            if (byId("confirmarPassword")) byId("confirmarPassword").value = "";

            setDirty(false);
        } catch (err) {
            console.error("Error al guardar:", err);
            statusEl.textContent = err.message || "Error al guardar. Reintenta.";
            btnSave.disabled = false;
        }
    });

    /* ------------------------- Carga inicial (GET) ------------------------- */
    (async function init() {
        // Pestaña persistida
        switchTo(getSavedTab());

        // Setup auto-format para campos JSON
        setupJsonAutoFormat();

        try {
            const res = await fetch("/api/medico/config", {
                headers: {
                    "Accept": "application/json",
                    ...getCsrfHeader()
                }
            });

            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            state = await res.json();
            console.log("Config cargada:", state); // Debug

            // Poblar todos los formularios
            populateFromState(state);

            setDirty(false);
        } catch (err) {
            console.error("Error al cargar configuración:", err);
            statusEl.textContent = "No se pudo cargar la configuración.";
        }
    })();

})();