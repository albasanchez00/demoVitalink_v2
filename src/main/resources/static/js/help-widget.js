class HelpWidget {
    constructor() {
        this.isOpen = false;
        this.currentPage = window.location.pathname;
        this.init();
    }

    init() {
        this.createWidget();
        this.loadContextualHelp();
    }

    createWidget() {
        // Botón flotante
        const button = document.createElement('div');
        button.innerHTML = `
            <div id="help-button" style="
                position: fixed;
                bottom: 20px;
                right: 20px;
                width: 60px;
                height: 60px;
                background: #3498db;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                z-index: 9999;
            ">
                <span style="color: white; font-size: 24px;">?</span>
            </div>
        `;

        // Panel de ayuda
        const panel = document.createElement('div');
        panel.innerHTML = `
            <div id="help-panel" style="
                position: fixed;
                right: 20px;
                bottom: 100px;
                width: 350px;
                max-height: 500px;
                background: white;
                border-radius: 10px;
                box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                display: none;
                z-index: 9998;
                overflow: hidden;
            ">
                <div style="padding: 20px; border-bottom: 1px solid #eee;">
                    <h3 style="margin: 0;">💡 Ayuda Rápida</h3>
                </div>
                <div id="help-content" style="padding: 20px; overflow-y: auto; max-height: 400px;">
                    <!-- Contenido dinámico -->
                </div>
            </div>
        `;

        document.body.appendChild(button);
        document.body.appendChild(panel);

        // Event listeners
        document.getElementById('help-button').addEventListener('click', () => {
            this.toggle();
        });
    }

    loadContextualHelp() {
        const content = document.getElementById('help-content');
        let helpText = '';

        // Ayuda contextual según la página
        if (this.currentPage.includes('/citas')) {
            helpText = `
                <h4>📅 Gestión de Citas</h4>
                <p><strong>Para crear una cita:</strong></p>
                <ol>
                    <li>Haz clic en "Nueva Cita"</li>
                    <li>Selecciona el médico</li>
                    <li>Elige fecha y hora</li>
                    <li>Describe tu motivo</li>
                </ol>
                <p><strong>Estados de cita:</strong></p>
                <ul>
                    <li>🟡 Pendiente - Esperando confirmación</li>
                    <li>🟢 Confirmada - Cita aceptada</li>
                    <li>🔴 Cancelada - Cita anulada</li>
                    <li>✅ Completada - Cita realizada</li>
                </ul>
            `;
        } else if (this.currentPage.includes('/sintomas')) {
            helpText = `
                <h4>🩺 Registro de Síntomas</h4>
                <p><strong>¿Cómo registrar un síntoma?</strong></p>
                <ol>
                    <li>Haz clic en "Nuevo Síntoma"</li>
                    <li>Selecciona la zona corporal afectada</li>
                    <li>Describe detalladamente (máx 500 caracteres)</li>
                    <li>Guarda el registro</li>
                </ol>
                <p><strong>Tip:</strong> Sé específico en la descripción para ayudar a tu médico.</p>
            `;
        } else if (this.currentPage.includes('/chat')) {
            helpText = `
                <h4>💬 Chat con tu Médico</h4>
                <p><strong>Funciones disponibles:</strong></p>
                <ul>
                    <li>✉️ Enviar mensajes instantáneos</li>
                    <li>✓✓ Ver confirmación de lectura</li>
                    <li>🔇 Silenciar conversaciones</li>
                    <li>📁 Archivar chats antiguos</li>
                </ul>
                <p><strong>Nota:</strong> Los mensajes se envían en tiempo real.</p>
            `;
        } else {
            helpText = `
                <h4>👋 Bienvenido a VitaLink</h4>
                <p>Sistema integral de gestión de salud.</p>
                <p><strong>Navegación rápida:</strong></p>
                <ul>
                    <li><a href="/docs" style="color: #0A2540;">📚 Documentación completa</a></li>
                    <li><a href="/citas" style="color: #0A2540;">📅 Mis citas</a></li>
                    <li><a href="/sintomas" style="color: #0A2540;">🩺 Mis síntomas</a></li>
                    <li><a href="/tratamientos" style="color: #0A2540;">💊 Mis tratamientos</a></li>
                    <li><a href="/chat" style="color: #0A2540;">💬 Mensajes</a></li>
                </ul>
            `;
        }
        content.innerHTML = helpText;
    }

    toggle() {
        const panel = document.getElementById('help-panel');
        this.isOpen = !this.isOpen;
        panel.style.display = this.isOpen ? 'block' : 'none';
    }
}

// Inicializar cuando cargue la página
document.addEventListener('DOMContentLoaded', () => {
    new HelpWidget();
});