// 👉 4. Función para mostrar u ocultar el chat (puede llamarse desde HTML)
function toggleChat() {
    const box = document.getElementById("chat-box");
    box.style.display = (box.style.display === "none" || box.style.display === "") ? "flex" : "none";
}

// 👉 5. Envío de mensajes y conexión con la IA
async function handleKey(event) {
    if (event.key === "Enter") {
        const input = document.getElementById("chat-input");
        const message = input.value.trim();
        if (!message) return;

        appendMessage("Usuario", message);
        input.value = "";

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

// 👉 6. Función para mostrar mensajes en el chat
function appendMessage(sender, text) {
    const messagesContainer = document.getElementById("chat-messages");
    const messageElement = document.createElement("div");
    messageElement.innerHTML = `<strong>${sender}:</strong> ${text}`;
    messagesContainer.appendChild(messageElement);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}