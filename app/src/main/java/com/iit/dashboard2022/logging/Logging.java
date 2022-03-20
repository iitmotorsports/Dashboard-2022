package com.iit.dashboard2022.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Logging {
    private final static Logger LOGGER = LoggerFactory.getLogger("Dashboard");

    public static Logger getLogger() {
        return LOGGER;
    }
}
