package com.ceatformacion.demovitalink_v2.utils;

import java.util.Random;

public class PasswordGenerator {
    public static String generar(int longitud) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < longitud; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }

        return sb.toString();
    }
}
