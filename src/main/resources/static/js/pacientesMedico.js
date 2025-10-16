(() => {
    const $ = (sel) => document.querySelector(sel);
    const tbody = $("#tbodyPacientes");
    const q = $("#q");
    const estado = $("#estado");
    const orden = $("#orden");
    const btnBuscar = $("#btnBuscar");
    const prev = $("#prev");
    const next = $("#next");
    const pageInfo = $("#pageInfo");
    const sizeSel = $("#size");
    const countPage = $("#countPage");
    const countTotal = $("#countTotal");

    const state = {
        page: 0,
        size: parseInt(sizeSel.value, 10),
        sort: orden.value,
        q: "",
        estado: ""
    };

    function toSafeIso(val) {
        // Si viene "YYYY-MM-DD", lo pasamos a "YYYY-MM-DDT00:00:00" para que Date no devuelva NaN.
        if (!val) return null;
        if (/^\d{4}-\d{2}-\d{2}$/.test(val)) return `${val}T00:00:00`;
        return val;
    }

    function fmtDate(iso) {
        if (!iso) return "—";
        const d = new Date(toSafeIso(iso));
        if (isNaN(d.getTime())) return "—";
        return d.toLocaleString("es-ES", { dateStyle: "medium", timeStyle: "short" });
    }

    function badge(estado) {
        const cls = (estado || "").toLowerCase() === "activo" ? "activo" : "inactivo";
        return `<span class="badge ${cls}">${estado || "—"}</span>`;
    }

    function renderRows(items) {
        if (!items?.length) {
            tbody.innerHTML = `<tr><td colspan="7">No se han encontrado clientes con los filtros aplicados.</td></tr>`;
            return;
        }
        tbody.innerHTML = items.map(p => `
      <tr>
        <td>${p.nombre ?? ""} ${p.apellidos ?? ""}</td>
        <td>${p.numeroTarjetaSanitaria ?? "—"}</td>
        <td>
          <div>${p.correo ?? "—"}</div>
          <div>${p.telefono ?? ""}</div>
        </td>
        <td>${p.edad ?? "—"}</td>
        <td>${badge(p.estado)}</td>
        <td>${fmtDate(p.ultimaConsulta)}</td>
        <td style="text-align:right;">
          <div class="acciones">
            <button class="btn" data-action="perfil" data-id="${p.id}">Ver perfil</button>
            <button class="btn" data-action="historial" data-id="${p.id}">Historial</button>
            <button class="btn primary" data-action="tratamientos" data-id="${p.id}">Tratamientos</button>
          </div>
        </td>
      </tr>
    `).join("");
    }

    function attachActions() {
        tbody.addEventListener("click", (e) => {
            const btn = e.target.closest("button[data-action]");
            if (!btn) return;
            const id = btn.getAttribute("data-id");
            const action = btn.getAttribute("data-action");
            if (action === "perfil") window.location.href = `/medico/clientes/${id}`;
            if (action === "historial") window.location.href = `/medico/clientes/${id}/historial`;
            if (action === "tratamientos") window.location.href = `/medico/clientes/${id}/tratamientos`;
        });
    }

    async function fetchClientes() {
        const params = new URLSearchParams({
            page: state.page,
            size: state.size,
            sort: state.sort
        });
        if (state.q) params.append("q", state.q);
        if (state.estado) params.append("estado", state.estado);

        try {
            const url = `/api/medico/clientes?${params.toString()}`;
            const resp = await fetch(url, { headers: { "Accept": "application/json" } });

            if (resp.status === 401 || resp.status === 403) {
                window.location.href = "/inicioSesion";
                return;
            }
            if (!resp.ok) {
                const txt = await resp.text();
                console.error("Error fetch clientes:", resp.status, txt);
                tbody.innerHTML = `<tr><td colspan="7">Se produjo un error al cargar los clientes (código ${resp.status}).</td></tr>`;
                return;
            }

            const data = await resp.json();
            console.log("[/api/medico/clientes] OK:", data);

            // Badge total (si existe en el HTML)
            const badgeTotal = document.querySelector("#badgeTotal");
            if (badgeTotal) badgeTotal.textContent = data.totalElements ?? 0;

            // Normalizamos las claves que usa la tabla
            const items = (data.content || []).map(c => ({
                id: c.clienteId,
                nombre: c.nombre,
                apellidos: c.apellidos,
                numeroTarjetaSanitaria: c.numeroTarjetaSanitaria,
                telefono: c.telefono,
                correo: c.correoElectronico,
                edad: c.edad,
                estado: c.estado,
                ultimaConsulta: c.ultimaConsulta
            }));

            renderRows(items);
            pageInfo.textContent = `${(data.pageable?.pageNumber ?? state.page) + 1} / ${data.totalPages || 1}`;
            countPage.textContent = `${data.numberOfElements ?? 0}`;
            countTotal.textContent = `${data.totalElements ?? 0}`;
            prev.disabled = (state.page <= 0);
            next.disabled = (state.page >= ((data.totalPages ?? 1) - 1));
        } catch (err) {
            console.error(err);
            tbody.innerHTML = `<tr><td colspan="7">No se pudo conectar con el servidor.</td></tr>`;
        }
    }

    // Listeners
    btnBuscar.addEventListener("click", () => {
        state.q = q.value.trim();
        state.estado = estado.value;
        state.sort = orden.value;
        state.page = 0;
        fetchClientes();
    });

    prev.addEventListener("click", () => {
        if (state.page > 0) { state.page -= 1; fetchClientes(); }
    });
    next.addEventListener("click", () => {
        state.page += 1;
        fetchClientes();
    });
    sizeSel.addEventListener("change", () => {
        state.size = parseInt(sizeSel.value, 10);
        state.page = 0;
        fetchClientes();
    });

    // Enter para buscar
    q.addEventListener("keydown", (e) => { if (e.key === "Enter") btnBuscar.click(); });

    // Init
    attachActions();
    fetchClientes();
})();
