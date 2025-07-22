async function handleKey(event) {
  if (event.key === "Enter") {
    const input = document.getElementById("chat-input");
    const message = input.value.trim();
    if (!message) return;

    appendMessage("Usuario", message);
    input.value = ""; // Limpiar el input

    try {
      const response = await fetch("https://primary-production-8eee.up.railway.app/webhook/asistente", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message })
      });

      const data = await response.json();

      if (data.reply) {
        appendMessage("Asistente", data.reply);
      } else {
        appendMessage("Asistente", "Lo siento, no pude entender tu mensaje.");
      }
    } catch (error) {
      console.error("Error en el asistente:", error);
      appendMessage("Asistente", "Ocurrió un error al procesar tu mensaje.");
    }
  }
}

function appendMessage(sender, text) {
  const messagesContainer = document.getElementById("chat-messages");
  const messageElement = document.createElement("div");
  messageElement.innerHTML = `<strong>${sender}:</strong> ${text}`;
  messagesContainer.appendChild(messageElement);
}
