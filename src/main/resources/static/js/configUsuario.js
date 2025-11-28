// Navegación entre secciones
function mostrarSeccion(id, btn) {
    // Ocultar todas las secciones
    document.querySelectorAll('.config-seccion').forEach(s => {
        s.classList.remove('activa');
    });

    // Mostrar sección seleccionada
    const seccion = document.getElementById(id);
    if (seccion) {
        seccion.classList.add('activa');
    }

    // Actualizar estado del menú
    document.querySelectorAll('.config-nav-item').forEach(b => {
        b.classList.remove('activo');
    });
    if (btn) {
        btn.classList.add('activo');
    }
}

// Toggle visibilidad de contraseña
function togglePassword(inputId, btn) {
    const input = document.getElementById(inputId);
    const iconEye = btn.querySelector('.icon-eye');
    const iconEyeOff = btn.querySelector('.icon-eye-off');

    if (input.type === 'password') {
        input.type = 'text';
        iconEye.style.display = 'none';
        iconEyeOff.style.display = 'block';
    } else {
        input.type = 'password';
        iconEye.style.display = 'block';
        iconEyeOff.style.display = 'none';
    }
}

// Indicador de fortaleza de contraseña
function checkPasswordStrength(password) {
    const fill = document.getElementById('strength-fill');
    const text = document.getElementById('strength-text');

    let strength = 0;
    let label = '';
    let color = '';

    if (password.length === 0) {
        fill.style.width = '0%';
        text.textContent = 'Introduce una contraseña';
        text.style.color = '#666';
        return;
    }

    // Criterios
    if (password.length >= 8) strength++;
    if (password.length >= 12) strength++;
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++;
    if (/\d/.test(password)) strength++;
    if (/[^a-zA-Z0-9]/.test(password)) strength++;

    switch (strength) {
        case 0:
        case 1:
            label = 'Muy débil';
            color = '#ef4444';
            break;
        case 2:
            label = 'Débil';
            color = '#f97316';
            break;
        case 3:
            label = 'Aceptable';
            color = '#eab308';
            break;
        case 4:
            label = 'Fuerte';
            color = '#22c55e';
            break;
        case 5:
            label = 'Muy fuerte';
            color = '#10b981';
            break;
    }

    fill.style.width = (strength * 20) + '%';
    fill.style.backgroundColor = color;
    text.textContent = label;
    text.style.color = color;
}

// Sistema de temas
(() => {
    const KEY = 'theme';

    function applyTheme(mode) {
        if (mode === 'auto') {
            const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            document.body.classList.toggle('dark-theme', prefersDark);
        } else {
            document.body.classList.toggle('dark-theme', mode === 'dark');
        }
    }

    function init() {
        const saved = localStorage.getItem(KEY) || 'auto';
        applyTheme(saved);

        // Sincroniza radios
        const r = document.querySelector(`input[name="tema"][value="${saved}"]`);
        if (r) r.checked = true;

        // Reacciona a cambios del sistema si está en "auto"
        const mql = window.matchMedia('(prefers-color-scheme: dark)');
        const onSystemChange = () => {
            if ((localStorage.getItem(KEY) || 'auto') === 'auto') applyTheme('auto');
        };
        mql.addEventListener ? mql.addEventListener('change', onSystemChange)
            : mql.addListener(onSystemChange);

        // Cambios desde el selector de tema
        document.querySelectorAll('input[name="tema"]').forEach(el => {
            el.addEventListener('change', (e) => {
                const v = e.target.value;
                localStorage.setItem(KEY, v);
                applyTheme(v);
            });
        });
    }

    document.addEventListener('DOMContentLoaded', init);
})();