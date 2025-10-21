(() => {
    const $ = s => document.querySelector(s);
    const tbody = $("#tbodyCitas");
    const q=$("#q"), estado=$("#estado"), desde=$("#desde"), hasta=$("#hasta");
    const btnBuscar=$("#btnBuscar"), prev=$("#prev"), next=$("#next"), pageInfo=$("#pageInfo");
    const sizeSel=$("#size"), orden=$("#orden");

    // Autocomplete paciente/medico (reutiliza /api/admin/usuarios?q=…)
    const pacienteInput=$("#pacienteInput"), idPaciente=$("#idPaciente"), pacienteSugg=$("#pacienteSugg");
    const medicoInput=$("#medicoInput"), idMedico=$("#idMedico"), medicoSugg=$("#medicoSugg");

    const state = { page:0, size: parseInt(sizeSel.value,10), sort: orden.value };
    btnBuscar.addEventListener("click", ()=>{ state.page=0; cargar(); });
    sizeSel.addEventListener("change", ()=>{ state.size=parseInt(sizeSel.value,10); state.page=0; cargar(); });
    orden.addEventListener("change", ()=>{ state.sort=orden.value; state.page=0; cargar(); });
    prev.addEventListener("click", ()=>{ if(state.page>0){ state.page--; cargar(); }});
    next.addEventListener("click", ()=>{ state.page++; cargar(); });

    const fmtDate = d => d ? new Date(d).toLocaleDateString('es-ES') : "—";
    const fmtTime = t => t ?? "—";

    // --- Autocomplete helpers
    function bindAutocomplete(inputEl, hiddenIdEl, listEl){
        let t=null;
        inputEl.addEventListener("input", ()=>{
            hiddenIdEl.value="";
            if(t) clearTimeout(t);
            const term=inputEl.value.trim();
            if(!term){ listEl.style.display="none"; listEl.innerHTML=""; return; }
            t=setTimeout(async ()=>{
                const url = new URL(location.origin + "/api/admin/usuarios");
                url.searchParams.set("q", term);
                url.searchParams.set("size", 8);
                const r= await fetch(url, {headers:{Accept:"application/json"}});
                if(!r.ok){ listEl.style.display="none"; listEl.innerHTML=""; return; }
                const page= await r.json();
                listEl.innerHTML = (page.content||[]).map(u=>`
          <li data-id="${u.id}" data-label="${escapeHtml(u.display)}"
              style="padding:8px 10px;cursor:pointer;border-bottom:1px solid #f2f4f7;">
            ${escapeHtml(u.display)}
          </li>`).join("");
                listEl.style.display="block";
                listEl.querySelectorAll("li").forEach(li=>{
                    li.addEventListener("click", ()=>{
                        hiddenIdEl.value = li.dataset.id;
                        inputEl.value   = li.dataset.label.replace(/ — #\d+$/,'');
                        listEl.style.display="none"; listEl.innerHTML="";
                        state.page=0; cargar();
                    });
                });
            }, 180);
        });
        document.addEventListener("click", (e)=>{
            if(!e.target.closest(".usuario-autocomplete")){ listEl.style.display="none"; listEl.innerHTML=""; }
        });
    }
    bindAutocomplete(pacienteInput, idPaciente, pacienteSugg);
    bindAutocomplete(medicoInput, idMedico, medicoSugg);

    function escapeHtml(s){ return s.replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m])); }

    // --- Carga tabla
    async function cargar(){
        const url = new URL(location.origin + "/api/admin/citas");
        url.searchParams.set("page", state.page);
        url.searchParams.set("size", state.size);
        url.searchParams.set("sort", state.sort);
        if(q.value.trim()) url.searchParams.set("q", q.value.trim());
        if(estado.value)   url.searchParams.set("estado", estado.value);
        if(desde.value)    url.searchParams.set("desde",  desde.value);
        if(hasta.value)    url.searchParams.set("hasta",  hasta.value);
        if(idPaciente.value) url.searchParams.set("idPaciente", idPaciente.value);
        if(idMedico.value)   url.searchParams.set("idMedico",  idMedico.value);

        const res = await fetch(url, { headers:{Accept:"application/json"}});
        if(!res.ok){ console.error("Error", res.status); return; }
        const page = await res.json();

        tbody.innerHTML = page.content.map(c => `
      <tr>
        <td>${c.id}</td>
        <td>${escapeHtml(c.pacienteNombre)} ${c.pacienteId?`(#${c.pacienteId})`:""}</td>
        <td>${escapeHtml(c.medicoNombre)} ${c.medicoId?`(#${c.medicoId})`:""}</td>
        <td>${escapeHtml(c.titulo ?? "—")}</td>
        <td class="hora">${fmtTime(c.hora)}><span class="badge-estado badge-${(c.estado||'').toUpperCase()}">${c.estado ?? "—"}</span></td>
        <td>${fmtDate(c.fecha)}</td>
        <td>${fmtTime(c.hora)}</td>
        <td>
          <button class="ver" data-id="${c.id}">Ver</button>
          <button class="cancel" data-id="${c.id}">Cancelar</button>
        </td>
      </tr>
    `).join("");

        pageInfo.textContent = `Página ${page.number+1} de ${page.totalPages} • ${page.totalElements} registros`;
        prev.disabled = page.first;
        next.disabled = page.last;

        tbody.querySelectorAll("button.ver").forEach(b=>{
            b.addEventListener("click", ()=>ver(parseInt(b.dataset.id,10)));
        });
        tbody.querySelectorAll("button.cancel").forEach(b=>{
            b.addEventListener("click", ()=>cancelar(parseInt(b.dataset.id,10)));
        });
    }

    async function ver(id){
        const r = await fetch(`/api/admin/citas/${id}`, {headers:{Accept:"application/json"}});
        if(!r.ok) return alert("No se pudo cargar la cita");
        const c = await r.json();
        alert(`Cita #${c.id}
Paciente: ${c.pacienteNombre}
Médico: ${c.medicoNombre}
Título: ${c.titulo}
Estado: ${c.estado}
Fecha: ${fmtDate(c.fecha)} ${fmtTime(c.hora)}`);
    }

    async function cancelar(id){
        if(!confirm("¿Cancelar esta cita?")) return;
        const r = await fetch(`/api/admin/citas/${id}`, {method:"DELETE"});
        if(!r.ok) return alert("No se pudo cancelar");
        cargar();
    }

    cargar();
})();
