document.addEventListener('DOMContentLoaded', function() {
    // Seleccionamos el contenedor del calendario en el HTML
    var calendarEl = document.getElementById('calendar');

    // Inicializamos FullCalendar en el contenedor
    var calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth', // Vista por defecto: mes
        events: [], // Aquí se cargarán las citas más adelante (desde un backend)
    });

    // Renderizamos el calendario en la página
    calendar.render();

    // Capturamos el formulario para registrar una nueva cita
    document.getElementById('form-cita').addEventListener('submit', function(event) {
        event.preventDefault(); // Evita el envío automático del formulario

        // Obtener los valores del formulario
        var paciente = document.getElementById('paciente').value;
        var fecha = document.getElementById('fecha').value;
        var tipoAtencion = document.getElementById('tipo').value;

        // Validar que todos los campos estén completos
        if (paciente && fecha && tipoAtencion) {
            var tipoTexto = document.querySelector(`#tipo option[value="${tipoAtencion}"]`).textContent;

            // Crear un nuevo evento (cita) con los datos ingresados
            var nuevaCita = {
                title: `${paciente} - ${tipoTexto}`, // Nombre del paciente y tipo de cita
                start: fecha // Fecha y hora de la cita
            };

            // Agregar la cita al calendario
            calendar.addEvent(nuevaCita);

            // Limpiar el formulario después de agregar la cita
            document.getElementById('paciente').value = "";
            document.getElementById('fecha').value = "";
            document.getElementById('tipo').value = "medica_presencial";
        } else {
            alert("Por favor, completa todos los campos.");
        }
    });
});


