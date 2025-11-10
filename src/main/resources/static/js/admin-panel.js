/* =========================================================================
 *  ADMIN — Panel de Usuarios (ROLE_ADMIN)
 *  Control de pestañas "Médicos / Pacientes"
 * ========================================================================= */
(() => {
    // Aseguramos que estamos en la vista correcta
    const navTabs = document.querySelectorAll(".us-tab");
    const seccionMedicos = document.getElementById("usuarios-medicos");
    const seccionPacientes = document.getElementById("usuarios-pacientes");

    if (!navTabs.length || !seccionMedicos || !seccionPacientes) return;

    // Inicialización: mostrar Médicos por defecto
    function mostrarSeccion(tipo) {
        // Oculta ambas secciones
        seccionMedicos.style.display = "none";
        seccionPacientes.style.display = "none";

        // Quita la clase activa a todas las pestañas
        navTabs.forEach(t => t.classList.remove("is-active"));

        // Muestra la sección elegida
        if (tipo === "medicos") {
            seccionMedicos.style.display = "block";
            document.querySelector('[data-tab="medicos"]')?.classList.add("is-active");
        } else if (tipo === "pacientes") {
            seccionPacientes.style.display = "block";
            document.querySelector('[data-tab="pacientes"]')?.classList.add("is-active");
        }

        // Guarda la preferencia en localStorage (opcional)
        localStorage.setItem("adminTab", tipo);
    }

    // Recuperar la última pestaña activa (si existe)
    const lastTab = localStorage.getItem("adminTab") || "medicos";
    mostrarSeccion(lastTab);

    // Evento click para alternar entre pestañas
    navTabs.forEach(tab => {
        tab.addEventListener("click", e => {
            e.preventDefault();
            const tipo = tab.dataset.tab; // “medicos” o “pacientes”
            mostrarSeccion(tipo);
        });
    });

    console.log("✅ admin-panel.js cargado correctamente (control de pestañas)");
})();