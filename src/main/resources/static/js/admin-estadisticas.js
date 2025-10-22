/* =========================================================================
 * ADMIN — Estadísticas & Reportes (ROLE_ADMIN)
 * ========================================================================= */
(() => {
    if (!document.getElementById('admin-estadisticas')) return;

    // ------ helpers base ------
    const qs = (s, r = document) => r.querySelector(s);
    async function safeFetchJSON(url, opts = {}) {
        const r = await fetch(url, opts);
        if (!r.ok) {
            // intenta parsear JSON de error; si no, texto
            let err;
            try { err = await r.json(); } catch { err = await r.text(); }
            throw err;
        }
        return r.json();
    }

    // ------ estado + refs ------
    const state = {
        from: null,
        to: null,
        groupBy: 'day',
        medicoId: '',
        citasPage: 0,
        citasSize: 10,
    };
    const $fDesde = qs('#fDesde');
    const $fHasta = qs('#fHasta');
    const $group  = qs('#groupBy');
    const $medico = qs('#medicoSel');
    const $btnAplicar = qs('#btnAplicar');
    const $btnLimpiar = qs('#btnLimpiar');
    const $btnExport = qs('#btnExportCSV');

    const kpis = {
        pacAct: qs('#kpiPacAct'),
        medAct: qs('#kpiMedAct'),
        tratAct: qs('#kpiTratAct'),
        adh: qs('#kpiAdh'),
        citasW: qs('#kpiCitasW'),
        espera: qs('#kpiEspera'),
    };

    const tb = {
        citas: qs('#tbCitas'),
        adh: qs('#tbAdh'),
        sint: qs('#tbSint'),
        trat: qs('#tbTrat'),
    };
    const citasPrev = qs('#citasPrev'), citasNext = qs('#citasNext'), citasInfo = qs('#citasInfo');

    // charts (Chart.js)
    let chAdh, chCitas, chSint, chTrat, chEsp, chUsr;

    // ------ utils ------
    function buildUrl(path, params = {}) {
        const url = new URL(location.origin + path);
        Object.entries(params).forEach(([k, v]) => {
            if (v !== null && v !== '' && v !== undefined) url.searchParams.set(k, v);
        });
        return url;
    }
    function fmtPct(x) { return (x == null ? '—' : (Math.round(x * 100) / 100) + '%'); }
    function fmtNum(x) { return x == null ? '—' : x.toLocaleString('es-ES'); }
    function cssVar(name) { return getComputedStyle(document.documentElement).getPropertyValue(name).trim(); }

    // ------ carga de médicos (select) ------
    let medicosCache;
    async function cargarMedicos() {
        if (medicosCache) return medicosCache;
        // usa tu endpoint actual
        const url = buildUrl('/api/admin/medicos', { page: 0, size: 200 });
        const page = await safeFetchJSON(url, { headers: { 'Accept': 'application/json' } });
        medicosCache = (page.content || []).map(m => ({ id: m.id, nombre: m.nombreCompleto || m.nombre || m.username }));
        medicosCache.forEach(m => {
            const opt = document.createElement('option');
            opt.value = m.id;
            opt.textContent = m.nombre;
            $medico.appendChild(opt);
        });
        return medicosCache;
    }

    // ------ KPIs ------
    async function loadKPIs() {
        const url = buildUrl('/api/admin/stats/overview', {
            from: state.from, to: state.to, medicoId: state.medicoId
        });
        const data = await safeFetchJSON(url, { headers: { 'Accept': 'application/json' } });
        kpis.pacAct.textContent  = fmtNum(data.pacientesActivos);
        kpis.medAct.textContent  = fmtNum(data.medicosActivos);
        kpis.tratAct.textContent = fmtNum(data.tratamientosActivos);
        kpis.adh.textContent     = fmtPct(data.adherenciaMedia);
        kpis.citasW.textContent  = fmtNum(data.citasSemana);
        kpis.espera.textContent  = (data.esperaMediaDias != null) ? data.esperaMediaDias.toFixed(1) + ' d' : '—';
    }

    // ------ charts ------
    function commonChartOpts() {
        const grid = cssVar('--grid-color') || 'rgba(0,0,0,.1)';
        const text = cssVar('--color_texto') || '#334';
        return {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { labels: { color: text } },
                tooltip: { mode: 'index', intersect: false },
            },
            scales: {
                x: { grid: { color: grid }, ticks: { color: text } },
                y: { grid: { color: grid }, ticks: { color: text } },
            }
        };
    }

    async function loadCharts() {
        // Serie Adherencia
        let url = buildUrl('/api/admin/stats/series/adherencia', {
            from: state.from, to: state.to, groupBy: state.groupBy, medicoId: state.medicoId
        });
        const adh = await safeFetchJSON(url, { headers: { 'Accept': 'application/json' } });

        // Serie Citas
        url = buildUrl('/api/admin/stats/series/citas', {
            from: state.from, to: state.to, groupBy: state.groupBy, medicoId: state.medicoId
        });
        const citas = await safeFetchJSON(url, { headers: { 'Accept': 'application/json' } });

        // Top Síntomas (el backend acepta /series/sintomas y /top/sintomas)
        url = buildUrl('/api/admin/stats/top/sintomas', { from: state.from, to: state.to, limit: 10 });
        const sint = await safeFetchJSON(url, { headers: { 'Accept': 'application/json' } });

        // Serie Tratamientos
        url = buildUrl('/api/admin/stats/series/tratamientos', {
            from: state.from, to: state.to, groupBy: state.groupBy
        });
        const trat = await safeFetchJSON(url, { headers: { 'Accept': 'application/json' } });

        // Especialidades (el backend acepta /series/especialidades y /distrib/especialidades)
        url = buildUrl('/api/admin/stats/distrib/especialidades', { from: state.from, to: state.to });
        const esp = await safeFetchJSON(url, { headers: { 'Accept': 'application/json' } });

        // Serie Usuarios
        url = buildUrl('/api/admin/stats/series/usuarios', {
            from: state.from, to: state.to, groupBy: state.groupBy
        });
        const usr = await safeFetchJSON(url, { headers: { 'Accept': 'application/json' } });

        // Render charts
        const make = (ctx, conf) => (window.Chart ? new Chart(ctx, conf) : null);
        chAdh?.destroy(); chCitas?.destroy(); chSint?.destroy(); chTrat?.destroy(); chEsp?.destroy(); chUsr?.destroy();

        chAdh = make(document.getElementById('chAdherencia'), {
            type: 'line',
            data: { labels: adh.labels || [], datasets: [{ label: 'Adherencia %', data: adh.values || [] }] },
            options: commonChartOpts()
        });

        chCitas = make(document.getElementById('chCitas'), {
            type: 'bar',
            data: {
                labels: citas.labels || [],
                datasets: [
                    { label: 'Creadas',    data: citas.created   || [] },
                    { label: 'Atendidas',  data: citas.attended  || [] },
                    { label: 'Canceladas', data: citas.cancelled || [] }
                ]
            },
            options: commonChartOpts()
        });

        chSint = make(document.getElementById('chSintomas'), {
            type: 'bar',
            data: {
                labels: (sint || []).map(s => s.nombre),
                datasets: [{ label: 'Registros', data: (sint || []).map(s => s.total) }]
            },
            options: commonChartOpts()
        });

        chTrat = make(document.getElementById('chTratamientos'), {
            type: 'bar',
            data: {
                labels: trat.labels || [],
                datasets: [
                    { label: 'Iniciados',   data: trat.started  || [] },
                    { label: 'Finalizados', data: trat.finished || [] }
                ]
            },
            options: commonChartOpts()
        });

        chEsp = make(document.getElementById('chEspecialidades'), {
            type: 'doughnut',
            data: {
                labels: (esp || []).map(e => e.especialidad),
                datasets: [{ data: (esp || []).map(e => e.total) }]
            },
            options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
        });

        chUsr = make(document.getElementById('chUsuarios'), {
            type: 'line',
            data: {
                labels: usr.labels || [],
                datasets: [
                    { label: 'Pacientes', data: usr.pacientes || [] },
                    { label: 'Médicos',   data: usr.medicos   || [] }
                ]
            },
            options: commonChartOpts()
        });
    }

    // ------ Reportes ------
    function getRange() {
        const from = $fDesde.value || '2025-09-01';
        const to   = $fHasta.value || '2025-10-31';
        return { from, to };
    }

    // Citas (con paginación)
    async function loadRepCitas(pageOverride) {
        if (typeof pageOverride === 'number') state.citasPage = pageOverride;
        const { from, to } = getRange();
        const url = buildUrl('/api/admin/reportes/citas', {
            from, to, medicoId: $medico.value || '',
            page: state.citasPage, size: state.citasSize, sort: 'fecha,desc'
        });
        const page = await safeFetchJSON(url, { headers: { 'Accept': 'application/json' } });

        tb.citas.innerHTML = '';
        if (!page.content?.length) {
            tb.citas.innerHTML = `<tr><td colspan="5" style="text-align:center;color:#999">Sin resultados</td></tr>`;
        } else {
            page.content.forEach(c => {
                // en el service devolvemos 'titulo' (no 'motivo')
                const fechaTxt = (c.fecha ?? '').toString();
                tb.citas.insertAdjacentHTML('beforeend', `
          <tr>
            <td>${fechaTxt}</td>
            <td>${c.pacienteNombre ?? '—'}</td>
            <td>${c.medicoNombre ?? '—'}</td>
            <td>${c.estado ?? '—'}</td>
            <td>${c.titulo ?? '—'}</td>
          </tr>
        `);
            });
        }
        citasInfo.textContent = `Página ${page.number + 1} de ${page.totalPages || 1} — ${page.totalElements} registros`;
        citasPrev.disabled = page.first; citasNext.disabled = page.last;
    }

    // Adherencia (proxy)
    async function loadRepAdh(page = 0) {
        const { from, to } = getRange();
        const url = buildUrl('/api/admin/stats/reportes/adherencia', {
            from, to, page, size: 10, sort: 'pct,desc'
        });
        const data = await safeFetchJSON(url, { headers: { 'Accept': 'application/json' } });
        tb.adh.innerHTML = '';
        if (!data.content?.length) {
            tb.adh.innerHTML = `<tr><td colspan="4" style="text-align:center;color:#999">Sin resultados</td></tr>`;
        } else {
            data.content.forEach(r => {
                tb.adh.insertAdjacentHTML('beforeend', `
          <tr>
            <td>${r.paciente ?? '—'}</td>
            <td>${r.tratamiento ?? '—'}</td>
            <td>${r.adherencia != null ? r.adherencia.toFixed(1) + '%' : '—'}</td>
            <td>${r.omisiones ?? '—'}</td>
          </tr>
        `);
            });
        }
    }

    // Síntomas
    /* ------------ loader: Síntomas ------------ */
    async function loadRepSint(page = 0) {
        const { from, to } = getRange();
        const url = buildUrl('/api/admin/stats/reportes/sintomas', {
            from, to, page, size: 10, sort: 'fecha_registro,desc'
        });
        const data = await safeFetchJSON(url, { headers: { 'Accept': 'application/json' } });
        const tbSint = tb.sint;
        tbSint.innerHTML = '';

        if (!data.content?.length) {
            tbSint.innerHTML = `<tr><td colspan="5" style="text-align:center;color:#999">Sin resultados</td></tr>`;
        } else {
            data.content.forEach(r => {
                const fecha = r.fecha ? new Date(r.fecha).toLocaleDateString('es-ES') : '—';
                tbSint.insertAdjacentHTML('beforeend', `
        <tr>
          <td>${fecha}</td>
          <td>${r.paciente ?? '—'}</td>
          <td>${r.tipo ?? '—'}</td>
          <td>${r.severidad ?? '—'}</td>
          <td>${r.zona ?? '—'}</td>
        </tr>
      `);
            });
        }
    }

    // Tratamientos
    async function loadRepTrat(page = 0) {
        const { from, to } = getRange();
        const url = buildUrl('/api/admin/stats/reportes/tratamientos', {
            from, to, page, size: 10, sort: 'fecha_inicio,desc'
        });
        const data = await safeFetchJSON(url, { headers: { 'Accept': 'application/json' } });
        tb.trat.innerHTML = '';
        if (!data.content?.length) {
            tb.trat.innerHTML = `<tr><td colspan="5" style="text-align:center;color:#999">Sin resultados</td></tr>`;
        } else {
            data.content.forEach(r => {
                const ini = r.inicio ? r.inicio.toString().slice(0, 10) : '—';
                const fin = r.fin ? r.fin.toString().slice(0, 10) : '—';
                tb.trat.insertAdjacentHTML('beforeend', `
          <tr>
            <td>${ini}</td>
            <td>${fin}</td>
            <td>${r.paciente ?? '—'}</td>
            <td>${r.medico ?? '—'}</td>
            <td>${r.estado ?? '—'}</td>
          </tr>
        `);
            });
        }
    }

    // ------ export CSV (cuando tengas endpoint listo) ------
    function exportCSV() {
        const url = buildUrl('/api/admin/reportes/export', {
            from: state.from, to: state.to, medicoId: state.medicoId, groupBy: state.groupBy
        });
        window.location.href = url.toString();
    }

    // ------ tabs accesibles ------
    (function setupTabs() {
        const tabs = document.querySelectorAll('.rep__tabs [role=tab]');
        const panels = document.querySelectorAll('.rep__panel');
        tabs.forEach(btn => btn.addEventListener('click', () => {
            tabs.forEach(b => b.setAttribute('aria-selected', 'false'));
            panels.forEach(p => p.classList.add('is-hidden'));
            btn.setAttribute('aria-selected', 'true');
            document.getElementById(btn.dataset.tab).classList.remove('is-hidden');

            if (btn.dataset.tab === 'repCitas')     loadRepCitas(0);
            if (btn.dataset.tab === 'repAdh')       loadRepAdh(0);
            if (btn.dataset.tab === 'repSintomas')  loadRepSint(0);
            if (btn.dataset.tab === 'repTrat')      loadRepTrat(0);
        }));
    })();

    // ------ eventos ------
    $btnAplicar.addEventListener('click', async () => {
        state.from = $fDesde.value || null;
        state.to   = $fHasta.value || null;
        state.groupBy = $group.value;
        state.medicoId = $medico.value;
        state.citasPage = 0;
        await Promise.all([loadKPIs(), loadCharts(), loadRepCitas(0)]);
    });

    $btnLimpiar.addEventListener('click', async () => {
        $fDesde.value = ''; $fHasta.value = ''; $group.value = 'day'; $medico.value = '';
        state.from = state.to = null; state.groupBy = 'day'; state.medicoId = '';
        state.citasPage = 0;
        await Promise.all([loadKPIs(), loadCharts(), loadRepCitas(0)]);
    });

    $btnExport.addEventListener('click', exportCSV);
    citasPrev.addEventListener('click', () => { if (state.citasPage > 0) loadRepCitas(state.citasPage - 1); });
    citasNext.addEventListener('click', () => { loadRepCitas(state.citasPage + 1); });

    // ------ init ------
    (async function init() {
        await cargarMedicos();
        // últimos 30 días
        const now = new Date(); const d30 = new Date(now); d30.setDate(now.getDate() - 30);
        $fDesde.value = d30.toISOString().slice(0, 10);
        $fHasta.value = now.toISOString().slice(0, 10);
        state.from = $fDesde.value; state.to = $fHasta.value;

        await Promise.all([loadKPIs(), loadCharts(), loadRepCitas(0)]);
    })();
})();