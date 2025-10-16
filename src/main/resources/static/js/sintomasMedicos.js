// ===============================
// JS — Panel Médico: Síntomas
// Archivo: /static/js/sintomasMedico.js
// ===============================
document.addEventListener("DOMContentLoaded", () => {
    const $ = (sel) => document.querySelector(sel);

    // --- Elementos ---
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

    // --- Estado ---
    let state = {
        page: 0,
        totalPages: 0,
        lastQuery: null,
    };

    // --- Utilidades ---
    function toIsoLocal(dtLocalValue) {
        if (!dtLocalValue) return null;
        return dtLocalValue + ':00'; // añade segundos
    }

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

        // ⚠️ construimos SIEMPRE con encode y con '?' seguro
        const base = `/api/medico/sintomas/${encodeURIComponent(idUsuario)}`;
        return `${base}?${params.toString()}`;
    }

    function renderRows(content) {
        if (!content || content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="padding:10px;">Sin resultados.</td></tr>`;
            return;
        }
        tbody.innerHTML = content.map(s => {
            const fecha = s.fechaRegistro ? s.fechaRegistro.replace('T',' ').slice(0,16) : '';
            const tipo  = s.tipo ?? '';
            const zona  = s.zona ?? '';
            const desc  = (s.descripcion ?? '').replace(/\n/g,'<br>');
            return `
        <tr>
          <td style="padding:8px;">${fecha}</td>
          <td style="padding:8px;">${tipo}</td>
          <td style="padding:8px;">${zona}</td>
          <td style="padding:8px;">${desc}</td>
          <td style="padding:8px; text-align:center;">
            <button class="btn btn-outline" data-action="ver" data-id="${s.id_sintoma}">Ver</button>
            <button class="btn btn-danger"  data-action="del" data-id="${s.id_sintoma}">Eliminar</button>
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
        // LOG útil para depurar
        console.debug('[GET]', url);

        const res = await fetch(url, { headers: { 'Accept': 'application/json' }});
        if (!res.ok) {
            const text = await res.text().catch(()=>'');
            console.error('Error fetch:', res.status, text);
            tbody.innerHTML = `<tr><td colspan="5">Error al cargar (${res.status}).</td></tr>`;
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
        const res = await fetch(`/api/medico/sintomas/detalle/${id}`);
        if (res.ok) {
            const s = await res.json();
            alert(
                `Síntoma #${s.id_sintoma}\nFecha: ${s.fechaRegistro}\nTipo: ${s.tipo}\nZona: ${s.zona}\n\n${s.descripcion ?? ''}`
            );
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
        tbody.innerHTML = `<tr><td colspan="5" style="padding:10px;">Sin resultados.</td></tr>`;
        renderPagination(0,0);
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
            headers: { 'Content-Type':'application/json' },
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

    // --- Autocarga (opcional) ---
    // fId.value = '5';
    // buscar(0);
});