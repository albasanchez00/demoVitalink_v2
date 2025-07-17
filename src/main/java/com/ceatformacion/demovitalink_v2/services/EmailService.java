package com.ceatformacion.demovitalink_v2.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void enviarCredenciales(String destinatario, String nombre, String contrasena) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Tus credenciales de acceso");
        mensaje.setText("Hola " + nombre + ",\n\nTu cuenta ha sido creada.\n" +
                "Usuario: " + destinatario + "\n" +
                "Contraseña: " + contrasena + "\n\nPor favor, cambia la contraseña tras iniciar sesión.");
        mailSender.send(mensaje);
    }
}
