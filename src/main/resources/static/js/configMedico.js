/* =========================================================================
 *  CONFIGURACIÓN DEL MÉDICO — JS principal (CORREGIDO)
 *  - Tabs accesibles + persistencia pestaña
 *  - Carga inicial (GET /api/medico/config)
 *  - Guardado (PUT /api/medico/config)
 *  - Estado "dirty" + aviso al salir
 *  - Previsualización firma (y hook opcional de upload)
 *  - Agenda + Notificaciones (estructura correcta para DTOs)
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

    /* ------------------------- Build Payload (CORREGIDO) ------------------------- */
    /**
     * Construye el payload con la estructura EXACTA que esperan los DTOs del backend:
     * - AgendaDTO: reglas (objeto), disponibilidad (map), instruccionesPorTipo (map)
     * - NotificacionesDTO: canales (objeto), eventos (objeto), plantillas (map)
     */
    function buildPayloadFromForms() {
        // ═══════════════════════════════════════════════════════════════
        // PERFIL
        // ═══════════════════════════════════════════════════════════════
        const perfil = {
            nombreMostrar: byId("nombreMostrar")?.value || null,
            especialidad: byId("especialidad")?.value || null,
            colegiado: byId("colegiado")?.value || null,
            bio: byId("bio")?.value || null,
            firmaTexto: byId("firmaTexto")?.value || null,
            firmaImagenUrl: state?.perfil?.firmaImagenUrl || null
        };

        // ═══════════════════════════════════════════════════════════════
        // UI
        // ═══════════════════════════════════════════════════════════════
        const ui = {
            tema: byId("tema")?.value || "auto",
            idioma: byId("idioma")?.value || "es",
            zonaHoraria: byId("zona")?.value || "Europe/Madrid",
            home: byId("home")?.value || "dashboard"
        };

        // ═══════════════════════════════════════════════════════════════
        // AGENDA — Backend espera OBJETOS, no strings JSON
        // ═══════════════════════════════════════════════════════════════
        let agenda = null;
        if (formAgenda) {
            const reglasText = byId("reglasJson")?.value?.trim() || "{}";
            const disponibilidadText = byId("disponibilidadJson")?.value?.trim() || "{}";
            const instruccionesText = byId("instruccionesPorTipoJson")?.value?.trim() || "{}";

            // Validar JSONs
            const invalids = [];
            if (!isValidJson(reglasText)) invalids.push("Reglas");
            if (!isValidJson(disponibilidadText)) invalids.push("Disponibilidad");
            if (!isValidJson(instruccionesText)) invalids.push("Instrucciones por tipo");

            if (invalids.length) {
                throw new Error("JSON inválido en: " + invalids.join(", "));
            }

            // Parsear a objetos (lo que espera el DTO)
            const reglas = parseJsonSafe(reglasText, {
                antelacionMinHoras: 24,
                cancelacionMinHoras: 6,
                overbooking: false
            });

            const disponibilidad = parseJsonSafe(disponibilidadText, {});
            const instruccionesPorTipo = parseJsonSafe(instruccionesText, {});

            agenda = {
                duracionGeneralMin: Number(byId("duracion")?.value) || 20,
                bufferMin: Number(byId("buffer")?.value) || 5,
                reglas,              // ← Objeto ReglasDTO
                disponibilidad,      // ← Map<String, List<BloqueDTO>>
                instruccionesPorTipo // ← Map<String, String>
            };
        }

        // ═══════════════════════════════════════════════════════════════
        // NOTIFICACIONES — Backend espera OBJETOS, no strings JSON
        // ═══════════════════════════════════════════════════════════════
        let notificaciones = null;
        if (formNotis) {
            // Canales como objeto CanalesDTO
            const canales = {
                email: !!formNotis.querySelector('input[name="notificaciones.canales.email"]')?.checked,
                sms: !!formNotis.querySelector('input[name="notificaciones.canales.sms"]')?.checked,
                inapp: !!formNotis.querySelector('input[name="notificaciones.canales.inapp"]')?.checked
            };

            // Eventos como objeto EventosDTO
            const eventos = {
                nuevaCita: !!formNotis.querySelector('input[name="notificaciones.eventos.nuevaCita"]')?.checked,
                cancelacion: !!formNotis.querySelector('input[name="notificaciones.eventos.cancelacion"]')?.checked,
                noAsiste: !!formNotis.querySelector('input[name="notificaciones.eventos.noAsiste"]')?.checked,
                mensaje: !!formNotis.querySelector('input[name="notificaciones.eventos.mensaje"]')?.checked
            };

            // Plantillas como Map<String, String>
            const plantillasText = byId("plantillasJson")?.value?.trim() || "{}";
            if (!isValidJson(plantillasText)) {
                throw new Error("JSON inválido en: Plantillas de notificaciones");
            }
            const plantillas = parseJsonSafe(plantillasText, {});

            notificaciones = {
                canales,        // ← Objeto CanalesDTO
                eventos,        // ← Objeto EventosDTO
                silencioDesde: byId("silencioDesde")?.value || "22:00",
                silencioHasta: byId("silencioHasta")?.value || "07:00",
                plantillas      // ← Map<String, String>
            };
        }

        // ═══════════════════════════════════════════════════════════════
        // PAYLOAD FINAL — Estructura ConfigMedicoDTO
        // ═══════════════════════════════════════════════════════════════
        return {
            perfil,
            ui,
            agenda,
            notificaciones,
            version: state?.version || null
        };
    }

    /* ------------------------- Populate Forms (CORREGIDO) ------------------------- */
    /**
     * Pobla los formularios desde el state que devuelve el backend.
     * El backend devuelve objetos parseados, no strings JSON.
     */
    function populateFromState(data) {
        if (!data) return;

        // ═══════════════════════════════════════════════════════════════
        // PERFIL
        // ═══════════════════════════════════════════════════════════════
        if (data.perfil) {
            const p = data.perfil;
            if (byId("nombreMostrar")) byId("nombreMostrar").value = p.nombreMostrar || "";
            if (byId("especialidad")) byId("especialidad").value = p.especialidad || "";
            if (byId("colegiado")) byId("colegiado").value = p.colegiado || "";
            if (byId("bio")) byId("bio").value = p.bio || "";
            if (byId("firmaTexto")) byId("firmaTexto").value = p.firmaTexto || "";

            // Firma imagen preview
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

        // ═══════════════════════════════════════════════════════════════
        // UI
        // ═══════════════════════════════════════════════════════════════
        if (data.ui) {
            const u = data.ui;
            if (byId("tema")) byId("tema").value = u.tema || "auto";
            if (byId("idioma")) byId("idioma").value = u.idioma || "es";
            if (byId("zona")) byId("zona").value = u.zonaHoraria || "Europe/Madrid";
            if (byId("home")) byId("home").value = u.home || "dashboard";
        }

        // ═══════════════════════════════════════════════════════════════
        // AGENDA — Convertir objetos a JSON strings para los textareas
        // ═══════════════════════════════════════════════════════════════
        if (data.agenda) {
            const a = data.agenda;
            if (byId("duracion")) byId("duracion").value = a.duracionGeneralMin ?? 20;
            if (byId("buffer")) byId("buffer").value = a.bufferMin ?? 5;

            // Los textareas necesitan JSON string formateado
            if (byId("reglasJson")) {
                byId("reglasJson").value = prettyJson(a.reglas || {
                    antelacionMinHoras: 24,
                    cancelacionMinHoras: 6,
                    overbooking: false
                });
            }
            if (byId("disponibilidadJson")) {
                byId("disponibilidadJson").value = prettyJson(a.disponibilidad || {});
            }
            if (byId("instruccionesPorTipoJson")) {
                byId("instruccionesPorTipoJson").value = prettyJson(a.instruccionesPorTipo || {});
            }
        }

        // ═══════════════════════════════════════════════════════════════
        // NOTIFICACIONES — Poblar checkboxes y textarea
        // ═══════════════════════════════════════════════════════════════
        if (data.notificaciones) {
            const n = data.notificaciones;

            // Canales (checkboxes)
            if (n.canales) {
                const emailCb = formNotis?.querySelector('input[name="notificaciones.canales.email"]');
                const smsCb = formNotis?.querySelector('input[name="notificaciones.canales.sms"]');
                const inappCb = formNotis?.querySelector('input[name="notificaciones.canales.inapp"]');

                if (emailCb) emailCb.checked = !!n.canales.email;
                if (smsCb) smsCb.checked = !!n.canales.sms;
                if (inappCb) inappCb.checked = !!n.canales.inapp;
            }

            // Eventos (checkboxes)
            if (n.eventos) {
                const nuevaCitaCb = formNotis?.querySelector('input[name="notificaciones.eventos.nuevaCita"]');
                const cancelacionCb = formNotis?.querySelector('input[name="notificaciones.eventos.cancelacion"]');
                const noAsisteCb = formNotis?.querySelector('input[name="notificaciones.eventos.noAsiste"]');
                const mensajeCb = formNotis?.querySelector('input[name="notificaciones.eventos.mensaje"]');

                if (nuevaCitaCb) nuevaCitaCb.checked = !!n.eventos.nuevaCita;
                if (cancelacionCb) cancelacionCb.checked = !!n.eventos.cancelacion;
                if (noAsisteCb) noAsisteCb.checked = !!n.eventos.noAsiste;
                if (mensajeCb) mensajeCb.checked = !!n.eventos.mensaje;
            }

            // Silencio
            if (byId("silencioDesde")) byId("silencioDesde").value = n.silencioDesde || "22:00";
            if (byId("silencioHasta")) byId("silencioHasta").value = n.silencioHasta || "07:00";

            // Plantillas (textarea con JSON)
            if (byId("plantillasJson")) {
                byId("plantillasJson").value = prettyJson(n.plantillas || {});
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
            // try {
            //   const fd = new FormData();
            //   fd.append("file", file);
            //   const res = await fetch("/api/medico/config/firma/imagen", {
            //     method: "POST",
            //     body: fd,
            //     headers: { ...getCsrfHeader() }
            //   });
            //   if (!res.ok) throw new Error(`HTTP ${res.status}`);
            //   const { url } = await res.json();
            //   state.perfil.firmaImagenUrl = url;
            //   setDirty(true);
            // } catch (err) {
            //   console.error("Upload firma falló:", err);
            //   firmaPreview.textContent = "No se pudo subir la imagen.";
            // }
        });
    }

    /* ------------------------- Auto-format JSON on blur ------------------------- */
    function setupJsonAutoFormat() {
        const jsonFields = [
            "#reglasJson",
            "#disponibilidadJson",
            "#instruccionesPorTipoJson",
            "#plantillasJson"
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