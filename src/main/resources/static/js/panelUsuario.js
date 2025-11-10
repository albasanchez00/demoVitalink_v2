// === Panel de Usuario: líneas clicables sin modificar el HTML ===
document.addEventListener("DOMContentLoaded", () => {
    const root = document.getElementById("panel-usuario");
    if (!root) return;

    // 🔗 Rutas reales del sistema (ajustadas a tu estructura)
    const go = {
        evolSintomas: "/usuarios/estadisticas?s=sintomas",
        progTrat:     "/usuarios/estadisticas?s=tratamientos",
        adherencia:   "/usuarios/estadisticas?s=adherencia",
        citasPasadas: "/usuarios/historial/citas",
        informes:     "/usuarios/historial/informes",
        proxMed:      "/usuarios/tratamientos",
        proxCita:     "/usuarios/citas",
        alertas:      "/usuarios/tratamientos?f=alertas",
        mensajes:     "/usuarios/mensajes",
        notifs:       "/usuarios/notificaciones",
        perfil:       "/usuarios/configUsuario#perfil",
        notifCfg:     "/usuarios/configUsuario#notificaciones",
        asistente:    "/usuarios/asistente"
    };

    // ✅ Hace un <p> clicable sin alterar su contenido interno
    const makeClickable = (p, href) => {
        if (!p) return;
        p.classList.add("p-as-link");
        p.tabIndex = 0;
        p.addEventListener("click", () => location.href = href);
        p.addEventListener("keypress", (e) => {
            if (e.key === "Enter" || e.key === " ") location.href = href;
        });
    };

    const cards = Array.from(root.querySelectorAll(".card"));
    const byTitle = (t) =>
        cards.find((c) =>
            c.querySelector("h3")?.textContent.trim().toLowerCase().includes(t)
        );

    // === Secciones del panel ===
    // Estadísticas
    {
        const c = byTitle("estadísticas");
        if (c) {
            const ps = c.querySelectorAll("p");
            makeClickable(ps[0], go.evolSintomas);
            makeClickable(ps[1], go.progTrat);
            makeClickable(ps[2], go.adherencia);
        }
    }

    // Historial
    {
        const c = byTitle("historial");
        if (c) {
            const ps = c.querySelectorAll("p");
            makeClickable(ps[0], go.citasPasadas);
            makeClickable(ps[1], go.informes);
        }
    }

    // Recordatorios
    {
        const c = byTitle("recordatorios");
        if (c) {
            const ps = c.querySelectorAll("p");
            makeClickable(ps[0], go.proxMed);
            makeClickable(ps[1], go.proxCita);
            makeClickable(ps[2], go.alertas);
        }
    }

    // Mensajes
    {
        const c = byTitle("mensajes");
        if (c) {
            const ps = c.querySelectorAll("p");
            makeClickable(ps[0], go.mensajes);
            makeClickable(ps[1], go.notifs);
        }
    }

    // Configuración
    {
        const c = byTitle("configuración");
        if (c) {
            const ps = c.querySelectorAll("p");
            makeClickable(ps[0], go.perfil);
            makeClickable(ps[1], go.notifCfg);
        }
    }

    // Asistente
    {
        const c = byTitle("asistente");
        if (c) {
            const ps = c.querySelectorAll("p");
            makeClickable(ps[0], go.asistente);
        }
    }
});