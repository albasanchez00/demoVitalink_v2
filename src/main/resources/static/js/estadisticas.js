document.addEventListener('DOMContentLoaded', () => {
    /* ========= Helpers UI ========= */
    const log = (...a) => console.log('[Stats]', ...a);
    const showError = (msg) => { alert('Error estadísticas: ' + msg); console.error('[Stats] ' + msg); };

    /* ========= id_usuario desde Thymeleaf =========
       Usa UNA de estas opciones en la vista:
       A) <script th:inline="javascript">window.ID_USUARIO = /*[[${id_usuario}]]/ 0;</script>
    B) <input type="hidden" id="idUsuario" th:value="${id_usuario}">
        */
    const hiddenId = document.getElementById('idUsuario');
    const ID_USUARIO = Number(window.ID_USUARIO ?? (hiddenId ? hiddenId.value : 0)) || 0;
    if (!ID_USUARIO) {
        showError('No se pudo determinar el id_usuario. Revisa la vista.');
        return;
    }

    /* ========= Fechas por defecto (últimos 30 días) ========= */
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

    /* ========= Verifica Chart.js y defaults ========= */
    if (typeof Chart === 'undefined') {
        showError('Chart.js no está cargado. Incluye el <script> de Chart antes de este archivo.');
        return;
    }
    Chart.defaults.locale = 'es-ES';
    Chart.defaults.font.family = 'system-ui, -apple-system, Segoe UI, Roboto, Arial';

    /* ========= Registro de charts y utilidades ========= */
    const charts = {};                       // idCanvas -> instancia Chart
    const get = id => document.getElementById(id);

    // Overlay "sin datos"
    function msgIfEmpty(id, isEmpty) {
        const canvas = get(id); if (!canvas) return;
        const card = canvas.closest('.card') || canvas.parentElement;
        if (!card) return;
        card.style.position = 'relative';
        let overlay = card.querySelector('.empty');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.className = 'empty';
            overlay.style.cssText =
                'position:absolute;inset:0;display:flex;align-items:center;justify-content:center;font-size:14px;color:#667;opacity:.85;background:transparent;pointer-events:none;';
            overlay.textContent = 'Sin datos para el rango seleccionado';
            card.appendChild(overlay);
        }
        overlay.style.display = isEmpty ? 'flex' : 'none';
    }

    // Redimensiona charts cuando cambie el tamaño de su contenedor
    const refreshCharts = () => Object.values(charts).forEach(c => c && c.resize());
    // Observa todos los contenedores .chart-box
    document.querySelectorAll('.chart-box').forEach(box => {
        const ro = new ResizeObserver(refreshCharts);
        ro.observe(box);
    });
    // También al redimensionar ventana
    window.addEventListener('resize', refreshCharts);

    // Crea/actualiza un chart (única versión) + fix overflow
    function ensureChart(id, type, data, options = {}) {
        const el = get(id); if (!el) return;
        if (charts[id]) { charts[id].destroy(); charts[id] = null; }

        // Seguridad extra: evita desbordes horizontales
        el.style.width = '100%';
        el.style.maxWidth = '100%';
        el.closest('.chart-box')?.style.setProperty('overflow', 'hidden');

        const baseOpts = {
            responsive: true,
            maintainAspectRatio: false,
            layout: { padding: 8 },
            plugins: {
                legend: {
                    position: 'top',
                    labels: { boxWidth: 10, font: ctx => ({ size: (window.innerWidth < 720 ? 11 : 12) }) }
                },
                tooltip: { intersect: false, mode: 'index' }
            },
            scales: (type !== 'doughnut') ? {
                x: { ticks: { autoSkip: true, maxRotation: 0 }, grid: { color: 'rgba(0,0,0,.06)' } },
                y: { beginAtZero: true, ticks: { precision: 0 }, grid: { color: 'rgba(0,0,0,.06)' } }
            } : {},
            elements: { point: { radius: 2.5 }, line: { tension: 0.2 } }
        };

        charts[id] = new Chart(el, { type, data, options: { ...baseOpts, ...options } });
    }

    /* ========= Adaptadores de datos =========
       Espera:
         - Serie por día: [{ fecha: 'YYYY-MM-DD', valor: number }, ...]
         - Por categoría: [{ categoria: 'Texto', valor: number }, ...]
    */
    function linea(id, label, serie = []) {
        const labels = (serie || []).map(p => p.fecha);
        const values = (serie || []).map(p => (Number.isFinite(p.valor) ? p.valor : 0));
        ensureChart(id, 'line', { labels, datasets: [{ label, data: values }] });
        msgIfEmpty(id, !serie || serie.length === 0);
    }

    function barras(id, label, items = []) {
        const labels = (items || []).map(i => i.categoria);
        const values = (items || []).map(i => (Number.isFinite(i.valor) ? i.valor : 0));
        ensureChart(id, 'bar', { labels, datasets: [{ label, data: values }] });
        msgIfEmpty(id, !items || items.length === 0);
    }

    function donut(id, label, items = []) {
        const labels = (items || []).map(i => i.categoria);
        const values = (items || []).map(i => (Number.isFinite(i.valor) ? i.valor : 0));
        ensureChart(id, 'doughnut', { labels, datasets: [{ label, data: values }] }, {
            plugins: { legend: { position: 'bottom' } }
        });
        msgIfEmpty(id, !items || items.length === 0);
    }

    /* ========= Llamada a la API ========= */
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

    /* ========= Render principal ========= */
    async function actualizar() {
        try {
            const id_usuario = ID_USUARIO;
            const desde = $desde?.value || fmt(hace30);
            const hasta = $hasta?.value || fmt(hoy);
            const tipo  = $tipo?.value  || 'todos';

            const r = await cargar({ id_usuario, desde, hasta, tipo });
            log('Respuesta', r);

            // IDs de tus <canvas> dentro de .chart-box
            linea ('chartSintomasDia',      'Síntomas por día',     r.sintomasPorDia);
            barras('chartSintomasTipo',     'Síntomas por tipo',    r.sintomasPorTipo);
            linea ('chartCitasDia',         'Citas por día',        r.citasPorDia);
            donut ('chartCitasEstado',      'Próximas vs Pasadas',  r.citasProximasVsPasadas);
            barras('chartTratamientosTipo', 'Tratamientos',         r.tratamientosPorTipo);

            // Ajuste por si hay cambios de alto tras pintar
            refreshCharts();

            const totalValores =
                (r.sintomasPorDia?.length || 0) +
                (r.sintomasPorTipo?.length || 0) +
                (r.citasPorDia?.length || 0) +
                (r.citasProximasVsPasadas?.length || 0) +
                (r.tratamientosPorTipo?.length || 0);

            if (totalValores === 0) {
                showError('La respuesta está vacía. Revisa fechas, id_usuario o datos de BD.');
            }
        } catch (e) {
            showError(e.message);
        }
    }

    /* ========= Eventos ========= */
    $aplicar?.addEventListener('click', actualizar);
    actualizar();
});