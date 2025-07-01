document.addEventListener("DOMContentLoaded", function() {
    const cardData = [
        { title: "📊 Estadísticas", items: ["📉 Evolución de síntomas", "📊 Progreso del tratamiento", "✅ Adherencia al tratamiento"] },
        { title: "📄 Historial Médico", items: ["📅 Citas pasadas y diagnósticos", "📂 Descarga de informes"] },
        { 
          title: "⏰ Recordatorios", 
          items: [
            "🔔 Próximos medicamentos", 
            "🏥 Próxima cita", 
            "⚠️ Alertas de dosis olvidadas", 
            "<a href='../../templates/registroCliente.html' class='btn-ver-mas'>Ver más</a>"
          ] 
        },
        { 
          title: "📩 Mensajes", 
          items: [
            "💬 3 mensajes sin leer", 
            "🔔 Notificaciones recientes", 
            "<a href='../../templates/mensajesUsuario.html' class='btn-ver-mas'>Ir a bandeja</a>"
          ] 
        },
        
        { title: "⚙️ Configuración", items: ["👤 Editar perfil", "🔧 Ajustes de notificaciones"] },
        { title: "🤖 Asistente", items: ["🗣️ Consultar", "🔧 Configuración"] }
    ];
    
    const dashboard = document.querySelector(".dashboard-overview");
    dashboard.innerHTML = cardData.map(card => `
        <div class="card">
            <h3>${card.title}</h3>
            ${card.items.map(item => `<p>${item}</p>`).join("")}
        </div>
    `).join("");
    
    const navLinks = document.querySelectorAll(".nav_panelUser a");
    navLinks.forEach(link => {
        link.addEventListener("click", function() {
            navLinks.forEach(l => l.classList.remove("active"));
            this.classList.add("active");
        });
    });
});

function toggleChat() {
    const box = document.getElementById("chat-box");
    box.style.display = (box.style.display === "none" || box.style.display === "") ? "flex" : "none";
  }
  
  function handleKey(event) {
    if (event.key === "Enter") {
      const input = document.getElementById("chat-input");
      const message = input.value.trim();
      if (message) {
        const container = document.getElementById("chat-messages");
        const userMsg = document.createElement("p");
        userMsg.innerHTML = `<strong>Tú:</strong> ${message}`;
        container.appendChild(userMsg);
        input.value = "";
        container.scrollTop = container.scrollHeight;
      }
    }
  }
  