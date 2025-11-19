/* =========================================================================
 *  ADMIN – Chat mejorado con búsqueda, opciones y funcionalidades completas
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

    // ===== ESTADO DE BÚSQUEDA =====
    let searchResults = [];
    let currentSearchIndex = -1;

    // ==== Referencias DOM ====
    const $lista = qs('#listaConversaciones', $root);
    const $prev = qs('#prev', $root);
    const $next = qs('#next', $root);
    const $pageInfo = qs('#pageInfo', $root);
    const $size = qs('#size', $root);
    const $title = qs('#convTitle');
    const $msgs = qs('#mensajes');
    const $form = qs('#form-msg');
    const $msg = qs('#msg');
    const $searchBar = qs('#search-bar');
    const $searchInput = qs('#search-input');
    const $searchResults = qs('#search-results');
    const $optionsMenu = qs('#options-menu');

    // ==== WebSocket (STOMP) ====
    let stompClient = null;
    let currentSubscription = null;
    let typingSubscription = null;
    let reconnectAttempts = 0;
    const MAX_RECONNECT_ATTEMPTS = 10;

    function connectWebSocket() {
        const socket = new SockJS('/ws-chat');
        stompClient = Stomp.over(socket);
        stompClient.debug = () => {}; // Silenciar logs

        stompClient.connect({},
            () => {
                console.log('✅ WebSocket conectado');
                reconnectAttempts = 0;
                if (state.current) {
                    attachStomp(state.current);
                }
            },
            (error) => {
                console.warn('⚠️ Error de conexión WebSocket:', error);
                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    reconnectAttempts++;
                    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000);
                    console.log(`Reintentando en ${delay}ms... (intento ${reconnectAttempts})`);
                    setTimeout(connectWebSocket, delay);
                }
            }
        );

        // Manejo de desconexión
        socket.onclose = () => {
            console.warn('🔌 WebSocket cerrado, intentando reconectar...');
            if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                setTimeout(connectWebSocket, 3000);
            }
        };
    }

    // ==== Listado de conversaciones ====
    async function loadConvs() {
        const url = buildURL('/api/admin/chat/conversaciones', {
            q: state.q,
            tipo: state.tipo,
            page: state.page,
            size: state.size,
        });

        try {
            const page = await safeFetchJSON(url, { headers: { Accept: 'application/json' } });
            renderConvs(page);

            // Abrir automáticamente la primera conversación si no hay una activa
            if (!state.current && page.content && page.content.length > 0) {
                openConv(page.content[0].id);
            }
        } catch (error) {
            console.error('❌ Error cargando conversaciones:', error);
            $lista.innerHTML = '<li class="empty-state">Error al cargar conversaciones.</li>';
        }
    }

    // ==== Crear conversación directa ====
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
                $nuevoUser.value = '';
                await loadConvs();
                alert('Conversación creada o reabierta correctamente.');
            } catch (err) {
                console.error('❌ Error creando conversación:', err);
                alert('No se pudo crear la conversación: ' + err);
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
                        <button class="delete-btn" title="Eliminar conversación">🗑️</button>
                    </div>
                </div>
            `;

            li.querySelector('.info').onclick = () => openConv(c.id);

            // Botón eliminar
            li.querySelector('.delete-btn').onclick = async (e) => {
                e.stopPropagation();
                if (confirm('¿Seguro que deseas eliminar esta conversación?')) {
                    try {
                        await fetch(`/api/admin/chat/conversaciones/${c.id}`, { method: 'DELETE' });
                        await loadConvs();
                    } catch (err) {
                        console.error('❌ Error eliminando:', err);
                        alert('No se pudo eliminar la conversación');
                    }
                }
            };

            if (state.current === c.id) li.classList.add('is-active');
            $lista.appendChild(li);
        });

        $pageInfo.textContent = `Página ${page.number + 1}/${page.totalPages} – ${page.totalElements}`;
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

        // Cerrar búsqueda al cambiar de conversación
        cerrarBusqueda();

        await loadMsgs(true);
        attachStomp(id);

        // Activa vista "chat abierto" en móvil
        document.body.classList.add('chat-open');
    }

    async function loadMsgs(scrollBottom = false) {
        if (!state.current) return;

        const url = buildURL(
            `/api/admin/chat/conversaciones/${state.current}/mensajes`,
            { page: state.msgPage, size: state.msgSize }
        );

        try {
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
        } catch (error) {
            console.error('❌ Error cargando mensajes:', error);
            $msgs.innerHTML = '<div class="empty-state">Error al cargar mensajes.</div>';
        }
    }

    function renderMsg(m) {
        const mine = isMe(m.remitenteNombre) ? ' me' : '';
        return `
            <div class="msg${mine}" data-content="${esc(m.contenido)}">
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

        try {
            const page = await safeFetchJSON(url, { headers: { Accept: 'application/json' } });
            const prevScroll = $msgs.scrollHeight;
            const older = [...page.content].reverse().map(renderMsg).join('');
            $msgs.insertAdjacentHTML('afterbegin', older);
            $msgs.scrollTop = $msgs.scrollHeight - prevScroll;
        } catch (error) {
            console.error('❌ Error cargando mensajes antiguos:', error);
        }
    }

    // ==== Envío de mensajes ====
    if ($form) {
        $form.addEventListener('submit', async e => {
            e.preventDefault();
            if (!state.current) return;
            const text = ($msg.value || '').trim();
            if (!text) return;

            try {
                // Enviar al backend
                await fetch(`/api/admin/chat/conversaciones/${state.current}/mensajes`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'text/plain',
                        Accept: 'application/json',
                    },
                    body: text,
                });

                // ✅ Eco optimista: mostrar inmediatamente
                const currentUser = getCurrentDisplayName();
                $msgs.insertAdjacentHTML(
                    'beforeend',
                    renderMsg({
                        remitenteNombre: currentUser,
                        creadoEn: new Date().toISOString(),
                        contenido: text,
                    })
                );
                $msgs.scrollTop = $msgs.scrollHeight;

                $msg.value = '';
            } catch (error) {
                console.error('❌ Error enviando mensaje:', error);
                alert('No se pudo enviar el mensaje');
            }
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

    // ==== STOMP (tiempo real) ====
    function attachStomp(convId) {
        if (!stompClient || !stompClient.connected) {
            console.warn('⚠️ STOMP no conectado, intentando reconectar...');
            connectWebSocket();
            return;
        }

        // Desuscribirse de conversación anterior
        if (currentSubscription) {
            try {
                currentSubscription.unsubscribe();
            } catch (e) {
                console.warn('Error al desuscribirse:', e);
            }
            currentSubscription = null;
        }

        // Suscribirse a nuevos mensajes
        try {
            currentSubscription = stompClient.subscribe(`/topic/conversaciones.${convId}`, frame => {
                const m = JSON.parse(frame.body);
                if (!m || m.convId !== state.current) return;

                // ✅ FILTRAR eco duplicado - Si soy yo, no mostrar
                const currentUser = getCurrentDisplayName();
                if (m.remitenteNombre === currentUser) return;

                $msgs.insertAdjacentHTML(
                    'beforeend',
                    renderMsg({
                        remitenteNombre: m.remitenteNombre,
                        creadoEn: m.creadoEn,
                        contenido: m.contenido,
                    })
                );
                $msgs.scrollTop = $msgs.scrollHeight;

                // Notificación sonora (opcional)
                playNotificationSound();
            });

            console.log(`✅ Suscrito a conversación ${convId}`);
        } catch (error) {
            console.error('❌ Error al suscribirse:', error);
        }
    }

    // ==== Sonido de notificación ====
    function playNotificationSound() {
        try {
            const audio = new Audio('/sounds/notification.mp3');
            audio.volume = 0.3;
            audio.play().catch(e => console.log('No se pudo reproducir sonido', e));
        } catch (e) {
            console.log('Audio no disponible');
        }
    }

    // ===== FUNCIONALIDAD BÚSQUEDA =====

    document.getElementById('search-btn')?.addEventListener('click', function(){
        if (!state.current) {
            alert('Abre una conversación primero');
            return;
        }

        $searchBar.classList.toggle('active');

        if ($searchBar.classList.contains('active')) {
            $searchInput.focus();
        } else {
            cerrarBusqueda();
        }
    });

    $searchInput?.addEventListener('input', function(e){
        const query = e.target.value.trim().toLowerCase();

        if (!query) {
            limpiarBusqueda();
            return;
        }

        buscarEnMensajes(query);
    });

    function buscarEnMensajes(query) {
        const mensajes = Array.from($msgs.querySelectorAll('.msg'));
        searchResults = [];

        // Limpiar highlights previos
        mensajes.forEach(msg => {
            const content = msg.dataset.content || '';
            const remitente = msg.querySelector('strong')?.textContent || '';
            msg.innerHTML = msg.classList.contains('me')
                ? esc(content)
                : '<strong>' + esc(remitente) + '</strong><br>' + esc(content);
        });

        // Buscar y resaltar
        mensajes.forEach((msg, index) => {
            const content = msg.dataset.content || '';
            if (content.toLowerCase().includes(query)) {
                searchResults.push({ element: msg, index: index });

                // Resaltar coincidencias
                const regex = new RegExp('(' + query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + ')', 'gi');
                const highlighted = content.replace(regex, '<span class="highlight">$1</span>');

                if (msg.classList.contains('me')) {
                    msg.innerHTML = highlighted;
                } else {
                    const remitente = msg.querySelector('strong')?.textContent || '';
                    msg.innerHTML = '<strong>' + esc(remitente) + '</strong><br>' + highlighted;
                }
            }
        });

        // Actualizar UI
        $searchResults.textContent = searchResults.length + ' resultado' + (searchResults.length !== 1 ? 's' : '');
        document.getElementById('prev-result').disabled = searchResults.length === 0;
        document.getElementById('next-result').disabled = searchResults.length === 0;

        if (searchResults.length > 0) {
            currentSearchIndex = 0;
            navegarAResultado(0);
        } else {
            currentSearchIndex = -1;
        }
    }

    function navegarAResultado(index) {
        if (searchResults.length === 0) return;

        // Limpiar highlight activo
        document.querySelectorAll('.highlight.active').forEach(el => {
            el.classList.remove('active');
        });

        // Activar nuevo highlight
        const result = searchResults[index];
        const highlights = result.element.querySelectorAll('.highlight');
        if (highlights.length > 0) {
            highlights[0].classList.add('active');
        }

        // Scroll al resultado
        result.element.scrollIntoView({ behavior: 'smooth', block: 'center' });

        // Actualizar contador
        $searchResults.textContent = `${index + 1} de ${searchResults.length}`;
    }

    document.getElementById('prev-result')?.addEventListener('click', function(){
        if (searchResults.length === 0) return;
        currentSearchIndex = (currentSearchIndex - 1 + searchResults.length) % searchResults.length;
        navegarAResultado(currentSearchIndex);
    });

    document.getElementById('next-result')?.addEventListener('click', function(){
        if (searchResults.length === 0) return;
        currentSearchIndex = (currentSearchIndex + 1) % searchResults.length;
        navegarAResultado(currentSearchIndex);
    });

    document.getElementById('close-search')?.addEventListener('click', function(){
        cerrarBusqueda();
    });

    function cerrarBusqueda() {
        $searchBar.classList.remove('active');
        $searchInput.value = '';
        limpiarBusqueda();
    }

    function limpiarBusqueda() {
        // Limpiar highlights
        const mensajes = Array.from($msgs.querySelectorAll('.msg'));
        mensajes.forEach(msg => {
            const content = msg.dataset.content || '';
            const remitente = msg.querySelector('strong')?.textContent || '';
            msg.innerHTML = msg.classList.contains('me')
                ? esc(content)
                : '<strong>' + esc(remitente) + '</strong><br>' + esc(content);
        });

        searchResults = [];
        currentSearchIndex = -1;
        $searchResults.textContent = '0 resultados';
        document.getElementById('prev-result').disabled = true;
        document.getElementById('next-result').disabled = true;
    }

    // ===== FUNCIONALIDAD OPCIONES =====

    document.getElementById('options-btn')?.addEventListener('click', function(e){
        e.stopPropagation();
        if (!state.current) {
            alert('Abre una conversación primero');
            return;
        }
        $optionsMenu.classList.toggle('active');
    });

    // Cerrar menú al hacer clic fuera
    document.addEventListener('click', function(e){
        if (!e.target.closest('#options-btn') && !e.target.closest('#options-menu')) {
            $optionsMenu?.classList.remove('active');
        }
    });

    // ✅ 1. SILENCIAR CONVERSACIÓN
    document.getElementById('mute-conv')?.addEventListener('click', function(){
        if (!state.current) return;

        if (confirm('¿Silenciar esta conversación?\n\nNo recibirás notificaciones, pero los mensajes seguirán llegando.')) {
            fetch(`/api/admin/chat/conversaciones/${state.current}/silenciar`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ muted: true })
            })
                .then(resp => {
                    if (!resp.ok) throw new Error('Error al silenciar');
                    alert('✅ Conversación silenciada');
                    loadConvs();
                })
                .catch(err => {
                    console.error('Error:', err);
                    alert('❌ No se pudo silenciar la conversación');
                });
        }

        $optionsMenu.classList.remove('active');
    });

    // ✅ 2. ARCHIVAR CONVERSACIÓN
    document.getElementById('archive-conv')?.addEventListener('click', function(){
        if (!state.current) return;

        if (confirm('¿Archivar esta conversación?\n\nSe moverá a la sección de archivados.')) {
            fetch(`/api/admin/chat/conversaciones/${state.current}/archivar`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ archived: true })
            })
                .then(resp => {
                    if (!resp.ok) throw new Error('Error al archivar');
                    alert('✅ Conversación archivada');
                    state.current = null;
                    $msgs.innerHTML = '<div class="empty-state">Selecciona una conversación</div>';
                    loadConvs();
                })
                .catch(err => {
                    console.error('Error:', err);
                    alert('❌ No se pudo archivar la conversación');
                });
        }

        $optionsMenu.classList.remove('active');
    });

    // ✅ 3. VER DETALLES
    document.getElementById('details-conv')?.addEventListener('click', async function(){
        if (!state.current) return;

        try {
            const resp = await fetch(`/api/admin/chat/conversaciones/${state.current}/detalles`);
            if (!resp.ok) throw new Error('Error al cargar detalles');

            const detalles = await resp.json();
            const info =
                '📋 DETALLES DE LA CONVERSACIÓN\n\n' +
                'ID: ' + detalles.id + '\n' +
                'Tipo: ' + (detalles.tipo || 'DIRECT') + '\n' +
                'Servicio: ' + (detalles.servicio || 'CHAT') + '\n\n' +
                '👥 Miembros (' + detalles.miembros.length + '):\n' +
                detalles.miembros.map(m => '  • ' + m).join('\n') + '\n\n' +
                '💬 Total mensajes: ' + detalles.totalMensajes + '\n' +
                '📬 No leídos: ' + detalles.noLeidos + '\n\n' +
                '🔕 Silenciada: ' + (detalles.muted ? 'Sí' : 'No') + '\n' +
                '📁 Archivada: ' + (detalles.archived ? 'Sí' : 'No') + '\n\n' +
                '📅 Creada: ' + new Date(detalles.creadoEn).toLocaleString('es-ES');

            alert(info);
        } catch (err) {
            console.error('Error:', err);
            alert('❌ No se pudieron cargar los detalles');
        }

        $optionsMenu.classList.remove('active');
    });

    // ✅ 4. LIMPIAR HISTORIAL
    document.getElementById('clear-history')?.addEventListener('click', function(){
        if (!state.current) return;

        if (confirm('⚠️ ¿Estás seguro de que quieres limpiar el historial?\n\n' +
            'Se eliminarán TODOS los mensajes de esta conversación.\n' +
            'Esta acción NO se puede deshacer.')) {

            fetch(`/api/admin/chat/conversaciones/${state.current}/historial`, {
                method: 'DELETE'
            })
                .then(resp => {
                    if (!resp.ok) throw new Error('Error al limpiar historial');
                    alert('✅ Historial limpiado');
                    $msgs.innerHTML = '<div class="empty-state">No hay mensajes en esta conversación.</div>';
                })
                .catch(err => {
                    console.error('Error:', err);
                    alert('❌ No se pudo limpiar el historial');
                });
        }

        $optionsMenu.classList.remove('active');
    });

    // ✅ 5. ELIMINAR CONVERSACIÓN
    document.getElementById('delete-conv')?.addEventListener('click', async function(){
        if (!state.current) return;

        if (confirm('¿Eliminar esta conversación?')) {
            try {
                await fetch(`/api/admin/chat/conversaciones/${state.current}`, { method: 'DELETE' });
                state.current = null;
                $msgs.innerHTML = '<div class="empty-state">Selecciona una conversación</div>';
                await loadConvs();
            } catch (err) {
                console.error('Error:', err);
                alert('No se pudo eliminar la conversación');
            }
        }

        $optionsMenu.classList.remove('active');
    });

    // ==== Inicialización ====
    connectWebSocket();
    loadConvs();

    // Hacer stompClient global para debugging si es necesario
    window.stompClient = stompClient;
})();