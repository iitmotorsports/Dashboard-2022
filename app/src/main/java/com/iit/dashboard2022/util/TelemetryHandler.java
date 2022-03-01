package com.iit.dashboard2022.util;

import com.husbylabs.warptables.WTClient;
import com.husbylabs.warptables.WarpTableInstance;
import com.husbylabs.warptables.WarpTablesAPI;

import java.net.InetSocketAddress;

/**
 * A telemetry handler utilizing the WarpTables API
 *
 * @see <a href="https://husbylabs.com/warptables">https://husbylabs.com/warptables</a>
 * @author Noah Husby
 */
public class TelemetryHandler {

    // TODO: Fix the Lombok support in IntelliJ

    private static final TelemetryHandler instance = new TelemetryHandler();

    private TelemetryHandler() {
    }

    private WarpTableInstance warpTableInstance = null;

    /**
     * Starts the WarpTables instance
     *
     * @param address Address of remote server
     */
    public void start(InetSocketAddress address) throws Exception {
        if(warpTableInstance != null && ((WTClient) warpTableInstance).isConnected()) {
            // TODO: Log
            warpTableInstance.stop();
        }
        warpTableInstance = WarpTablesAPI.createClient(address);
        ((WTClient) warpTableInstance).enableAutoConnect();
        warpTableInstance.start();
    }

    /**
     * Stops the WarpTables instance
     */
    public void stop() {
        if(warpTableInstance != null) {
            warpTableInstance.stop();
        }
    }

    /**
     * Checks if the WarpTables instance is connected
     *
     * @return True if connected, false if not
     */
    public boolean isConnected() {
        return warpTableInstance != null && ((WTClient) warpTableInstance).isConnected();
    }

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
