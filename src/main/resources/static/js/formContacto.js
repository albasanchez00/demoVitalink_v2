document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("formulario").addEventListener("submit", validarFormulario);
});

function validarFormulario(event) {
    event.preventDefault(); // Evita el envío del formulario si hay errores

    var nombre = document.getElementById("nombre").value.trim();
    var correo = document.getElementById("correo").value.trim();
    var telefono = document.getElementById("telefono").value.trim();

    var regexCorreo = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    var regexTelefono = /^(7|8|9)\d{8}$/; // Teléfono español: 9 dígitos y empieza con 7, 8 o 9

    // Validar que el nombre no esté vacío
    if (nombre === "") {
        alert("Por favor, ingresa tu nombre.");
        return;
    }

    // Validar que el correo no esté vacío y tenga un formato válido
    if (correo === "") {
        alert("Por favor, ingresa tu correo electrónico.");
        return;
    } else if (!regexCorreo.test(correo)) {
        alert("Por favor, ingresa un correo electrónico válido.");
        return;
    }

    // Validar el número de teléfono si se ha ingresado
    if (telefono !== "" && !regexTelefono.test(telefono)) {
        alert("El número de teléfono debe ser español, contener 9 dígitos y empezar con 7, 8 o 9.");
        return;
    }

    // Si todo es correcto, mostrar mensaje de éxito y redirigir a contacto.html
    alert("Formulario enviado correctamente.");

    // Redirigir a la página de contacto (ajusta la ruta según la ubicación real)
    window.location.href = "../Vista/contacto.html"; // Ajusta esta ruta según sea necesario
}
