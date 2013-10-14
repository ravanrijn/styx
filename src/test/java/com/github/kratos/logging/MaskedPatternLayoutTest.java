package com.github.kratos.logging;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MaskedPatternLayoutTest {

    private MaskedPatternLayout maskedPatternLayout = new MaskedPatternLayout();

    @Test
    public void testMaskPasswordsShouldFailWhenToStringPasswordIsNotReplaced() {
        String message = maskedPatternLayout.maskPasswords("password=[pswpsw123]");
        assertEquals("Expected message to be replaced", "password=[********]", message);
    }

    @Test
    public void testMaskPasswordsShouldFailWhenJsonPasswordIsNotReplaced() {
        String message = maskedPatternLayout.maskPasswords("\"password\":\"pswpsw123\"");
        assertEquals("Expected message to be replaced", "\"password\":\"********\"", message);
    }

    @Test
    public void testMaskPasswordsShouldFailWhenPasswordLiteralIsReplaced() {
        String message = maskedPatternLayout.maskPasswords("\"grant_type\":\"password\"");
        assertEquals("Expected message to not be replaced", "\"grant_type\":\"password\"", message);
    }

}
