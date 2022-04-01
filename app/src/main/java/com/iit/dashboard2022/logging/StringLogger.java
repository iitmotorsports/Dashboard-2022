package com.iit.dashboard2022.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

public interface StringLogger {
    void onLoggingEvent(ILoggingEvent event, LayoutWrappingEncoder<ILoggingEvent> encoder);
}
