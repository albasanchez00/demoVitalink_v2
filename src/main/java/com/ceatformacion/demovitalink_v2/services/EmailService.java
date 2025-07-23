package com.ceatformacion.demovitalink_v2.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public boolean enviarCredenciales(String destinatario, String nombre, String contrasena) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("Tus credenciales de acceso");
            mensaje.setText("Hola " + nombre + ",\n\nTu cuenta ha sido creada.\n" +
                    "Usuario: " + destinatario + "\n" +
                    "Contraseña: " + contrasena + "\n\nPor favor, cambia la contraseña tras iniciar sesión.");
            mailSender.send(mensaje);
            logger.info("Correo enviado exitosamente a: " + destinatario);
            return true;
        } catch (Exception e) {
            logger.error("Error al enviar correo a " + destinatario + ": " + e.getMessage());
            return false;
        }
    }

}
