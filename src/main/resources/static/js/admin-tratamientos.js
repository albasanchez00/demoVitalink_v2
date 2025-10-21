(() => {
    const $ = s => document.querySelector(s);
    const tbody = $("#tbodyTrat");
    const q = $("#q"), estado = $("#estado");
    const btnBuscar = $("#btnBuscar");
    const prev = $("#prev"), next = $("#next"), pageInfo = $("#pageInfo");
    const sizeSel = $("#size"), orden = $("#orden");

    // Autocomplete usuario
    const usuarioInput = $("#usuarioInput");
    const idUsuario = $("#idUsuario");
    const usuarioSugg = $("#usuarioSugg");
    const usuarioClear = $("#usuarioClear");

    const state = { page:0, size: parseInt(sizeSel.value,10), sort: orden.value };

    btnBuscar.addEventListener("click", () => { state.page=0; cargar(); });
    sizeSel.addEventListener("change", () => { state.size=parseInt(sizeSel.value,10); state.page=0; cargar(); });
    orden.addEventListener("change", () => { state.sort=orden.value; state.page=0; cargar(); });
    prev.addEventListener("click", () => { if(state.page>0){ state.page--; cargar(); }});
    next.addEventListener("click", () => { state.page++; cargar(); });

    const fmt = d => d ? new Date(d).toLocaleDateString('es-ES') : "—";

    // --- Autocomplete utils ---
    let acTimer = null;
    usuarioInput.addEventListener("input", () => {
        idUsuario.value = ""; // si escribe, invalidamos selección previa
        if (acTimer) clearTimeout(acTimer);
        const term = usuarioInput.value.trim();
        if (!term) { hideSugg(); return; }
        acTimer = setTimeout(() => fetchUsuarios(term), 180);
    });

    usuarioClear.addEventListener("click", () => {
        usuarioInput.value = "";
        idUsuario.value = "";
        hideSugg();
    });

    document.addEventListener("click", (e) => {
        if (!e.target.closest(".usuario-autocomplete")) hideSugg();
    });

    async function fetchUsuarios(term){
        const url = new URL(location.origin + "/api/admin/usuarios");
        url.searchParams.set("q", term);
        url.searchParams.set("size", 8);
        const res = await fetch(url, { headers: { "Accept":"application/json" }});
        if(!res.ok){ hideSugg(); return; }
        const page = await res.json();
        renderSugg(page.content || []);
    }

    function renderSugg(items){
        if (!items.length){ hideSugg(); return; }
        usuarioSugg.innerHTML = items.map(u => `
      <li data-id="${u.id}" data-label="${escapeHtml(u.display)}"
          style="padding:8px 10px;cursor:pointer;border-bottom:1px solid #f2f4f7;">
        ${escapeHtml(u.display)}
      </li>
    `).join("");
        usuarioSugg.style.display = "block";
        usuarioSugg.querySelectorAll("li").forEach(li => {
            li.addEventListener("click", () => {
                idUsuario.value = li.dataset.id;
                usuarioInput.value = li.dataset.label.replace(/ — #\d+$/,''); // muestra limpio
                hideSugg();
                state.page = 0;
                cargar(); // auto-lanzar búsqueda al elegir usuario
            });
        });
    }

    function hideSugg(){ usuarioSugg.style.display = "none"; usuarioSugg.innerHTML = ""; }
    function escapeHtml(s){ return s.replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m])); }

    // --- Carga tabla ---
    async function cargar(){
        const url = new URL(location.origin + "/api/admin/tratamientos");
        url.searchParams.set("page", state.page);
        url.searchParams.set("size", state.size);
        url.searchParams.set("sort", state.sort);
        if(q.value.trim()) url.searchParams.set("q", q.value.trim());
        if(estado.value)   url.searchParams.set("estado", estado.value);
        if(idUsuario.value) url.searchParams.set("idUsuario", idUsuario.value);

        const res = await fetch(url, { headers: { "Accept":"application/json" }});
        if(!res.ok){ console.error("Error", res.status); return; }
        const page = await res.json();

        function estadoClass(e){
            const v = (e ?? '').toString().toLowerCase();
            if (v === 'activo') return 'pill--activo';
            if (v === 'finalizado') return 'pill--finalizado';
            return 'pill--inactivo';
        }

        tbody.innerHTML = page.content.map(t => `
          <tr>
            <td>${t.id}</td>
            <td>${t.usuarioNombre} (${t.usuarioId ? '#' + t.usuarioId : '—'})</td>
            <td>${t.nombreTratamiento ?? "—"}</td>
            <td><span class="pill-estado ${estadoClass(t.estado)}">${t.estado ?? "—"}</span></td>
            <td>${fmt(t.fechaInicio)}</td>
            <td>${fmt(t.fechaFin)}</td>
            <td>
              <button data-id="${t.id}" class="btn-sec ver">Ver</button>
              <button data-id="${t.id}" class="btn-sec btn-danger inactivar">Finalizar</button>
            </td>
          </tr>
        `).join("");

        pageInfo.textContent = `Página ${page.number+1} de ${page.totalPages} • ${page.totalElements} registros`;
        prev.disabled = page.first;
        next.disabled = page.last;

        tbody.querySelectorAll("button.ver").forEach(b => {
            b.addEventListener("click", () => ver(parseInt(b.dataset.id,10)));
        });
        tbody.querySelectorAll("button.inactivar").forEach(b => {
            b.addEventListener("click", () => inactivar(parseInt(b.dataset.id,10)));
        });
    }

    async function ver(id){
        const res = await fetch(`/api/admin/tratamientos/${id}`, { headers:{ "Accept":"application/json"}});
        if(!res.ok) return alert("No se pudo cargar el tratamiento");
        const t = await res.json();
        alert(`Tratamiento #${t.id}\nUsuario: ${t.usuarioNombre}\nTratamiento: ${t.nombreTratamiento}\nEstado: ${t.estado}`);
    }

    async function inactivar(id){
        if(!confirm("¿Finalizar este tratamiento?")) return;
        const res = await fetch(`/api/admin/tratamientos/${id}`, { method:"DELETE" });
        if(!res.ok) return alert("No se pudo finalizar");
        cargar();
    }

    cargar();
})();