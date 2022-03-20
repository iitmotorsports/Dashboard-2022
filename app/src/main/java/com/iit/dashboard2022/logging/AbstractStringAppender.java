package com.iit.dashboard2022.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

public abstract class AbstractStringAppender extends AppenderBase<ILoggingEvent> {

    protected LayoutWrappingEncoder<ILoggingEvent> encoder = null;

    public LayoutWrappingEncoder<ILoggingEvent> getEncoder() {
        return this.encoder;
    }

    public void setEncoder(LayoutWrappingEncoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }
}
