package com.iit.dashboard2022.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * A utility to direct Logback messages to the app's outputs.
 *
 * @author Noah Husby
 */
public class StringAppender extends AppenderBase<ILoggingEvent> {

    private static final List<StringLogger> loggers = Collections.synchronizedList(Lists.newArrayList());

    /**
     * Registers a new {@link StringLogger} to receive data.
     *
     * @param l The {@link StringLogger} to be linked to Logback.
     */
    public static void register(StringLogger l) {
        loggers.add(l);
    }

    /**
     * Checks whether a given {@link StringLogger} has already been registered.
     *
     * @param l The {@link StringLogger} to be checked.
     * @return True if already registered, false otherwise.
     */
    public static boolean isRegistered(StringLogger l) {
        return loggers.contains(l);
    }

    protected LayoutWrappingEncoder<ILoggingEvent> encoder = null;

    /*
     * Utility method for logback.
     * Do not remove.
     */
    public LayoutWrappingEncoder<ILoggingEvent> getEncoder() {
        return this.encoder;
    }

    /*
     * Utility method for logback.
     * Do not remove.
     */
    public void setEncoder(LayoutWrappingEncoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        loggers.forEach(l -> l.onLoggingEvent(eventObject, encoder));
    }
}
