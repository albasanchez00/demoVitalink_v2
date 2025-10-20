// ===== Chat helpers: compat con ids "#...2" (los de tu HTML) =====
function getEl(...ids) {
    for (const id of ids) {
        const el = document.getElementById(id);
        if (el) return el;
    }
    return null;
}

// Mostrar/ocultar chat
function toggleChat() {
    const box = getEl("chat-box2", "chat-box");
    if (!box) return;
    const cur = box.style.display;
    box.style.display = (cur === "none" || cur === "") ? "flex" : "none";
}

// Envío de mensajes con Enter (si existe input)
document.addEventListener("DOMContentLoaded", () => {
    const input = getEl("chat-input2", "chat-input");
    if (input) {
        input.addEventListener("keydown", handleKeySafe);
    }
});

async function handleKeySafe(event) {
    if (event.key !== "Enter") return;
    const input = getEl("chat-input2", "chat-input");
    const msgBox = getEl("chat-messages2", "chat-messages");
    if (!input || !msgBox) return;

    const message = input.value.trim();
    if (!message) return;

    appendMessage("Usuario", message);
    input.value = "";

    try {
        const response = await fetch("https://primary-production-8eee.up.railway.app/webhook/asistente", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message })
        });
        const data = await response.json();
        appendMessage("Asistente", data?.reply || "Lo siento, no pude entender tu mensaje.");
    } catch (error) {
        console.error("Error en el asistente:", error);
        appendMessage("Asistente", "Ocurrió un error al procesar tu mensaje.");
    }
}

function appendMessage(sender, text) {
    const messagesContainer = getEl("chat-messages2", "chat-messages");
    if (!messagesContainer) return;
    const el = document.createElement("div");
    el.innerHTML = `<strong>${sender}:</strong> ${text}`;
    messagesContainer.appendChild(el);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

/* =========================================================================
 *  ADMIN — Navegación del Panel (Usuarios / subpestañas)
 * ========================================================================= */
(() => {
    const { qs } = window.$common || { qs: (s, r=document) => r.querySelector(s) };

    const btnUsuarios    = qs('#navAdminUsuarios');
    const dashboard      = qs('.dashboard-overview');
    const sectionUsuarios= qs('#admin-usuarios');

    if (btnUsuarios && sectionUsuarios) {
        btnUsuarios.addEventListener('click', (e) => {
            e.preventDefault();
            if (dashboard) dashboard.style.setProperty('display','none');
            sectionUsuarios.style.setProperty('display','block');
        });
    }

    // Sub-tabs internas
    document.addEventListener('click', (e) => {
        const a = e.target.closest('.us-tab');
        if (!a) return;
        e.preventDefault();
        document.querySelectorAll('.us-tab').forEach(x => x.classList.remove('is-active'));
        a.classList.add('is-active');

        const tab = a.dataset.tab;
        const med = qs('#usuarios-medicos');
        const pac = qs('#usuarios-pacientes');
        if (med && pac) {
            med.style.display = (tab === 'medicos') ? 'block' : 'none';
            pac.style.display = (tab === 'pacientes') ? 'block' : 'none';
        }
    });
})();
