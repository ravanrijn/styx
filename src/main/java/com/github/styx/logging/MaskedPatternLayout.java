package com.github.styx.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * A logback pattern layout that performs masking of passwords.
 */
public class MaskedPatternLayout extends PatternLayout {

    @Override
    public String doLayout(ILoggingEvent event) {
        String message = super.doLayout(event);
        if(event.getLoggerName().equals("org.springframework.web.client.RestTemplate")) {
            message = maskPasswords(message);
        }
        return message;
    }

    /**
     * Mask all password types in the given message.
     *
     * @param message  the message to mask the passwords in
     *
     * @return a message with all passwords masked
     */
    protected String maskPasswords(String message) {
        if (message.contains("password")) {
            message = maskPassword(message, "password=[", "]");
            message = maskPassword(message, "\"password\":\"", "\"");
        }
        return message;
    }

    /**
     * Mask a password in the given message that starts with the given start token and
     * ends with the end token.
     *
     * @param message     the message to mask the password in
     * @param startToken  the start token of the password
     * @param endToken    the end token of the password
     *
     * @return a message with the password masked
     */
    private String maskPassword(String message, String startToken, String endToken) {
        int startIndex = message.indexOf(startToken);
        if (startIndex > -1) {
            int endIndex = message.indexOf(endToken, startIndex + startToken.length());
            message = message.replace(message.substring(startIndex, endIndex + 1), startToken.concat("********").concat(endToken));
        }
        return message;
    }

}
