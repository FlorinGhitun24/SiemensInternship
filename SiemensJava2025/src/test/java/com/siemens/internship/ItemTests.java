package com.siemens.internship;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemTests {
    private final Item validator = new Item();

    @Test
    public void testValidEmail() {
        assertTrue(validator.validate("test@example.com"));
        assertTrue(validator.validate("user.name+first+second@example.co.uk"));
        assertTrue(validator.validate("user_name@example.org"));
    }

    @Test
    public void testInvalidEmail() {
        assertFalse(validator.validate("address"));
        assertFalse(validator.validate("@username.com"));
        assertFalse(validator.validate("username@.com"));
        assertFalse(validator.validate("username@site..com"));
    }
}
