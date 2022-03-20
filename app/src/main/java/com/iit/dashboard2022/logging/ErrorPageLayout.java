package com.iit.dashboard2022.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

import java.util.Date;
import java.util.Locale;

/**
 * An appender for handling system errors and warnings on the Errors tab
 * <p>
 * Note: Static objects must be used in this context
 *
 * @author Noah Husby
 */
public class ErrorPageLayout extends LayoutBase<ILoggingEvent> {

    @Override
    public String doLayout(ILoggingEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.ENGLISH, "[%1$tT %2$S]", new Date(event.getTimeStamp()), event.getLevel()));
        builder.append(" [").append(event.getLoggerName()).append("]");
        builder.append(": ").append(event.getFormattedMessage()).append("\n");
        return builder.toString();
    }
}
