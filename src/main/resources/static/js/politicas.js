// ==================================================
// RESALTADO ACTIVO SEGÚN SECCIÓN VISIBLE EN EL TOC
// ==================================================
(function() {
    const sections = document.querySelectorAll('.policy__content .policy__section[id]');
    const tocLinks = document.querySelectorAll('.policy__toc a');

    // Crear mapa de ID de sección -> enlace del TOC
    const map = new Map();
    tocLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (href && href.includes('#')) {
            const id = href.split('#')[1];
            map.set(id, link);
        }
    });

    // Intersection Observer para detectar qué sección está visible
    const io = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            const link = map.get(entry.target.id);
            if (!link) return;

            if (entry.isIntersecting) {
                // Remover clase activa de todos los enlaces
                tocLinks.forEach(l => l.classList.remove('is-active'));

                // Añadir clase activa al enlace actual
                link.classList.add('is-active');

                // SOLO hacer scroll en el TOC (sidebar), NO en la página principal
                // Verificar que el elemento está dentro del TOC antes de hacer scroll
                const tocContainer = link.closest('.policy__toc');
                if (tocContainer && link.offsetParent) {
                    // Calcular posición relativa dentro del TOC
                    const tocRect = tocContainer.getBoundingClientRect();
                    const linkRect = link.getBoundingClientRect();

                    // Solo hacer scroll si el enlace no está visible en el TOC
                    if (linkRect.top < tocRect.top || linkRect.bottom > tocRect.bottom) {
                        link.scrollIntoView({ block: 'nearest', behavior: 'auto', inline: 'nearest' });
                    }
                }
            }
        });
    }, {
        rootMargin: '-64px 0px -60% 0px',
        threshold: 0.1
    });

    // Observar todas las secciones
    sections.forEach(section => io.observe(section));
})();

// ==================================================
// TOC COLAPSABLE EN MÓVIL
// ==================================================
(function() {
    const toc = document.querySelector('.policy__toc');
    const btn = toc?.querySelector('.policy__toc-toggle');

    if (!toc || !btn) return;

    btn.addEventListener('click', () => {
        const isExpanded = toc.getAttribute('aria-expanded') === 'true';
        const newState = !isExpanded;

        toc.setAttribute('aria-expanded', String(newState));
        btn.setAttribute('aria-expanded', String(newState));
    });
})();

// ==================================================
// EVENTO PARA ABRIR CONFIGURACIÓN DE COOKIES
// ==================================================
// Este evento se dispara cuando el usuario hace clic en "Configuración de cookies"
// dentro de politicaCookies.html
window.addEventListener('openCookieSettings', () => {
    // Aquí deberías llamar a tu función que abre el modal/banner de cookies
    // Ejemplo: openCookieModal();
    console.log('Abrir configuración de cookies');
});