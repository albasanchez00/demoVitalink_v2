// === Panel Multi-Rol: USER, ADMIN, MEDICO ===
document.addEventListener("DOMContentLoaded", () => {

    // ====== PANEL DE USUARIO (USER) ======
    const panelUsuario = document.getElementById("panel-usuario");
    if (panelUsuario) {
        const goUser = {
            evolSintomas: "/usuarios/estadisticas?s=sintomas",
            progTrat:     "/tratamientos",
            adherencia:   "/usuarios/estadisticas?s=adherencia",
            citasPasadas: "/usuarios/historialPaciente",
            informes:     "/usuarios/historialPaciente#informes",
            proxMed:      "/tratamientos",
            proxCita:     "/pedirCita",
            alertas:      "/tratamientos?f=alertas",
            mensajes:     "/usuarios/mensajes",
            notifs:       "/usuarios/notificaciones",
            perfil:       "/usuarios/configUsuario",
            notifCfg:     "/usuarios/configUsuario#notificaciones",
            asistente:    "#",
            consultar:    "#"
        };

        setupPanel(panelUsuario, goUser);
    }

    // ====== PANEL DE ADMIN ======
    const panelAdmin = document.getElementById("panel-admin");
    if (panelAdmin) {
        const goAdmin = {
            usuarios:         "/admin/usuarios",
            usuariosActivos:  "/admin/usuarios?filter=activos",
            usuariosInactivos:"/admin/usuarios?filter=inactivos",
            tratActivos:      "/admin/tratamientos?filter=activos",
            tratRevision:     "/admin/tratamientos?filter=revision",
            citasTotales:     "/admin/citas",
            citasHoy:         "/admin/citas?filter=hoy",
            reportes:         "/admin/estadisticas",
            areasMonitoreadas:"/admin/estadisticas#areas",
            mensajes:         "/admin/chat",
            notifs:           "/admin/chat#notificaciones",
            configsSistema:   "/admin/config",
            ultimaActualizacion: "/admin/config#updates"
        };

        setupPanel(panelAdmin, goAdmin);
    }

    // ====== PANEL DE MEDICO ======
    const panelMedico = document.getElementById("panel-medico");
    if (panelMedico) {
        const goMedico = {
            pacientesTotal:   "/medico/pacientes",
            pacientesActivos: "/medico/pacientes?filter=activos",
            citasHoy:         "/medico/citas?filter=hoy",
            proximaCita:      "/medico/citas",
            tratActivos:      "/medico/tratamientos?filter=activos",
            tratSeguimiento:  "/medico/tratamientos?filter=seguimiento",
            reportesRecientes:"/medico/sintomas",
            alertasDetectadas:"/medico/sintomas?filter=alertas",
            mensajes:         "/medico/chat",
            notifs:           "/medico/chat#notificaciones",
            configsMedico:    "/medico/configMedico",
            ultimaActualizacion: "/medico/configMedico#updates"
        };

        setupPanel(panelMedico, goMedico);
    }

    // ===== FUNCIÓN COMÚN PARA CONFIGURAR PANELES =====
    function setupPanel(root, routes) {
        const makeClickable = (p, href) => {
            if (!p || !href) return;
            p.classList.add("p-as-link");
            p.tabIndex = 0;
            p.style.cursor = "pointer";

            const navigate = () => {
                if (href === "#") return; // Enlaces de asistente
                location.href = href;
            };

            p.addEventListener("click", navigate);
            p.addEventListener("keypress", (e) => {
                if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    navigate();
                }
            });
        };

        const cards = Array.from(root.querySelectorAll(".card"));
        const byTitle = (keywords) => {
            const lowerKeywords = keywords.toLowerCase();
            return cards.find((c) => {
                const title = c.querySelector("h3")?.textContent.trim().toLowerCase() || "";
                return title.includes(lowerKeywords);
            });
        };

        // Detectar rol basado en el ID del panel
        const isUser = root.id === "panel-usuario";
        const isAdmin = root.id === "panel-admin";
        const isMedico = root.id === "panel-medico";

        if (isUser) {
            // USUARIO: Estadísticas
            const cardEst = byTitle("estadísticas");
            if (cardEst) {
                const ps = cardEst.querySelectorAll("p");
                makeClickable(ps[0], routes.evolSintomas);
                makeClickable(ps[1], routes.progTrat);
                makeClickable(ps[2], routes.adherencia);
            }

            // USUARIO: Historial
            const cardHist = byTitle("historial");
            if (cardHist) {
                const ps = cardHist.querySelectorAll("p");
                makeClickable(ps[0], routes.citasPasadas);
                makeClickable(ps[1], routes.informes);
            }

            // USUARIO: Recordatorios
            const cardRec = byTitle("recordatorios");
            if (cardRec) {
                const ps = cardRec.querySelectorAll("p");
                makeClickable(ps[0], routes.proxMed);
                makeClickable(ps[1], routes.proxCita);
                makeClickable(ps[2], routes.alertas);
            }

            // USUARIO: Mensajes
            const cardMsg = byTitle("mensajes");
            if (cardMsg) {
                const ps = cardMsg.querySelectorAll("p");
                makeClickable(ps[0], routes.mensajes);
                makeClickable(ps[1], routes.notifs);
            }

            // USUARIO: Configuración
            const cardConf = byTitle("configuración");
            if (cardConf) {
                const ps = cardConf.querySelectorAll("p");
                makeClickable(ps[0], routes.perfil);
                makeClickable(ps[1], routes.notifCfg);
            }

            // USUARIO: Asistente
            const cardAst = byTitle("asistente");
            if (cardAst) {
                const ps = cardAst.querySelectorAll("p");
                makeClickable(ps[0], routes.consultar);
            }
        }

        if (isAdmin) {
            // ADMIN: Gestión de Usuarios
            const cardUsuarios = byTitle("usuarios");
            if (cardUsuarios) {
                const ps = cardUsuarios.querySelectorAll("p");
                makeClickable(ps[0], routes.usuarios);
                makeClickable(ps[1], routes.usuariosActivos); // Para "Activos"
            }

            // ADMIN: Tratamientos
            const cardTrat = byTitle("tratamientos");
            if (cardTrat) {
                const ps = cardTrat.querySelectorAll("p");
                makeClickable(ps[0], routes.tratActivos);
                makeClickable(ps[1], routes.tratRevision);
            }

            // ADMIN: Agenda y Citas
            const cardCitas = byTitle("agenda");
            if (cardCitas) {
                const ps = cardCitas.querySelectorAll("p");
                makeClickable(ps[0], routes.citasTotales);
                makeClickable(ps[1], routes.citasHoy);
            }

            // ADMIN: Estadísticas Globales
            const cardEst = byTitle("estadísticas");
            if (cardEst) {
                const ps = cardEst.querySelectorAll("p");
                makeClickable(ps[0], routes.reportes);
                makeClickable(ps[1], routes.areasMonitoreadas);
            }

            // ADMIN: Mensajería
            const cardMsg = byTitle("mensajería");
            if (cardMsg) {
                const ps = cardMsg.querySelectorAll("p");
                makeClickable(ps[0], routes.mensajes);
                makeClickable(ps[1], routes.notifs);
            }

            // ADMIN: Configuraciones
            const cardConf = byTitle("configuraciones");
            if (cardConf) {
                const ps = cardConf.querySelectorAll("p");
                makeClickable(ps[0], routes.configsSistema);
                makeClickable(ps[1], routes.ultimaActualizacion);
            }
        }

        if (isMedico) {
            // MEDICO: Pacientes Vinculados
            const cardPac = byTitle("pacientes");
            if (cardPac) {
                const ps = cardPac.querySelectorAll("p");
                makeClickable(ps[0], routes.pacientesTotal);
                makeClickable(ps[1], routes.pacientesActivos);
            }

            // MEDICO: Citas Programadas
            const cardCitas = byTitle("citas");
            if (cardCitas) {
                const ps = cardCitas.querySelectorAll("p");
                makeClickable(ps[0], routes.citasHoy);
                makeClickable(ps[1], routes.proximaCita);
            }

            // MEDICO: Tratamientos
            const cardTrat = byTitle("tratamientos");
            if (cardTrat) {
                const ps = cardTrat.querySelectorAll("p");
                makeClickable(ps[0], routes.tratActivos);
                makeClickable(ps[1], routes.tratSeguimiento);
            }

            // MEDICO: Reportes Sintomáticos
            const cardRep = byTitle("reportes");
            if (cardRep) {
                const ps = cardRep.querySelectorAll("p");
                makeClickable(ps[0], routes.reportesRecientes);
                makeClickable(ps[1], routes.alertasDetectadas);
            }

            // MEDICO: Mensajería
            const cardMsg = byTitle("mensajería");
            if (cardMsg) {
                const ps = cardMsg.querySelectorAll("p");
                makeClickable(ps[0], routes.mensajes);
                makeClickable(ps[1], routes.notifs);
            }

            // MEDICO: Configuración
            const cardConf = byTitle("configuración");
            if (cardConf) {
                const ps = cardConf.querySelectorAll("p");
                makeClickable(ps[0], routes.configsMedico);
                makeClickable(ps[1], routes.ultimaActualizacion);
            }
        }
    }
});