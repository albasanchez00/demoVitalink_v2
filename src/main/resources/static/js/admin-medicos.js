/* =========================================================================
 *  ADMIN — Médicos (ROLE_ADMIN)
 * ========================================================================= */
(() => {
    if (!document.getElementById('usuarios-medicos')) return;
    const { qs, /*buildSort,*/ pageInfo, confirmDialog, safeFetchJSON } = window.$common;

    // ⚠️ Desactivamos SORT por ahora para evitar 500 por campo incorrecto en JPA
    const state = { page:0, size:10, q:"", sort: "id_usuario,desc" };

    const $q         = qs("#qMed");
    const $btnBuscar = qs("#btnMedBuscar");
    const $tbody     = qs("#tbodyMedicos");
    const $prev      = qs("#medPrev");
    const $next      = qs("#medNext");
    const $info      = qs("#medPageInfo");
    const $size      = qs("#medSize");
    const $formCrear = qs("#formMedCrear");

    async function load(){
        const url = new URL(location.origin + "/api/admin/medicos");
        url.searchParams.set("page", state.page);
        url.searchParams.set("size", state.size);
        if (state.sort) url.searchParams.set("sort", state.sort);
        if (state.q)    url.searchParams.set("q", state.q);

        try {
            const page = await safeFetchJSON(url, { headers: {"Accept":"application/json"} });
            render(page);
        } catch (err) {
            console.error(err);
            $tbody.innerHTML = `<tr><td colspan="3">Error al cargar: ${err.message}</td></tr>`;
            $info.textContent = "—";
            $prev.disabled = $next.disabled = true;
        }
    }

    function render(page){
        if (!page.content || page.content.length === 0){
            $tbody.innerHTML = `<tr><td colspan="3" class="muted">No hay médicos</td></tr>`;
        } else {
            $tbody.innerHTML = page.content.map(u => `
        <tr>
          <td>${u.id ?? u.id_usuario ?? ""}</td>
          <td>${u.username}</td>
          <td><button class="danger del" data-id="${u.id ?? u.id_usuario}">Eliminar</button></td>
        </tr>
      `).join("");
        }
        pageInfo($info, page);
        $prev.disabled = page.first;
        $next.disabled = page.last;
    }

    // Eventos
    if ($btnBuscar) $btnBuscar.addEventListener("click", ()=>{ state.q = ($q?.value||"").trim(); state.page=0; load(); });
    if ($q) $q.addEventListener("keydown", e=>{ if (e.key==="Enter"){ e.preventDefault(); $btnBuscar?.click(); }});
    if ($size) $size.addEventListener("change", e=>{ state.size = parseInt(e.target.value,10); state.page=0; load(); });
    if ($prev) $prev.addEventListener("click", ()=>{ state.page = Math.max(0, state.page-1); load(); });
    if ($next) $next.addEventListener("click", ()=>{ state.page = state.page+1; load(); });

    if ($tbody) $tbody.addEventListener("click", async (e)=>{
        const btn = e.target.closest("button.del");
        if (!btn) return;
        const id = btn.dataset.id;
        if (await confirmDialog("¿Eliminar este médico?")) {
            const res = await fetch(`/api/admin/medicos/${id}`, { method:"DELETE" });
            if (res.ok) load(); else alert("No se pudo eliminar (FK o error).");
        }
    });

    if ($formCrear) $formCrear.addEventListener("submit", async (e)=>{
        e.preventDefault();
        const fd = new FormData($formCrear);
        const username = fd.get("username")?.trim();
        const password = fd.get("password")?.trim();
        if (!username || !password) return;

        const url = new URL(location.origin + "/api/admin/medicos");
        url.searchParams.set("username", username);
        url.searchParams.set("password", password);

        try {
            await safeFetchJSON(url, { method:"POST" });
            $formCrear.reset();
            state.page = 0;
            load();
        } catch (err) {
            alert("Error creando médico: " + err.message);
        }
    });

    // Carga inicial defer hasta que sea visible
    const target = document.getElementById('usuarios-medicos');
    if (target) {
        const observer = new IntersectionObserver(entries=>{
            entries.forEach(e=>{ if(e.isIntersecting){ load(); observer.disconnect(); }});
        }, { threshold: 0.2 });
        observer.observe(target);
    }
})();
