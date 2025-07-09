document.addEventListener('DOMContentLoaded', function () {
    var calendarEl = document.getElementById('calendar');

    var calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        locale: 'es', // opcional, para que se muestre en español
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay'
        },
        events: {
            url: '/api/citas',
            method: 'GET',
            extraParams: {
                username: username // <--- Cambia esto si vas a pasarlo dinámicamente
            },
            failure: function () {
                alert('Error al cargar las citas');
            }
        }
    });
    calendar.render();
});
