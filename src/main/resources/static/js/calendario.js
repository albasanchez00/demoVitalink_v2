document.addEventListener('DOMContentLoaded', function () {
    const calendarEl = document.getElementById('calendar');

    const calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'es',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay'
        },
        events: '/api/citas',
        eventClick: function(info) {
            const title = info.event.title;
            const description = info.event.extendedProps.description || '';
            const location = "Consultorio Médico"; // Cambia esto si tienes dirección real

            const start = new Date(info.event.start);
            const end = new Date(info.event.end);

            // Formato requerido por Google Calendar: YYYYMMDDTHHmmssZ
            const formatDate = (date) => {
                return date.toISOString().replace(/[-:]/g, '').split('.')[0] + 'Z';
            };

            const startFormatted = formatDate(start);
            const endFormatted = formatDate(end);

            const gcalUrl = `https://calendar.google.com/calendar/render?action=TEMPLATE` +
                `&text=${encodeURIComponent(title)}` +
                `&dates=${startFormatted}/${endFormatted}` +
                `&details=${encodeURIComponent(description)}` +
                `&location=${encodeURIComponent(location)}`;

            const confirmar = confirm(`📅 ${title}\n📝 ${description}\n\n¿Quieres agregar esta cita a tu Google Calendar?`);
            if (confirmar) {
                window.open(gcalUrl, '_blank');
            }
        }
    });

    calendar.render();
});
