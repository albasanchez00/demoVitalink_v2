(() => {
    const $ = s => document.querySelector(s);
    const tbody = $("#tbodyTrat");
    const q = $("#q"), estado = $("#estado");
    const btnBuscar = $("#btnBuscar");
    const prev = $("#prev"), next = $("#next"), pageInfo = $("#pageInfo");
    const sizeSel = $("#size"), orden = $("#orden");

    // Autocomplete usuario
    const usuarioInput = $("#usuarioInput");
    const idUsuario = $("#idUsuario");
    const usuarioSugg = $("#usuarioSugg");
    const usuarioClear = $("#usuarioClear");

    const state = { page: 0, size: parseInt(sizeSel.value, 10), sort: orden.value };

    // === Navegación ===
    btnBuscar.addEventListener("click", () => { state.page = 0; cargar(); });
    sizeSel.addEventListener("change", () => { state.size = parseInt(sizeSel.value, 10); state.page = 0; cargar(); });
    orden.addEventListener("change", () => { state.sort = orden.value; state.page = 0; cargar(); });
    prev.addEventListener("click", () => { if (state.page > 0) { state.page--; cargar(); } });
    next.addEventListener("click", () => { state.page++; cargar(); });

    const fmt = d => d ? new Date(d).toLocaleDateString('es-ES') : "—";

    // === Autocomplete ===
    let acTimer = null;
    usuarioInput.addEventListener("input", () => {
        idUsuario.value = "";
        if (acTimer) clearTimeout(acTimer);
        const term = usuarioInput.value.trim();
        if (!term) { hideSugg(); return; }
        acTimer = setTimeout(() => fetchUsuarios(term), 180);
    });

    usuarioClear.addEventListener("click", () => {
        usuarioInput.value = "";
        idUsuario.value = "";
        hideSugg();
    });

    document.addEventListener("click", (e) => {
        if (!e.target.closest(".usuario-autocomplete")) hideSugg();
    });

    async function fetchUsuarios(term) {
        const url = new URL(location.origin + "/api/admin/usuarios");
        url.searchParams.set("q", term);
        url.searchParams.set("size", 8);
        const res = await fetch(url, { headers: { "Accept": "application/json" } });
        if (!res.ok) { hideSugg(); return; }
        const page = await res.json();
        renderSugg(page.content || []);
    }

    function renderSugg(items) {
        if (!items.length) { hideSugg(); return; }
        usuarioSugg.innerHTML = items.map(u => `
          <li data-id="${u.id}" data-label="${escapeHtml(u.display)}"
              style="padding:8px 10px;cursor:pointer;border-bottom:1px solid #f2f4f7;">
            ${escapeHtml(u.display)}
          </li>
        `).join("");
        usuarioSugg.style.display = "block";
        usuarioSugg.querySelectorAll("li").forEach(li => {
            li.addEventListener("click", () => {
                idUsuario.value = li.dataset.id;
                usuarioInput.value = li.dataset.label.replace(/ — #\d+$/, '');
                hideSugg();
                state.page = 0;
                cargar();
            });
        });
    }

    function hideSugg() { usuarioSugg.style.display = "none"; usuarioSugg.innerHTML = ""; }
    function escapeHtml(s) { return s.replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m])); }

    // === Cargar tabla ===
    async function cargar() {
        const url = new URL(location.origin + "/api/admin/tratamientos");
        url.searchParams.set("page", state.page);
        url.searchParams.set("size", state.size);
        url.searchParams.set("sort", state.sort);
        if (q.value.trim()) url.searchParams.set("q", q.value.trim());
        if (estado.value) url.searchParams.set("estado", estado.value);
        if (idUsuario.value) url.searchParams.set("idUsuario", idUsuario.value);

        const res = await fetch(url, { headers: { "Accept": "application/json" } });
        if (!res.ok) { console.error("Error", res.status); return; }
        const page = await res.json();

        function estadoClass(e) {
            const v = (e ?? '').toString().toLowerCase();
            if (v === 'activo') return 'pill--activo';
            if (v === 'finalizado') return 'pill--finalizado';
            return 'pill--inactivo';
        }

        tbody.innerHTML = page.content.map(t => `
          <tr>
            <td>${t.id}</td>
            <td>${t.usuarioNombre} (${t.usuarioId ? '#' + t.usuarioId : '—'})</td>
            <td>${t.nombreTratamiento ?? "—"}</td>
            <td><span class="pill-estado ${estadoClass(t.estado)}">${t.estado ?? "—"}</span></td>
            <td>${fmt(t.fechaInicio)}</td>
            <td>${fmt(t.fechaFin)}</td>
            <td class="acciones">
              <button data-id="${t.id}" class="btn-sec ver">Ver</button>
              <button data-id="${t.id}" class="btn-sec editar">Editar</button>
              <button data-id="${t.id}" class="btn-sec success finalizar">Finalizar</button>
              <button data-id="${t.id}" class="btn-sec danger eliminar">Eliminar</button>
            </td>
          </tr>
        `).join("");

        pageInfo.textContent = `Página ${page.number + 1} de ${page.totalPages} • ${page.totalElements} registros`;
        prev.disabled = page.first;
        next.disabled = page.last;

        tbody.querySelectorAll(".ver").forEach(b => b.addEventListener("click", () => ver(+b.dataset.id)));
        tbody.querySelectorAll(".editar").forEach(b => b.addEventListener("click", () => editar(+b.dataset.id)));
        tbody.querySelectorAll(".finalizar").forEach(b => b.addEventListener("click", () => finalizar(+b.dataset.id)));
        tbody.querySelectorAll(".eliminar").forEach(b => b.addEventListener("click", () => eliminar(+b.dataset.id)));
    }

    // === Acciones ===
    async function ver(id) {
        const res = await fetch(`/api/admin/tratamientos/${id}`, { headers: { "Accept": "application/json" } });
        if (!res.ok) return alert("No se pudo cargar el tratamiento");
        const t = await res.json();

        const estadoClass =
            t.estado?.toLowerCase() === "activo" ? "pill--activo" :
                t.estado?.toLowerCase() === "finalizado" ? "pill--finalizado" :
                    "pill--inactivo";

        const contenido = document.createElement("div");
        contenido.innerHTML = `
        <div class="ver-modal">
            <p><span class="emoji">🩺</span> <strong>Tratamiento #${t.id}</strong></p>
            <p><span class="emoji">👤</span> <strong>Usuario:</strong> ${t.usuarioNombre || "—"}</p>
            <p><span class="emoji">💊</span> <strong>Tratamiento:</strong> ${t.nombreTratamiento || "—"}</p>
            <p><span class="emoji">📅</span> <strong>Inicio:</strong> ${fmt(t.fechaInicio)}</p>
            <p><span class="emoji">📆</span> <strong>Fin:</strong> ${fmt(t.fechaFin)}</p>
            <p><span class="emoji">📊</span> <strong>Estado:</strong> 
                <span class="pill-estado ${estadoClass}">${t.estado || "—"}</span>
            </p>
            ${t.diagnostico ? `<p><span class="emoji">🧠</span> <strong>Diagnóstico:</strong> ${t.diagnostico}</p>` : ""}
        </div>
    `;

        const ok = await confirmDialog("Detalles del tratamiento", contenido);
        if (ok) cargar(); // opcional, refresca si se cierra con “Aceptar”
    }


    async function editar(id) {
        const res = await fetch(`/api/admin/tratamientos/${id}`, { headers: { "Accept": "application/json" } });
        if (!res.ok) return alert("No se pudo cargar el tratamiento");
        const t = await res.json();

        const formHtml = `
      <div style="display:flex;flex-direction:column;gap:.5rem;">
        <label>Nombre:</label>
        <input id="editNombre" value="${t.nombreTratamiento || ""}">
        <label>Diagnóstico:</label>
        <input id="editDiag" value="${t.diagnostico || ""}">
        <label>Estado:</label>
        <select id="editEstado">
          <option value="Activo" ${t.estado === "Activo" ? "selected" : ""}>Activo</option>
          <option value="Finalizado" ${t.estado === "Finalizado" ? "selected" : ""}>Finalizado</option>
          <option value="Inactivo" ${t.estado === "Inactivo" ? "selected" : ""}>Inactivo</option>
        </select>
        <label>Fecha inicio:</label>
        <input id="editInicio" type="date" value="${t.fechaInicio ? new Date(t.fechaInicio).toISOString().slice(0,10) : ""}">
        <label>Fecha fin:</label>
        <input id="editFin" type="date" value="${t.fechaFin ? new Date(t.fechaFin).toISOString().slice(0,10) : ""}">
      </div>
    `;

        const wrapper = document.createElement("div");
        wrapper.innerHTML = formHtml;
        const ok = confirmDialog("Editar tratamiento", wrapper);
        if (!ok) return;

        const body = {
            nombre_tratamiento: wrapper.querySelector("#editNombre").value.trim(),
            diagnostico: wrapper.querySelector("#editDiag").value.trim(),
            estado_tratamiento: wrapper.querySelector("#editEstado").value,
            fecha_inicio: wrapper.querySelector("#editInicio").value || null,
            fecha_fin: wrapper.querySelector("#editFin").value || null
        };

        const put = await fetch(`/api/admin/tratamientos/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
        });
        if (!put.ok) return alert("Error al actualizar tratamiento");
        cargar();
    }

    // Diálogo genérico reutilizable
    function confirmDialog(titulo, contenidoNode) {
        const modal = document.createElement("dialog");
        modal.classList.add("modal-gen");
        modal.innerHTML = `
      <div class="wrap-modal">
        <form method="dialog">
          <h3>${titulo}</h3>
          <div class="contenido"></div>
          <menu>
            <button value="cancel">Cancelar</button>
            <button value="ok" class="btn-primario">Guardar</button>
          </menu>
        </form>
      </div>
    `;
        modal.querySelector(".contenido").appendChild(contenidoNode);
        document.body.appendChild(modal);
        modal.showModal();
        return new Promise(resolve => {
            modal.addEventListener("close", () => {
                const ok = modal.returnValue === "ok";
                modal.remove();
                resolve(ok);
            });
        });
    }


    async function finalizar(id) {
        if (!confirm("¿Finalizar este tratamiento?")) return;
        const res = await fetch(`/api/admin/tratamientos/${id}/finalizar`, { method: "PATCH" });
        if (!res.ok) return alert("Error al finalizar tratamiento");
        cargar();
    }

    async function eliminar(id) {
        if (!confirm("⚠️ ¿Eliminar este tratamiento definitivamente?")) return;
        const res = await fetch(`/api/admin/tratamientos/${id}`, { method: "DELETE" });
        if (!res.ok) return alert("No se pudo eliminar el tratamiento");
        cargar();
    }

    cargar();
})();