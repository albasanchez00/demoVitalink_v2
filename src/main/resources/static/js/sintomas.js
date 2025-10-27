document.addEventListener("DOMContentLoaded", () => {
    // --- DOM ---
    const tbody     = document.getElementById("tbody-sintomas");
    const btnAbrir  = document.getElementById("btn-abrir");
    const modal     = document.getElementById("modal");
    const btnCerrar = document.getElementById("btn-cerrar");
    const form      = document.getElementById("form-sintoma");

    // --- Opciones de selects (ajústalas a tu app) ---
    const TIPOS = [
        "DOLOR_CABEZA","FATIGA","NAUSEAS","VOMITOS",
        "FIEBRE","MAREO","TOS","DOLOR_MUSCULAR"
    ];
    const ZONAS = [
        "CABEZA","PECHO","ABDOMEN","ESPALDA",
        "GARGANTA","BRAZOS","PIERNAS","GENERAL"
    ];

    // --- Config API ---
    const API_BASE = "/api/sintomas";
    const URL_LIST = `${API_BASE}/mios`;

    // --- CSRF (si Spring Security lo expone en meta) ---
    const CSRF_TOKEN  = document.querySelector('meta[name="_csrf"]')?.content;
    const CSRF_HEADER = document.querySelector('meta[name="_csrf_header"]')?.content;

    // Helper fetch con credenciales y CSRF
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

    // ===== Utilidades UI / datos =====
    function abrir() {
        if (!modal) return;
        // al abrir (modo crear), repoblamos selects sin valor preseleccionado
        populateSelects();
        modal.classList.remove("oculto");
        modal.style.display = "flex";
    }

    function cerrar() {
        if (!modal) return;
        modal.classList.add("oculto");
        modal.style.display = "none";
        form?.reset();
        const idInput = document.getElementById("id_sintoma");
        if (idInput) idInput.value = ""; // salir de modo edición
    }

    // Repoblar un select con la lista completa y marcar el valor actual
    function fillSelect(selectEl, options, selectedValue = "") {
        if (!selectEl) return;
        const selected = (selectedValue ?? "").trim();

        // limpiar opciones
        selectEl.innerHTML = "";

        // placeholder (opcional)
        const ph = new Option("", "", true, false);
        ph.textContent = "";
        selectEl.add(ph);

        // todas las opciones
        options.forEach(opt => selectEl.add(new Option(opt, opt)));

        // si viene un valor que no está en el catálogo, se añade para no perderlo
        if (selected && !options.includes(selected)) {
            const phantom = new Option(selected, selected, true, true);
            phantom.dataset.temp = "true";
            selectEl.add(phantom, 1); // justo tras el placeholder
        }

        // seleccionar
        if (selected) selectEl.value = selected;
    }

    function populateSelects(selected = {}) {
        fillSelect(document.getElementById("tipo"), TIPOS, selected.tipo);
        fillSelect(document.getElementById("zona"), ZONAS, selected.zona);
    }

    // Escapar para atributos data-*
    function esc(v = "") {
        return String(v)
            .replace(/&/g, "&amp;").replace(/</g, "&lt;")
            .replace(/>/g, "&gt;").replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    // ISO → value de <input type="datetime-local">
    function toLocalInputValue(iso) {
        if (!iso) return "";
        const d = new Date(iso);
        if (isNaN(d)) return "";
        const off = d.getTimezoneOffset();
        const local = new Date(d.getTime() - off * 60000);
        return local.toISOString().slice(0, 16); // yyyy-MM-ddTHH:mm
    }

    const btnCancelar = document.getElementById("btn-cancelar");
    btnCancelar?.addEventListener("click", cerrar);
    btnAbrir?.addEventListener("click", abrir);
    btnCerrar?.addEventListener("click", cerrar);
    modal?.addEventListener("click", (e) => { if (e.target === modal) cerrar(); });
    document.addEventListener("keydown", (e) => { if (e.key === "Escape" && !modal.classList.contains("oculto")) cerrar(); });

    // --- Cargar lista ---
    cargarSintomas();

    async function cargarSintomas() {
        if (!tbody) return;
        tbody.innerHTML = `<tr><td colspan="5">Cargando...</td></tr>`;
        try {
            const res = await authFetch(URL_LIST);
            if (res.status === 401 || res.status === 403) {
                tbody.innerHTML = `<tr><td colspan="5">No autorizado. Inicia sesión.</td></tr>`;
                return;
            }
            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            const data = await res.json();
            if (!Array.isArray(data) || data.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5">No hay síntomas.</td></tr>`;
                return;
            }
            tbody.innerHTML = data.map(rowHtml).join("");
        } catch (e) {
            console.error("Error al cargar síntomas:", e);
            tbody.innerHTML = `<tr><td colspan="5">Error al cargar los datos.</td></tr>`;
        }
    }

    function rowHtml(it) {
        return `
      <tr>
        <td>${fmtFecha(it.fechaRegistro)}</td>
        <td>${it.tipo ?? ""}</td>
        <td>${it.zona ?? ""}</td>
        <td>${it.descripcion ?? ""}</td>
        <td class="acciones">
          <button class="btn btn-sec"
                  data-action="edit"
                  data-id="${it.id_sintoma}"
                  data-tipo="${esc(it.tipo ?? "")}"
                  data-zona="${esc(it.zona ?? "")}"
                  data-desc="${esc(it.descripcion ?? "")}"
                  data-fecha="${esc(it.fechaRegistro ?? "")}">
            Editar
          </button>
          <button class="btn btn-sec danger"
                  data-action="delete"
                  data-id="${it.id_sintoma}">
            Eliminar
          </button>
        </td>
      </tr>
    `;
    }

    function fmtFecha(iso) {
        if (!iso) return "";
        try { return new Date(iso).toLocaleString(); } catch { return iso; }
    }

    // ===== Delegación en la tabla (Editar / Eliminar) =====
    tbody?.addEventListener("click", (ev) => {
        const btn = ev.target.closest("button[data-action]");
        if (!btn) return;

        const id = btn.dataset.id;

        if (btn.dataset.action === "edit") {
            // repoblar selects con TODAS las opciones y seleccionar las actuales
            populateSelects({ tipo: btn.dataset.tipo || "", zona: btn.dataset.zona || "" });

            document.getElementById("id_sintoma").value      = id || "";
            document.getElementById("descripcion").value     = btn.dataset.desc || "";
            document.getElementById("fechaRegistro").value   = toLocalInputValue(btn.dataset.fecha);
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
            cargarSintomas();
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
            fechaRegistro: document.getElementById("fechaRegistro")?.value || null, // yyyy-MM-ddTHH:mm
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
            cargarSintomas();
        } catch (e) {
            console.error("Error al guardar síntoma:", e);
            alert("No se pudo guardar el síntoma.");
        }
    });

    console.log("[sintomas.js] cargado");
});