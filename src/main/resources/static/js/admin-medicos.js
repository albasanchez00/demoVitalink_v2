/* =========================================================================
 *  ADMIN — Médicos (ROLE_ADMIN)
 * ========================================================================= */
(() => {
    if (!document.getElementById('usuarios-medicos')) return;
    const { qs, pageInfo, confirmDialog, safeFetchJSON } = window.$common;

    const state = { page: 0, size: 10, q: "", sort: "id_usuario,desc" };

    const $q = qs("#qMed");
    const $btnBuscar = qs("#btnMedBuscar");
    const $tbody = qs("#tbodyMedicos");
    const $prev = qs("#medPrev");
    const $next = qs("#medNext");
    const $info = qs("#medPageInfo");
    const $size = qs("#medSize");
    const $formCrear = qs("#formMedCrear");

    // =====================================================
    //  MODAL EDICIÓN MÉDICO
    // =====================================================
    function abrirModal(datos) {
        const modal = document.getElementById("modalEditar");
        const modalCampos = modal.querySelector("#modalCampos");
        const form = modal.querySelector("#modalForm");
        const btnCancelar = modal.querySelector("#btnCancelar");
        const titulo = modal.querySelector("#modalTitulo");

        if (!modal || !modalCampos || !form) {
            console.error("⚠️ No se encontró el modal en el DOM.");
            return;
        }

        // Rellenar dinámicamente el contenido del modal
        titulo.textContent = `Editar médico #${datos.id}`;
        modalCampos.innerHTML = `
            <label>Nombre de usuario:</label>
            <input id="inputUsername" type="text" required minlength="3" value="${datos.username || ""}">
            <label>Nueva contraseña (opcional):</label>
            <input id="inputPassword" type="password" placeholder="••••••••">
        `;

        // Mostrar modal
        modal.style.display = "flex";

        // Botón cancelar
        btnCancelar.onclick = () => modal.style.display = "none";

        // Guardar cambios
        form.onsubmit = async (e) => {
            e.preventDefault();

            const username = modal.querySelector("#inputUsername").value.trim();
            const password = modal.querySelector("#inputPassword").value.trim() || null;

            try {
                const res = await fetch(`/api/admin/medicos/${datos.id}`, {
                    method: "PATCH",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ username, password })
                });

                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                toast("✅ Médico actualizado correctamente");
                modal.style.display = "none";
                load(); // refresca tabla
            } catch (err) {
                console.error(err);
                toast("❌ Error al actualizar médico");
            }
        };
    }

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

    // =====================================================
    //  CARGA Y RENDER DE MÉDICOS
    // =====================================================
    async function load() {
        const url = new URL(location.origin + "/api/admin/medicos");
        url.searchParams.set("page", state.page);
        url.searchParams.set("size", state.size);
        if (state.sort) url.searchParams.set("sort", state.sort);
        if (state.q) url.searchParams.set("q", state.q);

        try {
            const page = await safeFetchJSON(url, { headers: { "Accept": "application/json" } });
            render(page);
        } catch (err) {
            console.error(err);
            $tbody.innerHTML = `<tr><td colspan="4">Error al cargar: ${err.message}</td></tr>`;
            $info.textContent = "—";
            $prev.disabled = $next.disabled = true;
        }
    }

    function render(page) {
        if (!page.content || page.content.length === 0) {
            $tbody.innerHTML = `<tr><td colspan="4" class="muted">No hay médicos</td></tr>`;
        } else {
            $tbody.innerHTML = page.content.map(u => `
                <tr>
                    <td>${u.id ?? u.id_usuario ?? ""}</td>
                    <td>${u.username}</td>
                    <td style="width:160px;">
                        <button class="edit" data-id="${u.id ?? u.id_usuario}" data-username="${u.username}">Editar</button>
                        <button class="danger del" data-id="${u.id ?? u.id_usuario}">Eliminar</button>
                    </td>
                </tr>
            `).join("");
        }
        pageInfo($info, page);
        $prev.disabled = page.first;
        $next.disabled = page.last;
    }

    // =====================================================
    //  EVENTOS
    // =====================================================
    if ($btnBuscar) $btnBuscar.addEventListener("click", () => { state.q = ($q?.value || "").trim(); state.page = 0; load(); });
    if ($q) $q.addEventListener("keydown", e => { if (e.key === "Enter") { e.preventDefault(); $btnBuscar?.click(); } });
    if ($size) $size.addEventListener("change", e => { state.size = parseInt(e.target.value, 10); state.page = 0; load(); });
    if ($prev) $prev.addEventListener("click", () => { state.page = Math.max(0, state.page - 1); load(); });
    if ($next) $next.addEventListener("click", () => { state.page = state.page + 1; load(); });

    if ($tbody) $tbody.addEventListener("click", async (e) => {
        const btnDel = e.target.closest("button.del");
        const btnEdit = e.target.closest("button.edit");
        if (btnDel) {
            const id = btnDel.dataset.id;
            if (await confirmDialog("¿Eliminar este médico?")) {
                const res = await fetch(`/api/admin/medicos/${id}`, { method: "DELETE" });
                if (res.ok) load(); else alert("No se pudo eliminar (FK o error).");
            }
        } else if (btnEdit) {
            const id = btnEdit.dataset.id;
            const username = btnEdit.dataset.username;
            abrirModal({ id, username });
        }
    });

    if ($formCrear) $formCrear.addEventListener("submit", async (e) => {
        e.preventDefault();
        const fd = new FormData($formCrear);
        const username = fd.get("username")?.trim();
        const password = fd.get("password")?.trim();
        if (!username || !password) return;

        const url = new URL(location.origin + "/api/admin/medicos");
        url.searchParams.set("username", username);
        url.searchParams.set("password", password);

        try {
            await safeFetchJSON(url, { method: "POST" });
            $formCrear.reset();
            state.page = 0;
            load();
        } catch (err) {
            alert("Error creando médico: " + err.message);
        }
    });

    // Carga inicial defer hasta que sea visible
    const target = document.getElementById('usuarios-medicos');
    if (target) {
        const observer = new IntersectionObserver(entries => {
            entries.forEach(e => { if (e.isIntersecting) { load(); observer.disconnect(); } });
        }, { threshold: 0.2 });
        observer.observe(target);
    }
})();