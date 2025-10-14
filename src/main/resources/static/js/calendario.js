document.addEventListener('DOMContentLoaded', function () {
    const calendarEl = document.getElementById('calendar');
    if (!calendarEl) return;

    // Helpers
    const colorPorEstado = {
        PENDIENTE:  '#f59e0b',
        CONFIRMADA: '#16a34a',
        CANCELADA:  '#ef4444'
    };

    // Fuente de eventos según modo
    const buildEventsSource = () => {
        const mode = (window.CAL_MODE || 'USER').toUpperCase();

        if (mode === 'MEDICO') {
            // FullCalendar nos pasa el rango visible
            return function(fetchInfo, success, failure) {
                const params = new URLSearchParams({
                    desde: fetchInfo.startStr.substring(0,10),
                    hasta: fetchInfo.endStr.substring(0,10)
                });
                if (window.MEDICO_ID) params.set('medicoId', String(window.MEDICO_ID));

                fetch('/medico/citas/events?' + params.toString(), { headers: { 'Accept': 'application/json' }})
                    .then(r => r.ok ? r.json() : Promise.reject(r.statusText))
                    .then(data => success(data)) // [{ id,title,start,end,estado,pacienteNombre,descripcion }]
                    .catch(failure);
            };
        }

        // Paciente
        return '/api/citas/mias'; // Ajusta si tu endpoint es otro
    };

    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'es',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay'
        },
        firstDay: 1,
        timeZone: 'local',
        events: buildEventsSource(),

        // Pintar color por estado
        eventDidMount(info) {
            const estado = info.event.extendedProps.estado;
            const c = colorPorEstado[estado] || '#3b82f6';
            info.el.style.backgroundColor = c;
            info.el.style.borderColor = c;

            // Tooltip simple con nombre de paciente si existe
            const pac = info.event.extendedProps.pacienteNombre;
            if (pac) info.el.title = `${info.event.title} — ${pac}`;
        },

        // Título + subinfo (estado o paciente)
        eventContent(arg) {
            const t = arg.event.title || '';
            const estado = arg.event.extendedProps.estado || '';
            const pac = arg.event.extendedProps.pacienteNombre || '';
            const sub = pac || estado;
            return { html: `<div><strong>${t}</strong><div style="font-size:11px">${sub}</div></div>` };
        },

        // Click → abrir Google Calendar
        eventClick: function(info) {
            const title = info.event.title || 'Cita';
            const ext = info.event.extendedProps || {};
            const description = ext.description || ext.descripcion || ''; // soporta ambos nombres
            const location = "Consultorio Médico"; // cámbialo si tienes dirección real

            const start = new Date(info.event.start);
            // si tu backend no envía end, calculamos +60min por defecto
            const end = info.event.end || new Date(start.getTime() + 60 * 60000);

            const formatDate = (date) => date.toISOString().replace(/[-:]/g, '').split('.')[0] + 'Z';

            const gcalUrl = `https://calendar.google.com/calendar/render?action=TEMPLATE`
                + `&text=${encodeURIComponent(title)}`
                + `&dates=${formatDate(start)}/${formatDate(end)}`
                + `&details=${encodeURIComponent(description)}`
                + `&location=${encodeURIComponent(location)}`;

            const confirmar = confirm(`📅 ${title}\n📝 ${description}\n\n¿Quieres agregar esta cita a tu Google Calendar?`);
            if (confirmar) window.open(gcalUrl, '_blank');
        }
    });

    calendar.render();
});