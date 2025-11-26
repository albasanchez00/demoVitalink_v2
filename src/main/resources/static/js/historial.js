// ===== Utils fechas =====
function parseDateFlexible(dateStr, timeStr) {
    if (!dateStr && !timeStr) return null;
    if (dateStr instanceof Date) return dateStr;
    if (typeof dateStr === "number") return new Date(dateStr);

    if (typeof dateStr === "string") {
        if (/^\d{4}-\d{2}-\d{2}/.test(dateStr)) {
            const iso = timeStr ? `${dateStr}T${timeStr}` : dateStr;
            const d1 = new Date(iso);
            if (!isNaN(d1)) return d1;
        }
        const m = dateStr.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);
        if (m) {
            const dd = +m[1], mm = +m[2]-1, yy = +m[3];
            const d2 = new Date(yy, mm, dd, 0, 0, 0);
            if (!isNaN(d2)) return d2;
        }
        const d3 = new Date(dateStr);
        if (!isNaN(d3)) return d3;
    }
    if (timeStr) {
        const d4 = new Date(`${dateStr} ${timeStr}`);
        if (!isNaN(d4)) return d4;
    }
    return null;
}

function formatDateLocal(d) {
    if (!(d instanceof Date) || isNaN(d)) return "—";
    return d.toLocaleString("es-ES", {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// ===== Render =====
const lista = document.querySelector(".hist-lista");

function clearClientEvents() {
    lista?.querySelectorAll("li[data-js='true']").forEach(li => li.remove());
    // También ocultar el mensaje de "No hay registros" del servidor
    const emptyMsg = lista?.querySelector("li:not([data-js])");
    if (emptyMsg) emptyMsg.style.display = "none";
}

function liEvento({ fecha, tipo, titulo, descripcion, zona, estado, urlDetalle, urlEditar }) {
    const li = document.createElement("li");
    li.className = "hist-item";
    li.dataset.js = "true";

    const tipoChip =
        tipo === "SINTOMA" ? '<span class="chip chip-sintoma">Síntoma</span>' :
            tipo === "TRATAMIENTO" ? '<span class="chip chip-tratamiento">Tratamiento</span>' :
                '<span class="chip chip-cita">Cita</span>';

    const estadoChip = estado ? `<span class="chip chip-estado">${estado}</span>` : '';

    li.innerHTML = `
    <div class="hist-fecha"><small>${fecha || "—"}</small></div>
    <div class="hist-tipo">${tipoChip} ${estadoChip}</div>
    <div class="hist-contenido">
      <h4>${titulo ?? ""}</h4>
      ${descripcion ? `<p>${descripcion}</p>` : ""}
      ${zona ? `<div class="hist-meta"><span><strong>Zona:</strong> ${zona.replace('_', ' ')}</span></div>` : ""}
      <div class="hist-item-actions">
        ${[urlDetalle ? `<a class="link" href="${urlDetalle}">Ver</a>` : "", urlEditar ? `<a class="link" href="${urlEditar}">Editar</a>` : ""]
        .filter(Boolean).join('<span> · </span>')}
      </div>
    </div>`;
    return li;
}

function showEmptyMessage() {
    clearClientEvents();
    const li = document.createElement("li");
    li.dataset.js = "true";
    li.innerHTML = `<p class="text_center">No hay registros para mostrar.</p>`;
    lista?.appendChild(li);
}

function showErrorMessage(msg) {
    clearClientEvents();
    const li = document.createElement("li");
    li.dataset.js = "true";
    li.innerHTML = `<p class="text_center" style="color: #c00;">${msg}</p>`;
    lista?.appendChild(li);
}

function showLoadingMessage() {
    clearClientEvents();
    const li = document.createElement("li");
    li.dataset.js = "true";
    li.innerHTML = `<p class="text_center">Cargando historial...</p>`;
    lista?.appendChild(li);
}

// ===== Mapeo de eventos desde la API unificada =====
function mapEvento(e) {
    const d = parseDateFlexible(e.fecha);
    return {
        fechaISO: e.fecha,
        fecha: formatDateLocal(d),
        tipo: e.tipo,
        titulo: e.titulo || "—",
        descripcion: e.descripcion || "",
        zona: e.zona || null,
        estado: e.estado || null,
        urlDetalle: e.urlDetalle,
        urlEditar: e.urlEditar
    };
}

// ===== API =====
function getUserId() {
    // Prioridad: window.USER_ID > data-attribute > null
    if (window.USER_ID && window.USER_ID !== 0) {
        return window.USER_ID;
    }
    const main = document.querySelector('#main_panelUser');
    if (main?.dataset?.userId) {
        return parseInt(main.dataset.userId, 10);
    }
    return null;
}

async function fetchHistorial(filtros = {}) {
    const userId = getUserId();

    if (!userId) {
        console.error("[Historial] No se encontró userId válido");
        showErrorMessage("Error: No se pudo identificar al usuario");
        return [];
    }

    // Construir query params
    const params = new URLSearchParams();
    params.append('page', filtros.page || 0);
    params.append('size', filtros.size || 50);

    if (filtros.tipo) params.append('tipo', filtros.tipo);
    if (filtros.desde) params.append('desde', filtros.desde);
    if (filtros.hasta) params.append('hasta', filtros.hasta);
    if (filtros.zona) params.append('zona', filtros.zona);
    if (filtros.estadoTratamiento) params.append('estadoTratamiento', filtros.estadoTratamiento);

    const url = `/api/usuarios/${userId}/historial?${params.toString()}`;
    console.log("[Historial] Fetching:", url);

    const res = await fetch(url, {
        headers: { Accept: "application/json" },
        credentials: "same-origin"
    });

    if (res.status === 401 || res.status === 403) {
        window.location.href = "/inicioSesion";
        return [];
    }

    if (!res.ok) {
        throw new Error(`HTTP ${res.status}`);
    }

    const data = await res.json();
    return data;
}

// ===== Filtros =====
function readFiltersFromForm() {
    const form = document.querySelector(".hist-form");
    if (!form) return {};

    const f = new FormData(form);

    return {
        tipo: (f.get("tipo") || "").toUpperCase() || null,
        desde: f.get("desde") || null,
        hasta: f.get("hasta") || null,
        zona: (f.get("zona") || "").trim().toUpperCase() || null,
        estadoTratamiento: (f.get("estadoTratamiento") || "").toUpperCase() || null,
        page: 0,
        size: 50
    };
}

// ===== Carga + render =====
async function cargarYRenderizarEventos(filtros = {}) {
    try {
        showLoadingMessage();

        const data = await fetchHistorial(filtros);

        // La API devuelve un Page con content
        const eventos = (data.content || []).map(mapEvento);

        if (eventos.length === 0) {
            showEmptyMessage();
            return;
        }

        // Limpiar y pintar
        clearClientEvents();
        const frag = document.createDocumentFragment();
        eventos.forEach(ev => frag.appendChild(liEvento(ev)));
        lista?.appendChild(frag);

        // Mostrar info de paginación
        renderPaginacion(data, filtros);

    } catch (e) {
        console.error("[Historial] Error:", e);
        showErrorMessage("Error al cargar el historial. Intenta recargar la página.");
    }
}

// ===== Paginación =====
function renderPaginacion(pageData, filtros) {
    let container = document.querySelector(".hist-paginacion");

    if (!container) {
        container = document.createElement("div");
        container.className = "hist-paginacion";
        document.querySelector(".hist-timeline")?.appendChild(container);
    }

    const { number: currentPage, totalPages, totalElements, size } = pageData;

    if (!totalElements || totalPages <= 1) {
        container.innerHTML = `<span>Total: ${totalElements || 0} registros</span>`;
        return;
    }

    container.innerHTML = `
        <span>Página ${currentPage + 1} de ${totalPages} (${totalElements} registros)</span>
        <div class="paginacion-btns">
            <button class="btn btn_linea" id="btnPrevHist" ${currentPage === 0 ? 'disabled' : ''}>« Anterior</button>
            <button class="btn btn_linea" id="btnNextHist" ${currentPage >= totalPages - 1 ? 'disabled' : ''}>Siguiente »</button>
        </div>
    `;

    document.getElementById("btnPrevHist")?.addEventListener("click", () => {
        if (currentPage > 0) {
            filtros.page = currentPage - 1;
            cargarYRenderizarEventos(filtros);
        }
    });

    document.getElementById("btnNextHist")?.addEventListener("click", () => {
        if (currentPage < totalPages - 1) {
            filtros.page = currentPage + 1;
            cargarYRenderizarEventos(filtros);
        }
    });
}

// ===== Hook del formulario =====
function setupFilters() {
    const form = document.querySelector(".hist-form");
    if (!form) return;

    form.addEventListener("submit", (ev) => {
        ev.preventDefault();
        const filtros = readFiltersFromForm();

        // Actualizar querystring en la URL
        const params = new URLSearchParams();
        // Mantener userId si existe
        const userId = getUserId();
        if (userId) params.set("userId", userId);
        if (filtros.tipo) params.set("tipo", filtros.tipo);
        if (filtros.desde) params.set("desde", filtros.desde);
        if (filtros.hasta) params.set("hasta", filtros.hasta);
        if (filtros.zona) params.set("zona", filtros.zona);
        if (filtros.estadoTratamiento) params.set("estadoTratamiento", filtros.estadoTratamiento);

        const qs = params.toString();
        const newUrl = qs ? `${location.pathname}?${qs}` : location.pathname;
        history.replaceState(null, "", newUrl);

        cargarYRenderizarEventos(filtros);
    });

    // Botón "Limpiar"
    const btnLimpiar = form.querySelector('a[href*="historialPaciente"]');
    if (btnLimpiar) {
        btnLimpiar.addEventListener("click", (e) => {
            e.preventDefault();
            form.reset();

            const userId = getUserId();
            const newUrl = userId ? `${location.pathname}?userId=${userId}` : location.pathname;
            history.replaceState(null, "", newUrl);

            cargarYRenderizarEventos({});
        });
    }
}

// ===== Carga inicial =====
function initialFiltersFromQuery() {
    const q = new URLSearchParams(location.search);

    const form = document.querySelector(".hist-form");
    if (form) {
        ["desde", "hasta", "tipo", "zona", "estadoTratamiento"].forEach(k => {
            if (q.has(k) && form.elements[k]) {
                form.elements[k].value = q.get(k);
            }
        });
    }

    return readFiltersFromForm();
}

// ===== Boot =====
document.addEventListener("DOMContentLoaded", () => {
    console.log("[Historial] Inicializando...");
    console.log("[Historial] USER_ID:", window.USER_ID);

    setupFilters();

    const filtrosIniciales = initialFiltersFromQuery();
    cargarYRenderizarEventos(filtrosIniciales);
});