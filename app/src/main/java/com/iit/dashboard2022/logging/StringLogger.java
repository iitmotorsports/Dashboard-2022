package com.iit.dashboard2022.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

/**
 * An interface representing an endpoint for logback events.
 * Register this using {@link StringAppender#register(StringLogger)}
 *
 * @author Noah Husby
 */
public interface StringLogger {
    /**
     * Fires each time a new message is logged.
     *
     * @param event   The log message event.
     * @param encoder The layout encoder from the logback configuration file.
     */
    void onLoggingEvent(ILoggingEvent event, LayoutWrappingEncoder<ILoggingEvent> encoder);
}
