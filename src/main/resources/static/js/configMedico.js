/* =========================================================================
 *  CONFIGURACIÓN DEL MÉDICO — JS principal
 *  - Tabs accesibles + persistencia pestaña
 *  - Carga inicial (GET /api/medico/config)
 *  - Guardado (PUT /api/medico/config)
 *  - Estado "dirty" + aviso al salir
 *  - Previsualización firma (y hook opcional de upload)
 *  - Agenda + Notificaciones (JSON validado y formateado)
 *  - Sin dependencias externas
 * ========================================================================= */
(() => {
    const root = document.querySelector("#main-configMedico");
    if (!root) return;

    /* ------------------------- Utils DOM ------------------------- */
    const $  = (sel, r = root) => r.querySelector(sel);
    const $$ = (sel, r = root) => Array.from(r.querySelectorAll(sel));
    const byId = (id) => document.getElementById(id);

    /* ------------------------- Elementos ------------------------- */
    const tabs     = $$(".tabs [role=tab]", root);
    const panels   = $$(".tab-panels > section", root);
    const btnSave  = $("#btnGuardar", root);
    const statusEl = $("#estadoGuardado", root);

    // Formularios
    const formPerfil = $("#form-perfil", root);
    const formUi     = $("#form-ui", root);
    const formAgenda = $("#form-agenda", root);
    const formNotis  = $("#form-notis", root);

    // Firma (imagen)
    const firmaInput   = $("#firmaImg", root);
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

    // Serializa un formulario via name con rutas a.b.c
    function serializeForm(form) {
        if (!form) return {};
        const out = {};
        const setDeep = (obj, path, value) => {
            const parts = path.split(".");
            let cur = obj;
            parts.forEach((p, idx) => {
                if (idx === parts.length - 1) cur[p] = value;
                else {
                    if (!cur[p] || typeof cur[p] !== "object") cur[p] = {};
                    cur = cur[p];
                }
            });
        };
        const fields = form.querySelectorAll("input, select, textarea");
        fields.forEach((el) => {
            if (!el.name) return;
            if (el.type === "file") return;
            const val = el.type === "checkbox" ? el.checked : el.value;
            setDeep(out, el.name, val);
        });
        return out;
    }

    function populateForm(form, data) {
        if (!form || !data) return;
        const getDeep = (obj, path) =>
            path.split(".").reduce((o, k) => (o ? o[k] : undefined), obj);

        const fields = form.querySelectorAll("input, select, textarea");
        fields.forEach((el) => {
            if (!el.name) return;
            const val = getDeep(data, el.name);
            if (typeof val === "undefined") return;
            if (el.type === "checkbox") el.checked = !!val;
            else el.value = val ?? "";
        });
    }

    // JSON helpers
    function isValidJson(text) {
        try { JSON.parse(text); return true; } catch { return false; }
    }
    function prettyOrOriginal(text) {
        try { return JSON.stringify(JSON.parse(text), null, 2); } catch { return text; }
    }

    // Convierte los formularios -> DTO que espera el back
    function buildPayloadFromForms() {
        // PERFIL
        const perfil = {
            nombreMostrar: byId("nombreMostrar")?.value || "",
            especialidad:  byId("especialidad")?.value || "",
            colegiado:     byId("colegiado")?.value || "",
            bio:           byId("bio")?.value || "",
            firmaTexto:    byId("firmaTexto")?.value || "",
            // Mantén la URL anterior si no hay nueva imagen subida
            firmaImagenUrl: state?.perfil?.firmaImagenUrl || ""
        };

        // UI
        const ui = {
            tema:        byId("tema")?.value || "auto",
            idioma:      byId("idioma")?.value || "es",
            zonaHoraria: byId("zona")?.value || "Europe/Madrid",
            home:        byId("home")?.value || "dashboard"
        };

        // AGENDA
        let agenda = null;
        if (formAgenda) {
            const reglasJson = byId("reglasJson")?.value?.trim() || "{}";
            const disponibilidadJson = byId("disponibilidadJson")?.value?.trim() || "{}";
            const instruccionesPorTipoJson = byId("instruccionesPorTipoJson")?.value?.trim() || "{}";

            const invalids = [];
            if (!isValidJson(reglasJson)) invalids.push("Reglas");
            if (!isValidJson(disponibilidadJson)) invalids.push("Disponibilidad");
            if (!isValidJson(instruccionesPorTipoJson)) invalids.push("Instrucciones por tipo");
            if (invalids.length) throw new Error("JSON inválido en: " + invalids.join(", "));

            agenda = {
                duracionGeneralMin: Number(byId("duracion")?.value ?? 20),
                bufferMin: Number(byId("buffer")?.value ?? 5),
                reglasJson,
                disponibilidadJson,
                instruccionesPorTipoJson
            };
        }

        // NOTIFICACIONES
        let notificaciones = null;
        if (formNotis) {
            const canales = {
                email:  !!formNotis.querySelector('input[name="notificaciones.canales.email"]')?.checked,
                sms:    !!formNotis.querySelector('input[name="notificaciones.canales.sms"]')?.checked,
                inapp:  !!formNotis.querySelector('input[name="notificaciones.canales.inapp"]')?.checked
            };
            const eventos = {
                nuevaCita:   !!formNotis.querySelector('input[name="notificaciones.eventos.nuevaCita"]')?.checked,
                cancelacion: !!formNotis.querySelector('input[name="notificaciones.eventos.cancelacion"]')?.checked,
                noAsiste:    !!formNotis.querySelector('input[name="notificaciones.eventos.noAsiste"]')?.checked,
                mensaje:     !!formNotis.querySelector('input[name="notificaciones.eventos.mensaje"]')?.checked
            };

            const plantillasJson = byId("plantillasJson")?.value?.trim() || "{}";
            if (!isValidJson(plantillasJson)) throw new Error("JSON inválido en: Plantillas");

            notificaciones = {
                canalesJson: JSON.stringify(canales),
                eventosJson: JSON.stringify(eventos),
                silencioDesde: byId("silencioDesde")?.value || "22:00",
                silencioHasta: byId("silencioHasta")?.value || "07:00",
                plantillasJson
            };
        }

        return { perfil, ui, agenda, notificaciones };
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
            if (e.key === "ArrowRight") { e.preventDefault(); tabs[(i + 1) % tabs.length].focus(); tabs[(i + 1) % tabs.length].click(); }
            if (e.key === "ArrowLeft")  { e.preventDefault(); tabs[(i - 1 + tabs.length) % tabs.length].focus(); tabs[(i - 1 + tabs.length) % tabs.length].click(); }
            if (e.key === "Home")       { e.preventDefault(); tabs[0].focus(); tabs[0].click(); }
            if (e.key === "End")        { e.preventDefault(); tabs[tabs.length - 1].focus(); tabs[tabs.length - 1].click(); }
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

    /* ------------------------- Firma: preview + (opcional) upload ------------------------- */
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

            // OPCIONAL: endpoint de subida
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
            //   state = state || {};
            //   state.perfil = state.perfil || {};
            //   state.perfil.firmaImagenUrl = url;
            //   setDirty(true);
            // } catch (err) {
            //   console.error("Upload firma falló:", err);
            //   firmaPreview.textContent = "No se pudo subir la imagen. Guarda sin imagen o reintenta.";
            // }
        });
    }

    /* ------------------------- Guardar (PUT) ------------------------- */
    btnSave.addEventListener("click", async () => {
        btnSave.disabled = true;
        statusEl.textContent = "Guardando…";

        try {
            const payload = buildPayloadFromForms(); // puede lanzar Error(JSON inválido)

            const res = await fetch("/api/medico/config", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    ...getCsrfHeader()
                },
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            state = await res.json(); // Respuesta ya en formato DTO

            // Repinta formularios por si el back normaliza valores
            populateForm(formPerfil, state);
            populateForm(formUi, state);
            populateForm(formAgenda, state);
            populateForm(formNotis, state);

            // Pretty print de jsons si llegaron minimizados
            ["#reglasJson", "#disponibilidadJson", "#instruccionesPorTipoJson", "#plantillasJson"]
                .forEach(sel => {
                    const el = $(sel);
                    if (el && el.value) el.value = prettyOrOriginal(el.value);
                });

            setDirty(false);
        } catch (err) {
            console.error("Error al guardar:", err);
            statusEl.textContent = (err && err.message) ? err.message : "Error al guardar. Reintenta.";
            btnSave.disabled = false;
        }
    });

    /* ------------------------- Carga inicial (GET) ------------------------- */
    (async function init() {
        // pestaña persistida
        switchTo(getSavedTab());

        try {
            const res = await fetch("/api/medico/config", {
                headers: { "Accept": "application/json", ...getCsrfHeader() }
            });
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            state = await res.json();

            // Poblar formularios
            populateForm(formPerfil, state);
            populateForm(formUi, state);
            populateForm(formAgenda, state);
            populateForm(formNotis, state);

            // Pretty print de jsons si ya vienen en texto
            ["#reglasJson", "#disponibilidadJson", "#instruccionesPorTipoJson", "#plantillasJson"]
                .forEach(sel => {
                    const el = $(sel);
                    if (el && el.value) el.value = prettyOrOriginal(el.value);
                });

            // Firma previa
            if (state?.perfil?.firmaImagenUrl && firmaPreview) {
                const img = document.createElement("img");
                img.alt = "Firma actual";
                img.loading = "lazy";
                img.style.maxWidth = "220px";
                img.style.maxHeight = "90px";
                img.src = state.perfil.firmaImagenUrl;
                firmaPreview.appendChild(img);
            }

            // Formateo automático cuando se sale del textarea
            ["#reglasJson", "#disponibilidadJson", "#instruccionesPorTipoJson", "#plantillasJson"]
                .forEach(sel => {
                    const el = $(sel);
                    if (!el) return;
                    el.addEventListener("blur", () => {
                        el.value = prettyOrOriginal((el.value || "").trim());
                    });
                });

            setDirty(false);
        } catch (err) {
            console.error("Error al cargar configuración:", err);
            statusEl.textContent = "No se pudo cargar la configuración.";
        }
    })();

    /* ------------------------- API pública mínima (opcional) -------------------------
     * window.ConfigMedico = { save: () => btnSave.click(), setDirty }
     * ------------------------------------------------------------------------------- */
})();
