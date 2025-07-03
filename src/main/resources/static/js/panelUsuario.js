document.addEventListener("DOMContentLoaded", function () {
    // 👉 1. Renderizado de tarjetas del panel
    const cardData = [
        { title: "📊 Estadísticas", items: ["📉 Evolución de síntomas", "📊 Progreso del tratamiento", "✅ Adherencia al tratamiento"] },
        { title: "📄 Historial Médico", items: ["📅 Citas pasadas y diagnósticos", "📂 Descarga de informes"] },
        {
            title: "⏰ Recordatorios",
            items: [
                "🔔 Próximos medicamentos",
                "🏥 Próxima cita",
                "⚠️ Alertas de dosis olvidadas",
            ]
        },
        {
            title: "📩 Mensajes",
            items: [
                "💬 3 mensajes sin leer",
                "🔔 Notificaciones recientes",
            ]
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

    // 👉 3. Evento para el input del chat
    const input = document.getElementById("chat-input");
    if (input) {
        input.addEventListener("keydown", handleKey);
        console.log("✅ Listener del chat activado");
    } else {
        console.warn("❌ No se encontró el input del asistente");
    }
});

// 👉 4. Función para mostrar u ocultar el chat (puede llamarse desde HTML)
function toggleChat() {
    const box = document.getElementById("chat-box");
    box.style.display = (box.style.display === "none" || box.style.display === "") ? "flex" : "none";
}

// 👉 5. Envío de mensajes y conexión con la IA
async function handleKey(event) {
    if (event.key === "Enter") {
        const input = document.getElementById("chat-input");
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

            if (data.reply) {
                appendMessage("Asistente", data.reply);
            } else {
                appendMessage("Asistente", "Lo siento, no pude entender tu mensaje.");
            }
        } catch (error) {
            console.error("Error en el asistente:", error);
            appendMessage("Asistente", "Ocurrió un error al procesar tu mensaje.");
        }
    }
}

// 👉 6. Función para mostrar mensajes en el chat
function appendMessage(sender, text) {
    const messagesContainer = document.getElementById("chat-messages");
    const messageElement = document.createElement("div");
    messageElement.innerHTML = `<strong>${sender}:</strong> ${text}`;
    messagesContainer.appendChild(messageElement);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}
