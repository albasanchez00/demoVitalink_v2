// ===============================
// JS — Panel Médico: Síntomas
// ===============================
document.addEventListener("DOMContentLoaded", () => {
    const $ = (sel) => document.querySelector(sel);

    // --- Elementos existentes ---
    const fForm    = $('#form-filtros');
    const fId      = $('#f-idUsuario');
    const fTipo    = $('#f-tipo');
    const fZona    = $('#f-zona');
    const fDesde   = $('#f-desde');
    const fHasta   = $('#f-hasta');
    const fSize    = $('#f-size');
    const fSort    = $('#f-sort');
    const btnLimpiar = $('#btn-limpiar');

    const cForm    = $('#form-crear');
    const cTipo    = $('#c-tipo');
    const cZona    = $('#c-zona');
    const cDesc    = $('#c-desc');
    const cFecha   = $('#c-fecha');
    const cMsg     = $('#crear-msg');

    const tbody    = $('#tabla-body');
    const btnPrev  = $('#btn-prev');
    const btnNext  = $('#btn-next');
    const pagInfo  = $('#pagin-info');

    // --- Modal elements ---
    const modalOverlay = $('#modal-sintoma');
    const modalBody    = $('#modal-body');
    const modalFooter  = $('#modal-footer');
    const modalTitle   = $('#modal-title-text');
    const modalIcon    = $('#modal-icon');
    const modalClose   = $('#modal-close');
    const modalBtnCancel = $('#modal-btn-cancel');

    // --- Estado ---
    let state = {
        page: 0,
        totalPages: 0,
        lastQuery: null,
        currentSintoma: null
    };

    // --- Utilidades ---
    function toIsoLocal(dtLocalValue) {
        if (!dtLocalValue) return null;
        return dtLocalValue + ':00';
    }

    function formatFecha(fechaISO) {
        if (!fechaISO) return '—';
        return fechaISO.replace('T', ' ').slice(0, 16);
    }

    // --- Modal Functions ---
    function openModal() {
        modalOverlay.classList.add('active');
        modalOverlay.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';
    }

    function closeModal() {
        modalOverlay.classList.remove('active');
        modalOverlay.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = '';
        state.currentSintoma = null;
    }

    function renderModalVer(sintoma) {
        state.currentSintoma = sintoma;
        modalIcon.textContent = '📋';
        modalTitle.textContent = `Síntoma #${sintoma.id_sintoma}`;

        modalBody.innerHTML = `
            <div class="sm-modal__row">
                <span class="sm-modal__label">ID</span>
                <span class="sm-modal__value">${sintoma.id_sintoma}</span>
            </div>
            <div class="sm-modal__row">
                <span class="sm-modal__label">Fecha</span>
                <span class="sm-modal__value">${formatFecha(sintoma.fechaRegistro)}</span>
            </div>
            <div class="sm-modal__row">
                <span class="sm-modal__label">Tipo</span>
                <span class="sm-modal__value"><span class="sm-badge">${sintoma.tipo || '—'}</span></span>
            </div>
            <div class="sm-modal__row">
                <span class="sm-modal__label">Zona</span>
                <span class="sm-modal__value">${sintoma.zona ? `<span class="sm-badge sm-badge--zona">${sintoma.zona}</span>` : '<span class="sm-modal__value--empty">No especificada</span>'}</span>
            </div>
            <div class="sm-modal__row">
                <span class="sm-modal__label">Descripción</span>
                <span class="sm-modal__value">${sintoma.descripcion || '<span class="sm-modal__value--empty">Sin descripción</span>'}</span>
            </div>
        `;

        modalFooter.innerHTML = `
            <button type="button" class="btn" id="modal-btn-edit">✏️ Editar</button>
            <button type="button" class="btn btn-primary" id="modal-btn-close">Cerrar</button>
        `;

        // Event listeners del footer
        $('#modal-btn-close').addEventListener('click', closeModal);
        $('#modal-btn-edit').addEventListener('click', () => renderModalEditar(sintoma));

        openModal();
    }

    function renderModalEditar(sintoma) {
        state.currentSintoma = sintoma;
        modalIcon.textContent = '✏️';
        modalTitle.textContent = `Editar Síntoma #${sintoma.id_sintoma}`;

        // Obtener opciones de los selects del formulario de crear
        const tipoOptions = cTipo.innerHTML;
        const zonaOptions = cZona.innerHTML;

        modalBody.innerHTML = `
            <form id="form-editar-modal" class="sm-form">
                <div class="sm-modal__row">
                    <label class="sm-modal__label" for="edit-tipo">Tipo</label>
                    <select id="edit-tipo" class="sm-modal__select" required>
                        ${tipoOptions}
                    </select>
                </div>
                <div class="sm-modal__row">
                    <label class="sm-modal__label" for="edit-zona">Zona</label>
                    <select id="edit-zona" class="sm-modal__select">
                        ${zonaOptions}
                    </select>
                </div>
                <div class="sm-modal__row">
                    <label class="sm-modal__label" for="edit-fecha">Fecha</label>
                    <input type="datetime-local" id="edit-fecha" class="sm-modal__input" />
                </div>
                <div class="sm-modal__row" style="grid-template-columns: 1fr;">
                    <label class="sm-modal__label" for="edit-desc">Descripción</label>
                    <textarea id="edit-desc" class="sm-modal__textarea" placeholder="Detalle del síntoma…">${sintoma.descripcion || ''}</textarea>
                </div>
            </form>
        `;

        // Setear valores actuales
        $('#edit-tipo').value = sintoma.tipo || '';
        $('#edit-zona').value = sintoma.zona || '';
        if (sintoma.fechaRegistro) {
            $('#edit-fecha').value = sintoma.fechaRegistro.slice(0, 16);
        }

        modalFooter.innerHTML = `
            <button type="button" class="btn" id="modal-btn-back">← Volver</button>
            <button type="button" class="btn btn-primary" id="modal-btn-save">💾 Guardar</button>
        `;

        $('#modal-btn-back').addEventListener('click', () => renderModalVer(sintoma));
        $('#modal-btn-save').addEventListener('click', guardarEdicion);

        openModal();
    }

    async function guardarEdicion() {
        const sintoma = state.currentSintoma;
        if (!sintoma) {
            console.error('No hay síntoma en state');
            return;
        }

        const body = {
            id_sintoma: sintoma.id_sintoma,
            usuario: sintoma.usuario,
            tipo: $('#edit-tipo').value,
            zona: $('#edit-zona').value || null,
            descripcion: $('#edit-desc').value || null,
            fechaRegistro: $('#edit-fecha').value ? toIsoLocal($('#edit-fecha').value) : sintoma.fechaRegistro
        };

        console.log('=== DEBUG PUT ===');
        console.log('URL:', `/api/medico/sintomas/detalle/${sintoma.id_sintoma}`);
        console.log('Body:', JSON.stringify(body, null, 2));

        try {
            const res = await fetch(`/api/medico/sintomas/detalle/${sintoma.id_sintoma}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });

            console.log('Response status:', res.status);
            const text = await res.text();
            console.log('Response body:', text);

            if (res.ok) {
                closeModal();
                buscar(state.page);
            } else {
                alert(`Error al guardar: ${res.status} - ${text}`);
            }
        } catch (err) {
            console.error('Error de conexión:', err);
            alert('Error de conexión.');
        }
    }

    // --- Modal event listeners ---
    modalClose.addEventListener('click', closeModal);
    modalBtnCancel?.addEventListener('click', closeModal);

    modalOverlay.addEventListener('click', (e) => {
        if (e.target === modalOverlay) closeModal();
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && modalOverlay.classList.contains('active')) {
            closeModal();
        }
    });

    // --- Funciones existentes actualizadas ---
    function buildQuery(idUsuario, pageOverride) {
        if (!idUsuario) return null;

        const params = new URLSearchParams();
        params.set('page', Number.isInteger(pageOverride) ? pageOverride : state.page);
        params.set('size', fSize.value || '10');
        params.set('sort', fSort.value || 'fechaRegistro,desc');

        if (fTipo.value) params.set('tipo', fTipo.value);
        if (fZona.value) params.set('zona', fZona.value);

        const d = toIsoLocal(fDesde.value);
        const h = toIsoLocal(fHasta.value);
        if (d) params.set('desde', d);
        if (h) params.set('hasta', h);

        const base = `/api/medico/sintomas/${encodeURIComponent(idUsuario)}`;
        return `${base}?${params.toString()}`;
    }

    function renderRows(content) {
        if (!content || content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="sm-empty">Sin resultados.</td></tr>`;
            return;
        }
        tbody.innerHTML = content.map(s => {
            const fecha = formatFecha(s.fechaRegistro);
            const tipo  = s.tipo ?? '';
            const zona  = s.zona ?? '';
            const desc  = (s.descripcion ?? '').substring(0, 60) + (s.descripcion?.length > 60 ? '…' : '');
            return `
        <tr>
            <td>${fecha}</td>
            <td><span class="sm-badge">${tipo}</span></td>
            <td>${zona ? `<span class="sm-badge sm-badge--zona">${zona}</span>` : '—'}</td>
            <td>${desc || '<span style="opacity:.5">Sin descripción</span>'}</td>
            <td>
                <div class="sm-table__actions">
                    <button class="sm-btn sm-btn--ver" data-action="ver" data-id="${s.id_sintoma}">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                            <circle cx="12" cy="12" r="3"/>
                        </svg>
                        Ver
                    </button>
                    <button class="sm-btn sm-btn--eliminar" data-action="del" data-id="${s.id_sintoma}">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="3 6 5 6 21 6"/>
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                        </svg>
                        Eliminar
                    </button>
                </div>
            </td>
        </tr>`;
        }).join('');
    }

    function renderPagination(page, totalPages) {
        pagInfo.textContent = `Página ${page + 1} de ${Math.max(totalPages, 1)}`;
        btnPrev.disabled = page <= 0;
        btnNext.disabled = page >= totalPages - 1;
    }

    async function buscar(pageOverride = 0) {
        const idUsuario = (fId.value || '').trim();
        const url = buildQuery(idUsuario, pageOverride);
        if (!url) { alert('Introduce un ID de paciente.'); return; }

        state.page = pageOverride;
        console.debug('[GET]', url);

        const res = await fetch(url, { headers: { 'Accept': 'application/json' }});
        if (!res.ok) {
            const text = await res.text().catch(() => '');
            console.error('Error fetch:', res.status, text);
            tbody.innerHTML = `<tr><td colspan="5" class="sm-empty">Error al cargar (${res.status}).</td></tr>`;
            renderPagination(0, 0);
            return;
        }

        const data = await res.json();
        renderRows(data.content);
        renderPagination(data.number, data.totalPages);
        state.totalPages = data.totalPages;
        state.lastQuery = url;
    }

    async function eliminarSintoma(id) {
        if (!confirm('¿Eliminar este síntoma?')) return;
        const del = await fetch(`/api/medico/sintomas/detalle/${id}`, { method: 'DELETE' });
        if (del.ok) buscar(state.page);
        else alert('No se pudo eliminar.');
    }

    async function verDetalle(id) {
        try {
            const res = await fetch(`/api/medico/sintomas/detalle/${id}`);
            if (res.ok) {
                const sintoma = await res.json();
                renderModalVer(sintoma);
            } else {
                alert('Error al cargar el detalle.');
            }
        } catch (err) {
            console.error('Error:', err);
            alert('Error de conexión.');
        }
    }

    // --- Eventos tabla ---
    tbody.addEventListener('click', (ev) => {
        const btn = ev.target.closest('button[data-action]');
        if (!btn) return;
        const id = btn.dataset.id;
        const action = btn.dataset.action;
        if (action === 'ver') verDetalle(id);
        if (action === 'del') eliminarSintoma(id);
    });

    // --- Eventos filtros ---
    fForm.addEventListener('submit', (e) => {
        e.preventDefault();
        buscar(0);
    });

    btnLimpiar.addEventListener('click', () => {
        fTipo.value = '';
        fZona.value = '';
        fDesde.value = '';
        fHasta.value = '';
        fSort.value = 'fechaRegistro,desc';
        fSize.value = '10';
        tbody.innerHTML = `<tr><td colspan="5" class="sm-empty">Sin resultados.</td></tr>`;
        renderPagination(0, 0);
    });

    btnPrev.addEventListener('click', () => {
        if (state.page > 0) buscar(state.page - 1);
    });
    btnNext.addEventListener('click', () => {
        if (state.page < state.totalPages - 1) buscar(state.page + 1);
    });

    // --- Alta rápida ---
    cForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        cMsg.textContent = '';

        const idUsuario = fId.value?.trim();
        if (!idUsuario) return alert('Primero indica el ID del paciente.');
        if (!cTipo.value) return alert('Selecciona un tipo de síntoma.');

        const body = {
            usuario: { id_usuario: Number(idUsuario) },
            tipo: cTipo.value,
            zona: cZona.value || null,
            descripcion: cDesc.value || null,
            fechaRegistro: cFecha.value ? toIsoLocal(cFecha.value) : null
        };

        const res = await fetch(`/api/medico/sintomas/${idUsuario}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (res.ok) {
            cMsg.textContent = 'Síntoma creado correctamente.';
            cForm.reset();
            buscar(0);
        } else {
            cMsg.textContent = 'Error al crear el síntoma.';
        }
    });
});