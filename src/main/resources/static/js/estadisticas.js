document.addEventListener('DOMContentLoaded', () => {
    // ---- Helpers UI ----
    const log = (...a) => console.log('[Stats]', ...a);
    const showError = (msg) => { alert('Error estadísticas: ' + msg); console.error('[Stats] ' + msg); };

    // ---- id_usuario desde Thymeleaf ----
    // En tu plantilla añade (opción A):
    // <script th:inline="javascript">window.ID_USUARIO = /*[[${id_usuario}]]*/ 0;</script>
    // (opcional) <input type="hidden" id="idUsuario" th:value="${id_usuario}">
    const hiddenId = document.getElementById('idUsuario');
    const ID_USUARIO = Number(window.ID_USUARIO ?? (hiddenId ? hiddenId.value : 0)) || 0;
    if (!ID_USUARIO) {
        showError('No se pudo determinar el id_usuario. Revisa la vista (Option A).');
        return;
    }

    // ---- Fechas por defecto (últimos 30 días) ----
    const hoy = new Date();
    const hace30 = new Date(); hace30.setDate(hoy.getDate() - 30);
    const fmt = d => d.toISOString().slice(0, 10);

    const $desde   = document.getElementById('desde');
    const $hasta   = document.getElementById('hasta');
    const $tipo    = document.getElementById('tipo');
    const $aplicar = document.getElementById('aplicar');

    if ($desde && $hasta) {
        if (!$desde.value) $desde.value = fmt(hace30);
        if (!$hasta.value) $hasta.value = fmt(hoy);
    }

    Chart.defaults.locale = 'es-ES';
    Chart.defaults.font.family = 'system-ui, -apple-system, Segoe UI, Roboto, Arial';

// Reemplaza tu ensureChart por este
    function ensureChart(id, type, data, options = {}) {
        const el = document.getElementById(id);
        if (!el) return;
        charts[id]?.destroy();

        const baseOpts = {
            maintainAspectRatio: false,
            responsive: true,
            plugins: {
                legend: { display: type !== 'line' || (data.datasets?.length ?? 0) > 1 },
                tooltip: {
                    callbacks: {
                        label: ctx => {
                            const v = ctx.parsed.y ?? ctx.parsed;
                            return `${ctx.dataset.label ?? 'Valor'}: ${new Intl.NumberFormat('es-ES').format(v)}`;
                        }
                    }
                }
            },
            scales: (type !== 'doughnut')
                ? {
                    x: { grid: { color: 'rgba(0,0,0,.06)' } },
                    y: {
                        beginAtZero: true,
                        ticks: { precision: 0 },
                        grid: { color: 'rgba(0,0,0,.06)' }
                    }
                }
                : {}
        };

        charts[id] = new Chart(el, {
            type,
            data,
            options: {
                ...baseOpts,
                elements: { point: { radius: 2.5 }, line: { tension: 0.2 } },
                ...options
            }
        });
    }
    // ---- Verifica Chart.js ----
    if (typeof Chart === 'undefined') {
        showError('Chart.js no está cargado. Asegúrate de incluir el <script> de Chart antes de este archivo.');
        return;
    }

    // ---- Charts registry ----
    const charts = {};
    const get = id => document.getElementById(id);

    function ensureChart(id, type, data, options = {}) {
        const el = get(id); if (!el) return;
        charts[id]?.destroy();
        charts[id] = new Chart(el, {
            type,
            data,
            options: {
                maintainAspectRatio: false,
                responsive: true,
                plugins: { legend: { display: true } },
                scales: type !== 'doughnut' ? { y: { beginAtZero: true } } : {},
                elements: { point: { radius: 3 }, line: { tension: 0.2 } },
                ...options
            }
        });
    }

    function msgIfEmpty(id, isEmpty) {
        const card = get(id)?.parentElement; if (!card) return;
        card.style.position = 'relative';
        let overlay = card.querySelector('.empty');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.className = 'empty';
            overlay.style.cssText = 'position:absolute;inset:0;display:flex;align-items:center;justify-content:center;font-size:14px;color:#667;opacity:.8;background:transparent;';
            card.appendChild(overlay);
        }
        overlay.textContent = 'Sin datos para el rango seleccionado';
        overlay.style.display = isEmpty ? 'flex' : 'none';
    }

    function linea(id, label, serie = []) {
        const data = {
            labels: (serie || []).map(p => p.fecha),
            datasets: [{ label, data: (serie || []).map(p => p.valor) }]
        };
        ensureChart(id, 'line', data);
        msgIfEmpty(id, !serie || serie.length === 0);
    }

    function barras(id, label, items = []) {
        const data = {
            labels: (items || []).map(i => i.categoria),
            datasets: [{ label, data: (items || []).map(i => i.valor) }]
        };
        ensureChart(id, 'bar', data);
        msgIfEmpty(id, !items || items.length === 0);
    }

    function donut(id, label, items = []) {
        const data = {
            labels: (items || []).map(i => i.categoria),
            datasets: [{ label, data: (items || []).map(i => i.valor) }]
        };
        ensureChart(id, 'doughnut', data);
        msgIfEmpty(id, !items || items.length === 0);
    }

    async function cargar({ id_usuario, desde, hasta, tipo = 'todos' }) {
        const params = new URLSearchParams({ id_usuario, desde, hasta, tipo });
        const url = `/api/estadisticas?${params.toString()}`;
        log('GET', url);
        const res = await fetch(url, { credentials: 'include' });
        if (!res.ok) {
            const txt = await res.text().catch(() => res.statusText);
            throw new Error(`HTTP ${res.status} - ${txt}`);
        }
        return res.json();
    }

    async function actualizar() {
        try {
            const id_usuario = ID_USUARIO;
            const desde = $desde?.value || fmt(hace30);
            const hasta = $hasta?.value || fmt(hoy);
            const tipo  = $tipo?.value  || 'todos';

            const r = await cargar({ id_usuario, desde, hasta, tipo });
            log('Respuesta', r);

            // Render (ajusta IDs a los de tus <canvas>)
            linea ('chartSintomasDia',        'Síntomas por día',       r.sintomasPorDia);
            barras('chartSintomasTipo',       'Síntomas por tipo',      r.sintomasPorTipo);
            linea ('chartCitasDia',           'Citas por día',          r.citasPorDia);
            donut ('chartCitasEstado',        'Próximas vs Pasadas',    r.citasProximasVsPasadas);
            barras('chartTratamientosTipo',   'Tratamientos',           r.tratamientosPorTipo);

            const totalValores = [
                ...(r.sintomasPorDia || []),
                ...(r.sintomasPorTipo || []),
                ...(r.citasPorDia || []),
                ...(r.citasProximasVsPasadas || []),
                ...(r.tratamientosPorTipo || [])
            ].length;
            if (totalValores === 0) {
                showError('La respuesta está vacía. Revisa fechas, id_usuario o datos de BD.');
            }
        } catch (e) {
            showError(e.message);
        }
    }

    $aplicar?.addEventListener('click', actualizar);
    actualizar();
});