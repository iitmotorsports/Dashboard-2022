package com.iit.dashboard2022.ecu;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * An enumeration of vehicle states.
 */

@RequiredArgsConstructor
@Getter
public enum State {
    INITIALIZING(0, "Teensy Initialize"),
    PRE_CHARGE(1, "PreCharge State"),
    IDLE(2, "Idle State"),
    CHARGING(3, "Charging State"),
    BUTTON(4, "Button State"),
    DRIVING(5, "Driving Mode State"),
    FAULT(6, "Fault State");

    private final int id;
    private final String name;

    /**
     * Gets the state by it's numerical ID.
     *
     * @param id ID of state.
     * @return {@link State} if exists, null otherwise.
     */
    public static State getStateById(int id) {
        for (State state : State.values()) {
            if (state.getId() == id) {
                return state;
            }
        }
        return null;
    }
}