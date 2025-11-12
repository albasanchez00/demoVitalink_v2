/* =========================================================================
 *  ADMIN — Chat (mismo layout que MÉDICO, endpoints ADMIN)
 * ========================================================================= */
(() => {
    const $root = document.getElementById('admin-mensajeria');
    if (!$root) return;

    // ==== Utilidades ====
    const qs = (s, r = document) => (r ? r.querySelector(s) : document.querySelector(s));
    const safeFetchJSON = (u, o) =>
        fetch(u, o).then(r => (r.ok ? r.json() : r.text().then(t => Promise.reject(t))));

    const buildURL = (base, params) => {
        const url = new URL(base, location.origin);
        Object.entries(params).forEach(([k, v]) => {
            if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, v);
        });
        return url;
    };

    const fmt = iso => {
        const d = new Date(iso);
        return Number.isNaN(d.getTime())
            ? '—'
            : d.toLocaleString('es-ES', { dateStyle: 'short', timeStyle: 'short' });
    };

    const esc = s =>
        (s || '').replace(/[&<>"']/g, c =>
            ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c])
        );

    const getCurrentDisplayName = () =>
        document.body?.dataset?.currentuser ||
        document.querySelector('meta[name="user-display"]')?.content ||
        'admin';

    const isMe = nombre =>
        (nombre || '').trim().toLowerCase() === getCurrentDisplayName().trim().toLowerCase();

    // ==== Estado global ====
    const state = {
        page: 0,
        size: 20,
        q: '',
        tipo: '',
        current: null,
        msgPage: 0,
        msgSize: 50,
    };

    // ==== Referencias DOM ====
    const $lista = qs('#listaConversaciones', $root);
    const $prev = qs('#prev', $root);
    const $next = qs('#next', $root);
    const $pageInfo = qs('#pageInfo', $root);
    const $size = qs('#size', $root);
    const $title = qs('#convTitle');
    const $meta = qs('#convMeta');
    const $msgs = qs('#mensajes');
    const $form = qs('#form-msg');
    const $msg = qs('#msg');

    // ==== Listado de conversaciones ====
    async function loadConvs() {
        const url = buildURL('/api/admin/chat/conversaciones', {
            q: state.q,
            tipo: state.tipo,
            page: state.page,
            size: state.size,
        });

        const page = await safeFetchJSON(url, { headers: { Accept: 'application/json' } });
        renderConvs(page);

        // 👉 Abrir automáticamente la primera conversación si no hay una activa
        if (!state.current && page.content && page.content.length > 0) {
            openConv(page.content[0].id);
        }
    }
