/* =========================================================================
 *  ADMIN — Pacientes (ROLE_ADMIN)
 * ========================================================================= */
(() => {
    if (!document.getElementById('usuarios-pacientes')) return;
    const { qs, pageInfo, safeFetchJSON } = window.$common;

    const state = { page:0, size:10, q:"" };
    const $q         = qs("#qPac");
    const $btnBuscar = qs("#btnPacBuscar");
    const $tbody     = qs("#tbodyPacientes");
    const $prev      = qs("#pacPrev");
    const $next      = qs("#pacNext");
    const $info      = qs("#pacPageInfo");
    const $size      = qs("#pacSize");

    let medicosCache = null;

    async function cargarMedicos() {
        if (medicosCache) return medicosCache;
        const url = new URL(location.origin + "/api/admin/medicos");
        url.searchParams.set("page", 0);
        url.searchParams.set("size", 100);
        try {
            const page = await safeFetchJSON(url, { headers: {"Accept":"application/json"} });
            medicosCache = (page.content || []).map(u => ({
                id: u.id ?? u.id_usuario,
                username: u.username
            }));
            return medicosCache;
        } catch (err) {
            console.error(err);
            return [];
        }
    }

    async function load(){
        const url = new URL(location.origin + "/api/admin/pacientes");
        url.searchParams.set("page", state.page);
        url.searchParams.set("size", state.size);
        if (state.q) url.searchParams.set("q", state.q);

        try {
            const page = await safeFetchJSON(url, { headers: {"Accept":"application/json"} });
            render(page);
        } catch (err) {
            console.error(err);
            $tbody.innerHTML = `<tr><td colspan="4">Error al cargar: ${err.message}</td></tr>`;
            $info.textContent = "—";
            $prev.disabled = $next.disabled = true;
        }
    }

    function render(page){
        if (!page.content || page.content.length === 0){
            $tbody.innerHTML = `<tr><td colspan="4" class="muted">No hay pacientes</td></tr>`;
        } else {
            $tbody.innerHTML = page.content.map(c => {
                const idC = c.idCliente ?? c.id_cliente ?? c.id;
                const nombre = `${c.nombre || ""} ${c.apellidos || ""}`.trim();
                const medName = c.medicoUsername || (c.medicoReferencia?.username ?? "—");
                return `
          <tr data-idc="${idC}">
            <td>${idC}</td>
            <td>${nombre || "—"}</td>
            <td data-medcol>${medName || "—"}</td>
            <td><button class="asignar">Asignar/Reasignar</button></td>
          </tr>`;
            }).join("");
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
        const btn = e.target.closest("button.asignar");
        if (!btn) return;

        const tr = btn.closest("tr");
        const idCliente = tr.dataset.idc;
        const medCol = tr.querySelector("[data-medcol]");

        const medicos = await cargarMedicos();
        if (!medicos.length) { alert("No hay médicos disponibles."); return; }

        let box = tr.querySelector(".inline-assign");
        if (box) box.remove();

        box = document.createElement("div");
        box.className = "inline-assign";
        box.style.display = "flex"; box.style.gap="8px"; box.style.marginTop="6px";

        const sel = document.createElement("select");
        sel.innerHTML = medicos.map(m => `<option value="${m.id}">${m.username}</option>`).join("");
        const ok = document.createElement("button"); ok.textContent="Guardar";
        const cancel = document.createElement("button"); cancel.textContent="Cancelar"; cancel.className="muted";

        box.appendChild(sel); box.appendChild(ok); box.appendChild(cancel);

        const trAssign = document.createElement("tr");
        const td = document.createElement("td"); td.colSpan = 4; td.appendChild(box);
        trAssign.appendChild(td); tr.after(trAssign);

        cancel.onclick = ()=> trAssign.remove();
        ok.onclick = async ()=>{
            const idMed = sel.value;
            const res = await fetch(`/api/admin/pacientes/${idCliente}/asignar-medico/${idMed}`, { method:"PATCH" });
            if (res.ok){
                medCol.textContent = (medicos.find(m=> m.id==idMed)?.username) || "—";
                trAssign.remove();
            } else {
                alert("No se pudo asignar el médico.");
            }
        };
    });

    const target = document.getElementById('usuarios-pacientes');
    if (target) {
        const observer = new IntersectionObserver(entries=>{
            entries.forEach(e=>{ if(e.isIntersecting){ load(); observer.disconnect(); }});
        }, { threshold: 0.2 });
        observer.observe(target);
    }
})();
