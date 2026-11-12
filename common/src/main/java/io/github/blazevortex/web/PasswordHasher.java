package io.github.blazevortex.web;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public final class PasswordHasher {

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;

    private PasswordHasher() {
    }

    public static String hash(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Password hashing failed", ex);
        }
    }

    public static byte[] newSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public static byte[] decodeSalt(String value) {
        if (value == null || value.isBlank()) {
            return new byte[0];
        }
        try {
            return HexFormat.of().parseHex(value);
        } catch (Exception ex) {
            return Base64.getDecoder().decode(value);
        }
    }

    public static boolean verify(String password, String hash, String saltHex) {
        if (hash == null || hash.isBlank()) {
            return false;
        }
        String computed = hash(password, decodeSalt(saltHex));
        if (computed.length() != hash.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < computed.length(); i++) {
            diff |= computed.charAt(i) ^ hash.charAt(i);
        }
        return diff == 0;
    }
}