// ==== Crear conversación directa (igual que médico) ====
    const $btnCrear = document.getElementById('btn-crear');
    const $nuevoUser = document.getElementById('nuevo-usuario');

    if ($btnCrear && $nuevoUser) {
        $btnCrear.addEventListener('click', async () => {
            const username = $nuevoUser.value.trim();
            if (!username) {
                alert('Introduce el nombre de usuario');
                return;
            }

            try {
                await fetch(`/api/admin/chat/conversaciones/directa?username=${encodeURIComponent(username)}`, {
                    method: 'POST',
                    headers: { Accept: 'application/json' },
                });
                await loadConvs();
                alert('Conversación creada o reabierta correctamente.');
            } catch (err) {
                console.error('❌ Error creando conversación:', err);
                alert('No se pudo crear la conversación.');
            }
        });
    }

    function renderConvs(page) {
        $lista.innerHTML = '';

        if (!page.content || !page.content.length) {
            $lista.innerHTML = '<li class="empty-state">No hay conversaciones disponibles.</li>';
            return;
        }

        page.content.forEach(c => {
            const li = document.createElement('li');
            li.dataset.id = c.id;
            li.innerHTML = `
                <div class="conv-item">
                    <div class="info">
                        <strong class="name">${esc(c.servicio || 'Conversación #' + c.id)}</strong>
                        <span class="last-msg">—</span>
                    </div>
                    <div class="meta">
                        <span class="time">—</span>
                        <span class="badge">${c.miembrosCount || 0}</span>
                    </div>
                </div>
            `;
            li.onclick = () => openConv(c.id);
            if (state.current === c.id) li.classList.add('is-active');
            $lista.appendChild(li);
        });

        $pageInfo.textContent = `Página ${page.number + 1}/${page.totalPages} — ${page.totalElements}`;
        $prev.disabled = page.first;
        $next.disabled = page.last;
        $prev.onclick = () => {
            if (!page.first) {
                state.page--;
                loadConvs();
            }
        };
        $next.onclick = () => {
            if (!page.last) {
                state.page++;
                loadConvs();
            }
        };
    }

    function markActive(id) {
        $lista.querySelectorAll('li').forEach(li => li.classList.remove('is-active'));
        const li = $lista.querySelector(`li[data-id="${id}"]`);
        if (li) li.classList.add('is-active');
    }

    // ==== Apertura de conversación y mensajes ====
    async function openConv(id) {
        state.current = id;
        state.msgPage = 0;
        markActive(id);
        if ($title) $title.textContent = 'CHAT';
        if ($meta) $meta.textContent = '';
        await loadMsgs(true);
        attachStomp(id);

        // 👉 Activa vista "chat abierto" en móvil (igual que médico)
        document.body.classList.add('chat-open');
    }

    async function loadMsgs(scrollBottom = false) {
        if (!state.current) return;
        const url = buildURL(
            `/api/admin/chat/conversaciones/${state.current}/mensajes`,
            { page: state.msgPage, size: state.msgSize }
        );

        const page = await safeFetchJSON(url, { headers: { Accept: 'application/json' } });
        const msgs = [...page.content].reverse();

        if (!msgs.length) {
            $msgs.innerHTML = '<div class="empty-state">No hay mensajes en esta conversación.</div>';
        } else {
            $msgs.innerHTML = msgs.map(renderMsg).join('');
            if (scrollBottom) $msgs.scrollTop = $msgs.scrollHeight;
        }

        // Scroll infinito hacia arriba
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
            <div class="msg${mine}">
                <strong>${esc(m.remitenteNombre)}</strong><br>
                ${esc(m.contenido)}
                <div class="time"><small>${fmt(m.creadoEn)}</small></div>
            </div>`;
    }

    async function appendOlder() {
        const url = buildURL(
            `/api/admin/chat/conversaciones/${state.current}/mensajes`,
            { page: state.msgPage, size: state.msgSize }
        );
        const page = await safeFetchJSON(url, { headers: { Accept: 'application/json' } });
        const prevScroll = $msgs.scrollHeight;
        const older = [...page.content].reverse().map(renderMsg).join('');
        $msgs.insertAdjacentHTML('afterbegin', older);
        $msgs.scrollTop = $msgs.scrollHeight - prevScroll;
    }

    // ==== Envío de mensajes ====
    if ($form) {
        $form.addEventListener('submit', async e => {
            e.preventDefault();
            if (!state.current) return;
            const text = ($msg.value || '').trim();
            if (!text) return;

            await fetch(`/api/admin/chat/conversaciones/${state.current}/mensajes`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'text/plain',
                    Accept: 'application/json',
                },
                body: text,
            });
            $msg.value = '';
            await loadMsgs(true);
        });

        // Ctrl/Cmd + Enter para enviar
        $msg.addEventListener('keydown', e => {
            if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
                e.preventDefault();
                $form.requestSubmit();
            }
        });
    }

    // ==== Paginación lateral ====
    if ($size) {
        $size.onchange = () => {
            state.size = parseInt($size.value, 10) || 20;
            state.page = 0;
            loadConvs();
        };
    }

    // ==== Botón "volver" móvil ====
    document.querySelector('.back-btn')?.addEventListener('click', () => {
        document.body.classList.remove('chat-open');
    });

    // ==== Inicialización ====
    loadConvs();

    // ==== STOMP (tiempo real) ====
    function attachStomp(convId) {
        if (!window.stompClient) return;
        if (window._sub) window._sub.unsubscribe();

        window._sub = window.stompClient.subscribe(`/topic/conversaciones.${convId}`, frame => {
            const m = JSON.parse(frame.body);
            if (!m || m.convId !== state.current) return;
            $msgs.insertAdjacentHTML(
                'beforeend',
                renderMsg({
                    remitenteNombre: m.remitenteNombre,
                    creadoEn: m.creadoEn,
                    contenido: m.contenido,
                })
            );
            $msgs.scrollTop = $msgs.scrollHeight;
        });
    }
})();