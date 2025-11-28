document.addEventListener("DOMContentLoaded", () => {
    // --- DOM ---
    const tbody         = document.getElementById("tbody-sintomas");
    const btnAbrir      = document.getElementById("btn-abrir");
    const modal         = document.getElementById("modal");
    const btnCerrar     = document.getElementById("btn-cerrar");
    const btnCancelar   = document.getElementById("btn-cancelar");
    const form          = document.getElementById("form-sintoma");
    const btnAplicar    = document.getElementById("btn-aplicar");
    const btnLimpiar    = document.getElementById("btn-limpiar");
    const estadoVacio   = document.getElementById("estado-vacio");
    const contador      = document.getElementById("contador-resultados");

    // Filtros
    const filtroTipo    = document.getElementById("filtro-tipo");
    const filtroZona    = document.getElementById("filtro-zona");
    const filtroDesde   = document.getElementById("filtro-desde");
    const filtroHasta   = document.getElementById("filtro-hasta");

    // --- Datos cargados (para filtrar en cliente) ---
    let sintomasData = [];

    // --- Opciones de selects ---
    const TIPOS = [
        { value: "DOLOR_CABEZA",   label: "Dolor de cabeza",   color: "#ef4444" },
        { value: "FATIGA",         label: "Fatiga",            color: "#f97316" },
        { value: "NAUSEAS",        label: "Náuseas",           color: "#eab308" },
        { value: "VOMITOS",        label: "Vómitos",           color: "#84cc16" },
        { value: "FIEBRE",         label: "Fiebre",            color: "#dc2626" },
        { value: "MAREO",          label: "Mareo",             color: "#a855f7" },
        { value: "TOS",            label: "Tos",               color: "#06b6d4" },
        { value: "DOLOR_MUSCULAR", label: "Dolor muscular",    color: "#ec4899" },
    ];

    const ZONAS = [
        { value: "CABEZA",   label: "Cabeza" },
        { value: "PECHO",    label: "Pecho" },
        { value: "ABDOMEN",  label: "Abdomen" },
        { value: "ESPALDA",  label: "Espalda" },
        { value: "GARGANTA", label: "Garganta" },
        { value: "BRAZOS",   label: "Brazos" },
        { value: "PIERNAS",  label: "Piernas" },
        { value: "GENERAL",  label: "General" },
    ];

    // Mapa rápido para colores
    const TIPO_COLOR_MAP = Object.fromEntries(TIPOS.map(t => [t.value, t.color]));
    const TIPO_LABEL_MAP = Object.fromEntries(TIPOS.map(t => [t.value, t.label]));
    const ZONA_LABEL_MAP = Object.fromEntries(ZONAS.map(z => [z.value, z.label]));

    // --- Config API ---
    const API_BASE = "/api/sintomas";
    const URL_LIST = `${API_BASE}/mios`;

    // --- CSRF ---
    const CSRF_TOKEN  = document.querySelector('meta[name="_csrf"]')?.content;
    const CSRF_HEADER = document.querySelector('meta[name="_csrf_header"]')?.content;

    // Helper fetch
    async function authFetch(url, options = {}) {
        const baseHeaders = {
            ...(options.headers || {}),
            ...(CSRF_TOKEN && CSRF_HEADER ? { [CSRF_HEADER]: CSRF_TOKEN } : {}),
        };
        return fetch(url, {
            credentials: "same-origin",
            ...options,
            headers: { "Accept": "application/json", ...baseHeaders },
        });
    }

    // ===== Inicializar filtros (selects) =====
    function initFiltros() {
        // Poblar select de tipo en filtros
        TIPOS.forEach(t => {
            filtroTipo?.appendChild(new Option(t.label, t.value));
        });
        // Poblar select de zona en filtros
        ZONAS.forEach(z => {
            filtroZona?.appendChild(new Option(z.label, z.value));
        });
    }

    // ===== Modal =====
    function abrir() {
        if (!modal) return;
        populateSelects();
        document.getElementById("titulo-modal").textContent = "Registrar síntoma";
        modal.classList.remove("oculto");
        modal.style.display = "flex";
    }

    function cerrar() {
        if (!modal) return;
        modal.classList.add("oculto");
        modal.style.display = "none";
        form?.reset();
        const idInput = document.getElementById("id_sintoma");
        if (idInput) idInput.value = "";
    }

    function fillSelect(selectEl, options, selectedValue = "") {
        if (!selectEl) return;
        const selected = (selectedValue ?? "").trim();
        selectEl.innerHTML = "";

        const ph = new Option("Seleccionar...", "", true, false);
        selectEl.add(ph);

        options.forEach(opt => {
            const o = new Option(opt.label || opt.value, opt.value);
            selectEl.add(o);
        });

        if (selected && !options.find(o => o.value === selected)) {
            const phantom = new Option(selected, selected, true, true);
            phantom.dataset.temp = "true";
            selectEl.add(phantom, 1);
        }

        if (selected) selectEl.value = selected;
    }

    function populateSelects(selected = {}) {
        fillSelect(document.getElementById("tipo"), TIPOS, selected.tipo);
        fillSelect(document.getElementById("zona"), ZONAS, selected.zona);
    }

    // Escapar HTML
    function esc(v = "") {
        return String(v)
            .replace(/&/g, "&amp;").replace(/</g, "&lt;")
            .replace(/>/g, "&gt;").replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    // ISO → datetime-local
    function toLocalInputValue(iso) {
        if (!iso) return "";
        const d = new Date(iso);
        if (isNaN(d)) return "";
        const off = d.getTimezoneOffset();
        const local = new Date(d.getTime() - off * 60000);
        return local.toISOString().slice(0, 16);
    }

    // ===== Event Listeners =====
    btnAbrir?.addEventListener("click", abrir);
    btnCerrar?.addEventListener("click", cerrar);
    btnCancelar?.addEventListener("click", cerrar);
    modal?.addEventListener("click", (e) => { if (e.target === modal) cerrar(); });
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && !modal.classList.contains("oculto")) cerrar();
    });

    // Filtros
    btnAplicar?.addEventListener("click", aplicarFiltros);
    btnLimpiar?.addEventListener("click", limpiarFiltros);

    // Enter en filtros
    [filtroTipo, filtroZona, filtroDesde, filtroHasta].forEach(el => {
        el?.addEventListener("keydown", (e) => {
            if (e.key === "Enter") aplicarFiltros();
        });
    });

    // ===== Cargar datos =====
    initFiltros();
    cargarSintomas();

    async function cargarSintomas() {
        if (!tbody) return;
        tbody.innerHTML = `<tr><td colspan="5" class="cargando">
            <div class="spinner"></div> Cargando síntomas...
        </td></tr>`;
        mostrarEstadoVacio(false);

        try {
            const res = await authFetch(URL_LIST);
            if (res.status === 401 || res.status === 403) {
                tbody.innerHTML = `<tr><td colspan="5">No autorizado. Inicia sesión.</td></tr>`;
                actualizarContador(0);
                return;
            }
            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            sintomasData = await res.json();

            if (!Array.isArray(sintomasData)) sintomasData = [];

            renderTabla(sintomasData);

        } catch (e) {
            console.error("Error al cargar síntomas:", e);
            tbody.innerHTML = `<tr><td colspan="5" class="error">Error al cargar los datos.</td></tr>`;
            actualizarContador(0);
        }
    }

    // ===== Filtrado =====
    function aplicarFiltros() {
        const tipo  = filtroTipo?.value || "";
        const zona  = filtroZona?.value || "";
        const desde = filtroDesde?.value ? new Date(filtroDesde.value) : null;
        const hasta = filtroHasta?.value ? new Date(filtroHasta.value + "T23:59:59") : null;

        const filtrados = sintomasData.filter(s => {
            // Filtro por tipo
            if (tipo && s.tipo !== tipo) return false;
            // Filtro por zona
            if (zona && s.zona !== zona) return false;
            // Filtro por fecha desde
            if (desde) {
                const fechaS = new Date(s.fechaRegistro);
                if (fechaS < desde) return false;
            }
            // Filtro por fecha hasta
            if (hasta) {
                const fechaS = new Date(s.fechaRegistro);
                if (fechaS > hasta) return false;
            }
            return true;
        });

        renderTabla(filtrados);
    }

    function limpiarFiltros() {
        if (filtroTipo)  filtroTipo.value = "";
        if (filtroZona)  filtroZona.value = "";
        if (filtroDesde) filtroDesde.value = "";
        if (filtroHasta) filtroHasta.value = "";
        renderTabla(sintomasData);
    }

    // ===== Render =====
    function renderTabla(data) {
        if (!Array.isArray(data) || data.length === 0) {
            tbody.innerHTML = "";
            mostrarEstadoVacio(true);
            actualizarContador(0);
            return;
        }

        mostrarEstadoVacio(false);
        tbody.innerHTML = data.map(rowHtml).join("");
        actualizarContador(data.length);
    }

    function mostrarEstadoVacio(show) {
        if (estadoVacio) {
            estadoVacio.classList.toggle("oculto", !show);
        }
    }

    function actualizarContador(n) {
        if (contador) {
            const total = sintomasData.length;
            if (n === total) {
                contador.textContent = `Mostrando ${n} síntoma${n !== 1 ? 's' : ''}`;
            } else {
                contador.textContent = `Mostrando ${n} de ${total} síntoma${total !== 1 ? 's' : ''}`;
            }
        }
    }

    function rowHtml(it) {
        const tipoColor = TIPO_COLOR_MAP[it.tipo] || "#6b7280";
        const tipoLabel = TIPO_LABEL_MAP[it.tipo] || it.tipo || "—";
        const zonaLabel = ZONA_LABEL_MAP[it.zona] || it.zona || "—";
        const zonaClass = it.zona ? "" : "texto-muted";

        return `
        <tr>
            <td class="col-fecha">${fmtFecha(it.fechaRegistro)}</td>
            <td>
                <span class="badge-tipo" style="--badge-color: ${tipoColor}">
                    ${tipoLabel}
                </span>
            </td>
            <td class="${zonaClass}">${zonaLabel}</td>
            <td class="col-desc">${esc(it.descripcion) || '<span class="texto-muted">Sin descripción</span>'}</td>
            <td class="col-acciones">
                <button class="btn-icono btn-editar" title="Editar"
                        data-action="edit"
                        data-id="${it.id_sintoma}"
                        data-tipo="${esc(it.tipo ?? "")}"
                        data-zona="${esc(it.zona ?? "")}"
                        data-desc="${esc(it.descripcion ?? "")}"
                        data-fecha="${esc(it.fechaRegistro ?? "")}">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                        <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/>
                        <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                </button>
                <button class="btn-icono btn-eliminar" title="Eliminar"
                        data-action="delete"
                        data-id="${it.id_sintoma}">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                        <path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                        <line x1="10" y1="11" x2="10" y2="17"/>
                        <line x1="14" y1="11" x2="14" y2="17"/>
                    </svg>
                </button>
            </td>
        </tr>`;
    }

    function fmtFecha(iso) {
        if (!iso) return "—";
        try {
            const d = new Date(iso);
            return d.toLocaleDateString("es-ES", {
                day: "2-digit",
                month: "short",
                year: "numeric",
                hour: "2-digit",
                minute: "2-digit"
            });
        } catch {
            return iso;
        }
    }

    // ===== Delegación tabla (Editar / Eliminar) =====
    tbody?.addEventListener("click", (ev) => {
        const btn = ev.target.closest("button[data-action]");
        if (!btn) return;

        const id = btn.dataset.id;

        if (btn.dataset.action === "edit") {
            populateSelects({ tipo: btn.dataset.tipo || "", zona: btn.dataset.zona || "" });
            document.getElementById("id_sintoma").value      = id || "";
            document.getElementById("descripcion").value     = btn.dataset.desc || "";
            document.getElementById("fechaRegistro").value   = toLocalInputValue(btn.dataset.fecha);
            document.getElementById("titulo-modal").textContent = "Editar síntoma";
            abrir();
        }

        if (btn.dataset.action === "delete") {
            eliminarSintoma(id);
        }
    });

    async function eliminarSintoma(id) {
        if (!id) return;
        if (!confirm("¿Eliminar este síntoma?")) return;

        try {
            const res = await authFetch(`${API_BASE}/${id}`, { method: "DELETE" });
            if (res.status === 401 || res.status === 403) {
                alert("No autorizado. Inicia sesión.");
                return;
            }
            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            // Actualizar datos locales y re-renderizar
            sintomasData = sintomasData.filter(s => s.id_sintoma != id);
            aplicarFiltros();

        } catch (e) {
            console.error("Error al eliminar síntoma:", e);
            alert("No se pudo eliminar el síntoma.");
        }
    }

    // ===== Crear / Actualizar síntoma =====
    form?.addEventListener("submit", async (ev) => {
        ev.preventDefault();

        const idEdit = document.getElementById("id_sintoma")?.value?.trim();

        const payload = {
            tipo:          document.getElementById("tipo")?.value || null,
            zona:          document.getElementById("zona")?.value || null,
            descripcion:   document.getElementById("descripcion")?.value || null,
            fechaRegistro: document.getElementById("fechaRegistro")?.value || null,
        };
        Object.keys(payload).forEach(k => payload[k] == null && delete payload[k]);

        const url    = idEdit ? `${API_BASE}/${idEdit}` : API_BASE;
        const method = idEdit ? "PUT" : "POST";

        try {
            const res = await authFetch(url, {
                method,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });

            if (res.status === 401 || res.status === 403) {
                alert("No autorizado. Inicia sesión.");
                return;
            }
            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            cerrar();
            cargarSintomas(); // Recargar todo

        } catch (e) {
            console.error("Error al guardar síntoma:", e);
            alert("No se pudo guardar el síntoma.");
        }
    });

    console.log("[sintomas.js] cargado v2.0");
});