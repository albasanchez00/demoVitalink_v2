/* =========================================================================
 *  ADMIN — Chat (mismo layout que MÉDICO, endpoints ADMIN)
 * ========================================================================= */
(() => {
    const $root = document.getElementById('admin-mensajeria');
    if (!$root) return;

    const qs = (s, r = document) => r.querySelector(s);
    const safeFetchJSON = (u, o) => fetch(u, o).then(r => (r.ok ? r.json() : r.text().then(t => Promise.reject(t))));

    // Estado
    const state = {
        page: 0, size: 20, q: '', tipo: '', // filtros list
        current: null, msgPage: 0, msgSize: 50, // hilo
    };

    // Refs (mismos IDs que médico)
    const $lista = qs('#listaConversaciones', $root);
    const $prev  = qs('#prev', $root);
    const $next  = qs('#next', $root);
    const $pageInfo = qs('#pageInfo', $root);
    const $size = qs('#size', $root);

    const $title = qs('#convTitle', $root);
    const $meta  = qs('#convMeta', $root);
    const $msgs  = qs('#mensajes', $root);
    const $form  = qs('#form-msg', $root);
    const $msg   = qs('#msg', $root);

    // Utils
    const buildURL = (base, params) => {
        const url = new URL(base, location.origin);
        Object.entries(params).forEach(([k,v]) => (v !== undefined && v !== null && v !== '') && url.searchParams.set(k, v));
        return url;
    };
    const fmt = iso => {
        const d = new Date(iso);
        return Number.isNaN(d.getTime()) ? '—' : d.toLocaleString('es-ES',{ dateStyle:'short', timeStyle:'short' });
    };
    const esc = s => (s || '').replace(/[&<>"']/g,c=>({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[c]));
    const getCurrentDisplayName = () =>
        document.body?.dataset?.currentuser
        || document.querySelector('meta[name="user-display"]')?.content
        || 'admin';
    const isMe = nombre => (nombre || '').trim().toLowerCase() === getCurrentDisplayName().trim().toLowerCase();

    // ===== Listado de conversaciones (ADMIN) =====
    async function loadConvs() {
        const url = buildURL('/api/admin/chat/conversaciones', { q: state.q, tipo: state.tipo, page: state.page, size: state.size });
        const page = await safeFetchJSON(url, { headers:{ 'Accept':'application/json' } });
        renderConvs(page);
    }

    function renderConvs(page) {
        $lista.innerHTML = '';
        page.content.forEach(c => {
            const li = document.createElement('li');
            li.dataset.id = c.id;
            li.innerHTML = `
        <span class="conv-title"> ${esc(c.servicio)} </span>
        <span class="pill pill--muted">${c.miembrosCount}</span>`;
            li.onclick = () => openConv(c.id);
            if (state.current === c.id) li.classList.add('is-active');
            $lista.appendChild(li);
        });

        $pageInfo.textContent = `Página ${page.number + 1}/${page.totalPages} — ${page.totalElements}`;
        $prev.disabled = page.first;
        $next.disabled = page.last;
        $prev.onclick = () => { if (!page.first) { state.page--; loadConvs(); } };
        $next.onclick = () => { if (!page.last)  { state.page++;  loadConvs(); } };
    }

    function markActive(id) {
        $lista.querySelectorAll('li').forEach(li => li.classList.remove('is-active'));
        const li = $lista.querySelector(`li[data-id="${id}"]`);
        if (li) li.classList.add('is-active');
    }

    // ===== Hilo =====
    async function openConv(id) {
        state.current = id;
        state.msgPage = 0;
        markActive(id);
        $title.textContent = 'CHAT';
        $meta.textContent = ''; // si quieres, coloca participantes aquí con un endpoint detalle
        await loadMsgs(true);
        attachStomp(id);
    }

    async function loadMsgs(scrollBottom = false) {
        if (!state.current) return;
        const url = buildURL(`/api/admin/chat/conversaciones/${state.current}/mensajes`, { page: state.msgPage, size: state.msgSize });
        const page = await safeFetchJSON(url, { headers: { 'Accept':'application/json' } });

        const msgs = [...page.content].reverse();
        $msgs.innerHTML = msgs.map(renderMsg).join('');
        if (scrollBottom) $msgs.scrollTop = $msgs.scrollHeight;

        // infin. arriba
        $msgs.onscroll = null;
        if (!page.last) {
            $msgs.onscroll = () => {
                if ($msgs.scrollTop === 0) {
                    state.msgPage++;
                    appendOlder();
                }
            };
        }
    }

    function renderMsg(m) {
        const mine = isMe(m.remitenteNombre) ? ' me' : '';
        return `
      <article class="msg${mine}">
        <header><b>${esc(m.remitenteNombre)}</b> · <small>${fmt(m.creadoEn)}</small></header>
        <p>${esc(m.contenido)}</p>
      </article>`;
    }

    async function appendOlder() {
        const url = buildURL(`/api/admin/chat/conversaciones/${state.current}/mensajes`, { page: state.msgPage, size: state.msgSize });
        const page = await safeFetchJSON(url, { headers: { 'Accept':'application/json' } });
        const prevScroll = $msgs.scrollHeight;
        const older = [...page.content].reverse().map(renderMsg).join('');
        $msgs.insertAdjacentHTML('afterbegin', older);
        $msgs.scrollTop = $msgs.scrollHeight - prevScroll;
    }

    // ===== Envío =====
    $form.addEventListener('submit', async e => {
        e.preventDefault();
        if (!state.current) return;
        const text = ($msg.value || '').trim();
        if (!text) return;

        await fetch(`/api/admin/chat/conversaciones/${state.current}/mensajes`, {
            method: 'POST',
            headers: { 'Content-Type':'text/plain', 'Accept':'application/json' },
            body: text,
        });
        $msg.value = '';
        await loadMsgs(true);
    });

    // Ctrl/Cmd + Enter
    $msg.addEventListener('keydown', e => {
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            e.preventDefault();
            $form.requestSubmit();
        }
    });

    // Paginación lateral (tamaño)
    $size.onchange = () => { state.size = parseInt($size.value, 10) || 20; state.page = 0; loadConvs(); };

    // Init
    loadConvs();

    // ===== STOMP (tiempo real) — mismo topic que médico =====
    function attachStomp(convId) {
        if (!window.stompClient) return;
        if (window._sub) window._sub.unsubscribe();

        window._sub = window.stompClient.subscribe(`/topic/conversaciones.${convId}`, frame => {
            const m = JSON.parse(frame.body);
            if (!m || m.convId !== state.current) return;
            $msgs.insertAdjacentHTML('beforeend', renderMsg({
                remitenteNombre: m.remitenteNombre,
                creadoEn: m.creadoEn,
                contenido: m.contenido,
            }));
            $msgs.scrollTop = $msgs.scrollHeight;
        });
    }
})();
