package com.icontrol.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class PasswordUtils {

    public static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generando hash", e);
        }
    }

    public static boolean matches(String raw, String storedHash) {
        return sha256(raw).equalsIgnoreCase(storedHash);
    }
}
