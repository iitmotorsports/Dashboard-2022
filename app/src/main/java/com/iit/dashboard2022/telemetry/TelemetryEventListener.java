package com.iit.dashboard2022.telemetry;

import com.husbylabs.warptables.events.EventListener;
import com.husbylabs.warptables.events.state.StatusChangedEvent;

public class TelemetryEventListener extends EventListener {
    @Override
    public void onStatusChangedEvent(StatusChangedEvent event) {
        // TODO: Update status on Telemetry Page. (event.getStatus())
        /*
         * There should be a single button with three states
         * 1. Client is disconnected -> Button is green and says "start"
         * 2. Client is connecting -> Button should be darker green and says "starting..."
         * 3. Client is connected -> Button is red and says "stop"
         */
    }
}
