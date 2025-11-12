/* =========================================================================
 *  ADMIN — Pacientes (ROLE_ADMIN)
 * ========================================================================= */
(() => {
    if (!document.getElementById('usuarios-pacientes')) return;
    const { qs, pageInfo, safeFetchJSON, confirmDialog } = window.$common;

    const state = { page: 0, size: 10, q: "" };
    const $q         = qs("#qPac");
    const $btnBuscar = qs("#btnPacBuscar");
    const $tbody     = qs("#tbodyPacientes");
    const $prev      = qs("#pacPrev");
    const $next      = qs("#pacNext");
    const $info      = qs("#pacPageInfo");
    const $size      = qs("#pacSize");

    let medicosCache = null;

    async function cargarMedicos() {
        if (medicosCache) return medicosCache;
        const url = new URL(location.origin + "/api/admin/medicos");
        url.searchParams.set("page", 0);
        url.searchParams.set("size", 100);
        try {
            const page = await safeFetchJSON(url, { headers: { "Accept": "application/json" } });
            medicosCache = (page.content || []).map(u => ({
                id: u.id ?? u.id_usuario,
                username: u.username
            }));
            return medicosCache;
        } catch (err) {
            console.error(err);
            return [];
        }
    }

    async function load() {
        const url = new URL(location.origin + "/api/admin/pacientes");
        url.searchParams.set("page", state.page);
        url.searchParams.set("size", state.size);
        if (state.q) url.searchParams.set("q", state.q);

        try {
            const page = await safeFetchJSON(url, { headers: { "Accept": "application/json" } });
            render(page);
        } catch (err) {
            console.error(err);
            $tbody.innerHTML = `<tr><td colspan="5">Error al cargar: ${err.message}</td></tr>`;
            $info.textContent = "—";
            $prev.disabled = $next.disabled = true;
        }
    }

    function render(page) {
        if (!page.content || page.content.length === 0) {
            $tbody.innerHTML = `<tr><td colspan="5" class="muted">No hay pacientes</td></tr>`;
        } else {
            $tbody.innerHTML = page.content.map(c => {
                const idC     = c.idCliente ?? c.id_cliente ?? c.id;
                const nombre  = `${c.nombre || ""} ${c.apellidos || ""}`.trim();
                const medName = c.medicoUsername || (c.medicoReferencia?.username ?? "—");
                const medId   = c.medicoId ?? c.medicoReferencia?.id ?? "";
                return `
          <tr data-idc="${idC}" data-medico-id="${medId}">
            <td>${idC}</td>
            <td>${nombre || "—"}</td>
            <td data-medcol>${medName || "—"}</td>
            <td style="width:280px;display: flex;justify-content: space-around;flex-direction: row;flex-wrap: wrap;">
                <button class="edit">Editar</button>
                <button class="asignar">Asignar/Reasignar</button>
                <button class="danger del">🗑️</button>
            </td>
          </tr>`;
            }).join("");
        }
        pageInfo($info, page);
        $prev.disabled = page.first;
        $next.disabled = page.last;
    }

    // ========== MODAL EDICIÓN ==========
    function abrirModal(datos) {
        const modal = document.getElementById("modalEditar");
        const modalCampos = modal.querySelector("#modalCampos");
        const form = modal.querySelector("#modalForm");
        const btnCancelar = modal.querySelector("#btnCancelar");
        const titulo = modal.querySelector("#modalTitulo");

        titulo.textContent = `Editar paciente #${datos.id}`;
        modalCampos.innerHTML = `
            <label>Nombre:</label>
            <input id="inputNombre" type="text" value="${datos.nombre || ""}">
            <label>Apellidos:</label>
            <input id="inputApellidos" type="text" value="${datos.apellidos || ""}">
            <label>Correo electrónico:</label>
            <input id="inputCorreo" type="email" value="${datos.correo || ""}">
            <label>Teléfono:</label>
            <input id="inputTelefono" type="text" value="${datos.telefono || ""}">
            <label>Dirección:</label>
            <input id="inputDireccion" type="text" value="${datos.direccion || ""}">
            <label>Código Postal:</label>
            <input id="inputCP" type="text" value="${datos.cp || ""}">
        `;
        modal.style.display = "flex";
        btnCancelar.onclick = () => modal.style.display = "none";

        form.onsubmit = async (e) => {
            e.preventDefault();
            const body = {
                nombre: qs("#inputNombre").value.trim(),
                apellidos: qs("#inputApellidos").value.trim(),
                correoElectronico: qs("#inputCorreo").value.trim(),
                telefono: qs("#inputTelefono").value.trim(),
                direccion: qs("#inputDireccion").value.trim(),
                cp_id: qs("#inputCP").value.trim()
            };
            try {
                const res = await fetch(`/api/admin/pacientes/${datos.id}`, {
                    method: "PATCH",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(body)
                });
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                toast("✅ Paciente actualizado correctamente");
                modal.style.display = "none";
                load();
            } catch (err) {
                console.error(err);
                toast("❌ Error al actualizar paciente");
            }
        };
    }

    // ===============================
    //  EVENTOS
    // ===============================
    if ($btnBuscar) $btnBuscar.addEventListener("click", () => { state.q = ($q?.value || "").trim(); state.page = 0; load(); });
    if ($q) $q.addEventListener("keydown", e => { if (e.key === "Enter") { e.preventDefault(); $btnBuscar?.click(); } });
    if ($size) $size.addEventListener("change", e => { state.size = parseInt(e.target.value, 10); state.page = 0; load(); });
    if ($prev) $prev.addEventListener("click", () => { state.page = Math.max(0, state.page - 1); load(); });
    if ($next) $next.addEventListener("click", () => { state.page = state.page + 1; load(); });

    if ($tbody) $tbody.addEventListener("click", async (e) => {
        const btnAsignar = e.target.closest("button.asignar");
        const btnEdit = e.target.closest("button.edit");
        const btnDel = e.target.closest("button.del");

        // === ELIMINAR PACIENTE ===
        if (btnDel) {
            const tr = btnDel.closest("tr");
            const idCliente = tr.dataset.idc;

            if (await confirmDialog(`¿Seguro que deseas eliminar al paciente #${idCliente}?`)) {
                try {
                    const res = await fetch(`/api/admin/pacientes/${idCliente}`, { method: "DELETE" });
                    if (!res.ok) throw new Error(`HTTP ${res.status}`);
                    toast("✅ Paciente eliminado correctamente");
                    load();
                } catch (err) {
                    console.error(err);
                    toast("❌ No se pudo eliminar el paciente (posibles datos vinculados)");
                }
            }
            return;
        }

        // === EDITAR PACIENTE ===
        if (btnEdit) {
            const tr = btnEdit.closest("tr");
            const idCliente = tr.dataset.idc;
            const nombreParts = tr.children[1].textContent.trim().split(" ");
            const datos = {
                id: idCliente,
                nombre: nombreParts[0] || "",
                apellidos: nombreParts.slice(1).join(" ") || "",
                correo: "",
                telefono: "",
                direccion: "",
                cp: ""
            };
            abrirModal(datos);
            return;
        }

        // === ASIGNAR MÉDICO ===
        if (!btnAsignar) return;
        const tr = btnAsignar.closest("tr");
        const idCliente = tr.dataset.idc;
        const medCol = tr.querySelector("[data-medcol]");

        const medicos = await cargarMedicos();
        if (!medicos.length) { alert("No hay médicos disponibles."); return; }

        let box = tr.querySelector(".inline-assign");
        if (box) box.remove();

        box = document.createElement("div");
        box.className = "inline-assign";
        box.style.display = "flex"; box.style.gap = "8px"; box.style.marginTop = "6px";

        const sel = document.createElement("select");
        sel.innerHTML = medicos.map(m => `<option value="${m.id}">${m.username}</option>`).join("");

        const actualId = tr.dataset.medicoId || "";
        if (actualId) sel.value = String(actualId);

        const ok = document.createElement("button"); ok.textContent = "Guardar";
        const cancel = document.createElement("button"); cancel.textContent = "Cancelar"; cancel.className = "muted";

        box.appendChild(sel); box.appendChild(ok); box.appendChild(cancel);

        const trAssign = document.createElement("tr");
        const td = document.createElement("td"); td.colSpan = 4; td.appendChild(box);
        trAssign.appendChild(td); tr.after(trAssign);

        cancel.onclick = () => trAssign.remove();

        ok.onclick = async () => {
            const idMed = sel.value;
            const token = document.querySelector('meta[name="_csrf"]')?.content;
            const header = document.querySelector('meta[name="_csrf_header"]')?.content;
            const headers = { "Accept": "application/json" };
            if (token && header) headers[header] = token;

            ok.disabled = true; ok.textContent = "Guardando…";

            try {
                const res = await fetch(`/api/admin/pacientes/${idCliente}/asignar-medico/${idMed}`, {
                    method: "PATCH",
                    headers
                });
                if (!res.ok) throw new Error(`HTTP ${res.status}`);

                let dto = null;
                try { dto = await res.json(); } catch (_) {}

                const name = dto?.medicoUsername ?? (medicos.find(m => String(m.id) === String(idMed))?.username) ?? "—";
                medCol.textContent = name;
                tr.dataset.medicoId = dto?.medicoId ?? idMed;

                toast("Asignación guardada ✅");
                trAssign.remove();
            } catch (err) {
                console.error(err);
                ok.disabled = false; ok.textContent = "Guardar";
                toast("No se pudo asignar el médico ❌");
            }
        };
    });

    // === CARGA DIFERIDA AL HACER SCROLL / VISUALIZAR ===
    const target = document.getElementById('usuarios-pacientes');
    if (target) {
        const observer = new IntersectionObserver(entries => {
            entries.forEach(e => { if (e.isIntersecting) { load(); observer.disconnect(); } });
        }, { threshold: 0.2 });
        observer.observe(target);
    }

    // === TOAST NOTIFICACIONES ===
    function toast(msg) {
        const t = document.createElement('div');
        t.textContent = msg;
        Object.assign(t.style, {
            position: 'fixed', right: '16px', bottom: '16px',
            padding: '10px 14px', background: '#0a8', color: '#fff',
            borderRadius: '10px', boxShadow: '0 6px 18px rgba(0,0,0,.25)', zIndex: 9999
        });
        document.body.appendChild(t);
        setTimeout(() => t.remove(), 2200);
    }
})();