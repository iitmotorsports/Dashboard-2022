package com.iit.dashboard2022.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class StringAppender extends AppenderBase<ILoggingEvent> {

    private static final List<StringLogger> loggers = Collections.synchronizedList(Lists.newArrayList());

    public static void register(StringLogger l) {
        loggers.add(l);
    }

    public static boolean isRegistered(StringLogger l) {
        return loggers.contains(l);
    }

    protected LayoutWrappingEncoder<ILoggingEvent> encoder = null;

    public LayoutWrappingEncoder<ILoggingEvent> getEncoder() {
        return this.encoder;
    }

    public void setEncoder(LayoutWrappingEncoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        loggers.forEach(l -> l.onLoggingEvent(eventObject, encoder));
    }
}
