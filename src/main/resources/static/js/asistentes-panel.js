(() => {
    // === Config ===
    const WEBHOOK_URL = "https://primary-production-8eee.up.railway.app/webhook/asistente";
    // n8n (producción)
    const TEMPS = { user: 0.7, admin: 0.4, medico: 0.3 }; // solo informativo, las usamos en n8n

    // === Utils ===
    const q = (s, r = document) => r.querySelector(s);

    // Detecta el único section activo por sec:authorize
    const activeSection = q(".section_main[data-role]");
    const ROLE = (activeSection?.dataset?.role || "user").toLowerCase();

    // Mapea IDs según rol (coinciden con tu HTML)
    const ids = {
        user:  { box:"chat-box",  input:"chat-input",  msgs:"chat-messages"  },
        admin: { box:"chat-box2", input:"chat-input2", msgs:"chat-messages2" },
        medico:{ box:"chat-box3", input:"chat-input3", msgs:"chat-messages3" }
    }[ROLE];

    // Session persistente por navegador
    const getSessionId = () => {
        let sid = localStorage.getItem("asst_session_id");
        if (!sid) { sid = Math.random().toString(36).slice(2) + Date.now().toString(36);
            localStorage.setItem("asst_session_id", sid); }
        return sid;
    };

    function appendMessage(sender, text) {
        const wrap = q(`#${ids.msgs}`);
        if (!wrap) return;
        const el = document.createElement("div");
        el.innerHTML = `<strong>${sender}:</strong> ${text}`;
        wrap.appendChild(el);
        wrap.scrollTop = wrap.scrollHeight;
    }

    async function sendToAssistant(message) {
        // CORS-safe: x-www-form-urlencoded (evita preflight)
        const body = new URLSearchParams({
            role: ROLE,
            message,
            sessionId: getSessionId()
        });

        const res = await fetch(WEBHOOK_URL, { method: "POST", body });
        // n8n → Respond to Webhook debe devolver { reply: "..." }
        const data = await res.json().catch(() => ({}));
        appendMessage("Asistente", data?.reply || "Lo siento, no pude entender tu mensaje.");
    }

    async function handleKey(e) {
        if (e.key !== "Enter") return;
        const input = q(`#${ids.input}`);
        const msg = (input?.value || "").trim();
        if (!msg) return;

        appendMessage("Usuario", msg);
        input.value = "";

        try { await sendToAssistant(msg); }
        catch (err) {
            console.error("Asistente error:", err);
            appendMessage("Asistente","Ocurrió un error al procesar tu mensaje.");
        }
    }

    // ==== Init (solo si existen los elementos del rol activo) ====
    document.addEventListener("DOMContentLoaded", () => {
        const input = q(`#${ids.input}`);
        if (input) {
            input.addEventListener("keydown", handleKey);
            console.log(`✅ Chat del rol '${ROLE}' listo (${ids.input}/${ids.msgs}).`);
        } else {
            console.warn(`❌ No se encontró input para el rol '${ROLE}'.`);
        }
    });

    // Exponer helper global para tus botones existentes:
    window.toggleChatById = function(id){
        const el = document.getElementById(id);
        if (el) el.classList.toggle("open");
    };
})();
