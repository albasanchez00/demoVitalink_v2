document.addEventListener("DOMContentLoaded", () => {
    // --- DOM ---
    const tbody     = document.getElementById("tbody-sintomas");
    const btnAbrir  = document.getElementById("btn-abrir");
    const modal     = document.getElementById("modal");
    const btnCerrar = document.getElementById("btn-cerrar");
    const form      = document.getElementById("form-sintoma");

    // --- Config API ---
    const API_BASE = "/api/sintomas";
    const URL_LIST = `${API_BASE}/mios`; // <- tu controlador expone GET /mios

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
            headers: {
                "Accept": "application/json",
                ...baseHeaders,
            },
        });
    }

    // --- UI helpers ---
    function abrir() {
        if (!modal) return;
        modal.classList.remove("oculto");     // por si tu CSS lo usa
        modal.style.display = "flex";         // fuerza que se vea
    }
    function cerrar() {
        if (!modal) return;
        modal.classList.add("oculto");
        modal.style.display = "none";         // fuerza que se oculte
        form?.reset();
    }
    const btnCancelar = document.getElementById("btn-cancelar");
    btnCancelar?.addEventListener("click", cerrar);
    btnAbrir?.addEventListener("click", abrir);
    btnCerrar?.addEventListener("click", cerrar);
    // cerrar al hacer click en el fondo
    modal?.addEventListener("click", (e) => {
        if (e.target === modal) cerrar();
    });

// cerrar con tecla ESC
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && !modal.classList.contains("oculto")) cerrar();
    });


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
        <td></td>
      </tr>
    `;
    }

    function fmtFecha(iso) {
        if (!iso) return "";
        try { return new Date(iso).toLocaleString(); } catch { return iso; }
    }

    // --- Crear síntoma ---
    form?.addEventListener("submit", async (ev) => {
        ev.preventDefault();

        const payload = {
            tipo:           document.getElementById("tipo")?.value || null,
            zona:           document.getElementById("zona")?.value || null,
            descripcion:    document.getElementById("descripcion")?.value || null,
            fechaRegistro:  document.getElementById("fechaRegistro")?.value || null, // ISO (yyyy-MM-ddTHH:mm)
        };

        // Limpia nulls para no enviar claves vacías si no quieres
        Object.keys(payload).forEach(k => payload[k] == null && delete payload[k]);

        try {
            const res = await authFetch(API_BASE, {
                method: "POST",
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
    console.log("btnAbrir:", document.getElementById("btn-abrir"));

});
