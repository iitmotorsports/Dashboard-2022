package com.iit.dashboard2022.ecu;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class ECUCommands {
    /**
     * Commands that can be sent through the `write` function
     */
    public static final byte[][] COMMANDS = {{123}, {111}, {45}, {127}, {90}, {-1}, {84}, {25}, {101}, {61}};
    public static final int CHARGE = 0;
    public static final int SEND_CAN_BUS_MESSAGE = 1;
    public static final int CLEAR_FAULT = 2;
    public static final int TOGGLE_CAN_BUS_SNIFF = 3; // TODO: implement CAN bus sniffer button
    public static final int TOGGLE_MIRROR_MODE = 4;
    public static final int ENTER_MIRROR_SET = 5;
    public static final int SEND_ECHO = 6;
    public static final int TOGGLE_REVERSE = 7;
    public static final int PRINT_LOOKUP = 8;
    public static final int SET_SERIAL_VAR = 9;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CHARGE,
            SEND_CAN_BUS_MESSAGE,
            CLEAR_FAULT,
            TOGGLE_CAN_BUS_SNIFF,
            TOGGLE_MIRROR_MODE,
            ENTER_MIRROR_SET,
            SEND_ECHO,
            TOGGLE_REVERSE,
            PRINT_LOOKUP,
            SET_SERIAL_VAR
    })
    @interface ECUCommand {
    }
}
