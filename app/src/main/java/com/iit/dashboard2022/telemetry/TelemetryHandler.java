package com.iit.dashboard2022.telemetry;

import com.husbylabs.warptables.Status;
import com.husbylabs.warptables.WTClient;
import com.husbylabs.warptables.WarpTablesAPI;
import com.iit.dashboard2022.util.Toaster;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * A telemetry handler utilizing the WarpTables API
 *
 * @see <a href="https://husbylabs.com/warptables">https://husbylabs.com/warptables</a>
 * @author Noah Husby
 */
public class TelemetryHandler {

    private static final TelemetryHandler instance = new TelemetryHandler();

    private TelemetryHandler() {
    }

    private WTClient client = null;

    /**
     * Starts the WarpTables instance
     *
     * @param address Address of remote server
     */
    public void start(InetSocketAddress address) {
        if (client != null) {
            // Proper cleanup did not occur. Manually cleaning up.
            client.stop();
            client = null;
        }
        client = (WTClient) WarpTablesAPI.createClient(address);
        client.addEventListener(new TelemetryEventListener());
        try {
            client.start();
        } catch (IOException e) {
            Toaster.showToast("Couldn't start Telemetry. Please check the logs.");
            e.printStackTrace(System.err);
        }
    }

    /**
     * Stops the WarpTables instance
     */
    public void stop() {
        if(client != null) {
            client.stop();
            client = null;
        }
    }

    public boolean isStarted() {
        return client != null && client.getStatus() != Status.STOPPED;
    }

    /**
     * Checks if the WarpTables instance is connected
     *
     * @return True if connected, false if not
     */
    public boolean isConnected() {
        return client != null && client.getStatus() == Status.CONNECTED;
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