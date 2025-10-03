package com.ceatformacion.demovitalink_v2.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CompositePasswordEncoder implements PasswordEncoder {
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(12);

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) return false;
        try {
            if (bcrypt.matches(rawPassword, encodedPassword)) return true;
        } catch (IllegalArgumentException ignore) {
            // si el formato del hash no es válido
        }
        // compatibilidad con contraseñas antiguas guardadas en plano
        return rawPassword.toString().equals(encodedPassword);
    }
}