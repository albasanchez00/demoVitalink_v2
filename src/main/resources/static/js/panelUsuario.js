document.addEventListener("DOMContentLoaded", function () {
    // === 0) Rol del usuario y sessionId persistente ===
    const ROLE = (document.body?.dataset?.role || "user").toLowerCase();
    const getSessionId = () => {
        let sid = localStorage.getItem("asst_session_id");
        if (!sid) {
            sid = Math.random().toString(36).slice(2) + Date.now().toString(36);
            localStorage.setItem("asst_session_id", sid);
        }
        return sid;
    };

    // 👉 1. Renderizado de tarjetas del panel (igual que antes)
    const cardData = [
        { title: "📊 Estadísticas", items: ["📉 Evolución de síntomas", "📊 Progreso del tratamiento", "✅ Adherencia al tratamiento"] },
        { title: "📄 Historial Médico", items: ["📅 Citas pasadas y diagnósticos", "📂 Descarga de informes"] },
        {
            title: "⏰ Recordatorios",
            items: ["🔔 Próximos medicamentos", "🏥 Próxima cita", "⚠️ Alertas de dosis olvidadas"]
        },
        {
            title: "📩 Mensajes",
            items: ["💬 3 mensajes sin leer", "🔔 Notificaciones recientes"]
        },
        { title: "⚙️ Configuración", items: ["👤 Editar perfil", "🔧 Ajustes de notificaciones"] },
        { title: "🤖 Asistente", items: ["🗣️ Consultar", "🔧 Configuración"] }
    ];

    const dashboard = document.querySelector(".dashboard-overview");
    if (dashboard) {
        dashboard.innerHTML = cardData.map(card => `
          <div class="card">
            <h3>${card.title}</h3>
            ${card.items.map(item => `<p>${item}</p>`).join("")}
          </div>
        `).join("");
    }

    // 👉 2. Activar links del panel
    const navLinks = document.querySelectorAll(".nav_panelUser a");
    navLinks.forEach(link => {
        link.addEventListener("click", function () {
            navLinks.forEach(l => l.classList.remove("active"));
            this.classList.add("active");
        });
    });
});