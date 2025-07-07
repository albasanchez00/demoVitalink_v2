document.addEventListener('DOMContentLoaded', function() {
    inicializarCalendario();

    // Inicializar filtros
    document.getElementById('filtro-estado').addEventListener('change', filtrarCitas);
});

function inicializarCalendario() {
    const calendarEl = document.getElementById('calendario');

    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,listMonth'
        },
        locale: 'es',
        eventDisplay: 'block',
        displayEventEnd: true,
        eventTimeFormat: {
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        },

        // Los eventos se cargarán desde el backend
        events: '/api/paciente/citas',

        eventClick: function(info) {
            mostrarDetallesCita(info.event.id);
        },

        eventDidMount: function(info) {
            // Personalizar la apariencia según el estado
            if (info.event.extendedProps.estado === 'cancelada') {
                info.el.style.textDecoration = 'line-through';
                info.el.style.opacity = '0.6';
            }
        }
    });

    calendar.render();
}

function cambiarVista(vista) {
    const btnCalendario = document.querySelector('.tab-btn:nth-child(1)');
    const btnLista = document.querySelector('.tab-btn:nth-child(2)');
    const vistaCalendario = document.getElementById('vista-calendario');
    const vistaLista = document.getElementById('vista-lista');

    if (vista === 'calendario') {
        vistaCalendario.style.display = 'block';
        vistaLista.style.display = 'none';
        btnCalendario.classList.add('active');
        btnLista.classList.remove('active');
    } else {
        vistaCalendario.style.display = 'none';
        vistaLista.style.display = 'block';
        btnCalendario.classList.remove('active');
        btnLista.classList.add('active');
    }
}

function mostrarDetallesCita(citaId) {
    fetch(`/api/citas/${citaId}`)
        .then(response => response.json())
        .then(cita => {
            const detalles = document.getElementById('detalles-cita');
            detalles.innerHTML = `
                <div class="detalles-grid">
                    <p><strong>Fecha:</strong> ${formatearFecha(cita.fecha)}</p>
                    <p><strong>Hora:</strong> ${cita.hora}</p>
                    <p><strong>Doctor:</strong> ${cita.doctor}</p>
                    <p><strong>Tipo:</strong> ${cita.tipo}</p>
                    <p><strong>Estado:</strong> <span class="estado-${cita.estado}">${cita.estado}</span></p>
                    <p><strong>Motivo:</strong> ${cita.motivo}</p>
                    ${cita.notas ? `<p><strong>Notas:</strong> ${cita.notas}</p>` : ''}
                </div>
            `;
            document.getElementById('modal-detalles').style.display = 'block';
        });
}

function cancelarCita(citaId) {
    if (confirm('¿Estás seguro de que deseas cancelar esta cita?')) {
        fetch(`/api/citas/${citaId}/cancelar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (response.ok) {
                    location.reload();
                } else {
                    alert('No se pudo cancelar la cita. Por favor, intenta más tarde.');
                }
            });
    }
}

function filtrarCitas() {
    const estado = document.getElementById('filtro-estado').value;
    const citas = document.querySelectorAll('.tarjeta-cita');

    citas.forEach(cita => {
        if (estado === 'todas' || cita.classList.contains(estado)) {
            cita.style.display = 'flex';
        } else {
            cita.style.display = 'none';
        }
    });
}

function formatearFecha(fecha) {
    return new Date(fecha).toLocaleDateString('es-ES', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

function cerrarModal() {
    document.getElementById('modal-detalles').style.display = 'none';
}