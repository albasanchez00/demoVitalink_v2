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
    return d.toLocaleString();
}

// ===== Render =====
const lista = document.querySelector(".hist-lista");
function clearClientEvents() {
    lista?.querySelectorAll("li[data-js='true']").forEach(li => li.remove());
}
function liEvento({ fecha, tipo, titulo, descripcion, zona, intensidad, urlDetalle, urlEditar }) {
    const li = document.createElement("li");
    li.className = "hist-item";
    li.dataset.js = "true";

    const tipoChip =
        tipo === "SINTOMA" ? '<span class="chip chip-sintoma">Síntoma</span>' :
            tipo === "TRATAMIENTO" ? '<span class="chip chip-tratamiento">Tratamiento</span>' :
                '<span class="chip chip-cita">Cita</span>';

    li.innerHTML = `
    <div class="hist-fecha"><small>${fecha || "—"}</small></div>
    <div class="hist-tipo">${tipoChip}</div>
    <div class="hist-contenido">
      <h4>${titulo ?? ""}</h4>
      ${descripcion ? `<p>${descripcion}</p>` : ""}
      ${zona ? `<div class="hist-meta"><span><strong>Zona:</strong> ${zona}</span></div>` : ""}
      ${Number.isFinite(intensidad) ? `<div class="hist-meta"><span><strong>Intensidad:</strong> ${intensidad}</span></div>` : ""}
      <div class="hist-item-actions">
        ${[urlDetalle ? `<a class="link" href="${urlDetalle}">Ver</a>` : "", urlEditar ? `<a class="link" href="${urlEditar}">Editar</a>` : ""]
        .filter(Boolean).join('<span> · </span>')}
      </div>
    </div>`;
    return li;
}

// ===== Adapters =====
function mapSintoma(s) {
    const dtRaw = s.fechaRegistro || s.fecha || s.createdAt || s.fechaSintoma || s.fechaHora || null;
    const d = parseDateFlexible(dtRaw);
    return {
        fechaISO: d ? d.toISOString() : null,
        fecha: formatDateLocal(d),
        tipo: "SINTOMA",
        titulo: (s.zona && s.zona.toString().trim().toUpperCase()) || s.titulo || "Síntoma",
        descripcion: s.descripcion || s.detalle || "",
        zona: s.zona || null,
        intensidad: Number.isFinite(s.intensidad) ? s.intensidad : null,
        urlDetalle: s.id ? `/sintomas/${s.id}` : null,
        urlEditar: s.id ? `/sintomas/${s.id}/editar` : null,
    };
}
function mapCita(c) {
    const d =
        parseDateFlexible(c.fechaHora) ||
        parseDateFlexible(c.fechaCita, c.horaCita) ||
        parseDateFlexible(c.fecha) || null;
    const desc = [c.descripcion || c.detalle || "", c.especialista ? `Con ${c.especialista}` : (c.lugar || c.ubicacion || "")]
        .filter(Boolean).join(" · ");
    return {
        fechaISO: d ? d.toISOString() : null,
        fecha: formatDateLocal(d),
        tipo: "CITA",
        titulo: c.titulo || c.motivo || "Cita",
        descripcion: desc || "",
        zona: null,
        intensidad: null,
        urlDetalle: c.id ? `/citas/${c.id}` : null,
        urlEditar: c.id ? `/citas/${c.id}/editar` : null,
    };
}
function mapTratamiento(t) {
    const d = parseDateFlexible(t.fechaInicio) || parseDateFlexible(t.createdAt) || parseDateFlexible(t.fechaAlta) || null;
    const desc = [t.dosis, t.frecuencia, t.formula, t.observaciones].filter(Boolean).join(" · ");
    return {
        fechaISO: d ? d.toISOString() : null,
        fecha: formatDateLocal(d),
        tipo: "TRATAMIENTO",
        titulo: `${t.nombre || t.titulo || "Tratamiento"}${t.estado ? ` · ${t.estado}` : ""}`,
        descripcion: desc,
        zona: null,
        intensidad: null,
        urlDetalle: t.id ? `/tratamientos/${t.id}` : null,
        urlEditar: t.id ? `/tratamientos/${t.id}/editar` : null,
    };
}

// ===== API - ACTUALIZADO para soportar userId =====
/**
 * Obtiene las URLs de API según el userId
 * Si userId existe y es válido, usa las rutas del médico con el ID del paciente
 * Si no, usa las rutas "/mios" (del usuario autenticado)
 */
function getAPIUrls() {
    // Leer userId del window.USER_ID (inyectado por Thymeleaf)
    const userId = window.USER_ID;

    // Si hay un userId válido (y no es 0), usar las rutas del médico
    if (userId && userId !== 0) {
        console.log(`[Historial] Cargando datos para userId: ${userId}`);
        return {
            sintomas: `/api/medico/sintomas/${userId}`,
            citas: `/api/citas?userId=${userId}`, // Ajusta según tu API de citas
            tratamientos: `/api/tratamientos?userId=${userId}`, // Ajusta según tu API de tratamientos
        };
    } else {
        // Sin userId válido, usar las rutas del usuario autenticado
        console.log("[Historial] Cargando datos del usuario autenticado (/mios)");
        return {
            sintomas: "/api/sintomas/mios",
            citas: "/api/citas",
            tratamientos: "/api/tratamientos/mios",
        };
    }
}

async function getJSON(url) {
    const res = await fetch(url, { headers: { Accept: "application/json" }, credentials: "same-origin" });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
}

