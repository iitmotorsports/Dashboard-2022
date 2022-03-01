package com.iit.dashboard2022.util;

/**
 * A telemetry handler utilizing the WarpTables API
 *
 * @see <a href="https://husbylabs.com/warptables">https://husbylabs.com/warptables</a>
 * @author Noah Husby
 */
public class TelemetryHandler {

    // TODO: Fix the Lombok support in IntelliJ

    private static final TelemetryHandler instance = new TelemetryHandler();



    /**
     * Gets the instance of the TelemetryHandler
     *
     * To be replaced by Lombok
     * @return {@link TelemetryHandler}
     */
    public static TelemetryHandler getInstance() {
        return instance;
    }

}
