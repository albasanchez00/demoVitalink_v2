document.addEventListener("DOMContentLoaded", () => {
    const mensajes = [
      { titulo: "Resultado de análisis disponible", remitente: "Centro Médico", fecha: "2024-04-14", cuerpo: "Tu análisis ya está disponible.", leido: false },
      { titulo: "Recordatorio: cita médica mañana", remitente: "Sistema", fecha: "2024-04-13", cuerpo: "Recuerda tu cita programada a las 10:00.", leido: false },
    ];

    const contenedor = document.querySelector(".config-usuario-formulario");
    contenedor.innerHTML = "";
    mensajes.forEach((msg, index) => {
      const art = document.createElement("article");
      art.className = "mensaje" + (msg.leido ? " leido" : "");
      art.innerHTML = `
        <h3>${msg.titulo}</h3>
        <p><strong>De:</strong> ${msg.remitente}</p>
        <p><strong>Fecha:</strong> ${msg.fecha}</p>
        <p>${msg.cuerpo}</p>
        <button onclick="marcarLeido(${index})">${msg.leido ? "Marcar como no leído" : "Marcar como leído"}</button>
      `;
      contenedor.appendChild(art);
    });

    window.marcarLeido = (i) => {
      mensajes[i].leido = !mensajes[i].leido;
      location.reload();
    };
});