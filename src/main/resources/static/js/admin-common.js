/* ========================================================================
 *  ADMIN — Helpers comunes
 *  ------------------------------------------------------------------------
 *  Uso: const { qs, qsa, buildSort, pageInfo, confirmDialog, safeFetchJSON } = window.$common;
 * ======================================================================== */
window.$common = (function(){
    const qs  = (sel, root=document) => root.querySelector(sel);
    const qsa = (sel, root=document) => Array.from(root.querySelectorAll(sel));
    const buildSort = (p, d='desc') => `${p},${d}`;
    const pageInfo = (el, page) => {
        if (!el || !page) return;
        el.textContent = `Página ${page.number+1} / ${page.totalPages || 1} — ${page.totalElements ?? 0} registros`;
    };
    const confirmDialog = (msg) => Promise.resolve(window.confirm(msg));

    // 👉 Fetch robusto: intenta JSON; si no, muestra texto (útil cuando el back devuelve HTML de error 500)
    async function safeFetchJSON(url, options) {
        const res = await fetch(url, options);
        const raw = await res.text();           // leemos como texto siempre
        let data = null;
        try { data = JSON.parse(raw); } catch(_) { /* no era JSON, puede ser HTML de error */ }

        if (!res.ok) {
            const msg = data?.detail || data?.message || raw.slice(0, 400);
            throw new Error(`HTTP ${res.status} — ${msg}`);
        }
        if (!data) throw new Error(`Respuesta no-JSON del servidor:\n${raw.slice(0, 400)}`);
        return data;
    }

    return { qs, qsa, buildSort, pageInfo, confirmDialog, safeFetchJSON };
})();
