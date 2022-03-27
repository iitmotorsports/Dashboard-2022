package com.iit.dashboard2022.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * An appender for handling system errors and warnings on the Errors tab
 * <p>
 * Note: Static objects must be used in this context
 *
 * @author Noah Husby
 */
public class LogFileAppender extends AbstractStringAppender {

    public static StringLogger logger;

    @Override
    protected void append(ILoggingEvent event) {
        if (logger != null) {
            logger.onLoggingEvent(event, encoder);
        }
    }
}
