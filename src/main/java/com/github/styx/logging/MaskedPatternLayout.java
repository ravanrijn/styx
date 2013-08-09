package com.github.styx.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class MaskedPatternLayout extends PatternLayout {

    @Override
    public String doLayout(ILoggingEvent event) {
        String message = super.doLayout(event);
        if(event.getLoggerName().equals("org.springframework.web.client.RestTemplate")) {
            message = maskPasswords(message);
        }
        return message;
    }

    private String maskPasswords(String message) {
        int startIndex = message.indexOf("password=[");
        if (startIndex > 0) {
            int endIndex = message.indexOf("]", startIndex);
            message = message.replace(message.substring(startIndex, endIndex + 1), "password=[********]");
        }
        return message;
    }

}
