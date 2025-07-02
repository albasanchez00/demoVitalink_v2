document.addEventListener("DOMContentLoaded", function () {
    const adminCardData = [
        {
            title: "👥 Gestión de Usuarios",
            items: [
                "➕ Añadir nuevo usuario",
                "📝 Editar información",
                "<a href='./adminUsuarios.html' class='btn-ver-mas'>Ver todos</a>"
            ]
        },
        {
            title: "📅 Control de Citas",
            items: [
                "📍 Ver citas programadas",
                "🗓️ Asignar o modificar citas",
                "<a href='./adminCitas.html' class='btn-ver-mas'>Gestionar</a>"
            ]
        },
        {
            title: "💊 Tratamientos",
            items: [
                "📋 Revisar planes actuales",
                "✏️ Ajustar tratamientos",
                "<a href='./adminTratamientos.html' class='btn-ver-mas'>Ver detalles</a>"
            ]
        },
        {
            title: "📊 Reportes Generales",
            items: [
                "📈 Estadísticas de uso",
                "📄 Informes del sistema",
                "<a href='./adminReportes.html' class='btn-ver-mas'>Ver reportes</a>"
            ]
        },
        {
            title: "🔔 Notificaciones",
            items: [
                "📬 Mensajes del sistema",
                "⚠️ Alertas recientes",
                "<a href='./adminMensajes.html' class='btn-ver-mas'>Abrir bandeja</a>"
            ]
        },
        {
            title: "⚙️ Configuración",
            items: [
                "🛠️ Preferencias del sistema",
                "🔐 Seguridad y roles"
            ]
        }
    ];

    const dashboard = document.querySelector(".dashboard-overview");
    dashboard.innerHTML = adminCardData.map(card => `
        <div class="card">
            <h3>${card.title}</h3>
            ${card.items.map(item => `<p>${item}</p>`).join("")}
        </div>
    `).join("");

    const navLinks = document.querySelectorAll(".nav_panelUser a");
    navLinks.forEach(link => {
        link.addEventListener("click", function () {
            navLinks.forEach(l => l.classList.remove("active"));
            this.classList.add("active");
        });
    });
});
