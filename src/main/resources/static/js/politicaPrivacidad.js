// Resaltado activo segun sección visible
(function(){
    const sections = document.querySelectorAll('.policy__content .policy__section[id]');
    const map = new Map([...document.querySelectorAll('.policy__toc a')]
        .map(a => [a.getAttribute('href').split('#')[1], a]));
    const io = new IntersectionObserver((entries) => {
        entries.forEach(e => {
        const link = map.get(e.target.id);
        if(!link) return;
        if(e.isIntersecting){
            document.querySelectorAll('.policy__toc a.is-active')
            .forEach(x => x.classList.remove('is-active'));
            link.classList.add('is-active');
            link.scrollIntoView({block:'nearest'});
        }
        });
    }, { rootMargin: '-64px 0px -60% 0px', threshold: 0.1 });
    sections.forEach(s => io.observe(s));
})();

// TOC colapsable
(function(){
    const toc = document.querySelector('.policy__toc');
    const btn = toc?.querySelector('.policy__toc-toggle');
    if(!toc || !btn) return;
    btn.addEventListener('click', () => {
        const exp = toc.getAttribute('aria-expanded') === 'true';
        toc.setAttribute('aria-expanded', String(!exp));
        btn.setAttribute('aria-expanded', String(!exp));
    });
})();

(function(){
    const sections = document.querySelectorAll('.policy__content .policy__section[id]');
    const map = new Map([...document.querySelectorAll('.policy__toc a')]
        .map(a => [a.getAttribute('href').split('#')[1], a]));
    const io = new IntersectionObserver((entries)=>{
        entries.forEach(e=>{
        const link = map.get(e.target.id);
        if(!link) return;
        if(e.isIntersecting){
            document.querySelectorAll('.policy__toc a.is-active')
            .forEach(x=>x.classList.remove('is-active'));
            link.classList.add('is-active');
            link.scrollIntoView({block:'nearest'});
        }
        });
    },{rootMargin:'-64px 0px -60% 0px',threshold:0.1});
    sections.forEach(s=>io.observe(s));
})();


// Activo en el TOC según sección visible
(function(){
    const sections = document.querySelectorAll('.policy__content .policy__section[id]');
    const map = new Map([...document.querySelectorAll('.policy__toc a')]
    .map(a => [a.getAttribute('href').split('#')[1], a]));
    const io = new IntersectionObserver((entries)=>{
    entries.forEach(e=>{
        const link = map.get(e.target.id);
        if(!link) return;
        if(e.isIntersecting){
        document.querySelectorAll('.policy__toc a.is-active')
            .forEach(x=>x.classList.remove('is-active'));
        link.classList.add('is-active');
        link.scrollIntoView({block:'nearest'});
        }
    });
    },{rootMargin:'-64px 0px -60% 0px',threshold:0.1});
    sections.forEach(s=>io.observe(s));
})();

// TOC colapsable en móvil
(function(){
    const toc = document.querySelector('.policy__toc');
    const btn = toc?.querySelector('.policy__toc-toggle');
    if(!toc || !btn) return;
    btn.addEventListener('click', () => {
    const exp = toc.getAttribute('aria-expanded') === 'true';
    toc.setAttribute('aria-expanded', String(!exp));
    btn.setAttribute('aria-expanded', String(!exp));
    });
})();

// (Opcional) Evento para abrir ajustes del banner de cookies
// Lanza tu cuadro de configuración cuando el usuario haga clic en "Configuración de cookies"
// window.addEventListener('openCookieSettings', () => { openCookieModal(); });


(function(){
    const sections = document.querySelectorAll('.policy__content .policy__section[id]');
    const map = new Map([...document.querySelectorAll('.policy__toc a')]
    .map(a => [a.getAttribute('href').split('#')[1], a]));
    const io = new IntersectionObserver((entries)=>{
    entries.forEach(e=>{
        const link = map.get(e.target.id);
        if(!link) return;
        if(e.isIntersecting){
        document.querySelectorAll('.policy__toc a.is-active')
            .forEach(x=>x.classList.remove('is-active'));
        link.classList.add('is-active');
        link.scrollIntoView({block:'nearest'});
        }
    });
    },{rootMargin:'-64px 0px -60% 0px',threshold:0.1});
    sections.forEach(s=>io.observe(s));
})();

(function(){
    const toc = document.querySelector('.policy__toc');
    const btn = toc?.querySelector('.policy__toc-toggle');
    if(!toc || !btn) return;
    btn.addEventListener('click', () => {
    const exp = toc.getAttribute('aria-expanded') === 'true';
    toc.setAttribute('aria-expanded', String(!exp));
    btn.setAttribute('aria-expanded', String(!exp));
    });
})();