/* =========================================================================
 *  ADMIN — Clientes (ROLE_ADMIN)
 *  Gestión completa de información de clientes
 * ========================================================================= */
(() => {
    if (!document.getElementById('usuarios-clientes')) return;
    const { qs, pageInfo, confirmDialog, safeFetchJSON } = window.$common;

    const state = { page: 0, size: 10, q: "", sort: "idCliente,desc" };

    const $q = qs("#qCli");
    const $btnBuscar = qs("#btnCliBuscar");
    const $tbody = qs("#tbodyClientes");
    const $prev = qs("#cliPrev");
    const $next = qs("#cliNext");
    const $info = qs("#cliPageInfo");
    const $size = qs("#cliSize");
    const $formCrear = qs("#formCliCrear");

    // =====================================================
    //  MODAL EDICIÓN CLIENTE
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
        titulo.textContent = `Editar cliente #${datos.id}`;
        modalCampos.innerHTML = `
            <label>Nombre:</label>
            <input id="inputNombre" type="text" required minlength="2" value="${datos.nombre || ""}">
            
            <label>Apellidos:</label>
            <input id="inputApellidos" type="text" value="${datos.apellidos || ""}">
            
            <label>Correo electrónico:</label>
            <input id="inputCorreo" type="email" required value="${datos.correoElectronico || datos.email || ""}">
            
            <label>Teléfono:</label>
            <input id="inputTelefono" type="tel" pattern="[0-9]{9}" placeholder="9 dígitos" value="${datos.telefono || ""}">
            
            <label>Dirección:</label>
            <input id="inputDireccion" type="text" value="${datos.direccion || ""}">
            
            <label>Ciudad:</label>
            <input id="inputCiudad" type="text" value="${datos.ciudad_id || ""}">
            
            <label>Código Postal:</label>
            <input id="inputCP" type="text" pattern="[0-9]{5}" placeholder="5 dígitos" value="${datos.cp || datos.cp_id || ""}">
            
        `;

        // Mostrar modal
        modal.style.display = "flex";

        // Botón cancelar
        btnCancelar.onclick = () => modal.style.display = "none";

        // Guardar cambios
        form.onsubmit = async (e) => {
            e.preventDefault();

            const body = {
                nombre: modal.querySelector("#inputNombre").value.trim(),
                apellidos: modal.querySelector("#inputApellidos").value.trim(),
                correoElectronico: modal.querySelector("#inputCorreo").value.trim(),
                telefono: modal.querySelector("#inputTelefono").value.trim(),
                direccion: modal.querySelector("#inputDireccion").value.trim(),
                cp_id: modal.querySelector("#inputCP").value.trim(),
            };

            try {
                const res = await fetch(`/api/admin/clientes/${datos.id}`, {
                    method: "PATCH",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(body)
                });

                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                toast("✅ Cliente actualizado correctamente");
                modal.style.display = "none";
                load(); // refresca tabla
            } catch (err) {
                console.error(err);
                toast("❌ Error al actualizar cliente");
            }
        };
    }

    // =====================================================
    //  MODAL CREAR/ASOCIAR USUARIO
    // =====================================================
    function abrirModalUsuario(idCliente, nombreCliente) {
        const modal = document.getElementById("modalEditar");
        const modalCampos = modal.querySelector("#modalCampos");
        const form = modal.querySelector("#modalForm");
        const btnCancelar = modal.querySelector("#btnCancelar");
        const titulo = modal.querySelector("#modalTitulo");

        if (!modal || !modalCampos || !form) {
            console.error("⚠️ No se encontró el modal en el DOM.");
            return;
        }

        titulo.textContent = `Crear usuario para: ${nombreCliente}`;
        modalCampos.innerHTML = `
            <label>Username:</label>
            <input id="inputUsername" type="text" required minlength="3" placeholder="nombre de usuario único">
            
            <label>Contraseña:</label>
            <input id="inputPassword" type="password" required minlength="8" placeholder="mínimo 8 caracteres">
            
            <label>Rol:</label>
            <select id="inputRol" required>
                <option value="ROLE_USER">Usuario (Cliente)</option>
                <option value="ROLE_MEDICO">Médico</option>
            </select>
            
            <small class="muted">Este usuario permitirá al cliente iniciar sesión en el sistema.</small>
        `;

        modal.style.display = "flex";
        btnCancelar.onclick = () => modal.style.display = "none";

        form.onsubmit = async (e) => {
            e.preventDefault();

            const username = modal.querySelector("#inputUsername").value.trim();
            const password = modal.querySelector("#inputPassword").value.trim();
            const rol = modal.querySelector("#inputRol").value;

            try {
                const url = new URL(location.origin + `/api/admin/clientes/${idCliente}/crear-usuario`);
                url.searchParams.set("username", username);
                url.searchParams.set("password", password);
                url.searchParams.set("rol", rol);

                await safeFetchJSON(url, { method: "POST" });
                toast("✅ Usuario creado y asociado correctamente");
                modal.style.display = "none";
                load();
            } catch (err) {
                console.error(err);
                toast(`❌ Error al crear usuario: ${err.message}`);
            }
        };
    }

    // =====================================================
    //  CARGA Y RENDER DE CLIENTES
    // =====================================================
    async function load() {
        const url = new URL(location.origin + "/api/admin/clientes");
        url.searchParams.set("page", state.page);
        url.searchParams.set("size", state.size);
        if (state.sort) url.searchParams.set("sort", state.sort);
        if (state.q) url.searchParams.set("q", state.q);

        try {
            const page = await safeFetchJSON(url, { headers: { "Accept": "application/json" } });
            render(page);
        } catch (err) {
            console.error(err);
            $tbody.innerHTML = `<tr><td colspan="6">Error al cargar: ${err.message}</td></tr>`;
        }
    }


    function render(page) {
        if (!page.content || page.content.length === 0) {
            $tbody.innerHTML = `<tr><td colspan="6" class="muted">No hay clientes registrados</td></tr>`;
        } else {
            $tbody.innerHTML = page.content.map(c => {
                const id = c.idCliente ?? c.id_cliente ?? c.id;
                const nombre = c.nombre || "";
                const apellidos = c.apellidos || "";
                const nombreCompleto = `${nombre} ${apellidos}`.trim() || "—";
                const email = c.correoElectronico || c.email || "—";
                const telefono = c.telefono || "—";
                const usuario = c.usuarioUsername || c.username || "—";
                const tieneUsuario = usuario !== "—";

                return `
                <tr data-id="${id}">
                    <td>${id}</td>
                    <td>${nombreCompleto}</td>
                    <td>${email}</td>
                    <td>${telefono}</td>
                    <td>${usuario}</td>
                    <td style="display:flex; gap:6px; flex-wrap:wrap;">
                        <button class="edit" data-id="${id}">Editar</button>
                        ${!tieneUsuario ?
                    `<button class="crear-usuario" data-id="${id}" data-nombre="${nombreCompleto}">Crear Usuario</button>`
                    : ''}
                        <button class="danger del" data-id="${id}">🗑️</button>
                    </td>
                </tr>`;
            }).join("");
        }
        pageInfo($info, page);
        $prev.disabled = page.first;
        $next.disabled = page.last;
    }

    // =====================================================
    //  EVENTOS
    // =====================================================
    if ($btnBuscar) $btnBuscar.addEventListener("click", () => {
        state.q = ($q?.value || "").trim();
        state.page = 0;
        load();
    });

    if ($q) $q.addEventListener("keydown", e => {
        if (e.key === "Enter") {
            e.preventDefault();
            $btnBuscar?.click();
        }
    });

    if ($size) $size.addEventListener("change", e => {
        state.size = parseInt(e.target.value, 10);
        state.page = 0;
        load();
    });

    if ($prev) $prev.addEventListener("click", () => {
        state.page = Math.max(0, state.page - 1);
        load();
    });

    if ($next) $next.addEventListener("click", () => {
        state.page = state.page + 1;
        load();
    });

    // EVENTOS DE TABLA
    if ($tbody) $tbody.addEventListener("click", async (e) => {
        const btnDel = e.target.closest("button.del");
        const btnEdit = e.target.closest("button.edit");
        const btnCrearUsuario = e.target.closest("button.crear-usuario");

        // === ELIMINAR CLIENTE ===
        if (btnDel) {
            const id = btnDel.dataset.id;
            if (await confirmDialog(`¿Seguro que deseas eliminar el cliente #${id}?\n\nEsto eliminará también su usuario asociado si existe.`)) {
                try {
                    const res = await fetch(`/api/admin/clientes/${id}`, { method: "DELETE" });
                    if (!res.ok) throw new Error(`HTTP ${res.status}`);
                    toast("✅ Cliente eliminado correctamente");
                    load();
                } catch (err) {
                    console.error(err);
                    toast("❌ No se pudo eliminar el cliente (posibles datos vinculados)");
                }
            }
            return;
        }

        // === EDITAR CLIENTE ===
        if (btnEdit) {
            const id = btnEdit.dataset.id;
            try {
                const cliente = await safeFetchJSON(`/api/admin/clientes/${id}`, {
                    headers: { "Accept": "application/json" }
                });
                abrirModal(cliente);
            } catch (err) {
                console.error(err);
                toast("❌ Error al cargar datos del cliente");
            }
            return;
        }

        // === CREAR USUARIO ===
        if (btnCrearUsuario) {
            const id = btnCrearUsuario.dataset.id;
            const nombre = btnCrearUsuario.dataset.nombre;
            abrirModalUsuario(id, nombre);
            return;
        }
    });

    // === CREAR CLIENTE ===
    if ($formCrear) $formCrear.addEventListener("submit", async (e) => {
        e.preventDefault();
        const fd = new FormData($formCrear);
        const nombre = fd.get("nombre")?.trim();
        const email = fd.get("email")?.trim();

        if (!nombre || !email) {
            toast("❌ Nombre y email son obligatorios");
            return;
        }

        try {
            const body = {
                nombre: nombre,
                correoElectronico: email
            };

            const res = await fetch("/api/admin/clientes", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });

            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            toast("✅ Cliente creado correctamente");
            $formCrear.reset();
            state.page = 0;
            load();
        } catch (err) {
            console.error(err);
            toast(`❌ Error al crear cliente: ${err.message}`);
        }
    });

    // === CARGA DIFERIDA AL HACER SCROLL / VISUALIZAR ===
    const target = document.getElementById('usuarios-clientes');
    if (target) {
        const observer = new IntersectionObserver(entries => {
            entries.forEach(e => {
                if (e.isIntersecting) {
                    load();
                    observer.disconnect();
                }
            });
        }, { threshold: 0.2 });
        observer.observe(target);
    }

    // === TOAST NOTIFICACIONES ===
    function toast(msg) {
        const t = document.createElement('div');
        t.textContent = msg;
        Object.assign(t.style, {
            position: 'fixed',
            right: '16px',
            bottom: '16px',
            padding: '10px 14px',
            background: '#0a8',
            color: '#fff',
            borderRadius: '10px',
            boxShadow: '0 6px 18px rgba(0,0,0,.25)',
            zIndex: 9999
        });
        document.body.appendChild(t);
        setTimeout(() => t.remove(), 2200);
    }

    console.log("✅ admin-clientes.js cargado correctamente");
})();