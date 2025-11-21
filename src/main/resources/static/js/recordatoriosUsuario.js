document.addEventListener("DOMContentLoaded", () => {
    // ====== Usuario ID ======
    const getIdUsuario = () => {
        const fromData = document.body?.dataset?.id_usuario;
        if (fromData) return parseInt(fromData, 10);
        const el = document.getElementById("id_usuario");
        if (el && el.value) return parseInt(el.value, 10);
        console.warn("⚠️ No se encontró id_usuario en el DOM.");
        return NaN;
    };

    // ====== DOM ======
    const form = document.getElementById("form-recordatorio");
    const lista = document.getElementById("lista-recordatorios");
    const resumen = document.getElementById("resumen");
    const btnLimpiar = document.getElementById("btn-limpiar");
    const selectVinculo = document.getElementById("vinculo");
    const chips = Array.from(document.querySelectorAll(".chip-filter"));

    let filter = "ALL";
    let cache = [];

    // ====== Helpers ======
    // ✅ Validar que fecha y hora existan
    const toISO = (fecha, hora) => {
        if (!fecha || !hora) {
            throw new Error("Fecha y hora son obligatorias");
        }
        return `${fecha}T${hora}:00`;
    };

    const parseISOToInputs = (iso) => {
        const d = new Date(iso);
        const pad = (n) => String(n).padStart(2, "0");
        return {
            fecha: `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`,
            hora: `${pad(d.getHours())}:${pad(d.getMinutes())}`,
        };
    };

    const fmtDate = (iso) =>
        new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" })
            .format(new Date(iso));

    const escapeHtml = (s = "") =>
        s.replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");

    // ====== API ======
    async function apiListar(id_usuario, params = {}) {
        const q = new URLSearchParams({ id_usuario, ...params }).toString();
        const res = await fetch(`/api/recordatorios?${q}`, { credentials: "same-origin" });
        if (!res.ok) throw new Error("Error listando");
        return res.json();
    }

    async function apiCrear(dto) {
        console.log('📤 POST /api/recordatorios:', dto);

        const res = await fetch(`/api/recordatorios`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "same-origin",
            body: JSON.stringify(dto),
        });

        const responseText = await res.text();

        if (!res.ok) {
            console.error('❌ Error del servidor (status ' + res.status + '):', responseText);
            throw new Error("Error creando: " + responseText);
        }

        return JSON.parse(responseText);
    }

    async function apiActualizar(id_recordatorio, dto) {
        console.log('📤 PUT /api/recordatorios/' + id_recordatorio + ':', dto);

        const res = await fetch(`/api/recordatorios/${id_recordatorio}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            credentials: "same-origin",
            body: JSON.stringify(dto),
        });

        const responseText = await res.text();

        if (!res.ok) {
            console.error('❌ Error del servidor (status ' + res.status + '):', responseText);
            throw new Error("Error actualizando: " + responseText);
        }

        return JSON.parse(responseText);
    }

    async function apiToggle(id_recordatorio, value) {
        const res = await fetch(`/api/recordatorios/${id_recordatorio}/completado?value=${value}`, {
            method: "PATCH",
            credentials: "same-origin",
        });
        if (!res.ok) throw new Error("Error toggling");
        return res.json();
    }

    async function apiEliminar(id_recordatorio) {
        const res = await fetch(`/api/recordatorios/${id_recordatorio}`, {
            method: "DELETE",
            credentials: "same-origin",
        });
        if (!res.ok) throw new Error("Error eliminando");
    }

    async function apiVinculos(id_usuario) {
        const res = await fetch(`/api/recordatorios/vinculos-activos?id_usuario=${id_usuario}`, {
            credentials: "same-origin",
        });
        if (!res.ok) throw new Error("Error vínculos");
        return res.json();
    }

    // ====== Vínculos ======
    async function cargarVinculos() {
        const id_usuario = getIdUsuario();
        if (!Number.isInteger(id_usuario)) return;

        try {
            const data = await apiVinculos(id_usuario);
            const grupos = ['<option value="">Sin vínculo</option>'];

            if (Array.isArray(data.TRATAMIENTOS)) {
                grupos.push('<optgroup label="Tratamientos activos">');
                grupos.push(
                    data.TRATAMIENTOS
                        .map((t) => `<option value="TRATAMIENTO:${t.id}">Tratamiento: ${escapeHtml(t.nombre)}</option>`)
                        .join("")
                );
                grupos.push("</optgroup>");
            }
            if (Array.isArray(data.CITAS)) {
                grupos.push('<optgroup label="Citas activas">');
                grupos.push(
                    data.CITAS
                        .map((c) => `<option value="CITA:${c.id}">Cita: ${escapeHtml(c.nombre)}</option>`)
                        .join("")
                );
                grupos.push("</optgroup>");
            }

            selectVinculo.innerHTML = grupos.join("");
        } catch (e) {
            console.warn("No se pudieron cargar los vínculos:", e);
            selectVinculo.innerHTML = '<option value="">Sin vínculo</option>';
        }
    }

    // ====== Render ======
    function filtrar(datos) {
        const now = new Date();
        if (filter === "HOY") return datos.filter((r) => new Date(r.fechaHora).toDateString() === now.toDateString());
        if (filter === "PROXIMOS") return datos.filter((r) => new Date(r.fechaHora) > now);
        if (filter === "PASADOS") return datos.filter((r) => new Date(r.fechaHora) < now);
        if (["MEDICAMENTO", "CITA", "TRATAMIENTO"].includes(filter)) return datos.filter((r) => r.tipo === filter);
        return datos;
    }

    function render(datos) {
        const shown = filtrar(datos);
        resumen.textContent = `${shown.length} recordatorio(s) · Total: ${datos.length}`;

        lista.innerHTML = "";
        if (!shown.length) {
            lista.innerHTML = "<li>No hay recordatorios para este filtro.</li>";
            return;
        }

        const html = shown.map((rec) => {
            const chipClase =
                rec.tipo === "MEDICAMENTO" ? "rec-chip rec-chip--med" :
                    rec.tipo === "CITA"        ? "rec-chip rec-chip--cita" :
                        rec.tipo === "TRATAMIENTO" ? "rec-chip rec-chip--trat" :
                            "rec-chip";

            return `
        <li class="rec-card" data-id="${rec.id_recordatorio}">
          <div class="rec-card__left">
            <label class="rec-card__title">
              <input type="checkbox" data-act="toggle" data-id_recordatorio="${rec.id_recordatorio}" ${rec.completado ? "checked" : ""} />
              <strong class="${rec.completado ? "rec-card__title--done" : ""}">
                ${escapeHtml(rec.titulo || "")}
              </strong>
            </label>

            <div class="rec-date">${fmtDate(rec.fechaHora)}</div>

            <div class="rec-tags">
              <span class="${chipClase}">${rec.tipo}</span>
              ${rec.vinculoTipo && rec.vinculoId ? `<span class="rec-chip rec-chip--ghost">${rec.vinculoTipo} #${rec.vinculoId}</span>` : ""}
            </div>

            ${rec.descripcion ? `<p class="rec-desc">${escapeHtml(rec.descripcion)}</p>` : ""}
          </div>

          <div class="rec-card__right">
            <button class="rec-btn" data-act="edit" data-id_recordatorio="${rec.id_recordatorio}">✏️ Editar</button>
            <button class="rec-btn rec-btn--danger" data-act="del" data-id_recordatorio="${rec.id_recordatorio}">🗑️ Eliminar</button>
          </div>
        </li>`;
        }).join("");

        lista.innerHTML = html;
    }

    // ====== Cargar lista ======
    async function cargarLista() {
        const id_usuario = getIdUsuario();
        if (!Number.isInteger(id_usuario)) return;
        try {
            cache = ["MEDICAMENTO", "CITA", "TRATAMIENTO"].includes(filter)
                ? await apiListar(id_usuario, { tipo: filter })
                : await apiListar(id_usuario);
            render(cache);
        } catch (e) {
            console.error("Error listando:", e);
            lista.innerHTML = "<li>Error cargando recordatorios</li>";
        }
    }

    // ====== Eventos ======
    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const id_usuario = getIdUsuario();
        if (!Number.isInteger(id_usuario)) return alert("No se encontró id_usuario");

        // ✅ Validar campos obligatorios
        const titulo = form.titulo.value.trim();
        const fecha = form.fecha.value;
        const hora = form.hora.value;

        if (!titulo) {
            return alert("El título es obligatorio");
        }
        if (!fecha) {
            return alert("La fecha es obligatoria");
        }
        if (!hora) {
            return alert("La hora es obligatoria");
        }

        // Parsear vínculo
        const vinc = form.vinculo.value || "";
        let vinculoTipo = null, vinculoId = null;
        if (vinc.includes(":")) {
            const [t, id] = vinc.split(":");
            vinculoTipo = t || null;
            vinculoId = id ? parseInt(id, 10) : null;
            if (Number.isNaN(vinculoId)) vinculoId = null;
        }

        // ✅ Construir fechaHora con validación
        let fechaHora;
        try {
            fechaHora = toISO(fecha, hora);
        } catch (err) {
            return alert(err.message);
        }

        // Construir DTO
        const dtoBase = {
            id_usuario,
            titulo,
            tipo: form.tipo.value,
            fechaHora,
            repeticion: form.repeticion.value || "NONE",
            canal: form.canal.value || "INAPP",
            vinculoTipo,
            vinculoId,
            descripcion: form.descripcion.value.trim() || null,
            completado: false
        };

        console.log('📦 DTO final a enviar:', dtoBase);

        try {
            const editId = form.editId.value ? parseInt(form.editId.value, 10) : null;

            if (editId) {
                // PUT: Incluir id_recordatorio
                await apiActualizar(editId, { ...dtoBase, id_recordatorio: editId });
            } else {
                // POST: Enviar con id_recordatorio = 0
                await apiCrear({ id_recordatorio: 0, ...dtoBase });
            }

            form.reset();
            form.editId.value = "";
            await cargarLista();
        } catch (e2) {
            console.error("Error creando/actualizando:", e2);
            alert("No se pudo guardar el recordatorio. Revisa la consola para más detalles.");
        }
    });

    btnLimpiar?.addEventListener("click", () => {
        form.reset();
        form.editId.value = "";
    });

    lista.addEventListener("click", async (e) => {
        const el = e.target.closest("button, input[type='checkbox']");
        if (!el) return;
        const id_recordatorio = parseInt(el.dataset.id_recordatorio, 10);

        if (el.type === "checkbox" && el.dataset.act === "toggle") {
            try {
                await apiToggle(id_recordatorio, el.checked);
                await cargarLista();
            } catch (err) {
                console.error(err);
                el.checked = !el.checked;
            }
        }

        if (el.dataset.act === "del") {
            if (confirm("¿Eliminar recordatorio?")) {
                await apiEliminar(id_recordatorio);
                await cargarLista();
            }
        }

        if (el.dataset.act === "edit") {
            const rec = cache.find((r) => r.id_recordatorio === id_recordatorio);
            if (!rec) return;
            form.editId.value = rec.id_recordatorio;
            form.titulo.value = rec.titulo || "";
            form.tipo.value = rec.tipo || "OTRO";
            form.repeticion.value = rec.repeticion || "NONE";
            form.canal.value = rec.canal || "INAPP";
            form.vinculo.value = (rec.vinculoTipo && rec.vinculoId != null) ? `${rec.vinculoTipo}:${rec.vinculoId}` : "";
            form.descripcion.value = rec.descripcion || "";
            const { fecha, hora } = parseISOToInputs(rec.fechaHora);
            form.fecha.value = fecha;
            form.hora.value = hora;
            window.scrollTo({ top: 0, behavior: "smooth" });
        }
    });

    chips.forEach((chip) =>
        chip.addEventListener("click", async () => {
            chips.forEach((c) => c.classList.remove("active"));
            chip.classList.add("active");
            filter = chip.dataset.filter;
            await cargarLista();
        })
    );

    // ====== Init ======
    (async function init() {
        await cargarVinculos();
        await cargarLista();
    })();
});