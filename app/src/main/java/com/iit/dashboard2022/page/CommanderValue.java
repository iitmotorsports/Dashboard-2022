package com.iit.dashboard2022.page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CommanderValue {
    TORQUE_VECTORING("Torque vectoring aggression", 0, 0, 0, 8);

    private final String name;
    private final int id;
    private final float initial;
    private final float min;
    private final float max;
}
