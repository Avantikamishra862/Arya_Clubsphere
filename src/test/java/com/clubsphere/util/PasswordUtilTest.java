package com.clubsphere.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void hashesAndVerifiesAPassword() {
        String hash = PasswordUtil.hashPassword("password123");
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(PasswordUtil.checkPassword("password123", hash));
        assertFalse(PasswordUtil.checkPassword("wrong-password", hash));
    }

    @Test
    void producesDifferentHashesForTheSamePassword() {
        String first = PasswordUtil.hashPassword("password123");
        String second = PasswordUtil.hashPassword("password123");
        assertNotEquals(first, second);
        assertTrue(PasswordUtil.checkPassword("password123", first));
        assertTrue(PasswordUtil.checkPassword("password123", second));
    }

    @Test
    void rejectsNullInputs() {
        assertFalse(PasswordUtil.checkPassword(null, "$2a$10$abcdefghijklmnopqrstuv"));
        assertFalse(PasswordUtil.checkPassword("password123", null));
        assertFalse(PasswordUtil.checkPassword(null, null));
    }
}
