package com.ssd.agenda_SSD_back.util;

import java.security.SecureRandom;

/**
 * Gera a senha temporária de primeiro acesso que o admin manda pro usuário
 * (ver roadmap "senha inicial gerada pelo admin"). Evita caracteres
 * ambíguos (0/O, 1/l/I) porque essa senha é lida e digitada por alguém, não
 * colada de um gerenciador de senhas.
 */
public final class PasswordGenerator {
    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGenerator() {
    }

    public static String generate() {
        StringBuilder senha = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            senha.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return senha.toString();
    }
}
