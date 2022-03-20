package com.iit.dashboard2022.ecu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.iit.dashboard2022.ecu.ECUMsg.DataType;
import com.iit.dashboard2022.util.Constants;
import com.iit.dashboard2022.util.Toaster;

import java.util.HashMap;

// TODO: Delete
public class ECUMsgHandler_old {

    private final HashMap<Long, String> faultMap = new HashMap<>(Constants.FAULTS.length);
    private final HashMap<Long, ECUMsg> messageMap = new HashMap<>();
    private final HashMap<Long, STATE> stateMap = new HashMap<>();
    private final ECUMsg[] messages = new ECUMsg[23];
    private final ECUKeyMap keyMap;
    private StateListener stateListener;

    public ECUMsgHandler_old(@NonNull ECUKeyMap ecuKeyMap) {
        keyMap = ecuKeyMap;
        messages[0] = new ECUMsg("[Front Teensy]", "[ LOG ] Fault State", DataType.UNSIGNED);
        messages[0] = new ECUMsg("[HeartBeat]", "[WARN]  Heartbeat is taking too long", DataType.UNSIGNED);
        messages[0] = new ECUMsg("[HeartBeat]", "[ LOG ] Beat", DataType.UNSIGNED);
        messages[0] = new ECUMsg("[Front Teensy]", "[ LOG ] Start Light", DataType.UNSIGNED);
        messages[0] = new ECUMsg("[Front Teensy]", "[ LOG ] Current State", DataType.UNSIGNED);
        messages[0].addMessageListener(val -> {
            if (stateListener != null) {
                stateListener.onStateChanged(stateMap.get(val));
            }
        });
        messages[0] = new ECUMsg("[SerialVar]", "[INFO]  Approximate Float value:", DataType.UNSIGNED);
    }

    private void getFaultStrIDs() {
        faultMap.clear();
        for (String fMsg : Constants.FAULTS) {
            Integer i = keyMap.getStrID(fMsg);
            if (i != null) {
                faultMap.put(i.longValue(), fMsg);
            }
        }
    }

    public ECUMsg[] getMessageArray() {
        return messages;
    }

    public void setGlobalStateListener(StateListener globalStateListener) {
        stateListener = globalStateListener;
    }

    public void loadMessageKeys() {
        messageMap.clear();
        for (ECUMsg msg : messages) {
            msg.load(messageMap, keyMap);
        }
        stateMap.clear();
        for (STATE state : STATE.values()) {
            Integer tagID = keyMap.getTagID(String.format("[%s]", state.title));
            if (tagID == null) {
                Toaster.showToast("Failed to set State Enum for " + state.title, Toaster.Status.WARNING);
            } else {
                stateMap.put(Long.valueOf(tagID), state);
            }
        }
        getFaultStrIDs();
    }

    @Nullable
    public String checkFaults(long strKey) {
        return faultMap.get(strKey);
    }

    public enum STATE { // Use actual name, brackets are added on when matching to actual state name
        Initializing("Teensy Initialize"),
        PreCharge("PreCharge State"),
        Idle("Idle State"),
        Charging("Charging State"),
        Button("Button State"),
        Driving("Driving Mode State"),
        Fault("Fault State");

        final String title;

        STATE(String title) {
            this.title = title;
        }

    }

    public interface StateListener {
        void onStateChanged(STATE state);
    }
}
