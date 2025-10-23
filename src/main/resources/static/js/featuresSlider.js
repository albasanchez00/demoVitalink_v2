// /js/featuresSlider.js
(() => {
    const root = document.querySelector('.features-slider');
    if (!root) return;
    const track = root.querySelector('.slider-track');
    const slides = Array.from(root.querySelectorAll('.slide'));
    const prev = root.querySelector('.prev');
    const next = root.querySelector('.next');
    const status = root.querySelector('.slider-status');

    let index = 0;
    const total = slides.length;

    function update() {
        const width = slides[0].getBoundingClientRect().width;
        track.style.translate = `-${index * width}px 0`;
        if (status) status.textContent = `Slide ${index + 1} de ${total}`;
        prev.disabled = index === 0;
        next.disabled = index === total - 1;
    }

    prev?.addEventListener('click', () => { index = Math.max(0, index - 1); update(); });
    next?.addEventListener('click', () => { index = Math.min(total - 1, index + 1); update(); });

    // Teclado
    root.addEventListener('keydown', (e) => {
        if (e.key === 'ArrowLeft') { prev.click(); }
        if (e.key === 'ArrowRight') { next.click(); }
    });
    // Resizing
    window.addEventListener('resize', () => requestAnimationFrame(update));

    // Inicio
    update();
})();