// ===== Filtros =====
function readFiltersFromForm() {
    const f = new FormData(document.querySelector(".hist-form"));
    const tipo = (f.get("tipo") || "").toUpperCase(); // "", "SINTOMA", "TRATAMIENTO", "CITA"
    const desde = f.get("desde") ? parseDateFlexible(f.get("desde")) : null; // yyyy-MM-dd del input date (depende del navegador)
    const hasta = f.get("hasta") ? parseDateFlexible(f.get("hasta")) : null;
    const zona  = (f.get("zona") || "").trim().toLowerCase();
    const intensidad = f.get("intensidad") ? Number(f.get("intensidad")) : null;
    const estadoTratamiento = (f.get("estadoTratamiento") || "").toUpperCase();

    // normaliza rango [desde..hasta] para incluir todo el día de "hasta"
    if (hasta) hasta.setHours(23,59,59,999);

    return { tipo, desde, hasta, zona, intensidad, estadoTratamiento };
}

function passesFilters(ev, F) {
    // tipo
    if (F.tipo && ev.tipo !== F.tipo) return false;

    // rango de fechas
    if (F.desde || F.hasta) {
        const d = ev.fechaISO ? new Date(ev.fechaISO) : null;
        if (!d || isNaN(d)) return false;
        if (F.desde && d < F.desde) return false;
        if (F.hasta && d > F.hasta) return false;
    }

    // zona (solo síntomas): contiene texto
    if (F.zona) {
        const z = (ev.zona || "").toString().toLowerCase();
        if (!z.includes(F.zona)) return false;
    }

    // intensidad exacta (si se pide y el evento es síntoma)
    if (Number.isFinite(F.intensidad)) {
        if (!Number.isFinite(ev.intensidad) || ev.intensidad !== F.intensidad) return false;
    }

    // estado tratamiento (si se pide y el evento es tratamiento: buscamos en el título " · ESTADO" o en desc)
    if (F.estadoTratamiento && ev.tipo === "TRATAMIENTO") {
        const blob = `${ev.titulo} ${ev.descripcion}`.toUpperCase();
        if (!blob.includes(F.estadoTratamiento)) return false;
    }

    return true;
}

// ===== Carga + render considerando filtros =====
async function cargarYRenderizarEventos(filtros) {
    try {
        // CAMBIO CRÍTICO: Obtener las URLs dinámicamente según el userId
        const API = getAPIUrls();

        const [sintomas, citas, tratamientos] = await Promise.allSettled([
            getJSON(API.sintomas), getJSON(API.citas), getJSON(API.tratamientos)
        ]);

        const events = [];

        // Procesar síntomas
        if (sintomas.status === "fulfilled") {
            // Puede ser un array directo o un objeto Page con content
            const sintomasData = Array.isArray(sintomas.value)
                ? sintomas.value
                : (sintomas.value.content || []);
            events.push(...sintomasData.map(mapSintoma));
        }

        // Procesar citas
        if (citas.status === "fulfilled") {
            const citasData = Array.isArray(citas.value)
                ? citas.value
                : (citas.value.content || []);
            events.push(...citasData.map(mapCita));
        }

        // Procesar tratamientos
        if (tratamientos.status === "fulfilled") {
            const tratamientosData = Array.isArray(tratamientos.value)
                ? tratamientos.value
                : (tratamientos.value.content || []);
            events.push(...tratamientosData.map(mapTratamiento));
        }

        // aplica filtros del formulario (si hay)
        const filtered = filtros ? events.filter(e => passesFilters(e, filtros)) : events;

        // ordena
        filtered.sort((a, b) => new Date(b.fechaISO || 0) - new Date(a.fechaISO || 0));

        // pinta
        clearClientEvents();
        const frag = document.createDocumentFragment();
        filtered.forEach(ev => frag.appendChild(liEvento(ev)));
        lista?.appendChild(frag);

        const emptyMsg = document.querySelector(".text_center");
        if (emptyMsg) emptyMsg.style.display = filtered.length > 0 ? "none" : "";
    } catch (e) {
        console.error("[Historial] Error pintando timeline:", e);
    }
}

// ===== Hook del formulario =====
function setupFilters() {
    const form = document.querySelector(".hist-form");
    if (!form) return;

    form.addEventListener("submit", (ev) => {
        ev.preventDefault(); // no recargar la página
        const filtros = readFiltersFromForm();

        // actualizar la querystring para que se vea reflejado en la URL
        const params = new URLSearchParams(new FormData(form));
        history.replaceState(null, "", `${location.pathname}?${params.toString()}`);

        cargarYRenderizarEventos(filtros);
    });
}

// ===== Carga inicial: respeta querystring si hay =====
function initialFiltersFromQuery() {
    const q = new URLSearchParams(location.search);
    if ([...q.keys()].length === 0) return null;

    // sincroniza valores en los inputs si han llegado por URL
    const form = document.querySelector(".hist-form");
    if (form) {
        ["desde","hasta","tipo","zona","intensidad","estadoTratamiento"].forEach(k => {
            if (q.has(k) && form.elements[k]) form.elements[k].value = q.get(k);
        });
    }
    return readFiltersFromForm();
}

// Boot
document.addEventListener("DOMContentLoaded", () => {
    setupFilters();

    // si hay filtros en la URL, cárgalos; si no, carga todo
    const filtrosIniciales = initialFiltersFromQuery();
    cargarYRenderizarEventos(filtrosIniciales);
});